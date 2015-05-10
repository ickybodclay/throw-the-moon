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
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class Moon extends Actor {
	private static final int DISTANCE_TO_EARTH_IN_MILES = 238900;
    private static final String TEXTURE_FILENAME = "moon.png";
    private static final String SFX_MOON_CRASH_FILENAME = "sfx/moon_crash.wav";
    private final Texture texture;
    private final Sound crashSfx;
    private TextureRegion currentFrame;
    private boolean falling;
    private int distance;

    public Moon(final AssetManager manager) {
        manager.setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
        manager.setLoader(Sound.class, new SoundLoader(new InternalFileHandleResolver()));
        manager.load(TEXTURE_FILENAME, Texture.class);
        manager.load(SFX_MOON_CRASH_FILENAME, Sound.class);
        manager.finishLoading();

        texture = manager.get(TEXTURE_FILENAME);
        crashSfx = manager.get(SFX_MOON_CRASH_FILENAME);

        currentFrame = new TextureRegion(texture);

        setWidth(currentFrame.getRegionWidth());
        setHeight(currentFrame.getRegionHeight());
        setOrigin(getWidth() / 2, getHeight() / 2);

        reset(); 
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(currentFrame,
                getX(), getY(),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                getScaleX(), getScaleY(),
                getRotation());
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        if (!getDebug()) return;
        shapes.setColor(Color.GRAY);
        shapes.rect(getX(), getY(), getWidth(), getHeight());
        shapes.setColor(Color.RED);
        shapes.circle(getX() + getOriginX(), getY() + getOriginY(), 10f);
    }

    public void startFalling() {
        if(falling) return;

        falling = true;
        distance = 0;

        crashSfx.play(1.0f, 0.5f, 0f);

        addAction(
            Actions.moveBy(10, -getHeight(), 10f, Interpolation.fade));
    }

    public boolean isFalling() {
        return falling;
    }

    public void reset() {
    	clearActions();
        falling = false;
        distance = DISTANCE_TO_EARTH_IN_MILES;
    }

	public void addDistance(float velocityY) {
		distance += velocityY;
	}

	public int getDistance() {
		return distance;
	}
}
