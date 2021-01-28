package gwel.game.graphics;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import diwan.fablab.gemmals.graphics.MyRenderer;
import gwel.game.anim.Animation;

import java.util.ArrayList;


public class ComplexShape implements Shape {
    protected ComplexShape parent;
    private final ArrayList<ComplexShape> children;
    private final ArrayList<Shape> shapes; // CompleShapes and simple shapes
    private Animation[] animations;
    private final Vector2 localOrigin;  // Pivot point
    private final Affine2 transform, oldTransform, nextTransform;
    private String id = "";
    private boolean transitioning = false;
    private float transitionDuration;
    private float transitionTime;


    public ComplexShape() {
        children = new ArrayList<>();
        shapes = new ArrayList<>();
        localOrigin = new Vector2();
        animations = new Animation[0];
        transform = new Affine2();
        oldTransform = new Affine2();
        nextTransform = new Affine2();
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
        if (shape instanceof ComplexShape) {
            children.add((ComplexShape) shape);
            ((ComplexShape) shape).parent = this;
        }
    }

    // Return all chilren (Drawables and ComplexShapes)
    public ArrayList<Shape> getShapes() {
      return shapes;
    }

    // Return only complexShape children
    public ArrayList<ComplexShape> getChildren() { return children; }

    // Remove all children
    public void clear() {
        children.clear();
        shapes.clear();
    }


    public Vector2 getLocalOrigin() {
      return localOrigin.cpy();
    }

    public void setLocalOrigin(float x, float y) {
        localOrigin.set(x, y);
    }


    public Affine2 getTransform() {
      return new Affine2(transform);
    }

    public Affine2 getAbsoluteTransform() {
      Affine2 mat = new Affine2(transform);
      if (parent == null) {
          return mat;
      } else {
          return mat.preMul(parent.getAbsoluteTransform());
      }
    }


    public void hardTransform(Affine2 transform) {
        transform.applyTo(localOrigin);
        for (Shape shape : shapes)
            shape.hardTransform(transform);
        if (transform.m00 != 0) {
            // Must scale animation tree
            for (Animation animation : animations)
                animation.scale(transform.m00);
        }
    }


    public String getId() {
        return id;
    }

    public void setId(String label) {
        id = label;
    }


    public ComplexShape getById(String label) {
        if (id.equals(label))
            return this;
        for (ComplexShape child : children) {
            if (child.getById(label) != null)
                return child.getById(label);
        }
        return null;
    }


    public ArrayList<String> getIdList() {
        ArrayList<String> list = new ArrayList<>();
        list.add(id);
        for (ComplexShape child : children) {
            list.addAll(child.getIdList());
        }
        return list;
    }


    public ArrayList<ComplexShape> getPartsList() {
        ArrayList<ComplexShape> list = new ArrayList<>();
        list.add(this);
        for (ComplexShape child : children) {
            list.addAll(child.getPartsList());
        }
        return list;
    }


    public Animation[] getAnimationList() { return animations; }

    public void setAnimationList(Animation[] animList) {
        animations = animList;
    }

    public void clearAnimationList() {
        transform.idt();
        animations = new Animation[0];
    }


    public Animation getAnimation(int n) { return animations[n]; }

    public void setAnimation(int i, Animation anim) { animations[i] = anim; }

    public void addAnimation(Animation anim) {
        Animation[] newAnimations = new Animation[animations.length+1];
        System.arraycopy(animations, 0, newAnimations, 0, animations.length);
        newAnimations[animations.length] = anim;
        animations = newAnimations;
    }

    public void removeAnimation(int idx) {
        Animation[] newAnimations = new Animation[animations.length-1];
        int i = 0;
        for (int n = 0; n < newAnimations.length; n++) {
            if (i == idx)
                i++;
            newAnimations[n] = animations[i++];
        }
        animations = newAnimations;
    }


    public void update(float dtime) {
        // dtime is in seconds

        if (transitioning) {
            transitionTime += dtime;
            float t = transitionTime/transitionDuration;
            if (transitionTime >= transitionDuration) {
                transitioning = false;
                t = 1.0f;
            }
            transform.setToTranslation(localOrigin);
            transform.mul(Animation.lerpAffine(oldTransform, nextTransform, t));
            transform.translate(-localOrigin.x, -localOrigin.y);
        } else if (animations.length > 0) {
            transform.setToTranslation(localOrigin);
            for (int i=animations.length-1; i>=0; i--) {
                transform.mul(animations[i].update(dtime));
            }
            transform.translate(-localOrigin.x, -localOrigin.y);
        }

        for (ComplexShape child : children)
            child.update(dtime);
    }

    public void reset() {
        if (animations.length > 0) {
            for (Animation anim : animations) {
                anim.reset();
            }
            transform.idt();
        }
        for (ComplexShape child : children)
            child.reset();
    }


    // Returns true if all animations are running
    public boolean isAnimationRunning() {
        boolean running = true;
        for (Animation animation : animations) {
            running = running && animation.isRunning();
        }
        return running;
    }

    // Returns true if any animation is stopped
    public boolean isAnimationStopped() {
        for (Animation animation : animations) {
            if (animation.isStopped())
                return true;
        }
        return false;
    }


    public void transitionAnimation(Animation[] nextAnims, float duration) {
      // duration is in seconds
        if (animations.length > 0) {
            transitioning = true;
            transitionDuration = duration;
            transitionTime = 0;
            oldTransform.idt();
            for (Animation anim : animations) {
                oldTransform.preMul(anim.getTransform());
            }
            nextTransform.idt();
            for (Animation anim : nextAnims) {
                nextTransform.preMul(anim.getTransform());
            }
        }
        animations = nextAnims;
    }


