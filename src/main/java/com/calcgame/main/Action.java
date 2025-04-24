package com.calcgame.main;

/**
 * Represents an input action from the player, contains functions to execute and (optionally) undo it.<br>
 * It should also store the context of this action (i.e. which button was clicked, it's position and container)
 * @see Action#redo(GameState)
 * @see Action#undo(GameState)
 * @see Action#undoable(GameState)
 */
public interface Action {
    void redo(GameState state);
    void undo(GameState state);
    boolean undoable(GameState state);
    default ActionContext getContext() {
        return null;
    }
    default Action andThen(Action other) {
        return new Action() {
            @Override
            public void redo(GameState state) {
                Action.this.redo(state);
                other.redo(state);
            }

            @Override
            public void undo(GameState state) {
                Action.this.undo(state);
                other.undo(state);
            }

            @Override
            public ActionContext getContext() {
                return Action.this.getContext();
            }

            @Override
            public boolean undoable(GameState state) {
                return Action.this.undoable(state) && other.undoable(state);
            }
        };
    }
}
