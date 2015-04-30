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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class Boss extends Actor {
    private static final String TEXTURE_FILENAME = "boss.png";
    private static final int FRAME_WIDTH = 512;
    private static final int FRAME_HEIGHT = 512;

    private final Texture texture;
    private final TextureRegion[] regions;

    private final Animation idle;
    private TextureRegion currentFrame;
    private float stateTime = 0.0f;

    private final Rectangle collisionArea;

    private int health;

    public Boss(final AssetManager manager) {
        manager.setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
        manager.load(TEXTURE_FILENAME, Texture.class);
        manager.finishLoading();

        texture = manager.get(TEXTURE_FILENAME);
        regions = TextureRegion.split(texture, FRAME_WIDTH, FRAME_HEIGHT)[0];
        idle = new Animation(0.1f, regions[0], regions[1], regions[2]);
        idle.setPlayMode(Animation.PlayMode.LOOP);

        currentFrame = idle.getKeyFrame(0.0f);

        setWidth(currentFrame.getRegionWidth());
        setHeight(currentFrame.getRegionHeight());
        setOrigin(getWidth() / 2, getHeight() / 2);

        collisionArea = new Rectangle(getX(), getY() + 80, (int) getWidth(), (int) getHeight() - 170);

        health = 5;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        currentFrame = idle.getKeyFrame(stateTime);
        collisionArea.setPosition(getX(), getY() + 80);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(getColor());
        batch.draw(currentFrame, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        batch.setColor(Color.WHITE);
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        if (!getDebug()) return;
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.GREEN);
        shapes.rect(collisionArea.x, collisionArea.y, collisionArea.width, collisionArea.height);
        shapes.setColor(Color.RED);
        shapes.circle(getX() + getOriginX(), getY() + getOriginY(), 10f);
    }

    public Rectangle getCollisionArea() {
        return collisionArea;
    }

    public void startBattle() {
        Vector2 pointA = new Vector2();
        Vector2 pointB = new Vector2();
        //Vector2 pointC = new Vector2();
        //Vector2 pointD = new Vector2();

        addAction(
            forever(
                sequence(
                    Actions.delay(3f),
                    Actions.moveTo(0, 0, 5f, Interpolation.swingIn),
                    Actions.delay(3f),
                    Actions.moveTo(0, 0, 5f, Interpolation.swingIn)
                )
            ));
    }

    public void takeDamage(int direction) {
        health--;

        if (health <= 0) {
            die();
            return;
        }

        addAction(
            sequence(
                parallel(
                    Actions.moveBy(20 * direction, 0, 0.3f, Interpolation.circleOut), sequence(color(Color.BLACK, 0.15f), color(Color.WHITE, 0.15f))),
                    Actions.moveTo(getX(), getY(), 0.1f, Interpolation.circleIn)));
    }

    public void die() {
        clearActions();
        addAction(
            sequence(
                fadeOut(0.4f, Interpolation.fade),
                removeActor()));
    }
}
