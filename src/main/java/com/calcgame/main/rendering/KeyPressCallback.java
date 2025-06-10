package com.calcgame.main.rendering;

@FunctionalInterface
public interface KeyPressCallback {
    void onPress(int action, int mods);
}
