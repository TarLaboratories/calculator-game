package com.calcgame.main.buttons;

import com.calcgame.main.Action;
import com.calcgame.main.GameState;

/**
 * A system button used in the shop
 */
public class NextRoundButton extends FuncButton {
    /**
     * Constructs the next round button
     */
    public NextRoundButton() {
        super("Next Round");
        text = "Next Round";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.doAction(Action.forFunction(state::nextRound, "nextRoundOnClick"));
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

    @Override
    public boolean isVital() {
        return true;
    }
}
