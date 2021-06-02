package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;

public class DropParticle extends Entity {
    static private final Avatar mould = Avatar.fromFile(Gdx.files.internal("avatar/props/drop.json"));
    private Avatar avatar;
    private Vector2 pos, pPos;
    private float dx, dy;
    private Vector2 velocity;
    //private Vector2 acceleration;
    private int age, ttl;


    public DropParticle(float x, float y) {
        avatar = mould.copy();
        pos = new Vector2(x, y);
        pPos = pos.cpy();
        velocity = new Vector2();
        age = 0;
        ttl = MathUtils.floor(MathUtils.random(2, 10));
        avatar.scale(0.006f);
        avatar.scale(ttl/10f, -ttl/10f);
    }

    public void impulse(float x, float y) {
        velocity.set(x, y);
    }

    public void reset(float x, float y) {
        pos.set(x, y);
        pPos.set(pos);
        velocity.set(0, 0);
        age = 0;
    }

    public void update(float dtime) {
        pPos.set(pos);
        velocity.add(0, -0.002f);
        pos.add(velocity);
        dx = pos.x - pPos.x;
        dy = pos.y - pPos.y;
        avatar.setPosition(pos.x, pos.y);
        avatar.setAngle(MathUtils.atan2(dx, dy) - MathUtils.HALF_PI);

        age++;
        if (age >= ttl)
            dispose();
    }

    @Override
    public void dispose() {
        mustDispose = true;
    }

    public void draw(MyRenderer renderer) {
        avatar.draw(renderer);
    }

}
