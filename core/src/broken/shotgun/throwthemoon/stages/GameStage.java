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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Random;

import broken.shotgun.throwthemoon.actors.Background;
import broken.shotgun.throwthemoon.actors.Enemy;
import broken.shotgun.throwthemoon.actors.Player;
import broken.shotgun.throwthemoon.models.EnemySpawn;
import broken.shotgun.throwthemoon.models.EnemySpawnWall;
import broken.shotgun.throwthemoon.models.Level;
import broken.shotgun.throwthemoon.models.SpawnLocation;

import static broken.shotgun.throwthemoon.ThrowTheMoonGame.isDebug;

public class GameStage extends Stage {
    private boolean debug;

    private final AssetManager manager;

    private Level currentLevel;
    private int wallIndex = 0;
    private Random random;

    private Background background;
    private Player player;

    private final Vector2 touchPoint;

    private static final float SCROLL_SCREEN_PERCENT_TRIGGER = 0.6f;
    private float playerScreenX = 0.0f;

    public GameStage(final AssetManager manager) {
        super(new ScreenViewport());

        this.manager = manager;

        loadLevel();

        random = new Random(System.currentTimeMillis());

        background = new Background(manager);
        addActor(background);

        player = new Player(manager);
        player.setX(100);
        player.setY(300);
        addActor(player);

        touchPoint = new Vector2();

        debug = isDebug();
        setDebugAll(debug);

        Gdx.input.setInputProcessor(this);

        addListener(new ActorGestureListener() {
            @Override
            public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == 0 && !(event.getTarget() instanceof Enemy)) {
                    player.moveTo(touchPoint.set(x, y));
                }

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
                player.performAttack(event, count);

                if (isDebug())
                    Gdx.app.log("GameStage", String.format("tap type:%s target:%s count:%d", event.getType().toString(), event.getTarget().toString(), count));
                super.tap(event, x, y, count, button);
            }
        });
    }

    private void loadLevel() {
        currentLevel = new Level();
        currentLevel.chapter = 1;

        EnemySpawnWall spawnWall = new EnemySpawnWall();
        spawnWall.spawnWallX = 1000;

        for(int i=0; i<3; ++i) {
            EnemySpawn spawn = new EnemySpawn();
            spawn.enemyId = 0;
            spawn.location = SpawnLocation.FRONT;
            spawnWall.enemySpawnList.add(spawn);
        }

        for(int i=0; i<2; ++i) {
            EnemySpawn spawn = new EnemySpawn();
            spawn.enemyId = 0;
            spawn.location = SpawnLocation.BACK;
            spawnWall.enemySpawnList.add(spawn);
        }

        currentLevel.enemySpawnWallList.add(spawnWall);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        playerScreenX = stageToScreenCoordinates(player.getPosition()).x;

        if(triggerSpawnWall(playerScreenX)) {
            for(EnemySpawn spawn : currentLevel.enemySpawnWallList.get(wallIndex).enemySpawnList) {
                spawnEnemy(spawn);
            }

            currentLevel.enemySpawnWallList.get(wallIndex).triggered = true;
        }
        else if(spawnWallActive()) {
            if(allOnscreenEnemiesDefeated()) {
                currentLevel.enemySpawnWallList.get(wallIndex).destroyed = true;
                wallIndex++;
            }
        }
        else if(shouldScrollCamera(playerScreenX)) {
            float shiftX = playerScreenX - (getWidth() * SCROLL_SCREEN_PERCENT_TRIGGER);
            getCamera().translate(shiftX, 0.0f, 0.0f);
            touchPoint.x += shiftX;
            player.moveTo(screenToStageCoordinates(touchPoint));
        }
    }

    public boolean shouldScrollCamera(float x) {
        return playerScreenX > getWidth() * SCROLL_SCREEN_PERCENT_TRIGGER;
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

    public void spawnEnemy(EnemySpawn spawn) {
        Enemy newEnemy = new Enemy(manager);
        Vector2 spawnPoint = new Vector2();
        spawnPoint.y = random.nextInt((int)(getHeight()-newEnemy.getHeight()));
        switch (spawn.location) {
            case FRONT:
                spawnPoint.x = getWidth() - 300;
                break;
            case BACK:
                spawnPoint.x = 50;
                break;
        }

        screenToStageCoordinates(spawnPoint);
        newEnemy.setPosition(spawnPoint.x, spawnPoint.y);
        newEnemy.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        newEnemy.addAction(Actions.fadeIn(0.5f));
        addActor(newEnemy);
    }

    public boolean allOnscreenEnemiesDefeated() {
        for(Actor entity : getActors()) {
            if(entity instanceof Enemy) return false;
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
}
