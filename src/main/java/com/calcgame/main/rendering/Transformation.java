package com.calcgame.main.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Transformation {
    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f modelViewMatrix;

    public Transformation() {
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
    }

    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public Matrix4f getModelViewMatrix(GameObject gameObject, Matrix4f viewMatrix) {
        Vector3f rotation = gameObject.getRotation();
        modelViewMatrix.identity().translate(gameObject.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameObject.getScale());
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }

    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();
        viewMatrix.identity();
        viewMatrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0));
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public PointLight transformLight(PointLight pointLight) {
        PointLight curPointLight = new PointLight(pointLight);
        Vector3f lightPos = curPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        return curPointLight;
    }

    public Vector3f transformDir(Vector3f dir) {
        Vector4f aux = new Vector4f(dir, 0);
        aux.mul(viewMatrix);
        return new Vector3f(aux.x, aux.y, aux.z);
    }

    public SpotLight transformLight(SpotLight spotLight) {
        return new SpotLight(transformLight(spotLight.getPointLight()), transformDir(spotLight.getConeDir()), spotLight.getCutoff());
    }

    public Vector4f projected(Vector4f vec) {
        return vec.mulProject(modelViewMatrix);
    }

    public Vector3f projected(Vector3f vec) {
        Vector4f out = projected(new Vector4f(vec, 1));
        return new Vector3f(out.x, out.y, out.z);
    }
}
