package com.calcgame.main.buttons;

import com.calcgame.main.Action;
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
        state.appendToLastAction(new Action("undo") {
            @Override
            protected void redoInternal() {
                state.undo();
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
