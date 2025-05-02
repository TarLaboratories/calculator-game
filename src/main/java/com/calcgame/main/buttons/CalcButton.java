package com.calcgame.main.buttons;

import org.python.core.PyComplex;
import com.calcgame.main.GameState;

import java.util.List;

public interface CalcButton {
    void onClick(GameState state, Properties properties);
    void onAdd(GameState state, Properties properties, PyComplex count);
    void render(GameState state, Properties properties);
    void destroy(GameState state, Properties properties);
    PyComplex getPrice(GameState state);
    int getWidth(GameState state, Properties properties);
    int getHeight(GameState state, Properties properties);
    CalcButton newButton(List<String> args);
    String getString();
    boolean isVital();
}
