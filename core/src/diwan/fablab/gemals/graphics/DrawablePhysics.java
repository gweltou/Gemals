package diwan.fablab.gemals.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import gwel.game.graphics.DrawableCircle;
import gwel.game.graphics.DrawablePolygon;


public class DrawablePhysics {
    public Body body;
    public DrawablePolygon[] shapes;
    private final Affine2 transform = new Affine2();

    public DrawablePhysics(Body body) {
        this.body = body;
        shapes = new DrawablePolygon[body.getFixtureList().size];

        int s = 0;
        for (Fixture fixture : body.getFixtureList()) {
            switch (fixture.getType()) {
                case Polygon:
                    PolygonShape polygonShape = (PolygonShape) fixture.getShape();
                    Vector2 vertex = new Vector2();
                    float[] vertices = new float[polygonShape.getVertexCount() * 2];
                    for (int i = 0; i < polygonShape.getVertexCount(); i++) {
                        polygonShape.getVertex(i, vertex);
                        vertices[i*2] = vertex.x;
                        vertices[i*2 + 1] = vertex.y;
                    }
                    DrawablePolygon polygon = new DrawablePolygon(vertices);
                    polygon.setColor(0.5f, 0.5f, 0.5f, 1);
                    if (fixture.isSensor())
                        polygon.setColorMod(1, 1, 1, 0.4f);
                    shapes[s++] = polygon;
                    break;
                case Circle:
                    CircleShape circleShape = (CircleShape) fixture.getShape();
                    Vector2 position = circleShape.getPosition();
                    DrawableCircle circle = new DrawableCircle(position.x, position.y, circleShape.getRadius());
                    circle.setColor(0.5f, 0.5f, 0.5f, 1);
                    if (fixture.isSensor())
                        circle.setColorMod(1, 1, 1, 0.4f);
                    shapes[s++] = circle;
                    break;
            }
        }
    }


    public void setColor(int i, Color color) {
        shapes[i].setColor(color);
    }

    public void setColorMod(float rm, float gm, float bm, float am) {
        for (DrawablePolygon shape : shapes)
            shape.setColorMod(rm, gm, bm, am);
    }


    public void draw(MyRenderer renderer) {
        transform.setToTranslation(body.getPosition());
        transform.rotateRad(body.getAngle());
        renderer.pushMatrix(transform);
        for (DrawablePolygon shape : shapes)
            shape.draw(renderer);

        renderer.popMatrix();
    }
}
