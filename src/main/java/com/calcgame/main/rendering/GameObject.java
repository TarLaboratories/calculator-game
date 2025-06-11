package com.calcgame.main.rendering;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class GameObject {
    private static final Logger LOGGER = LogManager.getLogger();

    private Mesh mesh;
    private final Vector3f position;
    private float scale;
    private final Vector3f rotation;
    private boolean selected;
    protected final List<GameObject> children = new ArrayList<>();

    protected GameObject() {
        position = new Vector3f();
        rotation = new Vector3f();
        setPosition(new Vector3f(0));
        setRotation(0, 0, 0);
        setScale(1);
        setSelected(false);
    }

    public GameObject(Mesh mesh) {
        this();
        setMesh(mesh);
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setPosition(Vector3f coords) {
        this.move(new Vector3f(coords).sub(this.position));
    }

    public void move(Vector3f delta) {
        this.position.add(delta);
        children.forEach((child) -> child.move(delta));
    }

    public void move(float x, float y, float z) {
        move(new Vector3f(x, y, z));
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.multiplyScale(scale/this.scale);
    }

    public void multiplyScale(float scaleMultiplier) {
        this.scale = this.scale*scaleMultiplier;
        children.forEach((child) -> child.multiplyScale(scaleMultiplier));
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        addRotation(x - this.rotation.x, y - this.rotation.y, z - this.rotation.z);
    }

    public void addRotation(float x, float y, float z) {
        this.rotation.x += x;
        this.rotation.y += y;
        this.rotation.z += z;
        this.children.forEach((child) -> child.addRotation(x, y, z));
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public float intersectRay(Vector3f origin, Vector3f dir, Transformation transformation, Matrix4f viewMatrix) {
        float distance = Float.POSITIVE_INFINITY;
        transformation.getModelViewMatrix(this, viewMatrix);
        for (Triangle triangle : getMesh().getTriangles()) {
            Triangle t = triangle.projected(transformation);
            float result = Intersectionf.intersectRayTriangle(origin, dir, t.v1(), t.v2(), t.v3(), 1e-3f);
            if (result != -1 && result < distance)
                distance = result;
        }
        return distance;
    }

    protected void render(Transformation transformation, Matrix4f viewMatrix, ShaderProgram shaderProgram) {
        Matrix4f modelViewMatrix = transformation.getModelViewMatrix(this, viewMatrix);
        shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
        shaderProgram.setUniform("material", mesh.getMaterial());
        mesh.render();
        for (GameObject child : children) child.render(transformation, viewMatrix, shaderProgram);
    }
}
