package com.calcgame.main;

/**
 * Represents an input action from the player, contains functions to execute and (optionally) undo it.<br>
 * It should also store the context of this action (i.e. which button was clicked, it's position and container)
 * @see Action#redo()
 * @see Action#undo()
 * @see Action#undoable()
 */
public abstract class Action {
    private ActionContext context;

    /**
     * Should do (or redo) the action.
     */
    abstract public void redo();

    /**
     * Should undo the action.
     * It should not be invoked if {@link Action#undoable()} returns false.
     */
    abstract public void undo();

    /**
     * It should be noted that any class that uses this class may disregard this
     * function's return value, and still invoke {@link Action#undo()}, though it is
     * not recommended.
     * @return whether {@link Action#undo()} is supposed to be invoked.
     */
    abstract public boolean undoable();

    /**
     * @return this Action's context, may be {@code null}
     */
    public ActionContext getContext() {
        return context;
    }

    /**
     * Sets this Action's context
     * @param context the context to set, may be {@code null}
     */
    public void setContext(ActionContext context) {
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
        return new Action() {
            @Override
            public void redo() {
                Action.this.redo();
                other.redo();
            }

            @Override
            public void undo() {
                Action.this.undo();
                other.undo();
            }

            @Override
            public ActionContext getContext() {
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
     * @return a new Action object
     */
    public static Action forFunction(Runnable f) {
        return new Action() {
            @Override
            public void redo() {
                f.run();
            }

            @Override
            public void undo() {}

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
     * @return a new Action object
     */
    public static Action forUndo(Runnable f) {
        return new Action() {
            @Override
            public void redo() {}

            @Override
            public void undo() {
                f.run();
            }

            @Override
            public boolean undoable() {
                return true;
            }
        };
    }
}
