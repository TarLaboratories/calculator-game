package com.calcgame.main.rendering;

import org.joml.Vector3f;

public class SpotLight {
    private PointLight pointLight;
    private Vector3f coneDir;
    private float cutoff;

    public SpotLight(PointLight pointLight, Vector3f coneDir, float cutoff) {
        setPointLight(pointLight);
        setConeDir(coneDir);
        setCutoff((float) Math.cos(cutoff));
    }

    public PointLight getPointLight() {
        return pointLight;
    }

    public float getCutoff() {
        return cutoff;
    }

    public Vector3f getConeDir() {
        return coneDir;
    }

    public void setConeDir(Vector3f coneDir) {
        this.coneDir = coneDir;
    }

    public void setPointLight(PointLight pointLight) {
        this.pointLight = pointLight;
    }

    public void setCutoff(float cutoff) {
        this.cutoff = cutoff;
    }
}
