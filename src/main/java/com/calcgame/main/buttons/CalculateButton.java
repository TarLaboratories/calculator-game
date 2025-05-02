package com.calcgame.main.buttons;

import com.calcgame.main.Action;
import com.calcgame.main.Formula;
import com.calcgame.main.Operation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.PyComplex;
import com.calcgame.main.GameState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CalculateButton extends FuncButton {
    private static final Logger LOGGER = LogManager.getLogger();

    public static Map<String, Operation> ops = new HashMap<>();
    public static Map<Operation, String> rev_ops = new HashMap<>();
    public static Map<String, Function<PyComplex, PyComplex>> funcs = new HashMap<>();
    public static Map<Function<PyComplex, PyComplex>, String> rev_funcs = new HashMap<>();

    public CalculateButton() {
        super("=");
        text = "=";
        tooltip = "Calculates the result of the expression currently on screen. If it is invalid, sets screen to 0.";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        String s = state.getScreen();
        try {
            Formula f = Formula.fromString(s);
            LOGGER.info("Parsed formula: {}", f.toString(state));
            PyComplex res = f.calc();
            String res_str = state.numToString(res);
            int op_count = f.countOperations();
            state.doAction(new Action() {
                @Override
                public void redo() {
                    state.addMoney(op_count);
                    state.setScreen(res_str);
                }

                @Override
                public void undo() {
                    state.addMoney(-op_count);
                    state.setScreen(s);
                }

                @Override
                public boolean undoable() {
                    return true;
                }
            });

        } catch (Formula.InvalidFormulaException e) {
            state.setScreen("0");
        }
    }

    public static void addOperation(String s, int priority, BiFunction<PyComplex, PyComplex, PyComplex> f) {
        Operation op = new Operation(priority, f);
        ops.put(s, op);
        rev_ops.put(op, s);
    }

    public static void addFunction(String s, Function<PyComplex, PyComplex> f) {
        funcs.put(s, f);
        rev_funcs.put(f, s);
    }

    @Override
    public boolean isVital() {
        return true;
    }
}
