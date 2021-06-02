package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;
import gwel.game.graphics.DrawableCircle;

public class Bubble extends Entity {
    static private final Avatar mould = Avatar.fromFile(Gdx.files.internal("avatar/props/bubble.json"));
    private Avatar bubble;
    private float timeToLive;
    private Array<Entity> particles;
    private final Vector2 direction = new Vector2();
    private static final Vector2 globalDirection = new Vector2(0, -0.001f);


    public Bubble(float x, float y, Array<Entity> particlesArray) {
        this.particles = particlesArray;
        bubble = mould.copy();
        float size = MathUtils.random(0.0004f, 0.0012f);
        bubble.scale(size, -size);
        bubble.scalePhysics(size);
        bubble.setPosition(x, y);

        timeToLive = MathUtils.random(2f, 4f);
        direction.set(globalDirection);
        if (MathUtils.random() < 0.2f)
            globalDirection.set(MathUtils.random(-0.001f, 0.001f), MathUtils.random(-0.001f, 0.0f));
    }

    public void update(float delta) {
        bubble.update(delta);
        bubble.position.add(direction);

        timeToLive -= delta;
        if (timeToLive <= 0) {
            float radius = ((DrawableCircle) bubble.physicsShapes.get(0)).getRadius()/4;
            int n = MathUtils.floor(MathUtils.random(2, 20));
            for (int i=0; i<n; i++) {
                float angle = MathUtils.random(MathUtils.PI2);
                float xOff = radius * MathUtils.cos(angle);
                float yOff = radius * MathUtils.sin(angle);
                DropParticle p = new DropParticle(bubble.position.x+xOff, bubble.position.y+yOff);
                p.impulse(0.04f * xOff/radius, 0.04f * yOff/radius);
                particles.add(p);
            }
            dispose();
        }
    }

    @Override
    public void dispose() {
        mustDispose = true;
    }

    public void draw(MyRenderer renderer) {
        bubble.draw(renderer);
    }
}
