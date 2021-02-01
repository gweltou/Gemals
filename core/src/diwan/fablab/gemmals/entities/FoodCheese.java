package diwan.fablab.gemmals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import gwel.game.entities.Avatar;

public class FoodCheese extends Food {

    public FoodCheese(World world, Vector2 position) {
        super();

        avatar = Avatar.fromFile(Gdx.files.internal("avatar/food_cheese_2.json"));
        avatar.scale(0.014f);

        buildBody(world, position, MathUtils.random(MathUtils.PI2));
        body.setUserData(this);
    }
}
