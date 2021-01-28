package diwan.fablab.gemmals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Json;
import diwan.fablab.gemmals.entities.Food;
import diwan.fablab.gemmals.entities.PhysicsAvatar;
import diwan.fablab.gemmals.graphics.DrawablePhysics;
import gwel.game.entities.Avatar;
import diwan.fablab.gemmals.entities.PhysicsCategories;

import java.util.Iterator;


public class Creature extends PhysicsAvatar {
    private final float TIME_STEP = 1f;
    private CreatureScreen stage;

    private enum State {
        DEAD,
        IDLE,
        SLEEPING,
    }

    public int age;
    State state;
    private float timeInStep;


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

    private float moodPerStepSleeping = 0.05f;

    private float sleepinessPerStepAwake = 0.8f;
    private float sleepinessPerStepSleeping = -2f;

    private float dirtinessPerStepAwake = 0.3f;


    public Creature(CreatureScreen stage) {
        this.stage = stage;

        state = State.IDLE;
        age = 0;
        timeInStep = 0;

        energy = energyCeiling * 0.8f;
        hungry = hungryCeiling * 0.6f;
        sleepy = sleepyCeiling * 0.1f;
        dirty = 20f;
        mood = moodCeiling * 0.5f;

        avatar = Avatar.fromFile(Gdx.files.internal("avatar/mufmuf_idle_02.json").file());
        avatar.scale(0.01f);
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
        fixtureDef.friction = 0.6f;
        fixtureDef.restitution = 0f;
        fixtureDef.filter.categoryBits = PhysicsCategories.GEMMAL;
        fixtureDef.filter.maskBits -= PhysicsCategories.FOOD;
        body.createFixture(fixtureDef);
        box.dispose();

        body.applyAngularImpulse(0.1f, true);

        drawablePhysics = new DrawablePhysics(body);
        drawablePhysics.setColor(0, new Color(0, 1, 0, 0.2f));
    }


    public void update(float delta) {
        timeInStep += delta;
        if (timeInStep > TIME_STEP) {
            timeInStep -= TIME_STEP;
            lifeStep();
        }

        if (state == State.IDLE)
            avatar.updateAnimation(4 * delta);
    }


    public void lifeStep() {
        float ageFactor = (float) age/5000;
        float energyFactor = energy/energyCeiling;
        float hungryFactor = hungry / hungryCeiling;
        float sleepyFactor = sleepy / sleepyCeiling;
        float moodFactor = mood/moodCeiling;

        switch (state) {
            case DEAD:
                break;

            case IDLE:
                energy = MathUtils.clamp(energy + energyPerStepAwake, 0 ,energyCeiling);
                hungry = MathUtils.clamp(hungry + hungerPerStepAwake, 0, hungryCeiling);
                sleepy = MathUtils.clamp(sleepy + sleepinessPerStepAwake, 0, sleepyCeiling);
                dirty = MathUtils.clamp(dirty + dirtinessPerStepAwake, 0, 100);

                mood += -0.2f*hungryFactor - 0.1f*sleepyFactor - 0.004f*dirty;
                mood = MathUtils.clamp(mood, 0, moodCeiling);

                if (hungryFactor > 0.5f && !stage.food.isEmpty()) {
                    for (Iterator<Food> iter = stage.food.iterator(); iter.hasNext(); ) {
                        Food food = iter.next();
                        if (food.isEdible()) {
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
                if (MathUtils.random() < sleepProb)
                    state = State.SLEEPING;
                break;

            case SLEEPING:
                energy = MathUtils.clamp(energy + energyPerStepSleeping, 0 ,energyCeiling);
                hungry = MathUtils.clamp(hungry + hungerPerStepSleeping, 0, hungryCeiling);
                mood = MathUtils.clamp(mood + moodPerStepSleeping, 0, moodCeiling);
                sleepy = MathUtils.clamp(sleepy + sleepinessPerStepSleeping, 0, sleepyCeiling);

                // Wake probability
                float wakeProb = (float) Math.pow(energyFactor, 2) *
                        (float) Math.pow(1-sleepyFactor, 1);
                wakeProb /= 5;
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
        age += 1;
    }


    public void feed() {
        hungry -= 35;
        sleepy += 20;
        energy += 10;
        dirty += 10;
        mood += 10;
    }

    public void clean() {
        dirty -= 35;
        sleepy += 20;
        energy -= 10;
        mood += 10;
        hungry += 10;
    }

    public void pet() {

    }

    public void play() {

    }

    public void saveData() {
        //Gdx.files.local()
        Json json = new Json();
        System.out.println(json.prettyPrint(this));
    }
}
