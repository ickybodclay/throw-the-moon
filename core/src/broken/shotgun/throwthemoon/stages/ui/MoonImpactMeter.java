package broken.shotgun.throwthemoon.stages.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import broken.shotgun.throwthemoon.actors.Moon;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class MoonImpactMeter {
    private final Moon moon;

    public MoonImpactMeter(final Moon moon) {
        this.moon = moon;
    }

    public void draw(ShapeRenderer renderer) {
        renderer.setColor(Color.GREEN);
        renderer.set(ShapeType.Filled);
        renderer.rect(moon.getImpactProgress() * Gdx.graphics.getWidth(), 10, Gdx.graphics.getWidth(), 50);
    }
}
