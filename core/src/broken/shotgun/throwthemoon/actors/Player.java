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

package broken.shotgun.throwthemoon.actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class Player extends Actor {
    private static final String TEXTURE_FILENAME = "camacho.png";
    private static final float MOVEMENT_DEAD_ZONE = 10.0f;
    private static final int FRAME_WIDTH = 256;
    private static final int FRAME_HEIGHT = 256;
    private static final float TAP_COUNT_INTERVAL = 0.4f;

    private final Texture texture;
    private final TextureRegion[] textureRegions;

    private final Animation idle;
    private final Animation walk;
    private final Animation attack;

    private final Rectangle collisionArea;
    private final Rectangle attackArea;

    private TextureRegion currentFrame;

    private enum State {
        IDLE,
        WALK,
        ATTACK
    }

    private State state;
    private final Vector2 moveTarget;
    private final Vector2 position;

    private float speed = 500.0f;

    private float stateTime = 0.0f;
    private boolean flipX = false;

    public Player(final AssetManager manager) {
        manager.setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
        manager.load(TEXTURE_FILENAME, Texture.class);
        manager.finishLoading();

        texture = manager.get(TEXTURE_FILENAME);
        textureRegions = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT)[0];

        idle = new Animation(0.1f, textureRegions[0], textureRegions[1], textureRegions[2]);
        idle.setPlayMode(Animation.PlayMode.LOOP);

        walk = new Animation(0.3f, textureRegions[3], textureRegions[4]);
        walk.setPlayMode(Animation.PlayMode.LOOP);

        attack = new Animation(0.3f, textureRegions[5], textureRegions[6], textureRegions[7]);
        attack.setPlayMode(Animation.PlayMode.NORMAL);

        setWidth(FRAME_WIDTH);
        setHeight(FRAME_HEIGHT);
        setOrigin(getWidth() / 2, getHeight() / 2);

        state = State.IDLE;
        currentFrame = idle.getKeyFrame(0.0f);

        moveTarget = new Vector2();
        position = new Vector2();

        collisionArea = new Rectangle(50, 0, (int)getWidth() - 100, (int)getHeight());
        attackArea = new Rectangle(0, 0, 0, 0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        switch (state) {
            case IDLE:
                currentFrame = idle.getKeyFrame(stateTime);
                break;
            case WALK:
                updateMovement(delta);
                currentFrame = walk.getKeyFrame(stateTime);
                break;
            case ATTACK:
                updateAttack(delta);
                break;
        }
    }

    private int currentAttackKeyFrame = 0;

    private void updateAttack(float delta) {
        currentFrame = attack.getKeyFrames()[currentAttackKeyFrame];

        if(stateTime > TAP_COUNT_INTERVAL) {
            state = State.IDLE;
            attackArea.set(0,0,0,0);
        }
    }

    private void updateMovement(float delta) {
        float deltaX = (getX() + getOriginX()) - moveTarget.x;
        float deltaY = (getY() + getOriginY()) - moveTarget.y;

        if(Math.abs(deltaX) > MOVEMENT_DEAD_ZONE || Math.abs(deltaY) > MOVEMENT_DEAD_ZONE) {
            double angle = (float)Math.atan2(deltaY, deltaX) * 180 / Math.PI;

            float moveX = (float) (Math.cos(angle * Math.PI / 180) * speed) * delta;
            float moveY = (float) (Math.sin(angle * Math.PI / 180) * speed) * delta;

            setX(getX() - moveX);
            setY(getY() - moveY);

            collisionArea.setPosition(getX() + 50, getY());

            flipX = deltaX > 0;
        }
        else {
            state = State.IDLE;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(currentFrame,
                getX(), getY(),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                flipX ? -getScaleX() : getScaleX(), getScaleY(),
                getRotation());
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        if (!getDebug()) return;
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.GREEN);
        shapes.rect(collisionArea.x, collisionArea.y, collisionArea.width, collisionArea.height);
        shapes.setColor(Color.RED);
        shapes.rect(attackArea.x, attackArea.y, attackArea.width, attackArea.height);
    }

    public void moveTo(Vector2 point) {
        state = State.WALK;
        moveTarget.set(point);
        attackArea.set(0, 0, 0, 0);
    }

    public void stop() {
        state = State.IDLE;
    }

    public Vector2 getPosition() {
        return position.set(getX(), getY());
    }

    public void performAttack(InputEvent event, int count) {
        flipX = getX() + getOriginX() > event.getStageX();
        stateTime = 0.0f;
        state = State.ATTACK;
        currentAttackKeyFrame =
            count % 5 == 0 ? 2 :
            count % 2 == 0 ? 1 :
            0;
        attackArea.set(getX() + (flipX ? -50 : (int)(getWidth() - 50)), getY() + (int)(getHeight() / 2) - 50, 100, 100);

        if(event.getTarget() instanceof Enemy) {
            Enemy target = (Enemy)event.getTarget();
            if(attackArea.overlaps(target.getCollisionArea())) {
                target.takeDamage();
            }
        }
    }

    public boolean isWalking() {
        return state == State.WALK;
    }

    public Rectangle getCollisionArea() {
        return collisionArea;
    }
}
