package com.calcgame.main.rendering;

import org.joml.Vector4f;

public class Material {
    private final Vector4f ambient;
    private final Vector4f diffuse;
    private final Vector4f specular;
    private final boolean hasTexture;
    private final float reflectance;

    public Material(Vector4f ambient, Vector4f diffuse, Vector4f specular, boolean hasTexture, float reflectance) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.hasTexture = hasTexture;
        this.reflectance = reflectance;
    }

    public boolean isTextured() {
        return hasTexture;
    }

    public float getReflectance() {
        return reflectance;
    }

    public Vector4f getAmbientColour() {
        return ambient;
    }

    public Vector4f getDiffuseColour() {
        return diffuse;
    }

    public Vector4f getSpecularColour() {
        return specular;
    }
}
