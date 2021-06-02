package diwan.fablab.gemals.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import diwan.fablab.gemals.Creature;
import diwan.fablab.gemals.graphics.MyRenderer;
import gwel.game.anim.TFEaseFromTo;
import gwel.game.anim.TFSin;
import gwel.game.anim.TFTimetable;
import gwel.game.anim.TimeFunction;
import gwel.game.entities.Avatar;
import gwel.game.entities.Entity;


public class DirtyFly extends Entity {
    public static final Avatar mould = Avatar.fromFile(Gdx.files.internal("avatar/dirtyfly.json"));
    private final Avatar avatar;
    private TFTimetable fnX, fnY;
    private TFEaseFromTo fn0, fn0Scale;
    private TFSin sinx;
    private float posX, posY, prevPosX = 0f;
    public State state;
    private Creature creature;

    public enum State {
        ARRIVING,
        STAYING,
        GOING_AWAY,
    }

    public DirtyFly(Creature creature) {
        this.creature = creature;
        avatar = mould.copy();
        avatar.shape.hardScale(0.01f, -0.01f);

        fnX = new TFTimetable(MathUtils.random(3.0f, 5.0f), true, true);
        float[] table = new float[MathUtils.ceil(MathUtils.random(5, 9))];
        table[0] = 0.0f;
        for (int i=1; i<table.length; i++)
            table[i] = MathUtils.random(-1, 1) * 0.5f;
        fnX.setTable(table);
        fnX.setEasing("smoother");
        fnX.setParam("duration", MathUtils.random(1.4f, 2.2f));

        fnY = new TFTimetable(MathUtils.random(3.0f, 5.0f), true, true);
        table = new float[MathUtils.ceil(MathUtils.random(5, 9))];
        table[0] = 0.0f;
        for (int i=1; i<table.length; i++)
            table[i] = MathUtils.random(-1, 1) * 0.5f;
        fnY.setTable(table);
        fnY.setEasing("smoother");
        fnY.setParam("duration", MathUtils.random(1.6f, 2.2f));

        fn0 = new TFEaseFromTo(-4f, 0.0f, 0f, 2f, "fastSlow", false, false);
        fn0Scale = new TFEaseFromTo(4f, 1.0f, 0f, 2f, "fastSlow", false, false);
        sinx = new TFSin(1.2f, 0.4f, 0.0f, 0.0f);

        state = State.ARRIVING;
    }

    public void update(float delta) {
        avatar.update(delta);
        fnX.update(delta);
        fnY.update(delta);
        sinx.update(delta);
        posX = fnX.getValue() + creature.body.getPosition().x + sinx.getValue();
        posY = fnY.getValue();

        switch (state) {
            case ARRIVING:
                fn0.update(delta);
                fn0Scale.update(delta);
                if (fn0.getState() == TimeFunction.STOPPED)
                    state = State.STAYING;
                posX += fn0.getValue();
                posY += fn0.getValue();
                avatar.setScale(fn0Scale.getValue());
                break;
            case GOING_AWAY:
                fn0.update(delta);
                if (fn0.getState() == TimeFunction.STOPPED)
                    mustDispose = true;
                posX += fn0.getValue();
                posY += fn0.getValue();
                break;
        }

        avatar.setPosition(posX, posY + 1f);
        avatar.setFlipX((posX - prevPosX) > 0);
        prevPosX = fnX.getValue();
    }

    public void draw(MyRenderer renderer) {
        avatar.draw(renderer);
    }

    public void dispose() {
        state = State.GOING_AWAY;
        fn0 = new TFEaseFromTo(0.0f, 4.0f, 0f, 2f, "fastSlow", false, false);
    }
}
