package broken.shotgun.throwthemoon.stages.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import broken.shotgun.throwthemoon.actors.Moon;

import static com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class MoonImpactMeter {
    private final Moon moon;
    private static final float PADDING_X = 50f;
    private static final float OFFSET_Y = 50f;
    private float chainHeight = 5f;
    private float moonRadius = 10f;
    private float earthRadius = 40f;
    private float targetRadius = 3f;


    public MoonImpactMeter(final Moon moon) {
        this.moon = moon;
    }

    public void draw(ShapeRenderer renderer) {
        renderer.set(ShapeType.Filled);

        renderer.setColor(Color.DARK_GRAY);
        renderer.rect(PADDING_X + (moon.getImpactProgress() * (Gdx.graphics.getWidth() - (PADDING_X * 2))), OFFSET_Y,
                (Gdx.graphics.getWidth() - (PADDING_X * 2)) - (moon.getImpactProgress() * (Gdx.graphics.getWidth() - (PADDING_X * 2))), chainHeight);

        renderer.setColor(Color.BLUE);
        renderer.circle(Gdx.graphics.getWidth() - PADDING_X, OFFSET_Y + (chainHeight / 2f), earthRadius);

        renderer.set(ShapeType.Line);
        renderer.setColor(Color.RED);
        renderer.circle(Gdx.graphics.getWidth() - PADDING_X, OFFSET_Y + (chainHeight / 2f), targetRadius);
        renderer.line(Gdx.graphics.getWidth() - PADDING_X, OFFSET_Y + (chainHeight / 2f) + 5f, Gdx.graphics.getWidth() - PADDING_X, OFFSET_Y + (chainHeight / 2f) - 5f);
        renderer.line(Gdx.graphics.getWidth() - PADDING_X - 5f, OFFSET_Y + (chainHeight / 2f), Gdx.graphics.getWidth() - PADDING_X + 5f, OFFSET_Y + (chainHeight / 2f));

        renderer.set(ShapeType.Filled);
        renderer.setColor(Color.LIGHT_GRAY);
        renderer.circle(PADDING_X + (moon.getImpactProgress() * (Gdx.graphics.getWidth() - (PADDING_X * 2))), OFFSET_Y + (chainHeight / 2f), moonRadius);
    }
}
