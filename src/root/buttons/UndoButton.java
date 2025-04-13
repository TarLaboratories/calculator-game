package root.buttons;

import root.GameState;

public class UndoButton extends TextButton {
    public UndoButton() {
        super("UNDO", "Undoes the last click.");
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.undo();
    }
}
