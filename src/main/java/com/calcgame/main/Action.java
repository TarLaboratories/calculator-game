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

    abstract public void redo();
    abstract public void undo();
    abstract public boolean undoable();
    public ActionContext getContext() {
        return context;
    }
    public void setContext(ActionContext context) {
        this.context = context;
    }
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
