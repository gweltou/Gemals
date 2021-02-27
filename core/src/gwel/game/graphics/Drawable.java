package gwel.game.graphics;


import diwan.fablab.gemals.graphics.MyRenderer;

public interface Drawable {
    void draw(MyRenderer renderer);
    void setColorMod(float rm, float gm, float bm, float am);
}
