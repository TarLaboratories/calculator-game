package com.calcgame.main.objects;

import com.calcgame.main.Utils;
import com.calcgame.main.rendering.GameObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Rectanglef;
import org.joml.Vector2f;

public abstract class ScreenObject extends GameObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private Screen screen;

    public abstract float getWidth();
    public abstract float getHeight();
    public abstract void setCentered(boolean centered);
    public abstract Vector2f getPadding();

    public Screen getScreen() {
        return screen;
    }

    protected void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void setScreenPos(Vector2f screenPos) {
        if (screen == null) {
            LOGGER.warn("Attempt to set screen position of a screen object to {} that is not yet bound to a screen!", screenPos);
        } else screen.setScreenPos(this, screenPos);
    }

    public Vector2f getScreenPos() {
        if (screen == null) {
            LOGGER.warn("Attempt to get screen position of a screen object that is not yet bound to a screen!");
            return new Vector2f(0);
        } else return screen.getScreenPos(this);
    }

    public Rectanglef getBoundingBox() {
        return new Rectanglef(
                new Vector2f(
                        getScreenPos().x - getPadding().x,
                        getScreenPos().y - getPadding().y
                ), new Vector2f(
                        getScreenPos().x + getWidth() + getPadding().x,
                        getScreenPos().y + getHeight() + getPadding().y
                )
        );
    }

    public boolean isOverlapping(ScreenObject other) {
        return Utils.intersectRectangles(getBoundingBox(), other.getBoundingBox());
    }
}
