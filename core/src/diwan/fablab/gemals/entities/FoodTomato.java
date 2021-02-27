package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import gwel.game.entities.Avatar;

public class FoodTomato extends Food {

    public FoodTomato(World world, Vector2 position) {
        super();

        avatar = Avatar.fromFile(Gdx.files.internal("avatar/food_tomato_2.json"));
        avatar.scale(0.014f);
        avatar.scalePhysics(0.014f);

        buildBody(world, position, MathUtils.random(MathUtils.PI2));
        body.setUserData(this);
    }
}
