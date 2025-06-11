package com.calcgame.main.objects;

import com.calcgame.main.Action;
import com.calcgame.main.Events;
import com.calcgame.main.GameState;
import com.calcgame.main.rendering.FontTexture;
import com.calcgame.main.rendering.GameObject;
import com.calcgame.main.rendering.Mesh;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public class Button extends GameObject {
    private final Logger LOGGER;
    private final TextObject label;

    public Button(GameState state, Vector3f pos, Mesh mesh, String name, Vector3f labelPos, Action onClick) {
        super(mesh);
        this.LOGGER = LogManager.getLogger(name);
        LOGGER.trace("Creating label for button {}", name);
        this.label = new TextObject(name, FontTexture.loadFont("font"));
        this.label.setCentered(true);
        this.label.setPosition(labelPos);
        this.children.add(this.label);
        this.setPosition(pos);
        LOGGER.trace("Created button {} at {}", name, pos);
        state.getEvent(Events.MOUSE_CLICK).addListener(new Action(name) {
            @Override
            protected void redoInternal() {
                if (Button.this.isSelected()) {
                    state.appendToLastAction(onClick).redo();
                }
            }

            @Override
            protected void undoInternal() {}

            @Override
            public boolean undoable() {
                return true;
            }
        }, name);
    }

    public void setName(String text) {
        LOGGER.trace("Changing button name from {} to {}", label.getText(), text);
        label.setText(text);
    }

    public String getName() {
        return label.getText();
    }
}
