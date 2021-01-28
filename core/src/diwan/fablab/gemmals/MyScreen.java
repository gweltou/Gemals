package diwan.fablab.gemmals;

import com.badlogic.gdx.Screen;

public abstract class MyScreen implements Screen {
    public abstract void tap(float x, float y, int count, int button);
}
