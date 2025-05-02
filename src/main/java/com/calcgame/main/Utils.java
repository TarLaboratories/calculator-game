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

public class Utils {
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

    public static boolean pyDefinesFunction(String script, String func_name) {
        return script.contains("def %s(".formatted(func_name));
    }

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

    public static Action actionFromPy(PythonInterpreter py, String script, String name, Logger out) {
        return actionFromPy(py, script, name, out, null, null);
    }

    public static JSONObject toJSON(PyComplex x) {
        if (x == null) return new JSONObject(Map.of("is_null", true));
        if (x == PyComplex.Inf) return new JSONObject(Map.of("inf", true));
        return new JSONObject(Map.of("real", x.real, "imag", x.imag));
    }

    public static PyComplex fromJSON(JSONObject o) {
        if (o.keySet().contains("is_null")) return null;
        if (o.keySet().contains("inf")) return PyComplex.Inf;
        return new PyComplex(o.getDouble("real"), o.getDouble("imag"));
    }
}
