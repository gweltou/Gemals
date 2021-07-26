package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;

public class Jellyfish extends Entity {
    private Avatar avatar = Avatar.fromFile(Gdx.files.internal("avatar/props/morgaze_anime_9.json"));
    private float direction;

    public Jellyfish() {
        avatar.scale(0.03f, -0.03f);
        avatar.timeScale(0.8f);
        respawn();
    }

    @Override
    public void draw(MyRenderer renderer) {
        avatar.draw(renderer);
    }

    @Override
    public void update(float delta) {
        avatar.position.add(direction, 0);
        avatar.update(delta);
        if (avatar.position.x < -4f || avatar.position.x > 4f) {
            respawn();
        }
    }

    private void respawn() {
        if (MathUtils.random() > 0.5) {
            direction = -0.002f;
            avatar.setFlipX(false);
            avatar.setPosition(3.9f, 1.5f);
        } else {
            direction = 0.002f;
            avatar.setFlipX(true);
            avatar.setPosition(-3.9f, 1.5f);

        }
    }

    @Override
    public void dispose() {

    }
}
