package com.calcgame.main.buttons;

import com.calcgame.main.Action;
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
        state.appendToLastAction(new Action("redo") {
            @Override
            protected void redoInternal() {
                state.redo();
            }

            @Override
            protected void undoInternal() {}

            @Override
            public boolean undoable() {
                return true;
            }

            @Override
            public boolean shouldSkipUndo() {
                return true;
            }
        }).redo();
    }

    @Override
    public boolean isVital() {
        return true;
    }
}
