package com.calcgame.main.objects;

import com.calcgame.main.rendering.GameObject;
import com.calcgame.main.rendering.Mesh;
import com.calcgame.main.rendering.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Screen extends GameObject {
    private float width, height;
    private final List<ScreenObject> screenObjects = new ArrayList<>();

    public Screen(float width, float height, Vector4f backgroundColor) {
        super(Mesh.quad(
                new Vector3f(-width/2, -height/2, 0),
                new Vector3f(-width/2, height/2, 0),
                new Vector3f(width/2, height/2, 0),
                new Vector3f(width/2, -height/2, 0),
                new Vector3f(0, 0, 1),
                backgroundColor
        ));
        this.width = width;
        this.height = height;
    }

    public Screen(float width, float height, Texture texture) {
        this(width, height, new Vector4f(0f));
        this.getMesh().setTexture(texture);
    }

    public Vector3f getTopLeftCornerPos() {
        return new Vector3f(this.getPosition()).sub(width/2, height/2, 0);
    }

    public Vector2f getScreenPos(ScreenObject obj) {
        return new Vector2f(obj.getPosition().x, obj.getPosition().y).sub(this.getTopLeftCornerPos().x, this.getTopLeftCornerPos().y);
    }

    public void setScreenPos(ScreenObject obj, Vector2f pos) {
        obj.setPosition(new Vector3f(this.getTopLeftCornerPos()).add(new Vector3f(pos, 0)));
    }

    public void add(ScreenObject obj) {
        children.add(obj);
        screenObjects.add(obj);
        obj.setCentered(true);
        obj.setScreen(this);
        boolean overlap = true;
        float minOverlappingY = getHeight();
        while (overlap) {
            overlap = false;
            for (ScreenObject child : screenObjects) {
                if (obj.isOverlapping(child)) {
                    overlap = true;
                    minOverlappingY = Math.min(minOverlappingY, child.getBoundingBox().maxY);
                    obj.setScreenPos(new Vector2f(child.getBoundingBox().maxX + obj.getPadding().x, obj.getScreenPos().y));
                    if (obj.getBoundingBox().maxX > this.getWidth()) {
                        obj.setScreenPos(new Vector2f(obj.getPadding().x + obj.getWidth()/2, minOverlappingY + obj.getPadding().y));
                        minOverlappingY = getHeight();
                    }
                }
            }
        }
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }
}
