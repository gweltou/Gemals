package diwan.fablab.gemmals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import diwan.fablab.gemmals.graphics.DrawablePhysics;
import gwel.game.entities.Avatar;
import gwel.game.entities.PhysicsCategories;

public class FoodTomato extends PhysicsAvatar {

    public FoodTomato(World world, Vector2 position) {
        avatar = Avatar.fromFile(Gdx.files.internal("avatar/food_tomato.json").file());
        avatar.scale(0.015f);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(0.11f);
        circle.setPosition(new Vector2(-0.07f, -0.01f));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.6f;
        fixtureDef.restitution = 0.2f;
        fixtureDef.filter.categoryBits = PhysicsCategories.FOOD;
        fixtureDef.filter.maskBits -= PhysicsCategories.GEMMAL;
        body.createFixture(fixtureDef);

        circle.setPosition(new Vector2(0.06f, 0.02f));
        circle.setRadius(0.12f);
        fixtureDef.shape = circle;
        body.createFixture(fixtureDef);

        circle.dispose();

        drawablePhysics = new DrawablePhysics(body);
        drawablePhysics.setColor(0, new Color(0, 1, 0, 0.2f));
        drawablePhysics.setColor(1, new Color(0, 1, 0, 0.2f));
    }
}
