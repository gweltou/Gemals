package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;

public class Grass extends Entity {
    private static Avatar[] moulds = new Avatar[] {
            Avatar.fromFile(Gdx.files.internal("avatar/props/grass.json")),
            Avatar.fromFile(Gdx.files.internal("avatar/props/grass2.json")),
            Avatar.fromFile(Gdx.files.internal("avatar/props/flower.json"))
    };
    private Avatar avatar;

    public Grass(float x, float y, float scale) {
        // Rarify flowers
        int r = MathUtils.random(moulds.length-1);
        if (r == moulds.length-1)
            r = MathUtils.random(moulds.length-1);
        avatar = moulds[r].copy();
        avatar.setPosition(x, y);
        avatar.scale(scale, -scale);
        avatar.setFlipX(MathUtils.random() > 0.5f);
    }

    @Override
    public void draw(MyRenderer renderer) {
        avatar.draw(renderer);
    }

    @Override
    public void update(float delta) {
        avatar.update(delta);
    }

    @Override
    public void dispose() {
        mustDispose = true;
    }
}
