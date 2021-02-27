package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import gwel.game.entities.Avatar;

public class FoodMeat extends Food {

    public FoodMeat(World world, Vector2 position) {
        super();

        avatar = Avatar.fromFile(Gdx.files.internal("avatar/food_meat.json"));
        avatar.scale(0.008f);
        avatar.scalePhysics(0.008f);

        buildBody(world, position, MathUtils.random(MathUtils.PI2));
        body.setUserData(this);
    }
}
