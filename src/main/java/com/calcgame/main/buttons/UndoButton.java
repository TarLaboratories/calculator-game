package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

public class UndoButton extends TextButton {
    public UndoButton() {
        super();
        text = "UNDO";
        tooltip = "Undoes the last click.";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.undo();
    }
}