    @Override
    public void setColorMod(float mr, float mg, float mb, float ma) {
        for (Shape shape : shapes)
            shape.setColorMod(mr, mg, mb, ma);
    }


    public void draw(MyRenderer renderer) {
        renderer.pushMatrix(transform);
        for (Drawable shape : shapes)
            shape.draw(renderer);
        renderer.popMatrix();
    }


    public ComplexShape copy() {
        ComplexShape copy = new ComplexShape();
        for (Shape shape : shapes)
            copy.addShape(shape.copy());
        copy.setLocalOrigin(localOrigin.x, localOrigin.y);
        Animation[] animList = new Animation[animations.length];
        for (int i=0; i<animList.length; i++)
            animList[i] = animations[i].copy();
        copy.setAnimationList(animList);

        return copy;
    }


    public static ComplexShape fromJson(JsonValue json) {
        ComplexShape cs = new ComplexShape();
        cs.setId(json.getString("id"));
        if (json.has("shapes")) {
            for (JsonValue shape : json.get("shapes")) {
                if (shape.has("type")) {  // Treat as simple shape
                    String type = shape.getString("type");
                    if (type.equals("polygon")) {
                        DrawablePolygon p = new DrawablePolygon();
                        p.setVertices(shape.get("vertices").asFloatArray());

                        int[] triangles = shape.get("triangles").asIntArray();
                        short[] trianglesShort = new short[triangles.length];
                        for (int j=0; j<triangles.length; j++)
                            trianglesShort[j] = (short) triangles[j];
                        p.setIndices(trianglesShort);

                        float[] c = shape.get("color").asFloatArray();
                        p.setColor(c[0], c[1], c[2], c[3]);

                        cs.addShape(p);
                    } else if (type.equals("circle")) {
                        float[] params = shape.get("params").asFloatArray();
                        DrawableCircle c = new DrawableCircle(params[0], params[1], params[2]);
                        float[] co = shape.get("color").asFloatArray();
                        c.setColor(co[0], co[1], co[2], co[3]);
                        cs.addShape(c);
                    }
                } else if (shape.has("id")) {  // Treat as ComplexShape
                    cs.addShape(fromJson(shape));
                }
            }
        }

        if (json.has("origin")) {
            float[] coord = json.get("origin").asFloatArray();
            cs.setLocalOrigin(coord[0], coord[1]);
        }

        if (json.has("animation")) {
            for (JsonValue animJson : json.get("animation").iterator())
                cs.addAnimation(Animation.fromJson(animJson));
        }

        return cs;
    }


    public JsonValue toJson(boolean saveAnim) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("id", new JsonValue(id));

        JsonValue origin = new JsonValue(JsonValue.ValueType.array);
        origin.addChild(new JsonValue(localOrigin.x));
        origin.addChild(new JsonValue(localOrigin.y));
        json.addChild("origin", origin);

        JsonValue shapes = new JsonValue(JsonValue.ValueType.array);
        for (Drawable shape : getShapes()) {
            if (shape instanceof ComplexShape) {
                shapes.addChild(((ComplexShape) shape).toJson(saveAnim));
            } else if (shape instanceof DrawablePolygon) {
                DrawablePolygon p = (DrawablePolygon) shape;
                JsonValue s = new JsonValue(JsonValue.ValueType.object);
                s.addChild("type", new JsonValue("polygon"));

                JsonValue colorArray = new JsonValue(JsonValue.ValueType.array);
                colorArray.addChild(new JsonValue(p.getColor().r));
                colorArray.addChild(new JsonValue(p.getColor().g));
                colorArray.addChild(new JsonValue(p.getColor().b));
                colorArray.addChild(new JsonValue(p.getColor().a));
                s.addChild("color", colorArray);

                JsonValue verticesArray = new JsonValue(JsonValue.ValueType.array);
                for (float vert : p.getVertices()) {
                    verticesArray.addChild(new JsonValue(vert));
                }
                s.addChild("vertices", verticesArray);

                JsonValue trianglesArray = new JsonValue(JsonValue.ValueType.array);
                for (short triangle : p.getIndices()) {
                    trianglesArray.addChild(new JsonValue(triangle));
                }
                s.addChild("triangles", trianglesArray);

                shapes.addChild(s);
            } else if (shape instanceof DrawableCircle) {
                DrawableCircle c = (DrawableCircle) shape;
                JsonValue s = new JsonValue(JsonValue.ValueType.object);
                s.addChild("type", new JsonValue("circle"));

                JsonValue colorArray = new JsonValue(JsonValue.ValueType.array);                colorArray.addChild(new JsonValue(c.getColor().r));
                colorArray.addChild(new JsonValue(c.getColor().g));
                colorArray.addChild(new JsonValue(c.getColor().b));
                colorArray.addChild(new JsonValue(c.getColor().a));
                s.addChild("color", colorArray);

                JsonValue paramsArray = new JsonValue(JsonValue.ValueType.array);
                paramsArray.addChild(new JsonValue(c.getCenter().x));
                paramsArray.addChild(new JsonValue(c.getCenter().y));
                paramsArray.addChild(new JsonValue(c.getRadius()));
                paramsArray.addChild(new JsonValue(c.getSegments()));
                s.addChild("params", paramsArray);

                shapes.addChild(s);
            }
        }

        json.addChild("shapes", shapes);

        if (saveAnim && animations.length > 0) {
            JsonValue jsonAnimations = new JsonValue(JsonValue.ValueType.array);
            for (Animation anim : animations)
                jsonAnimations.addChild(anim.toJson());
            json.addChild("animation", jsonAnimations);
        }

        return json;
    }


    public String toString() {
        String s = String.format(" [id:%s origin:%.1f %.1f]",
                id, localOrigin.x, localOrigin.y);
        return "ComplexShape@" + Integer.toHexString(hashCode()) + s;
    }
}
