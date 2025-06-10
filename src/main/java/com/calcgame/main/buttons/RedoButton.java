package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

/**
 * A system button that invokes {@link GameState#redo()} when clicked
 */
public class RedoButton extends FuncButton {
    /**
     * Constructs the redo button
     */
    public RedoButton() {
        super("REDO");
        text = "REDO";
        tooltip = "Redoes the last undone action";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.redo();
    }

    @Override
    public boolean isVital() {
        return true;
    }
}
