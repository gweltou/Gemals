package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;

public class Rock extends Entity {
    private static Avatar[] moulds = new Avatar[] {
            Avatar.fromFile(Gdx.files.internal("avatar/props/rocks.json")),
            Avatar.fromFile(Gdx.files.internal("avatar/props/rocks_2.json"))
    };
    private Avatar avatar;

    public Rock(float x, float y, float scale) {
        avatar = moulds[MathUtils.random(moulds.length-1)].copy();
        avatar.setPosition(x, y);
        avatar.scale(scale, -scale);
    }

    @Override
    public void draw(MyRenderer renderer) {
        avatar.draw(renderer);
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void dispose() {
        mustDispose = true;
    }
}
