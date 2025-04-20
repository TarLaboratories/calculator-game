package root;

public interface Action {
    void redo(GameState state);
    void undo(GameState state);
    boolean undoable(GameState state);
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
            public boolean undoable(GameState state) {
                return Action.this.undoable(state) && other.undoable(state);
            }
        };
    }
}
