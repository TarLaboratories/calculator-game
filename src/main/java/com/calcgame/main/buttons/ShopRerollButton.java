package com.calcgame.main.buttons;

import com.calcgame.main.Action;
import com.calcgame.main.GameState;

/**
 * A system button used in the shop
 */
public class ShopRerollButton extends FuncButton {
    /**
     * Constructs a new reroll button.
     */
    public ShopRerollButton() {
        super();
        text = "Reroll";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (state.getMoney().real < 1) return;
        state.doAction(new Action("rerollOnClick") {
            @Override
            protected void redoInternal() {
                state.addMoney(-1);
                state.refreshShop();
            }

            @Override
            protected void undoInternal() {
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
