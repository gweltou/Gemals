package diwan.fablab.gemmals.entities;

public class Food extends PhysicsAvatar {
    private boolean isEdible = false;

    public void setEdible() { isEdible = true; }

    public boolean isEdible() { return isEdible; }
}
