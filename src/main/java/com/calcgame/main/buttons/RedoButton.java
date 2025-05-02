package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

public class RedoButton extends FuncButton {
    public RedoButton() {
        super("REDO");
        text = "REDO";
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
