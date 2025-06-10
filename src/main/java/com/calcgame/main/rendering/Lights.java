package com.calcgame.main.rendering;

import java.util.ArrayList;
import java.util.List;

public class Lights {
    private final List<PointLight> pointLights = new ArrayList<>();
    private final List<SpotLight> spotLights = new ArrayList<>();
    private final List<DirLight> dirLights = new ArrayList<>();

    public Lights() {}

    public void addLight(PointLight pointLight) {
        pointLights.add(pointLight);
    }

    public void addLight(SpotLight spotLight) {
        spotLights.add(spotLight);
    }

    public void addLight(DirLight dirLight) {
        dirLights.add(dirLight);
    }

    public List<DirLight> getDirLights() {
        return dirLights;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }

    public List<SpotLight> getSpotLights() {
        return spotLights;
    }
}
