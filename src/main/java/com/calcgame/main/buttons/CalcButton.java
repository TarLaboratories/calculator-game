package com.calcgame.main.buttons;

import org.python.core.PyComplex;
import com.calcgame.main.GameState;

import java.util.List;

public interface CalcButton {
    /**
     * Should be invoked when the button is clicked
     * @param state the current game state
     * @param properties the properties of the button clicked
     */
    void onClick(GameState state, Properties properties);

    /**
     * Should be invoked when the button is added to a {@link com.calcgame.main.ButtonCollection}
     * @param state the current game state
     * @param properties the properties of the button after it was added
     * @param count the amount added
     */
    void onAdd(GameState state, Properties properties, PyComplex count);

    /**
     * Should render the button
     * @param state the current game state
     * @param properties the properties of the button to render
     */
    void render(GameState state, Properties properties);

    /**
     * Should erase the button
     * @param state the current game state
     * @param properties the properties of the button to erase
     */
    void destroy(GameState state, Properties properties);

    /**
     * Should return the price of the button, as if it was sold in the shop
     * @param state the current game state
     * @return the price of the button
     */
    PyComplex getPrice(GameState state);

    /**
     * Should return the width of the button, as if it was rendered
     * @param state the current game state
     * @param properties the properties of the not yet rendered button
     * @return the width of the button
     */
    int getWidth(GameState state, Properties properties);

    /**
     * Should return the height of the button, as if it was rendered
     * @param state the current game state
     * @param properties the properties of the not yet rendered button
     * @return the height of the button
     */
    int getHeight(GameState state, Properties properties);

    /**
     * Should create a new button with the specified arguments.
     * Should be invoked only on a {@code CalcButton} object that was initialised without arguments
     * @param args the arguments for initialisation, usually from a mod config file
     * @return a new {@code CalcButton}
     */
    CalcButton newButton(List<String> args);

    /**
     * @return the id of the button, set when invoking {@link CalcButton#newButton(List)}
     */
    String getString();

    /**
     * @return if the button is a system button (e.g. '=', 'UNDO', 'REDO', 'Reroll', etc.)
     */
    boolean isVital();
}
