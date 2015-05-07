package broken.shotgun.throwthemoon.screens;

import broken.shotgun.throwthemoon.ThrowTheMoonGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
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
	//private final Batch uiBatch;
	
	private static final String TEXTURE_FILENAME = "stageclearbg.png";
	private Texture background;
	//private BitmapFont font;
	//private static final String STAGE_CLEAR_MESSAGE = "Stage Clear!";
	
	public StageClearScreen(final ThrowTheMoonGame game) {
        this.manager = new AssetManager();
        this.stage = new Stage(new StretchViewport(WIDTH, HEIGHT));
        //this.uiBatch = new SpriteBatch();
        
        manager.setLoader(Texture.class, new TextureLoader(new InternalFileHandleResolver()));
        manager.load(TEXTURE_FILENAME, Texture.class);
        manager.finishLoading();

        background = manager.get(TEXTURE_FILENAME);
        Image backgroundImg = new Image(background);
        stage.addActor(backgroundImg);
        
        //font = new BitmapFont();
        //font.setColor(Color.WHITE);
        
        Gdx.input.setInputProcessor(stage);
        
        stage.addListener(new ActorGestureListener() {

			@Override
			public void tap(InputEvent event, float x, float y, int count, int button) {
				game.setScreen(new GameScreen(game));
				
				super.tap(event, x, y, count, button);
			}
        	
        });
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
        
        //uiBatch.begin();
        //font.draw(uiBatch, STAGE_CLEAR_MESSAGE, 0, Gdx.graphics.getHeight() * 0.75f, Gdx.graphics.getWidth(), Align.center, true);
        //uiBatch.end();
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
        //font.dispose();
	}

}
