package broken.shotgun.throwthemoon;

import com.badlogic.gdx.Game;

import broken.shotgun.throwthemoon.screens.GameScreen;

public class ThrowTheMoonGame extends Game {
	private static boolean DEBUG;

	public ThrowTheMoonGame() {
		DEBUG = false;
	}

	public ThrowTheMoonGame(boolean debug) {
		DEBUG = debug;
	}

	public static boolean isDebug() {
		return DEBUG;
	}

	@Override
	public void create () {
		setScreen(new GameScreen());
	}
}
