package com.calcgame.main;

import com.calcgame.main.buttons.CalcButton;

public record ActionContext(GameState state,
                            CalcButton.Properties properties,
                            ButtonCollection.Coordinate pos,
                            ButtonCollection collection) {
}
