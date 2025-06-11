package com.calcgame.main.rendering;

import com.calcgame.main.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static final Logger LOGGER = LogManager.getLogger();

    public void setVisible(boolean visible) {
        if (visible) glfwShowWindow(id);
        else glfwHideWindow(id);
    }

    public void swapBuffers() {
        glfwSwapBuffers(id);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(id);
    }

    public void setCursorPos(double x, double y) {
        glfwSetCursorPos(id, x, y);
        mouseX = x;
        mouseY = y;
    }

    public static class Builder {
        boolean resizable = false;
        boolean visible = false;
        boolean focused = false;
        boolean maximized = false;

        private static int toGLFWValue(boolean value) {
            return value ? GLFW_TRUE : GLFW_FALSE;
        }

        public Builder() {}

        public Builder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder focused(boolean focused) {
            this.focused = focused;
            return this;
        }

        public Builder maximized(boolean maximized) {
            this.maximized = maximized;
            return this;
        }

        public Window build(GameState state, String title, int width, int height) {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_VISIBLE, toGLFWValue(visible));
            glfwWindowHint(GLFW_RESIZABLE, toGLFWValue(resizable));
            glfwWindowHint(GLFW_FOCUSED, toGLFWValue(focused));
            glfwWindowHint(GLFW_MAXIMIZED, toGLFWValue(maximized));
            return new Window(state, title, width, height);
        }
    }

    private final long id;
    private String title;

    private int width;
    private int height;
    private boolean resized;

    private double mouseX;
    private double mouseY;

    private final HashMap<String, KeyPressCallback> callbacks;
    private final HashMap<Integer, List<String>> keysToCallbackIds;
    private Event onKeyPress;

    private Window(GameState state, String title, int width, int height) {
        this.id = glfwCreateWindow(width, height, title, NULL, NULL);
        this.width = width;
        this.height = height;
        this.resized = true;
        this.title = title;
        this.mouseX = 0;
        this.mouseY = 0;
        this.callbacks = new HashMap<>();
        this.keysToCallbackIds = new HashMap<>();
        this.onKeyPress = state.getEvent(Events.KEY_PRESS);
        glfwSetCursorPosCallback(id, ((window, xpos, ypos) -> {
            this.mouseX = xpos;
            this.mouseY = ypos;
        }));
        glfwSetMouseButtonCallback(id, ((window, button, action, mods) -> {
            if (action == GLFW_PRESS) return;
            state.doAction(Action.blank("input"));
            state.getEvent(Events.MOUSE_CLICK).emit(ActionContext.forData(state, new JSONObject(Map.of(
                    "x", mouseX,
                    "y", mouseY,
                    "button", button
            ))));
        }));
        glfwSetKeyCallback(id, ((window, key, scancode, action, mods) -> {
            LOGGER.trace("Keypress: window id = {}, key = {}, scancode = {}, action = {}, mods = {}", window, key, scancode, action, mods);
            if (window != id) LOGGER.warn("Key press callback window id does not match! ({} != {})", window, id);
            if (action == GLFW_RELEASE && keysToCallbackIds.containsKey(key)) {
                keysToCallbackIds.get(key).forEach((callbackId) -> {
                    if (callbacks.get(callbackId) != null) callbacks.get(callbackId).onPress(action, mods);
                });
            }
            state.doAction(Action.blank("input"));
            if (onKeyPress != null) onKeyPress.emit(ActionContext.forData(state, new JSONObject(Map.of(
                    "window_id", window,
                    "key", key,
                    "scancode", scancode,
                    "action", action,
                    "mods", mods
            ))));
        }));
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    public void setPos(int x, int y) {
        glfwSetWindowPos(id, x, y);
    }

    public void setSize(int width, int height) {
        glfwSetWindowSize(id, width, height);
        this.width = width;
        this.height = height;
        this.resized = true;
    }

    public void setTitle(String title) {
        glfwSetWindowTitle(id, title);
        this.title = title;
    }

    public void close() {
        glfwSetWindowShouldClose(id, true);
    }

    public void destroy() {
        glfwFreeCallbacks(id);
        glfwDestroyWindow(id);
    }

    public void addKeyCallback(String id, int key, KeyPressCallback callback) {
        callbacks.put(id, callback);
        if (!keysToCallbackIds.containsKey(key)) keysToCallbackIds.put(key, new ArrayList<>());
        keysToCallbackIds.get(key).add(id);
    }

    public void removeKeyCallback(String id) {
        callbacks.remove(id);
    }

    public void select() {
        glfwMakeContextCurrent(id);
    }

    public void setOnPressEvent(Event event) {
        onKeyPress = event;
    }

    public boolean isKeyPressed(int key) {
        return glfwGetKey(id, key) == GLFW_PRESS;
    }

    public Vector2f getMouse() {
        return new Vector2f((float) mouseX, (float) mouseY);
    }

    public void hideCursor() {
        glfwSetInputMode(id, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void showCursor() {
        glfwSetInputMode(id, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void updateSize() {
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowSize(id, w, h);
        this.setSize(w[0], h[0]);
    }
}
