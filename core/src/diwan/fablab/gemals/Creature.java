package diwan.fablab.gemals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import diwan.fablab.gemals.entities.Food;
import diwan.fablab.gemals.entities.PhysicsAvatar;
import diwan.fablab.gemals.entities.PhysicsCategories;
import diwan.fablab.gemals.graphics.DrawablePhysics;
import gwel.game.entities.Avatar;
import gwel.game.graphics.Shape;

import java.util.Iterator;


public class Creature extends PhysicsAvatar {
    private final float TIME_STEP = 1f;
    private final CreatureScreen stage;

    private enum State {
        EGG,
        IDLE,
        WALKING,
        SLEEPING,
        DEAD,
    }

    public int steps;
    State state;
    private float timeInStep;
    private int walkSide = -1;

    Avatar idleAvatar, sleepAvatar, eatAvatar, walkAvatar, happyAvatar;


    // When energy falls to 0 the creature's life is threatened
    // food gives energy back
    float energy;
    
    // Food lowers hunger
    // Hunger grows with time and activity (lower when sleeping)
    // Hunger has a ceiling
    float hungry;

    // Sleepiness
    // Sleepiness grows with time, activity, traits
    float sleepy;

    // Mood
    float mood;

    float dirty;


    // Traits
    // They vary with age, with life events and with genetic traits
    private float energyCeiling = 100f;
    private float hungryCeiling = 100f;
    private float sleepyCeiling = 100f;
    private float moodCeiling = 100f;

    private float energyPerStepAwake = -0.5f;
    private float energyPerStepSleeping = -0.02f;

    private float hungerPerStepAwake = 0.5f;
    private float hungerPerStepSleeping = 0.1f;

    private float moodPerStepSleeping = 0.03f;

    private float sleepinessPerStepAwake = 0.8f;
    private float sleepinessPerStepSleeping = -2f;

    private float dirtinessPerStepAwake = 0.3f;

    public Creature(CreatureScreen stage) {
        this.stage = stage;

        state = State.EGG;
        steps = 0;
        timeInStep = 0;

        energy = energyCeiling * 0.8f;
        hungry = hungryCeiling * 0.6f;
        sleepy = sleepyCeiling * 0.1f;
        dirty = 0f;
        mood = moodCeiling * 0.5f;

        float preMem = Gdx.app.getJavaHeap()/1024f;
        idleAvatar = Avatar.fromFile(Gdx.files.internal("avatar/mufmuf_idle_02.json"));
        idleAvatar.scale(0.01f);
        idleAvatar.scalePhysics(0.01f);
        idleAvatar.timeScale(3);
        sleepAvatar = Avatar.fromFile(Gdx.files.internal("avatar/mufmuf_sleep_g01.json"));
        sleepAvatar.scale(0.01f);
        sleepAvatar.timeScale(1.3f);
        eatAvatar = Avatar.fromFile(Gdx.files.internal("avatar/mufmuf_eat_8.json"));
        eatAvatar.scale(0.01f);
        eatAvatar.timeScale(4f);
        walkAvatar = Avatar.fromFile(Gdx.files.internal("avatar/mufmuf_walk_anime.json"));
        walkAvatar.scale(0.009f);
        walkAvatar.timeScale(3f);
        happyAvatar = Avatar.fromFile(Gdx.files.internal("avatar/mufmuf_happy_anime_3.json"));
        happyAvatar.scale(0.011f);
        happyAvatar.timeScale(10);

        Gdx.app.log("memory", "avatars creation");
        Gdx.app.log("memory", String.valueOf(Gdx.app.getJavaHeap()/1024f - preMem));

        avatar = Avatar.fromFile(Gdx.files.internal("avatar/eggBlue_2.json"));
        avatar.scale(0.015f);
    }


    public void buildBody(World world, Vector2 position) {
        final float BOX_WIDTH = 1.0f;
        final float BOX_HEIGHT = 1.0f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);

        PolygonShape box = new PolygonShape();
        box.set(new float[] {
                -BOX_WIDTH/2, BOX_HEIGHT,
                BOX_WIDTH/2, BOX_HEIGHT,
                BOX_WIDTH/2, 0,
                -BOX_WIDTH/2, 0});
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = box;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0f;
        fixtureDef.filter.categoryBits = PhysicsCategories.GEMAL;
        fixtureDef.filter.maskBits -= PhysicsCategories.FOOD;
        body.createFixture(fixtureDef);
        box.dispose();

        body.applyAngularImpulse(0.1f, true);

