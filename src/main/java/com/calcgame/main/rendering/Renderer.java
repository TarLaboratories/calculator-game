package com.calcgame.main.rendering;

import static org.lwjgl.glfw.GLFW.*;

public class RenderUtils {
    public static int WIDTH = 720, HEIGHT = 1360/2;
    // 3D stuff
    public static float PROJECTION_CENTER_X = WIDTH / 2f;
    public static float PROJECTION_CENTER_Y = HEIGHT / 2f;
    public static float FIELD_OF_VIEW = WIDTH * 0.8f;
    // cube const
    static int[][] CUBE_LINES = {{0, 1}, {1, 3}, {3, 2}, {2, 0}, {2, 6}, {3, 7}, {0, 4}, {1, 5}, {6, 7}, {6, 4}, {7, 5}, {4, 5}};
    static int[][] CUBE_VERTICES = {{-1, -1, -1}, {1, -1, -1}, {-1, 1, -1}, {1, 1, -1}, {-1, -1, 1}, {1, -1, 1}, {-1, 1, 1}, {1, 1, 1}};

    public static void drawLine(Window window, Vec3 v1, Vec3 v2) {
        System.out.println("1: x = " + v1.x + ", y = " + v1.y + ", z = " + v1.z);
        System.out.println("2: x = " + v2.x + ", y = " + v2.y + ", z = " + v2.z + "\n");

        Vec3 v1Project = project(v1.x, v1.y, v1.z);
        Vec3 v2Project = project(v2.x, v2.y, v2.z);

        System.out.println("1 Project: x = " + v1Project.x + ", y = " + v1Project.y);
        System.out.println("2 Project: x = " + v2Project.x + ", y = " + v2Project.y + "\n");

        g.drawLine((int) v1Project.x, (int) v1Project.y, (int) v2Project.x, (int) v2Project.y);
    }

    public static Vec3 project(float x, float y, float z){
        float scaleProjected = FIELD_OF_VIEW / (FIELD_OF_VIEW + z);
        float xProjected = (x * scaleProjected) + PROJECTION_CENTER_X;
        float yProjected = (y * scaleProjected) + PROJECTION_CENTER_Y;

        return new Vec3(xProjected, yProjected, scaleProjected);
    }
}
