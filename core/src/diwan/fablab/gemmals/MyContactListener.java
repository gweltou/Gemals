package diwan.fablab.gemmals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import diwan.fablab.gemmals.entities.Food;
import diwan.fablab.gemmals.entities.FoodTomato;
import diwan.fablab.gemmals.entities.PhysicsCategories;


public class MyContactListener implements ContactListener {
    private Fixture fixA, fixB;
    private Body bodyA, bodyB;
    private final Vector2 tmp1 = new Vector2();
    private final Vector2 tmp2 = new Vector2();


    public MyContactListener() {
    }


    @Override
    public void beginContact(Contact contact) {
        fixA = contact.getFixtureA();
        fixB = contact.getFixtureB();
        bodyA = fixA.getBody();
        bodyB = fixB.getBody();

        if (fixA.getFilterData().categoryBits == PhysicsCategories.FOOD &&
                fixB.getFilterData().categoryBits == PhysicsCategories.GROUND) {
            ((Food) bodyA.getUserData()).setEdible();
        }
        if (fixB.getFilterData().categoryBits == PhysicsCategories.FOOD &&
                fixA.getFilterData().categoryBits == PhysicsCategories.GROUND) {
            ((Food) bodyB.getUserData()).setEdible();
        }
    }


    @Override
    public void endContact(Contact contact) {
        fixA = contact.getFixtureA();
        fixB = contact.getFixtureB();
        bodyA = fixA.getBody();
        bodyB = fixB.getBody();
    }


    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        fixA = contact.getFixtureA();
        fixB = contact.getFixtureB();
        bodyA = fixA.getBody();
        bodyB = fixB.getBody();
    }


    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        fixA = contact.getFixtureA();
        fixB = contact.getFixtureB();
        bodyA = fixA.getBody();
        bodyB = fixB.getBody();
    }
}