        drawablePhysics = new DrawablePhysics(body);
        drawablePhysics.setColor(0, new Color(0, 1, 0, 0.2f));
    }


    public boolean contains(float x, float y) {
        Vector2 point = new Vector2(x, y);
        transform.inv().applyTo(point);
        for (Shape shape : avatar.physicsShapes) {
            if (shape.contains(point.x, point.y))
                return true;
        }
        return false;
    }


    public void update(float delta) {
        if (state == State.DEAD)
            return;

        if (avatar == walkAvatar)
            body.applyForceToCenter(4 * walkSide, 0, true);

        timeInStep += delta;
        if (timeInStep > TIME_STEP) {
            timeInStep -= TIME_STEP;
            lifeStep();
        }

        avatar.update(delta);
    }


    private void clampValues() {
        energy = MathUtils.clamp(energy, 0 ,energyCeiling);
        hungry = MathUtils.clamp(hungry, 0, hungryCeiling);
        sleepy = MathUtils.clamp(sleepy, 0, sleepyCeiling);
        dirty = MathUtils.clamp(dirty, 0, 100);
        mood = MathUtils.clamp(mood, 0, moodCeiling);
    }


    public void lifeStep() {
        //Gdx.app.log("memory", Gdx.app.getJavaHeap()/1024f + " " + Gdx.app.getNativeHeap()/1024f);

        float ageFactor = (float) steps / 5000;
        float energyFactor = energy / energyCeiling;
        float hungryFactor = hungry / hungryCeiling;
        float sleepyFactor = sleepy / sleepyCeiling;
        float moodFactor = mood / moodCeiling;

        switch (state) {
            case DEAD:
                break;

            case EGG:
                if (MathUtils.random() < 0.01 * steps) {
                    state = State.IDLE;
                    dirty = 15f;
                }
                break;

            case IDLE:
                avatar = idleAvatar;

                energy += energyPerStepAwake;
                hungry += hungerPerStepAwake;
                sleepy += sleepinessPerStepAwake;
                dirty += dirtinessPerStepAwake;
                mood += -0.2f*hungryFactor - 0.1f*sleepyFactor - 0.004f*dirty;
                clampValues();

                if (hungryFactor > 0.3f && !stage.food.isEmpty() && MathUtils.random() < 0.2f) {
                    for (Iterator<Food> iter = stage.food.iterator(); iter.hasNext(); ) {
                        Food food = iter.next();
                        if (food.isEdible()) {
                            avatar = eatAvatar;
                            food.dispose();
                            iter.remove();
                            feed();
                            break;
                        }
                    }
                }

                // Go to sleep
                // p = (e - 1)^4
                float sleepProb = (float) Math.pow(energyFactor - 1, 4) +
                        (float) Math.pow(sleepyFactor, 1) +
                        (float) Math.pow(moodFactor, 6);
                sleepProb /= 6;
                if (MathUtils.random() < sleepProb) {
                    state = State.SLEEPING;
                    sleepAvatar.setFlipX(MathUtils.random() < 0.5f);
                } else if (MathUtils.random() < 0.05f) {
                    state = State.WALKING;
                    avatar = walkAvatar;
                    if (stage.cameraWorld.position.x > body.getPosition().x)
                        walkSide = 1;
                    else
                        walkSide = -1;
                    walkAvatar.setFlipX(walkSide > 0);
                }
                break;

            case WALKING:
                if (Math.abs(stage.cameraWorld.position.x - body.getPosition().x) < 4)
                    state = State.IDLE;
                break;

            case SLEEPING:
                avatar = sleepAvatar;

                energy = MathUtils.clamp(energy + energyPerStepSleeping, 0 ,energyCeiling);
                hungry = MathUtils.clamp(hungry + hungerPerStepSleeping, 0, hungryCeiling);
                mood = MathUtils.clamp(mood + moodPerStepSleeping, 0, moodCeiling);
                sleepy = MathUtils.clamp(sleepy + sleepinessPerStepSleeping, 0, sleepyCeiling);

                // Wake probability
                float wakeProb = (float) Math.pow(energyFactor, 2) +
                        (float) Math.pow(1-sleepyFactor, 1);
                wakeProb /= 6;
                if (MathUtils.random() < wakeProb) {
                    // Death probability depends on age, energy, mood
                    float deathProbability = (float) Math.pow(ageFactor, 4) +
                            (float) Math.pow(1 - energyFactor, 6) +
                            (float) Math.pow(1 - moodFactor, 6);
                    deathProbability /= 12;
                    if (MathUtils.random() < deathProbability) {
                        state = State.DEAD;
                    } else {
                        // Wake up
                        state = State.IDLE;
                    }
                }
                break;
        }
        steps += 1;
    }


    public void feed() {
        hungry -= 35;
        sleepy += 20;
        energy += 10;
        dirty += 10;
        mood += 10;
        clampValues();
    }

    public void clean(float amount) {
        dirty -= 35 * amount;
        sleepy += 20 * amount;
        energy -= 10 * amount;
        mood += 10 * amount;
        hungry += 10 * amount;
        clampValues();
    }

    public void pet() {
        if (state == State.IDLE) {
            avatar = happyAvatar;
        }
        clampValues();
    }

    /*public void play() {
        clampValues();
    }*/

    public JsonValue getState() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("state", new JsonValue(state.toString()));
        json.addChild("steps", new JsonValue(steps));
        json.addChild("energy", new JsonValue(energy));
        json.addChild("sleepy", new JsonValue(sleepy));
        json.addChild("dirty", new JsonValue(dirty));
        json.addChild("mood", new JsonValue(mood));
        json.addChild("hungry", new JsonValue(hungry));

        return json;
    }
}
