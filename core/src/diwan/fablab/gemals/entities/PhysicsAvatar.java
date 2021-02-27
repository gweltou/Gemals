package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import diwan.fablab.gemals.GemalsGame;
import diwan.fablab.gemals.graphics.DrawablePhysics;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.graphics.Drawable;
import gwel.game.graphics.DrawableCircle;
import gwel.game.graphics.DrawablePolygon;
import gwel.game.graphics.Shape;


/**
 * An avatar which also exists in Box2D physic world
 *
 */
public class PhysicsAvatar implements Disposable, Drawable {
    protected Avatar avatar;
    protected Body body;
    protected FixtureDef fixtureDef;
    protected DrawablePhysics drawablePhysics;    // To show physics body in debug mode
    private final Affine2 transform = new Affine2();


    public void buildBody(World world, Vector2 position, float angle) {
        Gdx.app.log("PhysicsAvatar", "buildBody");
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        bodyDef.angle = angle;
        body = world.createBody(bodyDef);

        for (Shape shape : avatar.physicsShapes) {
            if (shape.getClass() == DrawableCircle.class) {
                DrawableCircle circle = (DrawableCircle) shape;
                CircleShape circleShape = new CircleShape();
                // Negate y coordinate to suit Box2D coordinates system
                circleShape.setPosition(new Vector2(circle.getCenter().x, -circle.getCenter().y));
                circleShape.setRadius(circle.getRadius());
                fixtureDef.shape = circleShape;
                body.createFixture(fixtureDef);
                circleShape.dispose();
            } else if (shape.getClass() == DrawablePolygon.class) {
                PolygonShape polygonShape = new PolygonShape();
                float[] vertices = ((DrawablePolygon) shape).getVertices();
                // Negate y coordinate to suit Box2D coordinates system
                for (int i = 1; i < vertices.length; i += 2)
                    vertices[i] *= -1;
                polygonShape.set(vertices);
                fixtureDef.shape = polygonShape;
                body.createFixture(fixtureDef);
                polygonShape.dispose();
            }

            drawablePhysics = new DrawablePhysics(body);
            for (int i = 0; i < drawablePhysics.shapes.length; i++)
                drawablePhysics.setColor(i, new Color(0, 1, 0, 0.2f));
        }
    }

    public void draw(MyRenderer renderer) {
        transform.setToTranslation(body.getPosition());
        // Y direction is upside-down
        transform.scale(1, -1);
        transform.rotateRad(-body.getAngle());
        renderer.pushMatrix(transform);
        avatar.draw(renderer);
        renderer.popMatrix();

        if (GemalsGame.DEBUG_PHYSICS)
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
