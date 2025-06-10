package com.calcgame.main.rendering;

import org.joml.Vector3f;

public class PointLight {
    private Vector3f colour;
    private Vector3f position;
    private float intensity;
    private Attenuation att;

    public PointLight(Vector3f colour, Vector3f position, float intensity, float constant, float linear, float exponent) {
        this.colour = new Vector3f(colour);
        this.position = new Vector3f(position);
        this.intensity = intensity;
        this.att = new Attenuation(constant, linear, exponent);
    }

    public PointLight(Vector3f colour, Vector3f position, float intensity) {
        this(colour, position, intensity, 0, 0, 1);
    }

    public PointLight(PointLight pointLight) {
        this.colour = new Vector3f(pointLight.colour);
        this.position = new Vector3f(pointLight.position);
        this.intensity = pointLight.intensity;
        this.att = new Attenuation(pointLight.att.constant, pointLight.att.linear, pointLight.att.exponent);
    }

    public record Attenuation(float constant, float linear, float exponent) {
    }

    public Vector3f getColor() {
        return colour;
    }

    public Attenuation getAttenuation() {
        return att;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }

    public void setAtt(Attenuation att) {
        this.att = att;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
}
