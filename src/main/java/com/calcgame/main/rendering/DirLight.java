package com.calcgame.main.rendering;

import org.joml.Vector3f;

public class DirLight {
    private Vector3f color;
    private Vector3f direction;
    private float intensity;

    public DirLight(Vector3f color, Vector3f direction, float intensity) {
        setColor(color);
        setDirection(direction);
        setIntensity(intensity);
    }

    public float getIntensity() {
        return intensity;
    }

    public Vector3f getColor() {
        return color;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
