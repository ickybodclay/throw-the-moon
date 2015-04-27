/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Broken Shotgun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package broken.shotgun.throwthemoon.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.List;
import java.util.Random;

import broken.shotgun.throwthemoon.actors.Background;
import broken.shotgun.throwthemoon.actors.Boss;
import broken.shotgun.throwthemoon.actors.Enemy;
import broken.shotgun.throwthemoon.actors.Moon;
import broken.shotgun.throwthemoon.actors.MoonChain;
import broken.shotgun.throwthemoon.actors.Player;
import broken.shotgun.throwthemoon.models.EnemySpawn;
import broken.shotgun.throwthemoon.models.EnemySpawnWall;
import broken.shotgun.throwthemoon.models.Level;
import broken.shotgun.throwthemoon.models.SpawnLocation;

import static broken.shotgun.throwthemoon.ThrowTheMoonGame.isDebug;

public class GameStage extends Stage {
    private static final String MUSIC_FILENAME = "SnestedLoops.ogg";
    private static final float WIDTH = 1920;
    private static final float HEIGHT = 1080;
    private boolean debug;

    private final AssetManager manager;

    private Level currentLevel;
    private int wallIndex = 0;
    private Random random;

    private Background background;
    private Player player;
    private Moon moon;
    private MoonChain chain;
    private Boss boss;

    private Music music;

    private final Vector2 touchPoint;

    private static final float SCROLL_SCREEN_PERCENT_TRIGGER = 0.6f;
    private float playerScreenX = 0.0f;

    public GameStage(final AssetManager manager) {
        super(new StretchViewport(WIDTH, HEIGHT));

        this.manager = manager;

        loadLevel();

        loadMusic();

        random = new Random(System.currentTimeMillis());

        background = new Background(manager);
        chain = new MoonChain(manager);
        player = new Player(manager);
        moon = new Moon(manager);
        boss = new Boss(manager);

        touchPoint = new Vector2();

        resetLevel();

        debug = isDebug();
        setDebugAll(debug);

        Gdx.input.setInputProcessor(this);

        addListener(new ActorGestureListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && !(event.getTarget() instanceof Enemy)) {
                    player.moveTo(touchPoint.set(x, y));
                }

                // FIXME replace String.format with StringBuilder for HTML
                if (isDebug())
                    Gdx.app.log("GameStage", String.format("touchDown %s %s", event.getType().toString(), event.getTarget().toString()));
                super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && player.isWalking()) {
                    player.stop();
                }
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
                player.moveTo(touchPoint.set(x, y));

