package com.calcgame.main.buttons;

import com.calcgame.main.Action;
import com.calcgame.main.GameState;

public class ShopRerollButton extends FuncButton {
    public ShopRerollButton() {
        super();
        text = "Reroll";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (state.getMoney().real < 1) return;
        state.doAction(new Action() {
            @Override
            public void redo() {
                state.addMoney(-1);
                state.refreshShop();
            }

            @Override
            public void undo() {
                state.addMoney(1);
            }

            @Override
            public boolean undoable() {
                return true;
            }
        });
    }

    @Override
    public boolean isVital() {
        return true;
    }
}
