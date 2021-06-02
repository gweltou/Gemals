package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;

public class Butterfly extends Entity {
    private static Avatar[] moulds = new Avatar[]{
            Avatar.fromFile(Gdx.files.internal("avatar/props/butterfly_blue.json")),
            Avatar.fromFile(Gdx.files.internal("avatar/props/butterfly_yellow.json"))
    };
    private Avatar avatar;
    private boolean flying = true;
    private float direction = -0.008f;

    public Butterfly(float x, float y) {
        avatar = moulds[MathUtils.random(1)].copy();
        avatar.scale(0.008f, -0.008f);
        avatar.timeScale(6);
        avatar.position.set(x, y);
    }

    @Override
    public void draw(MyRenderer renderer) {
        avatar.draw(renderer);
    }

    @Override
    public void update(float delta) {
        if (flying) {
            avatar.update(delta);
            avatar.position.add(direction, MathUtils.random(-0.023f, 0.028f));
            if (MathUtils.random() < 0.02f) {
                flying = false;
                avatar.resetAnimation();
            }
        } else {
            avatar.position.add(direction * MathUtils.random(), -0.005f);
            if (MathUtils.random() < 0.04f) {
                flying = true;
                if (MathUtils.random() < avatar.position.x * direction * 10) {
                    direction *= -1;
                    avatar.setFlipX(direction > 0);
                }
            }
        }
    }

    @Override
    public void dispose() {
        mustDispose = true;
    }
}
