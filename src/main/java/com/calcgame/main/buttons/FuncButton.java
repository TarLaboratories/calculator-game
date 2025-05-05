package com.calcgame.main.buttons;

import com.calcgame.main.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.python.core.PyComplex;
import org.python.util.PythonInterpreter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * A button that has functions for all main events
 */
public class FuncButton implements CalcButton {
    /**
     * The text displayed on the button, and the second part of this button's id
     */
    protected String text;

    /**
     * The id of the mod that registered this button
     */
    protected String mod_id;

    /**
     * This button's tooltip, may be {@code null}
     */
    protected @Nullable String tooltip;

    /**
     * The action to execute when this button is clicked
     */
    protected Action onClick;

    /**
     * The action to execute when this button is added to any {@link ButtonCollection}
     */
    protected Action onAdd;

    /**
     * The logger for this object, may have a non-default name
     */
    private final Logger LOGGER;

    /**
     * Constructs a new FuncButton with the default logger name.
     * Leaves all other fields {@code null}, to be assigned later.
     * @see FuncButton#newButton(List, String)
     */
    public FuncButton() {
        LOGGER = LogManager.getLogger();
    }

    /**
     * Constructs a new FuncButton with the specified name for it's logger.
     * Leaves all other fields {@code null}, to be assigned later.
     * @param logger_name the name of the logger
     * @see FuncButton#newButton(List, String)
     */
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
        ActionContext ctx = new ActionContext(state, properties, properties.pos, properties.collection, state.getScreen(), LOGGER);
        Action action = new Action("onAdd") {
            @Override
            protected void redoInternal() {
                assert getContext() != null;
                render(state, getContext().properties());
            }

            @Override
            protected void undoInternal() {
                assert getContext() != null;
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
                assert getContext() != null;
                return !(getContext().properties().count == null && !getContext().properties().infinity);
            }

            @Override
            public ActionContext getContext() {
                return ctx;
            }
        };
        if (onAdd == null) {
            if (!isVital()) LOGGER.warn("No add handler defined for button '{}'", text);
            state.appendToLastAction(action).redo();
            return;
        }
        state.appendToLastAction(action.andThen(onAdd)).redo();
        state.getEvent(Events.ADD).emit(ctx, getString());
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (onClick == null) throw new UnsupportedOperationException("This button was initialised without arguments, and is valid only for constructing com.calcgame.main.buttons");
        if (properties.price != null) {
            if (state.getMoney().__cmp__(properties.price) == -1) return;
            ActionContext ctx = new ActionContext(state, properties, properties.pos, properties.collection, state.getScreen(), LOGGER);
            state.appendToLastAction(new Action("buy") {
                @Override
                protected void redoInternal() {
                    state.subMoney(properties.price);
                    state.getCurrentButtons().add(FuncButton.this, properties.infinity ? PyComplex.Inf : new PyComplex(1));
                    properties.sold = true;
                    if (properties.rendered_button != null) state.getWindow().remove(properties.rendered_button);
                    if (properties.rendered_price != null) state.getWindow().remove(properties.rendered_price);
                    if (properties.rendered_count != null) state.getWindow().remove(properties.rendered_count);
                    if (properties.infinity) state.getSellableButtons().remove(FuncButton.this);
                    destroy(state, properties);
                }

                @Override
                protected void undoInternal() {
                    state.addMoney(properties.price);
                    properties.sold = false;
                    if (properties.infinity) state.getSellableButtons().add(FuncButton.this);
                    render(state, properties);
                }

                @Override
                public boolean undoable() {
                    return true;
                }
            }).redo();
            state.getEvent(Events.BUY).emit(ctx, getString());
        } else if (properties.count != null) {
            if (!properties.infinity && properties.count.real == 0 && properties.count.imag == 0) return;
            ActionContext ctx = new ActionContext(state, properties, properties.pos, properties.collection, state.getScreen(), LOGGER);
            state.doAction(new Action("onClick") {
                @Override
                protected void redoInternal() {
                    assert getContext() != null;
                    if (!getContext().properties().infinity) getContext().properties().decreaseCount();
                    render(state, getContext().properties());
                }

                @Override
                protected void undoInternal() {
                    assert getContext() != null;
                    if (!getContext().properties().infinity) getContext().properties().increaseCount();
                    render(state, getContext().properties());
                }

                @Override
                public boolean undoable() {
                    return true;
                }

                @Override
                public ActionContext getContext() {
                    return ctx;
                }
            }.andThen(onClick));
            state.getEvent(Events.CLICK).emit(ctx, getString());
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
    public CalcButton newButton(List<String> args, String mod_id) {
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
            out.mod_id = mod_id;
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

    /**
     * Returns the id of this button in the format 'modid:text'
     * @return the id of this button
     */
    @Override
    public String getString() {
        return "%s:%s".formatted(mod_id, text);
    }

    /**
     * Equivalent to {@link FuncButton#getString()}
     * @return the id of this button
     */
    @Override
    public String toString() {
        return getString();
    }
}
