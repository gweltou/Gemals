package diwan.fablab.gemals;

import com.badlogic.gdx.Screen;

public abstract class MyScreen implements Screen {
    public abstract boolean tap(float x, float y, int count, int button);
    public abstract boolean zoom(float initialDistance, float distance);
    public abstract boolean pan(float x, float y, float deltaX, float deltaY);
}
