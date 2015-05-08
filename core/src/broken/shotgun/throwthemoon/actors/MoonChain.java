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

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class MoonChain extends Actor {
    private static final String TEXTURE_FILENAME = "chain.png";
    private static final String SFX_CHAIN_PULL_FILENAME = "";
    private final Texture texture;
    private final Rectangle collisionArea;
    private final Vector2 position;
    //private final Sound chainPullSfx;

    private Player attachedPlayer;

    private static final int TILE_COUNT = 50;

    public MoonChain(final AssetManager manager) {
        manager.setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
        manager.setLoader(Sound.class, new SoundLoader(new InternalFileHandleResolver()));
        manager.load(TEXTURE_FILENAME, Texture.class);
        //manager.load(SFX_CHAIN_PULL_FILENAME, Sound.class);
        manager.finishLoading();

        texture = manager.get(TEXTURE_FILENAME);
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.Repeat);
        
        //chainPullSfx = manager.get(SFX_CHAIN_PULL_FILENAME);

        setWidth(texture.getWidth());
        setHeight(texture.getHeight() * TILE_COUNT);
        setOrigin(getWidth() / 2, 0);
        
        // Note: scale is not used in draw for the chain, this is a hack to make easier to put the chain down
        setScale(3f, 3f);

        collisionArea = new Rectangle(getX(), getY(), getWidth(), getHeight());
        position = new Vector2(getX(), getY());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(attachedPlayer != null) {
            setPosition(attachedPlayer.getX() + attachedPlayer.getOriginX() - (getWidth() / 2),
                    attachedPlayer.getY() + attachedPlayer.getOriginY());
        }

        collisionArea.setPosition(getX(), getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(getColor());
        batch.draw(texture,
                getX(), getY(), // x, y
                getWidth(), // width
                getHeight(), // height
                1, 0, // u, v
                0, TILE_COUNT); // u2, v2
        batch.setColor(Color.WHITE);
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
        if (!getDebug()) return;
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.GREEN);
        shapes.rect(collisionArea.x, collisionArea.y, collisionArea.width, collisionArea.height);
    }

    public void attachTail(Player player) {
        attachedPlayer = player;
        clearActions();
    }

    public void detachTail() {
        attachedPlayer = null;
        addAction(Actions.moveTo(-50, getY(), 12f, Interpolation.fade));
    }

    public boolean isAttached() {
        return attachedPlayer != null;
    }

    public Rectangle getCollisionArea() {
        return collisionArea;
    }

    public Vector2 getPosition() {
        return collisionArea.getPosition(position);
    }

	public void animatePull() {
		addAction(sequence(color(Color.GRAY, 0.10f), color(Color.WHITE, 0.10f)));
		float volume = 1f; // [0.0, 1.0]
		float pitch = 1f; // [0.5. 2.0]
		float pan = 0f; // [-1, 1]
		//chainPullSfx.play(volume, pitch, pan);
	}

	public void hintPullChain() {
		addAction(sequence(color(Color.GRAY, 0.40f), color(Color.WHITE, 0.40f), color(Color.GRAY, 0.40f), color(Color.WHITE, 0.40f), color(Color.GRAY, 0.40f), color(Color.WHITE, 0.40f)));
	}
}
