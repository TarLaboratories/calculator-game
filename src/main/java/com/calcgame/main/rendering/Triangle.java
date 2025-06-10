package com.calcgame.main.rendering;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record Triangle(Vector3f v1, Vector3f v2, Vector3f v3) {
    public Triangle projected(Transformation transformation) {
        return new Triangle(
                transformation.projected(v1),
                transformation.projected(v2),
                transformation.projected(v3)
        );
    }

    @Override
    public @NotNull String toString() {
        return "(" + v1 + ", " + v2 + ", " + v3 + ")";
    }
}
