package com.calcgame.main.buttons;

import com.calcgame.main.GameState;

public class NextRoundButton extends TextButton {
    public NextRoundButton() {
        super();
        text = "Next Round";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.nextRound();
    }

    @Override
    public void render(GameState state, Properties properties) {
        Properties p = Properties.copy(properties);
        p.price = p.count = null;
        super.render(state, p);
        properties.rendered_button = p.rendered_button;
    }

    @Override
    public int getWidth(GameState state, Properties properties) {
        return 100 + state.getButtonPadding();
    }
}
