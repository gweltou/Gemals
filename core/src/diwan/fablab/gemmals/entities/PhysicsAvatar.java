package diwan.fablab.gemmals.entities;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;
import diwan.fablab.gemmals.GemmalsGame;
import diwan.fablab.gemmals.graphics.DrawablePhysics;
import diwan.fablab.gemmals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.graphics.Drawable;


/**
 * An avatar which also exists in Box2D physic world
 *
 */
public class PhysicsAvatar implements Disposable, Drawable {
    protected Avatar avatar;
    protected Body body;
    protected DrawablePhysics drawablePhysics;    // To show physics body in debug mode
    private final Affine2 transform = new Affine2();


    public void draw(MyRenderer renderer) {
        transform.setToTranslation(body.getPosition());
        // Y direction is upside-down
        transform.scale(1, -1);
        transform.rotateRad(-body.getAngle());
        renderer.pushMatrix(transform);
        avatar.draw(renderer);
        renderer.popMatrix();

        if (GemmalsGame.DEBUG_PHYSICS)
            drawablePhysics.draw(renderer);
    }

    @Override
    public void setColorMod(float rm, float gm, float bm, float am) {
        avatar.shape.setColorMod(rm, gm, bm, am);
    }

    @Override
    public void dispose() {
        body.getWorld().destroyBody(body);
    }
}
