package diwan.fablab.gemals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import diwan.fablab.gemals.entities.*;
import diwan.fablab.gemals.graphics.DrawablePhysics;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;
import gwel.game.graphics.DrawableCircle;

import java.util.*;

import static com.badlogic.gdx.utils.TimeUtils.millis;


public class CreatureScreen extends MyScreen {
    private GemalsGame game;
    private MyRenderer renderer;
    private SpriteBatch batch;
    public OrthographicCamera cameraUI, cameraWorld;
    static public World world;
    private Creature creature;

    float timeOfDay;
    //float t = 0f;

    float[] touchPath;
    int touchPathIndex;
    Vector3 lastTouch, currentTouch;
    private boolean soap;

    private DrawableCircle buttonFeed;
    private DrawableCircle buttonClean;
    private DrawableCircle buttonPet;
    private DrawableCircle buttonPlay;

    private Avatar buttonFeedIcon, buttonCleanIcon, buttonPetIcon;
    private Avatar cursorSoap;

    private final Array<DrawablePhysics> drawables = new Array<>();
    public final Array<Entity> backgroundEntities = new Array<>();
    public final Array<Entity> foregroundEntities = new Array<>();
    public final Array<Food> food = new Array<>();
    public final Array<DirtyFly> flies = new Array<>();

    //private MyContactListener contactListener;

