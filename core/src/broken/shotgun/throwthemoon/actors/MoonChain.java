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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MoonChain extends Actor {
    private static final String TEXTURE_FILENAME = "chain.png";
    private final Texture texture;

    private Player attachedPlayer;

    private static final int TILE_COUNT = 50;

    public MoonChain(final AssetManager manager) {
        manager.setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
        manager.load(TEXTURE_FILENAME, Texture.class);
        manager.finishLoading();

        texture = manager.get(TEXTURE_FILENAME);
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.Repeat);

        setWidth(texture.getWidth());
        setHeight(texture.getHeight() * TILE_COUNT);
        setOrigin(getWidth() / 2, 0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(attachedPlayer != null) {
            setPosition(attachedPlayer.getX() + attachedPlayer.getOriginX(),
                    attachedPlayer.getY() + attachedPlayer.getOriginY());
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        batch.draw(texture,
                getX(), getY(), // x, y
                getWidth(), // width
                getHeight(), // height
                1, 0, // u, v
                0, TILE_COUNT); // u2, v2
    }

    public void attachTail(Player player) {
        attachedPlayer = player;
    }

    public void detachTail() {
        attachedPlayer = null;
    }
}
