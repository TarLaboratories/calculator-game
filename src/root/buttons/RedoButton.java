package root.buttons;

import root.GameState;

public class RedoButton extends TextButton {
    public RedoButton() {
        super();
        text = "REDO";
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        state.redo();
    }
}
