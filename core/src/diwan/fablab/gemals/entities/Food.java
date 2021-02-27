package diwan.fablab.gemals.entities;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Food extends PhysicsAvatar {
    private boolean isEdible = false;

    Food() {
        fixtureDef = new FixtureDef();
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.6f;
        fixtureDef.restitution = 0.2f;
        fixtureDef.filter.categoryBits = PhysicsCategories.FOOD;
        fixtureDef.filter.maskBits -= PhysicsCategories.GEMAL;
    }

    public void setEdible() { isEdible = true; }

    public boolean isEdible() { return isEdible; }
}
