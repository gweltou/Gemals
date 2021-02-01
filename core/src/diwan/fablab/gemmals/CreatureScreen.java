package diwan.fablab.gemmals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import diwan.fablab.gemmals.entities.*;
import diwan.fablab.gemmals.graphics.DrawablePhysics;
import diwan.fablab.gemmals.graphics.MyRenderer;
import gwel.game.graphics.DrawableCircle;


public class CreatureScreen extends MyScreen {
    private GemmalsGame game;
    private MyRenderer renderer;
    private SpriteBatch batch;
    private OrthographicCamera cameraUI, cameraWorld;
    static public World world;
    private Creature creature;

    float[] touchPath;
    int touchPathIndex;
    Vector3 lastTouch, currentTouch;

    private DrawableCircle buttonFeed;
    private DrawableCircle buttonClean;
    private DrawableCircle buttonPet;
    private DrawableCircle buttonPlay;

    private final Array<DrawablePhysics> drawables = new Array<>();
    public final Array<Food> food = new Array<>();

    //private MyContactListener contactListener;

    public CreatureScreen(GemmalsGame game) {
        this.game = game;

        world = new World(new Vector2(0.0f, -10.0f), true);
        world.setContactListener(new MyContactListener());

        renderer = new MyRenderer();
        batch = new SpriteBatch();

        cameraUI = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraWorld = new OrthographicCamera();

        touchPath = new float[512];
        touchPathIndex = 0;
        lastTouch = new Vector3();
        currentTouch = new Vector3();

        buildStageBorders(20, 10, 1);

        creature = new Creature(this);
        creature.buildBody(world, new Vector2(0, 0.4f));

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

        world.step(1/60.f, 6, 2);

        creature.update(delta);

        cameraWorld.update();


        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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

        for (DrawablePhysics drawable : drawables)
            drawable.draw(renderer);

        creature.draw(renderer);

        for (PhysicsAvatar physicsAvatar : food)
            physicsAvatar.draw(renderer);

        renderer.flush();


        renderer.setProjectionMatrix(cameraUI.combined);

        buttonFeed.draw(renderer);
        buttonClean.draw(renderer);
        buttonPet.draw(renderer);
        buttonPlay.draw(renderer);

        renderer.flush();

        if (GemmalsGame.DEBUG) {
            float height = game.defaultfont.getLineHeight() + 4;
            batch.begin();
            game.defaultfont.draw(batch, "state: " + creature.state, Gdx.graphics.getHeight()-40, Gdx.graphics.getHeight() - height);

            game.defaultfont.draw(batch, "life steps: " + creature.steps, 20, Gdx.graphics.getHeight() - height);
            game.defaultfont.draw(batch, "energy: " + creature.energy, 20, Gdx.graphics.getHeight() - 2*height);
            game.defaultfont.draw(batch, "hungry: " + creature.hungry, 20, Gdx.graphics.getHeight() - 3*height);
            game.defaultfont.draw(batch, "sleepy: " + creature.sleepy, 20, Gdx.graphics.getHeight() - 4*height);
            game.defaultfont.draw(batch, "dirty: " + creature.dirty, 20, Gdx.graphics.getHeight() - 5*height);
            game.defaultfont.draw(batch, "mood: " + creature.mood, 20, Gdx.graphics.getHeight() - 6*height);
            batch.end();
        }
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
        cameraWorld.zoom = 0.8f;

        float buttonBarWidth = Math.min(width, height);
        float buttonBarStep = buttonBarWidth / (5+3*4);
        float buttonRadius = 1.5f * buttonBarStep;
        buttonFeed = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + buttonBarStep + buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonFeed.setColor(Color.GREEN);
        buttonClean = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + 2*buttonBarStep + 3*buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonClean.setColor(Color.BLUE);
        buttonPet = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + 3*buttonBarStep + 5*buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonPet.setColor(Color.PINK);
        buttonPlay = new DrawableCircle(width*0.5f - buttonBarWidth*0.5f + 4*buttonBarStep + 7*buttonRadius,
                buttonBarStep + buttonRadius, buttonRadius);
        buttonPlay.setColor(Color.ORANGE);

        cameraUI.setToOrtho(false, width, height);
        cameraUI.position.set(width/2, height/2, 0);
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
    public void tap(float x, float y, int count, int button) {
        if (buttonFeed.getCenter().dst(x, y) < buttonFeed.getRadius()) {
            Gdx.app.log("TOUCH", "button feed");
            if (food.size < 5) {
                if (MathUtils.random() < 0.5f) {
                    food.add(new FoodMeat(world, new Vector2(0, 2)));
                } else {
                    food.add(new FoodTomato(world, new Vector2(0, 2)));
                }
            }
        } else if (buttonClean.getCenter().dst(x, y) < buttonClean.getRadius()) {
            Gdx.app.log("TOUCH", "button clean");
            creature.clean();
        } else if (buttonPet.getCenter().dst(x, y) < buttonPet.getRadius()) {
            Gdx.app.log("TOUCH", "button pet");
            creature.pet();
        } else if (buttonPlay.getCenter().dst(x, y) < buttonPlay.getRadius()) {
            Gdx.app.log("TOUCH", "button play");
            creature.play();
        }
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
