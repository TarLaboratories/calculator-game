package root;

public interface Action {
    void redo(GameState state);
    void undo(GameState state);
}