    public CreatureScreen(GemalsGame game) {
        this.game = game;

        Date date = new Date(millis());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int minutes = calendar.get(Calendar.MINUTE);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        timeOfDay = (hours/24.0f) + (minutes/1440.0f);

        Gdx.app.log("time", "min " + minutes + " h " + hours);


        world = new World(new Vector2(0.0f, -10.0f), true);
        world.setContactListener(new MyContactListener());

        renderer = new MyRenderer();
        batch = new SpriteBatch();

        cameraUI = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraWorld = new OrthographicCamera();
        //cameraWorld.zoom = 2;

        touchPath = new float[512];
        touchPathIndex = 0;
        lastTouch = new Vector3();
        currentTouch = new Vector3();

        // Build UI
        buttonFeedIcon = Avatar.fromFile(Gdx.files.internal("icons/feed_icon.json"));
        buttonCleanIcon = Avatar.fromFile(Gdx.files.internal("icons/soap_logo.json"));
        buttonPetIcon = Avatar.fromFile(Gdx.files.internal("icons/moumoun_01.json"));
        //buttonFeedIcon.scale(1, -1);

        cursorSoap = Avatar.fromFile(Gdx.files.internal("cursor/soap_anime.json"));
        cursorSoap.scale(2, -2);

        buildStageBorders(20, 10, 1);

        creature = new Creature(this);
        creature.buildBody(world, new Vector2(0, 0.4f));


        // Background props

        // Rocks
        int n = MathUtils.random(1, 3);
        for (int i=0; i<n; i++)
            backgroundEntities.add(new Rock(MathUtils.random(-3f, 3f), 0f, MathUtils.random(0.01f, 0.02f)));

        // Mushrooms
        n = MathUtils.random(2, 6);
        ArrayList<Float> floats = new ArrayList<>();
        for (int i=0; i<n; i++)
            floats.add(MathUtils.random(0.005f, 0.02f));
        Collections.sort(floats, Collections.reverseOrder());
        for (Float s : floats)
            backgroundEntities.add(new Mushroom(MathUtils.random(-3f, 3f), 0, s));

        // Grass
        for (int i=0; i<16; i++)
            backgroundEntities.add(new Grass(MathUtils.random(-3f, 3f), 0, MathUtils.random(0.01f, 0.014f)));

        // Foreground props
        // Grass
        floats.clear();
        for (int i=0; i<10; i++)
            floats.add(MathUtils.random(-0.25f, 0f));
        Collections.sort(floats, Collections.reverseOrder());
        for (Float y : floats)
            foregroundEntities.add(new Grass(MathUtils.random(-3f, 3f), y, MathUtils.random(0.011f, 0.018f)));
        // Butterflies
        for (int i=0; i<3; i++)
            foregroundEntities.add(new Butterfly(MathUtils.random(-3f, 3f), MathUtils.random(1f, 2f)));


        // Load save gwel.game
        // https://github.com/libgdx/libgdx/wiki/File-handling
        if (Gdx.files.local("savedata.json").exists()) {
            Gdx.app.log("savedata", "savedata file exists");
            FileHandle saveFile = Gdx.files.local("savedata.json");
            System.out.println(saveFile.readString());
        } else {
            Gdx.app.log("savedata", "savedata file doesn't exist");
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isTouched() && touchPathIndex<touchPath.length-4) {
            currentTouch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            if (currentTouch.dst2(lastTouch) > 8) {
                lastTouch.set(currentTouch);
                cameraUI.unproject(currentTouch);
                touchPath[touchPathIndex++] = currentTouch.x;
                touchPath[touchPathIndex++] = currentTouch.y;
            }
        } else {
            touchPathIndex = 0;
        }

        timeOfDay += delta / 86400;
        if (timeOfDay >= 1.0f) timeOfDay -= 1.0f;

        world.step(1/60.f, 6, 2);

        creature.update(delta);

        // Add flies if necessary
        if (MathUtils.floor(creature.dirty / 20f) > flies.size) {
            DirtyFly fly = new DirtyFly(creature);
            foregroundEntities.add(fly);
            flies.add(fly);
        }

        for (Iterator<Entity> iter = backgroundEntities.iterator(); iter.hasNext(); ) {
            Entity entity = iter.next();
            entity.update(delta);
            if (entity.mustDispose)
                iter.remove();
        }

        for (Iterator<Entity> iter = foregroundEntities.iterator(); iter.hasNext(); ) {
            Entity entity = iter.next();
            entity.update(delta);
            if (entity.mustDispose)
                iter.remove();
        }

        cameraWorld.update();


        //Gdx.gl.glClearColor(1, 1, 1, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.renderSky(timeOfDay);

		/*
		if (touchPathIndex >= 4) {
			shapeRenderer.setColor(0, 0, 1, 1);
			float lastX = touchPath[0];
			float lastY = touchPath[1];
			float nextX, nextY;
			for (int i = 2; i < touchPathIndex; ) {
				nextX = touchPath[i++];
				nextY = touchPath[i++];
				shapeRenderer.rectLine(lastX, lastY, nextX, nextY, 4);
				lastX = nextX;
				lastY = nextY;
			}
		}
		*/

        renderer.setProjectionMatrix(cameraWorld.combined);

        for (Entity entity : backgroundEntities)
            entity.draw(renderer);

        for (DrawablePhysics drawable : drawables)
            drawable.draw(renderer);

        creature.draw(renderer);

        for (PhysicsAvatar physicsAvatar : food)
            physicsAvatar.draw(renderer);

        for (Entity entity : foregroundEntities)
            entity.draw(renderer);


        renderer.flush();

        renderer.setProjectionMatrix(cameraUI.combined);

        buttonFeedIcon.draw(renderer);
        buttonCleanIcon.draw(renderer);
        buttonPetIcon.draw(renderer);
        //buttonPet.draw(renderer);
        buttonPlay.draw(renderer);

        if (soap) {
            //cursorSoap.update(delta);
            cursorSoap.draw(renderer);
        }

        renderer.flush();

        batch.begin();

        //buttonFeedSprite.draw(batch);

        if (GemalsGame.DEBUG) {
            float height = game.defaultfont.getLineHeight() + 4;
            game.defaultfont.draw(batch, "state: " + creature.state, Gdx.graphics.getWidth()-180, Gdx.graphics.getHeight() - height);
            game.defaultfont.draw(batch, "life steps: " + creature.steps, 20, Gdx.graphics.getHeight() - height);
            game.defaultfont.draw(batch, "energy: " + creature.energy, 20, Gdx.graphics.getHeight() - 2*height);
            game.defaultfont.draw(batch, "hungry: " + creature.hungry, 20, Gdx.graphics.getHeight() - 3*height);
            game.defaultfont.draw(batch, "sleepy: " + creature.sleepy, 20, Gdx.graphics.getHeight() - 4*height);
            game.defaultfont.draw(batch, "dirty: " + creature.dirty, 20, Gdx.graphics.getHeight() - 5*height);
            game.defaultfont.draw(batch, "mood: " + creature.mood, 20, Gdx.graphics.getHeight() - 6*height);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("RESIZE", "screen resizing " + width + " " + height);

        float camHeight = 3.0f;
        float camWidth = camHeight * width / height;
        //float x = camera.position.x;
        //float y = camera.position.y;
        cameraWorld.setToOrtho(false, camWidth, camHeight);
        cameraWorld.position.set(0, 0.5f, 0);

        float buttonBarWidth = Math.min(width, height);
        float buttonBarStep = buttonBarWidth / (5+3*4);
        float buttonRadius = 1.5f * buttonBarStep;
        buttonFeed = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + buttonBarStep + buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonFeed.setColor(Color.GREEN);
        buttonFeed.setColorMod(1, 1, 1, 0.4f);
        float spriteScale = buttonRadius*2;

        buttonFeedIcon.setPosition(width*0.5f - buttonBarWidth*0.5f + buttonBarStep + buttonRadius,
                buttonBarStep + buttonRadius);
        buttonFeedIcon.setScale(spriteScale * 0.032f, -spriteScale * 0.032f);

        buttonClean = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + 2*buttonBarStep + 3*buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonClean.setColor(Color.BLUE);
        buttonCleanIcon.setPosition(width*0.5f - buttonBarWidth*0.5f + 2*buttonBarStep + 3*buttonRadius,
                buttonBarStep + buttonRadius);
        buttonCleanIcon.setScale(spriteScale * 0.015f, -spriteScale * 0.02f);

        buttonPet = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + 3*buttonBarStep + 5*buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonPet.setColor(Color.PINK);
        buttonPetIcon.setPosition(width*0.5f - buttonBarWidth*0.5f + 3*buttonBarStep + 5*buttonRadius,
                buttonBarStep + buttonRadius);
        buttonPetIcon.setScale(spriteScale * 0.045f, -spriteScale * 0.045f);

        buttonPlay = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + 4*buttonBarStep + 7*buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonPlay.setColor(Color.ORANGE);

        cameraUI.setToOrtho(false, width, height);
        cameraUI.position.set(width/2, height/2, 0);
        batch.setProjectionMatrix(cameraUI.combined);
    }

    public void getState() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("creature", creature.getState());
        /*json.addChild("id", new JsonValue(id));

        JsonValue origin = new JsonValue(JsonValue.ValueType.array);
        origin.addChild(new JsonValue(localOrigin.x));
        origin.addChild(new JsonValue(localOrigin.y));
        json.addChild("origin", origin);*/

        //JsonValue shapes = new JsonValue(JsonValue.ValueType.array);
        Gdx.app.log("savestate", json.asString());
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
    }


