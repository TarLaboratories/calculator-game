package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

public class UndoButton extends FuncButton {
    public UndoButton() {
        super("UNDO");
        text = "UNDO";
        tooltip = "Undoes the last click.";
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
