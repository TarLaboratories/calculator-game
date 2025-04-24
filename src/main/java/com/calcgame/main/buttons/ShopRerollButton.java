package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

public class ShopRerollButton extends TextButton {
    public ShopRerollButton() {
        super();
        text = "Reroll";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (state.getMoney().real < 1) return;
        state.addMoney(-1);
        state.refreshShop();
    }
}
