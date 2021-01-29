package gwel.game.entities;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import diwan.fablab.gemmals.graphics.MyRenderer;
import gwel.game.anim.Animation;
import gwel.game.anim.AnimationCollection;
import gwel.game.graphics.ComplexShape;

import java.io.*;
import java.util.*;


public class Avatar {
    public ComplexShape shape;
    private final Vector2 position = new Vector2();
    private HashMap<String, Animation[][]> animations;  // Should have better perfs than animationCollection
    private ComplexShape[] partsList;


    public Avatar() {
    }


    public void setPosition(float x, float y) { position.set(x, y); }

    public void scale(float s) {
        shape.hardTransform(new Affine2().scale(s, s));
    }


    public void setShape(ComplexShape root) {
        shape = root;
        partsList = root.getPartsList().toArray(new ComplexShape[0]);
    }

    public ComplexShape getShape() { return shape; }


    public ComplexShape[] getPartsList() { return partsList; }


    public void setAnimationCollection(AnimationCollection collection) {
        ArrayList<String> partsName = new ArrayList<>();
        for (int i=0; i<partsList.length; i++)
            partsName.add(partsList[i].getId());

        animations = new HashMap<>();
        for (String animName : collection.getPosturesNameList()) {
            HashMap<String, Animation[]> fullAnimation = collection.getPosture(animName);
            Animation[][] fullAnimationArray = new Animation[partsList.length][];
            // fullAnimationArray entries are left to 'null' if empty
            Arrays.fill(fullAnimationArray, null);
            for(Map.Entry<String, Animation[]> entry : fullAnimation.entrySet()) {
                int idx = partsName.indexOf(entry.getKey());
                if (idx >= 0)
                    fullAnimationArray[idx] = entry.getValue();
            }
            animations.put(animName, fullAnimationArray);
        }
    }


    public String[] getAnimationsNameList() {
        return animations.keySet().toArray(new String[0]);
    }


    // Should be used by animation editor only (slow)
    public void setPosture(HashMap<String, Animation[]> posture, float duration) {
        Set<String> ids = posture.keySet();
        for (ComplexShape part : partsList) {
            if (ids.contains(part.getId())) {
                Animation[] anims;
                anims = posture.get(part.getId());
                part.transitionAnimation(anims, duration);
            } else {
                part.transitionAnimation(new Animation[0], duration);
            }
        }
        /*
        for (Map.Entry<String, Animation[]> entry : posture.entrySet()) {
            int i=0;
            for (; i<partsList.length; i++) {
                if (partsList[i].getId().equals(entry.getKey())) {
                    partsList[i].transitionAnimation((ArrayList<Animation>) Arrays.asList(entry.getValue()), duration);
                    break;
                }
            }
        }
        */
    }

    public void setPosture(HashMap<String, Animation[]> fullAnimation) {
        setPosture(fullAnimation, 0.2f);
    }


    // Activate a fullAnimation from the collection
    public void loadPosture(String postureName) {
        Animation[][] posture = animations.get(postureName);
        int i = 0;
        for (Animation[] animList : posture) {
            if (animList != null)
                partsList[i].setAnimationList(animList);
            i++;
        }
    }


    // Play every animation from the animationCollection sequencially
    public void playSequencially() {

    }


    public void updateAnimation(float dtime) { shape.update(dtime); }

    public void resetAnimation() {
        shape.reset();
    }

    public void clearAnimation() {
        for (ComplexShape part : partsList)
            part.clearAnimationList();
    }


    public void draw(MyRenderer renderer) {
        renderer.pushMatrix();
        renderer.translate(position.x, position.y);
        shape.draw(renderer);
        renderer.popMatrix();
    }


    public static Avatar fromFile(FileHandle file) {
        return fromFile(file, true, true);
    }

    public static Avatar fromFile(FileHandle file, boolean loadGeom, boolean loadAnim) {
        Avatar avatar = new Avatar();
        JsonValue fromJson = null;
        //InputStream in = new FileInputStream(file);
        fromJson = new JsonReader().parse(file);
        if (fromJson == null)
            return avatar;

        // Load shape first
        if (loadGeom && fromJson.has("geometry")) {
            JsonValue jsonGeometry = fromJson.get("geometry");
            avatar.setShape(ComplexShape.fromJson(jsonGeometry));
        }

        if (loadAnim && fromJson.has("animation")) {
            JsonValue jsonAnimation = fromJson.get("animation");
            AnimationCollection animationCollection = AnimationCollection.fromJson(jsonAnimation);
            avatar.setAnimationCollection(animationCollection);
            if (animationCollection.size() > 0) {
                avatar.setPosture(animationCollection.getPosture(0));
            }
        }

        return avatar;
    }

    public void saveFile(String filename, AnimationCollection animationCollection) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("geometry", shape.toJson(false));
        json.addChild("animation", animationCollection.toJson());

        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(json.prettyPrint(JsonWriter.OutputType.json, 80));
            writer.close();
            System.out.println("Avatar data saved to " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to " + filename);
            e.printStackTrace();
        }
    }
}
