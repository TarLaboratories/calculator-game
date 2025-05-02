package com.calcgame.main;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.python.core.PyComplex;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.io.Writer;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A utility class that should never be instantiated.
 * All methods are static.
 */
public class Utils {
    private Utils() {}
    /**
     * @return a new Writer object that is configured to write everything using {@link Logger#info(Object)}
     */
    public static Writer writerFromLogger(Logger logger) {
        return new Writer() {
            StringBuilder s = new StringBuilder();
            @Override
            public void write(char @NotNull [] cbuf, int off, int len) {
                for (int i = off; i < off + len; i++) s.append(cbuf[i]);
            }

            @Override
            public void flush() {
                if (s.isEmpty()) return;
                if (s.charAt(s.length() - 1) == '\n') s.replace(s.length() - 1, s.length(), "");
                logger.info(s);
                s = new StringBuilder();
            }

            @Override
            public void close() {}
        };
    }

    /**
     * @return whether the provided Python script defines a function with the specified name
     */
    public static boolean pyDefinesFunction(String script, String func_name) {
        return script.contains("def %s(".formatted(func_name));
    }

    /**
     * Generates a function from the specified python script and function name.
     * @param py the PythonInterpreter to use
     * @param script the Python script, from which to get the function
     * @param func_name the name of the function
     * @param out the logger, to which the function will output
     * @return a java function, that is equivalent to the specified function in python, or null if the provided script does not define it
     * @param <T> the type of the function's first and only argument
     */
    public static <T> Consumer<T> funcFromPy(PythonInterpreter py, String script, String func_name, Logger out) {
        if (!pyDefinesFunction(script, func_name)) {
            return null;
        }
        py.setOut(writerFromLogger(out));
        py.exec(script);
        PyObject func = py.get(func_name);
        return (x) -> {
            py.setOut(writerFromLogger(out));
            py.set("__arg", x);
            func.__call__(py.get("__arg"));
        };
    }

    /**
     * Generates an action object from the specified python script and name.
     * @param py the python interpreter to use
     * @param script the python script from which to get the functions
     * @param name the name of the function for {@code redo}
     * @param out the logger to use for the python functions to output
     * @param fallback_redo the redo function to use if the python function was not defined
     * @param fallback_undo the undo function to use if the python function was not defined
     * @return an action object, that has a python function with the name {@code name} as {@link Action#redo()}, and a function with name {@code name + "_rev"} as {@link Action#undo()}
     */
    public static Action actionFromPy(PythonInterpreter py, String script, String name, Logger out, Consumer<ActionContext> fallback_redo, Consumer<ActionContext> fallback_undo) {
        Consumer<ActionContext> redo = funcFromPy(py, script, name, out);
        Consumer<ActionContext> undo = funcFromPy(py, script, name + "_rev", out);
        return new Action() {
            @Override
            public void redo() {
                out.trace("Redoing function with name '{}'", name);
                if (redo != null) redo.accept(getContext());
                else if (fallback_redo != null) fallback_redo.accept(getContext());
                else out.debug("Neither redo or fallback_redo are callable in action '{}'", name);
            }

            @Override
            public void undo() {
                out.trace("Undoing function with name '{}'", name);
                if (undo != null) undo.accept(getContext());
                else if (fallback_undo != null) fallback_undo.accept(getContext());
                else out.debug("Neither undo or fallback_undo are callable in action '{}'", name);
            }

            @Override
            public boolean undoable() {
                return undo != null || fallback_undo != null;
            }
        };
    }

    /**
     * Equivalent to using {@link Utils#actionFromPy(PythonInterpreter, String, String, Logger, Consumer, Consumer)} with the last two arguments as {@code null}
     */
    public static Action actionFromPy(PythonInterpreter py, String script, String name, Logger out) {
        return actionFromPy(py, script, name, out, null, null);
    }

    /**
     * Serialises a PyComplex number
     * @see Utils#fromJSON(JSONObject)
     */
    public static JSONObject toJSON(PyComplex x) {
        if (x == null) return new JSONObject(Map.of("is_null", true));
        if (x == PyComplex.Inf) return new JSONObject(Map.of("inf", true));
        return new JSONObject(Map.of("real", x.real, "imag", x.imag));
    }

    /**
     * Deserializes a PyComplex number
     * @see Utils#toJSON(PyComplex)
     */
    public static PyComplex fromJSON(JSONObject o) {
        if (o.keySet().contains("is_null")) return null;
        if (o.keySet().contains("inf")) return PyComplex.Inf;
        return new PyComplex(o.getDouble("real"), o.getDouble("imag"));
    }
}
