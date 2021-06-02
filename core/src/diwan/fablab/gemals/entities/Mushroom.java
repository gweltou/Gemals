package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;
import gwel.game.graphics.ComplexShape;

public class Mushroom extends Entity {
    private static Avatar[] moulds = new Avatar[] {
            Avatar.fromFile(Gdx.files.internal("avatar/props/mushroom_g02.json")),
            Avatar.fromFile(Gdx.files.internal("avatar/props/mushroom_1_ver2g.json")),
            Avatar.fromFile(Gdx.files.internal("avatar/props/mushroom_1_ver2g.json"))
    };
    private Avatar shroom;

    public Mushroom(float x, float y, float scale) {
        shroom = moulds[MathUtils.random(moulds.length-1)].copy();
        shroom.setPosition(x, y);
        shroom.scale(scale, -scale);
        shroom.setFlipX(MathUtils.random() > 0.5f);
        shroom.timeScale(1f/(scale/0.003f));

        Color colorSpots = new Color(MathUtils.random(0.6f, 1.5f), MathUtils.random(0.6f, 1.5f), MathUtils.random(0.6f, 1.5f), 1);
        Color colorMain = new Color(MathUtils.random(0.6f, 1.2f), MathUtils.random(0.6f, 1.2f), MathUtils.random(0.6f, 1.2f), 1);
        shroom.shape.getById("spots").setColorMod(colorSpots.r, colorSpots.g, colorSpots.b, 1);
        shroom.shape.getById("stem").setColorMod(colorMain.r, colorMain.g, colorMain.b, 1);
        shroom.shape.getById("cap").setColorMod(colorMain.r, colorMain.g, colorMain.b, 1);
    }

    @Override
    public void draw(MyRenderer renderer) {
        shroom.draw(renderer);
    }

    @Override
    public void update(float delta) {
        shroom.update(delta);
    }

    @Override
    public void dispose() {
        mustDispose = true;
    }
}
