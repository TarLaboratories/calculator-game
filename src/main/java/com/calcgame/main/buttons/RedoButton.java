package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

public class RedoButton extends TextButton {
    public RedoButton() {
        super();
        text = "REDO";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.redo();
    }
}