    @Override
    public boolean tap(float x, float y, int count, int button) {
        /*Vector3 unproject = cameraWorld.unproject(new Vector3(x, y, 1.0f));
        if (creature.contains(unproject.x, unproject.y)) {
            Gdx.app.log("Contains", "TOUCH !");
        }*/
        cursorSoap.setPosition(x, y);

        if (buttonFeed.getCenter().dst(x, y) < buttonFeed.getRadius()) {
            if (food.size < 8) {
                float r = MathUtils.random();
                if (r < 0.2f) {
                    food.add(new FoodMeat(world, new Vector2(0, 2)));
                } else if (r < 0.4f) {
                    food.add(new FoodTomato(world, new Vector2(0, 2)));
                } else if (r < 0.6f) {
                    food.add(new FoodGreen(world, new Vector2(0, 2)));
                } else if (r < 0.8f) {
                    food.add(new FoodCarrot(world, new Vector2(0, 2)));
                } else {
                    food.add(new FoodCheese(world, new Vector2(0, 2)));
                }
            }
        } else if (buttonClean.getCenter().dst(x, y) < buttonClean.getRadius()) {
            Gdx.app.log("TOUCH", "button clean");
            soap = !soap;
        } else if (buttonPet.getCenter().dst(x, y) < buttonPet.getRadius()) {
            Gdx.app.log("TOUCH", "button pet");
            creature.pet();
        } else if (buttonPlay.getCenter().dst(x, y) < buttonPlay.getRadius()) {
            Gdx.app.log("TOUCH", "button play");
            creature.play();
        }
        return true;
    }


    @Override
    public boolean zoom(float initialDistance, float distance) {
        float delta = distance - initialDistance;
        cameraWorld.zoom = MathUtils.clamp(cameraWorld.zoom - delta*0.0001f, 0.2f, 3f);
        return true;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (soap) {
            cursorSoap.setPosition(x, Gdx.graphics.getHeight()-y);
            cursorSoap.update(1f/10f);
            if (MathUtils.random() < 0.2f) {
                Vector3 unproject = cameraWorld.unproject(new Vector3(x, y, 1.0f));
                if (creature.contains(unproject.x, unproject.y)) {
                    foregroundEntities.add(new Bubble(unproject.x, unproject.y, foregroundEntities));
                    creature.clean(0.01f);
                    int n = Math.max(0, flies.size - MathUtils.floor(creature.dirty / 20f));
                    for (int i=0; i<n; i++) {
                        if (flies.get(i).state == DirtyFly.State.STAYING)
                                flies.get(i).dispose();
                    }
                }
            }
        } else {
            cameraWorld.position.x -= deltaX * 0.002f * cameraWorld.zoom;
            cameraWorld.position.y += deltaY * 0.002f * cameraWorld.zoom;
        }
        return true;
    }


    public void buildStageBorders(float width, float height, float thickness) {
        thickness /= 2;

        // Create Box2D ground
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(0.0f, -thickness);
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(width *0.5f, thickness);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.density = 0.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.3f;
        fixtureDef.filter.categoryBits = PhysicsCategories.GROUND;
        groundBody.createFixture(fixtureDef);
        DrawablePhysics ground = new DrawablePhysics(groundBody);
        drawables.add(ground);

        // Left wall
        groundBodyDef.position.set(-width *0.5f - thickness, height *0.5f - thickness);
        groundBody = world.createBody(groundBodyDef);
        groundBox = new PolygonShape();
        groundBox.setAsBox(thickness, height *0.5f);
        fixtureDef.shape = groundBox;
        groundBody.createFixture(fixtureDef);
        ground = new DrawablePhysics(groundBody);
        drawables.add(ground);

        // Right wall
        groundBodyDef.position.set(width *0.5f + thickness, height *0.5f - thickness);
        groundBody = world.createBody(groundBodyDef);
        groundBox = new PolygonShape();
        groundBox.setAsBox(thickness, height *0.5f);
        fixtureDef.shape = groundBox;
        groundBody.createFixture(fixtureDef);
        ground = new DrawablePhysics(groundBody);
        drawables.add(ground);
    }
}