                super.pan(event, x, y, deltaX, deltaY);
            }

            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                player.performAttack(count);

                // FIXME replace String.format with StringBuilder for HTML
                if (isDebug())
                    Gdx.app.log("GameStage",
                        String.format("tap type:%s target:%s count:%d [x:%.2f, y:%.2f]",
                            event.getType().toString(), event.getTarget().toString(), count, x, y));
                super.tap(event, x, y, count, button);
            }
        });

        addListener(new InputListener() {
            int attackCounter = 0;

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.D:
                        if(!moon.isFalling())
                            moon.startFalling();
                        break;
                    case Input.Keys.SPACE:
                        attackCounter++;
                        player.performAttack(attackCounter);
                        return true;
                    case Input.Keys.LEFT:
                        player.velocity.x = -7;
                        player.startWalkState();
                        return true;
                    case Input.Keys.RIGHT:
                        player.velocity.x = 7;
                        player.startWalkState();
                        return true;
                    case Input.Keys.UP:
                        player.velocity.y = 7;
                        player.startWalkState();
                        return true;
                    case Input.Keys.DOWN:
                        player.velocity.y = -7;
                        player.startWalkState();
                        return true;
                }

                return super.keyDown(event, keycode);
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                switch (keycode) {
                    case Input.Keys.LEFT:
                    case Input.Keys.RIGHT:
                        player.velocity.x = 0;
                        return true;
                    case Input.Keys.UP:
                    case Input.Keys.DOWN:
                        player.velocity.y = 0;
                        return true;
                }
                return super.keyUp(event, keycode);
            }
        });
    }

    private void loadMusic() {
        manager.setLoader(Music.class, new MusicLoader(new InternalFileHandleResolver()));
        manager.load(MUSIC_FILENAME, Music.class);
        manager.finishLoading();

        music = manager.get(MUSIC_FILENAME);
        music.setLooping(true);
        music.play();
    }

    private void loadLevel() {
        currentLevel = new Level();
        currentLevel.chapter = 1;

        int wallX = (int) (Gdx.graphics.getWidth() * 0.75f);

        // boss
        EnemySpawnWall bossSpawnWall = new EnemySpawnWall();
        bossSpawnWall.spawnWallX = wallX;
        EnemySpawn bossSpawn = new EnemySpawn();
        bossSpawn.enemyId = 100;
        bossSpawn.location = SpawnLocation.FRONT;
        bossSpawnWall.enemySpawnList.add(bossSpawn);
        currentLevel.enemySpawnWallList.add(bossSpawnWall);

        wallX += (int) (Gdx.graphics.getWidth() * 0.75f);

        for(int i=0; i<3; ++i) {
            EnemySpawnWall spawnWall = new EnemySpawnWall();
            spawnWall.spawnWallX = wallX;

            for(int j=0; j<3; ++j) {
                EnemySpawn spawn = new EnemySpawn();
                spawn.enemyId = 0;
                spawn.location = SpawnLocation.FRONT;
                spawnWall.enemySpawnList.add(spawn);
            }

            if(i % 2 == 1) {
                for(int j=0; j<2; ++j) {
                    EnemySpawn spawn = new EnemySpawn();
                    spawn.enemyId = 0;
                    spawn.location = SpawnLocation.BACK;
                    spawnWall.enemySpawnList.add(spawn);
                }
            }

            currentLevel.enemySpawnWallList.add(spawnWall);

            wallX += (int) (Gdx.graphics.getWidth() * 0.75f);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        playerScreenX = stageToScreenCoordinates(player.getPosition()).x;

        handleCollisions();

        if(isChainOffscreen()) {
            startGameOver();
        }
        else if(triggerSpawnWall(player.getX())) {
            spawnEnemies(currentLevel.enemySpawnWallList.get(wallIndex).enemySpawnList);

            currentLevel.enemySpawnWallList.get(wallIndex).triggered = true;
        }
        else if(spawnWallActive()) {
            if(allOnscreenEnemiesDefeated()) {
                currentLevel.enemySpawnWallList.get(wallIndex).destroyed = true;
                wallIndex++;
            }
        }
        else if(shouldScrollCamera(playerScreenX)) {
            float shiftX = playerScreenX - (Gdx.graphics.getWidth() * SCROLL_SCREEN_PERCENT_TRIGGER);
            getCamera().translate(shiftX, 0.0f, 0.0f);
            if(!moon.isFalling()) moon.moveBy(shiftX, 0.0f);
        }
    }

    private void startGameOver() {
        player.die();
        music.stop();
    }

    private boolean isChainOffscreen() {
        return stageToScreenCoordinates(chain.getPosition()).x <= -32;
    }

    private void handleCollisions() {
        for(Actor entity : getActors()) {
            if(entity instanceof Enemy) {
                Enemy enemy = (Enemy) entity;
                if(player.getCollisionArea().overlaps(enemy.getCollisionArea())) {
                    if(chain.isAttached()) chain.detachTail();
                    player.takeDamage();
                }
                if(enemy.getCollisionArea().overlaps(player.getAttackArea())) {
                    enemy.takeDamage(player.getAttackArea().x < enemy.getX() + enemy.getOriginX() ? 1 : -1);
                    player.clearAttackArea();
                }
            }
            if(entity instanceof MoonChain && !((MoonChain) entity).isAttached() && !player.isTakingDamage()) {
                if (player.getCollisionArea().overlaps(((MoonChain) entity).getCollisionArea())) {
                    chain.attachTail(player);
                }
            }
        }
    }

    public boolean shouldScrollCamera(float x) {
        return playerScreenX > Gdx.graphics.getWidth() * SCROLL_SCREEN_PERCENT_TRIGGER;
    }

    public boolean triggerSpawnWall(float x) {
        return wallIndex < currentLevel.enemySpawnWallList.size() &&
                x >= currentLevel.enemySpawnWallList.get(wallIndex).spawnWallX &&
                !(currentLevel.enemySpawnWallList.get(wallIndex).triggered || currentLevel.enemySpawnWallList.get(wallIndex).destroyed);
    }

    public boolean spawnWallActive() {
        return wallIndex < currentLevel.enemySpawnWallList.size() &&
                currentLevel.enemySpawnWallList.get(wallIndex).triggered &&
                !currentLevel.enemySpawnWallList.get(wallIndex).destroyed;
    }

    public void spawnEnemies(List<EnemySpawn> spawnList) {
        int offsetY = 100;
        for(EnemySpawn spawn : spawnList) {
            if(spawn.enemyId == 0) {
                Enemy newEnemy = new Enemy(manager);
                Vector2 spawnPoint = new Vector2();
                spawnPoint.y = offsetY + random.nextInt((int) newEnemy.getHeight());
                switch (spawn.location) {
                    case FRONT:
                        spawnPoint.x = Gdx.graphics.getWidth() * 0.8f;
                        break;
                    case BACK:
                        spawnPoint.x = Gdx.graphics.getWidth() * 0.15f;
                        break;
                }

                screenToStageCoordinates(spawnPoint);
                newEnemy.setPosition(spawnPoint.x, spawnPoint.y);
                newEnemy.setColor(1.0f, 1.0f, 1.0f, 0.0f);
                newEnemy.addAction(Actions.fadeIn(0.5f));
                addActor(newEnemy);
            }
            else if(spawn.enemyId == 100) {
                Vector2 spawnPoint = new Vector2();
                spawnPoint.y = Gdx.graphics.getHeight() * 2/3;
                switch (spawn.location) {
                    case FRONT:
                        spawnPoint.x = Gdx.graphics.getWidth() * 0.70f;
                        break;
                    case BACK:
                        spawnPoint.x = Gdx.graphics.getWidth() * 0.15f;
                        break;
                }

                screenToStageCoordinates(spawnPoint);
                boss.setPosition(spawnPoint.x, spawnPoint.y);
                boss.setColor(1.0f, 1.0f, 1.0f, 0.0f);
                boss.addAction(Actions.fadeIn(0.5f));
                addActor(boss);
            }

            offsetY += 100;
        }
    }

    public boolean allOnscreenEnemiesDefeated() {
        for(Actor entity : getActors()) {
            if(entity instanceof Enemy) return false;
            if(entity instanceof Boss) return false;
        }
        return true;
    }

    @Override
    public void draw() {
        super.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public boolean keyDown(int keyCode) {
        switch (keyCode) {
            case Input.Keys.APOSTROPHE:
                debug = !debug;
                setDebugAll(debug);
                break;
        }
        return super.keyDown(keyCode);
    }

    public boolean isGameOver() {
        return player.getStage() == null;
    }

    public void resetLevel() {
        getActors().clear();
        addActor(background);
        addActor(moon);
        addActor(chain);
        addActor(player);

        // reset player position and add back to stage
        player.setX(getWidth() / 4);
        player.setY(getHeight() / 3);
        player.reset();
        moon.setPosition(0, getHeight() - 100);
        chain.attachTail(player);

        playerScreenX = 0.0f;

        getCamera().position.set(getWidth() / 2, getHeight() / 2, 0);

        touchPoint.set(0, 0);

        for(EnemySpawnWall wall : currentLevel.enemySpawnWallList) {
            wall.triggered = false;
            wall.destroyed = false;
        }
        wallIndex = 0;

        music.play();
    }
}
