package com.calcgame.main.rendering;

public class Resources {
    public static final String RESOURCE_PATH = "src/main/resources/";

    public static String model(String fileName) {
        return RESOURCE_PATH + "models/" + fileName + ".obj";
    }

    public static String shader(String fileName) {
        return RESOURCE_PATH + "shaders/" + fileName;
    }

    public static String texture(String fileName) {
        return "/textures/" + fileName + ".png";
    }

    public static String font() {
        return "font";
    }

    public static int fontCols() {
        return 16;
    }

    public static int fontRows() {
        return 16;
    }

    public static String font(String fileName) {
        if (!fileName.contains("."))
            return "/fonts/" + fileName + ".ttf";
        return "/fonts/" + fileName;
    }
}
