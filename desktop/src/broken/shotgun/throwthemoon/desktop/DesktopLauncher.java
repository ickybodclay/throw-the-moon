package broken.shotgun.throwthemoon.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import broken.shotgun.throwthemoon.ThrowTheMoonGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width=1280;
		config.height=720;
		config.vSyncEnabled=true;
		config.resizable = false;
		new LwjglApplication(new ThrowTheMoonGame(true), config);
	}
}
