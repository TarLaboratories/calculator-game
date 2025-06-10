package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

/**
 * A system button that invokes {@link GameState#undo()} when clicked
 */
public class UndoButton extends FuncButton {
    /**
     * Constructs the undo button
     */
    public UndoButton() {
        super("UNDO");
        text = "UNDO";
        tooltip = "Undoes the last action (clicking a button, etc.)";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.undo();
    }

    @Override
    public boolean isVital() {
        return true;
    }
}
