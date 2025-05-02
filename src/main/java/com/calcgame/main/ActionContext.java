package com.calcgame.main;

import com.calcgame.main.buttons.Properties;
import org.apache.logging.log4j.Logger;

public record ActionContext(GameState state,
                            Properties properties,
                            ButtonCollection.Coordinate pos,
                            ButtonCollection buttons, String screen,
                            Logger logger) {
}
