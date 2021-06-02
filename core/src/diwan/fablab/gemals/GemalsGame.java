package diwan.fablab.gemals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;


public class GemalsGame extends com.badlogic.gdx.Game implements GestureListener {
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_PHYSICS = false;

	MyScreen mainScreen;
	BitmapFont defaultfont;
	
	@Override
	public void create () {
		mainScreen = new CreatureScreen(this);
		setScreen(mainScreen);

		GestureDetector gd = new GestureDetector(this);
		Gdx.input.setInputProcessor(gd);

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DroidSerif-Bold.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 20;
		parameter.color = Color.WHITE;
		defaultfont = generator.generateFont(parameter);
		generator.dispose();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		Gdx.app.log("input", "touch down");
		return true;
	}


	@Override
	public boolean tap(float x, float y, int count, int button) {
		System.out.println("tap " + x + " " + y);
		return mainScreen.tap(x, Gdx.graphics.getHeight()-y, count, button);
	}

	@Override
	public boolean longPress(float x, float y) {
		Gdx.app.log("input", "long press");
		return true;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		//Gdx.app.log("input", "fling");
		return true;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		//Gdx.app.log("input", "pan start");
		return mainScreen.pan(x, y, deltaX, deltaY);
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		//Gdx.app.log("input", "pan stop");
		return true;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		Gdx.app.log("input", "zoom " + initialDistance + " " + distance);
		return mainScreen.zoom(initialDistance, distance);
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		//Gdx.app.log("input", "pinch start");
		return true;
	}

	@Override
	public void pinchStop() {
		//Gdx.app.log("input", "pinch stop");
	}
}
