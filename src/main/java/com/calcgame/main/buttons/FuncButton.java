package com.calcgame.main.buttons;

import com.calcgame.main.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.core.PyComplex;
import org.python.util.PythonInterpreter;
import com.calcgame.main.Action;
import com.calcgame.main.ActionContext;
import com.calcgame.main.GameState;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class FuncButton implements CalcButton {
    protected String text;
    protected String tooltip;
    protected Action onClick, onAdd;
    private final Logger LOGGER;

    public FuncButton() {
        LOGGER = LogManager.getLogger();
    }

    public FuncButton(String logger_name) {
        LOGGER = LogManager.getLogger("FuncButton/%s".formatted(logger_name));
    }

    @Override
    public boolean isVital() {
        return false;
    }

    @Override
    public void onAdd(GameState state, Properties properties, PyComplex old_count) {
        LOGGER.trace("Adding button '{}'", text);
        Action action = new Action() {
            @Override
            public void redo() {
                render(state, getContext().properties());
            }

            @Override
            public void undo() {
                LOGGER.trace("Undoing adding a button, count {} -> {}", getContext().properties().count, old_count);
                if (getContext().properties().count == null && !getContext().properties().infinity) {
                    LOGGER.debug("Undoing adding a button to the shop");
                    destroy(state, getContext().properties());
                } else if (old_count.__nonzero__()) {
                    getContext().properties().count = old_count;
                    render(state, getContext().properties());
                } else {
                    getContext().properties().count = old_count;
                    destroy(state, getContext().properties());
                }
            }

            @Override
            public boolean undoable() {
                return !(getContext().properties().count == null && !getContext().properties().infinity);
            }

            @Override
            public ActionContext getContext() {
                return new ActionContext(state, properties, properties.pos, properties.collection, state.getScreen(), LOGGER);
            }
        };
        if (onAdd == null) {
            if (!isVital()) LOGGER.warn("No add handler defined for button '{}'", text);
            state.appendToLastAction(action).redo();
            return;
        }
        state.appendToLastAction(action.andThen(onAdd)).redo();
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (onClick == null) throw new UnsupportedOperationException("This button was initialised without arguments, and is valid only for constructing com.calcgame.main.buttons");
        if (properties.price != null) {
            if (state.getMoney().__cmp__(properties.price) == -1) return;
            state.subMoney(properties.price);
            state.getCurrentButtons().add(this, properties.infinity ? PyComplex.Inf : new PyComplex(1));
            state.appendToLastAction(Action.forUndo(() -> {
                state.addMoney(properties.price);
                properties.sold = false;
                if (properties.infinity) state.getSellableButtons().add(this);
                render(state, properties);
            }));
            properties.sold = true;
            if (properties.rendered_button != null) state.getWindow().remove(properties.rendered_button);
            if (properties.rendered_price != null) state.getWindow().remove(properties.rendered_price);
            if (properties.rendered_count != null) state.getWindow().remove(properties.rendered_count);
            if (properties.infinity) state.getSellableButtons().remove(this);
            destroy(state, properties);
        } else if (properties.count != null) {
            if (!properties.infinity && properties.count.real == 0 && properties.count.imag == 0) return;
            state.doAction(new Action() {
                @Override
                public void redo() {
                    if (!getContext().properties().infinity) getContext().properties().decreaseCount();
                    render(state, getContext().properties());
                }

                @Override
                public void undo() {
                    if (!getContext().properties().infinity) getContext().properties().increaseCount();
                    render(state, getContext().properties());
                }

                @Override
                public boolean undoable() {
                    return true;
                }

                @Override
                public ActionContext getContext() {
                    return new ActionContext(state, properties, properties.pos, properties.collection, state.getScreen(), LOGGER);
                }
            }.andThen(onClick));
        }
    }

    /**
     * Creates a new button that has text and an action associated with it.
     * @param args arguments for creating the button in the following format:<br>
     *             {@code 0} - the text to be displayed<br>
     *             {@code 1} - a string of python code representing what to do when clicked<br>
     *             {@code 2} - the tooltip of the button to be displayed (optional)<br>
     *             {@code 3} - a string of python code representing the undo function (optional)
     * @return a button object that can be rendered and pressed
     */
    @Override
    public CalcButton newButton(List<String> args) {
        FuncButton out = new FuncButton(args.getFirst());
        try (PythonInterpreter py = new PythonInterpreter()) {
            Consumer<ActionContext> f = (ctx) -> {
                py.setOut(Utils.writerFromLogger(out.LOGGER));
                py.set("state", ctx.state());
                py.set("ctx", ctx);
                py.exec(args.get(1));
            };
            Consumer<ActionContext> u = (ctx) -> {
                py.setOut(Utils.writerFromLogger(out.LOGGER));
                py.set("state", ctx.state());
                py.set("ctx", ctx);
                py.exec(args.get(3));
            };
            out.text = args.getFirst();
            if (args.size() > 2) out.tooltip = args.get(2);
            out.onClick = Utils.actionFromPy(py, args.get(1), "on_click", out.LOGGER, f, u);
            out.onAdd = Utils.actionFromPy(py, args.get(1), "on_add", out.LOGGER, (_) -> {}, (_) -> {});
        } catch (RuntimeException e) {
            LOGGER.error("Failed to create new function button", e);
        }
        return out;
    }

    @Override
    public void render(GameState state, Properties properties) {
        switch (state.getRenderType()) {
            case CONSOLE -> {
                //TODO write render for console
            }
            case WINDOW -> {
                Window window = state.getWindow();
                if (properties.sold) return;
                if (properties.rendered_button != null) {
                    window.remove(properties.rendered_button);
                }
                Rectangle bounds = new Rectangle(properties.x, properties.y, getWidth(state, properties), getHeight(state, properties));
                if (properties.count != null) {
                    if (properties.rendered_count != null) {
                        window.remove(properties.rendered_count);
                    }
                    Label l = new Label(state.numToString(properties.count));
                    l.setBounds(bounds.x, bounds.y + 3*bounds.height/4, bounds.width, bounds.height/4);
                    l.setAlignment(Label.RIGHT);
                    properties.rendered_count = l;
                    window.add(l);
                }
                if (properties.price != null) {
                    if (properties.rendered_price != null) {
                        window.remove(properties.rendered_price);
                    }
                    Label l = new Label("$" + state.numToString(properties.price));
                    l.setBounds(bounds.x, bounds.y, bounds.width, bounds.height/4);
                    l.setAlignment(Label.LEFT);
                    properties.rendered_price = l;
                    window.add(l);
                }
                Button b = new Button(text);
                b.setBounds(bounds);
                b.addActionListener((_) -> onClick(state, properties));
                bounds.x += bounds.width + state.getButtonPadding()/2;
                bounds.width *= 3;
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (tooltip != null) state.setTooltip(tooltip, bounds);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        state.removeTooltip();
                    }
                });
                properties.rendered_button = b;
                window.add(b);
            }
        }
    }

    @Override
    public void destroy(GameState state, Properties properties) {
        if (properties.rendered_button != null) state.getWindow().remove(properties.rendered_button);
        if (properties.rendered_count != null) state.getWindow().remove(properties.rendered_count);
        if (properties.rendered_price != null) state.getWindow().remove(properties.rendered_price);
    }

    @Override
    public PyComplex getPrice(GameState state) {
        return new PyComplex(1);
    }

    @Override
    public int getWidth(GameState state, Properties properties) {
        return 50;
    }

    @Override
    public int getHeight(GameState state, Properties properties) {
        return 50;
    }

    @Override
    public String getString() {
        return text;
    }
}
