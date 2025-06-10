package com.calcgame.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Represents an input action from the player, contains functions to execute and (optionally) undo it.<br>
 * It should also store the context of this action (i.e. which button was clicked, it's position and container)
 * @see Action#redo()
 * @see Action#undoInternal()
 * @see Action#undoable()
 */
public abstract class Action {
    /**
     * The logger for all action objects
     */
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * The context of this action
     */
    private @Nullable ActionContext context;
    /**
     * The name of the action, used for logging purposes
     */
    public String name;

    /**
     * Constructs a new action with the specified name, requires all abstract functions to be defined
     * @param name the name of this action
     */
    public Action(String name) {
        this.name = name;
    }

    /**
     * Constructs a new action with the specified name and (nullable) context, requires all abstract functions to be defined
     * @param name the name of this action
     * @param ctx the starting context of this action, can be changed using {@link Action#setContext(ActionContext)}
     */
    public Action(String name, @Nullable ActionContext ctx) {
        this.name = name;
        this.context = ctx;
    }

    /**
     * Creates a new action with the specified name, redo and undo functions.
     * Equivalent to {@link Action#forFuncs(String, Consumer, Consumer, ActionContext)} with a {@code null} context
     * @param name the name of the function
     * @param redo the redo function
     * @param undo the undo function
     * @return the created action
     */
    @ForMods
    public static Action forFuncs(String name, Consumer<ActionContext> redo, Consumer<ActionContext> undo) {
        return forFuncs(name, redo, undo, null);
    }

    /**
     * Creates a new action with the specified name, context, redo and undo functions
     * @param name the name of the action
     * @param redo the redo function
     * @param undo the undo function
     * @param ctx the action context
     * @return the created action
     */
    public static Action forFuncs(String name, Consumer<ActionContext> redo, Consumer<ActionContext> undo, ActionContext ctx) {
        if (undo == null || redo == null) LOGGER.warn("Action.forFuncs (name={}) invoked with at least one null function argument. Please use Action.forFunc or Action.forUndo instead.", name);
        return new Action(name, ctx) {
            @Override
            protected void redoInternal() {
                if (redo != null) redo.accept(getContext());
            }

            @Override
            protected void undoInternal() {
                if (undo != null) undo.accept(getContext());
            }

            @Override
            public boolean undoable() {
                return undo != null;
            }
        };
    }

    /**
     * Does (or redoes) the action.
     */
    public final void redo() {
        LOGGER.trace("Redoing {}", name);
        redoInternal();
    }

    /**
     * Should do (or redo) the action.
     */
    protected abstract void redoInternal();

    /**
     * Undoes the action.
     * It should not be invoked if {@link Action#undoable()} returns false.
     */
    public final void undo() {
        LOGGER.trace("Undoing {}", name);
        undoInternal();
    }

    /**
     * Should undo the action.
     * It should not be invoked if {@link Action#undoable()} returns false.
     */
    protected abstract void undoInternal();

    /**
     * It should be noted that any class that uses this class may disregard this
     * function's return value, and still invoke {@link Action#undoInternal()}, though it is
     * not recommended.
     * @return whether {@link Action#undoInternal()} is supposed to be invoked.
     */
    abstract public boolean undoable();

    /**
     * Returns this action's context
     * @return this action's context, may be {@code null}
     */
    public @Nullable ActionContext getContext() {
        return context;
    }

    /**
     * Sets this Action's context
     * @param context the context to set, may be {@code null}
     */
    public void setContext(@Nullable ActionContext context) {
        this.context = context;
    }

    /**
     * Returns an {@code Action}, which has the parent's {@code undo()} and {@code redo()},
     * followed by {@code other.undo()} and {@code other.redo()}. {@code undoable()} only returns {@code true} if
     * both the parent and {@code other} are {@code undoable}. Inherits context only from its parent ({@code this})
     * @param other the Action, methods from which to invoke after the parent's methods
     * @return a new {@code Action} object, or {@code this} if {@code super.andThen} was already invoked with the same parameter
     */
    public Action andThen(Action other) {
        if (other.getContext() == null) other.setContext(this.getContext());
        return new Action("%s/%s".formatted(name, other.name)) {
            @Override
            protected void redoInternal() {
                Action.this.redoInternal();
                other.redoInternal();
            }

            @Override
            protected void undoInternal() {
                Action.this.undoInternal();
                other.undoInternal();
            }

            @Override
            public ActionContext getContext() {
                if (Action.this.getContext() == null) return other.getContext();
                return Action.this.getContext();
            }

            @Override
            public boolean undoable() {
                return Action.this.undoable() && other.undoable();
            }

            @Override
            public Action andThen(Action _other) {
                if (other == _other || other == this) return this;
                return super.andThen(_other);
            }
        };
    }

    /**
     * Returns an {@code Action} object, which runs the specified function in {@code redo()},
     * and does nothing in {@code undo()}. The result Action is not {@code undoable}, and it's context is {@code null}
     * @param f the function to run in {@code redo()}
     * @param name the name of the action
     * @return a new Action object
     */
    public static Action forFunction(Runnable f, String name) {
        return new Action(name) {
            @Override
            protected void redoInternal() {
                f.run();
            }

            @Override
            protected void undoInternal() {}

            @Override
            public boolean undoable() {
                return false;
            }
        };
    }

    /**
     * Returns an {@code Action} object, which runs the specified function in {@code undo()},
     * and does nothing in {@code redo()}. The result Action is {@code undoable}, and it's context is {@code null}
     * @param f the function to run in {@code undo()}
     * @param name the name of the action
     * @return a new Action object
     */
    public static Action forUndo(Runnable f, String name) {
        return new Action(name) {
            @Override
            protected void redoInternal() {}

            @Override
            protected void undoInternal() {
                f.run();
            }

            @Override
            public boolean undoable() {
                return true;
            }
        };
    }

    public static Action blank(String name) {
        return new Action(name) {
            @Override
            protected void redoInternal() {}

            @Override
            protected void undoInternal() {}

            @Override
            public boolean undoable() {
                return true;
            }
        };
    }
}
