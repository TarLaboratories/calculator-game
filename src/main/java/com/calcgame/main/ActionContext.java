package com.calcgame.main;

import com.calcgame.main.buttons.Properties;
import org.apache.logging.log4j.Logger;

/**
 * The context of an {@link Action}
 * @param state the {@link GameState} that launched this action
 * @param properties the properties of the button that caused this action, or {@code null} if it wasn't a button
 * @param pos the coordinates of the button that caused this action, or {@code null} if it wasn't a button
 * @param buttons the collection, from which the action was caused, or {@code null} if it wasn't caused from a collection
 * @param screen the calculator screen at the moment of launching this action
 * @param logger the logger to use when logging action specific things
 */
public record ActionContext(GameState state,
                            Properties properties,
                            ButtonCollection.Coordinate pos,
                            ButtonCollection buttons, String screen,
                            Logger logger) {
}
