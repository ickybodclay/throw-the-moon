package broken.shotgun.throwthemoon.screens;

import broken.shotgun.throwthemoon.ThrowTheMoonGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class StageClearScreen implements Screen {
	private static final float WIDTH = 1280f;
    private static final float HEIGHT = 720;
    
	private final AssetManager manager;
	private final Stage stage;
	
	private static final String TEXTURE_FILENAME = "stageclearbg.png";
	private static final String MUSIC_FILENAME = "dotty.mp3";
	private Texture background;
	private Music music;
	
	public StageClearScreen(final ThrowTheMoonGame game) {
        this.manager = new AssetManager();
        this.stage = new Stage(new StretchViewport(WIDTH, HEIGHT));
        
        manager.setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
        manager.load(TEXTURE_FILENAME, Texture.class);
		manager.load(MUSIC_FILENAME, Music.class);
        manager.finishLoading();

        background = manager.get(TEXTURE_FILENAME);
		music = manager.get(MUSIC_FILENAME);

        Image backgroundImg = new Image(background);
        stage.addActor(backgroundImg);
        
        Gdx.input.setInputProcessor(stage);
        
        stage.addListener(new ActorGestureListener() {
			@Override
			public void tap(InputEvent event, float x, float y, int count, int button) {
				music.stop();
				game.setScreen(new GameScreen(game));
				super.tap(event, x, y, count, button);
			}
        });

		music.setLooping(true);
		music.play();
	}

	@Override
	public void show() {
		
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.draw();
        stage.act(delta);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void dispose() {
		manager.dispose();
        stage.dispose();
	}

}
