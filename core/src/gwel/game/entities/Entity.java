package gwel.game.entities;

import diwan.fablab.gemals.graphics.MyRenderer;

public abstract class Entity {
    abstract public void draw(MyRenderer renderer);
    abstract public void update(float delta);
    public boolean mustDispose = false;
    abstract public void dispose();
}
