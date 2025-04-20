package root.buttons;

import java.util.List;

public class TextButton extends FuncButton {
    public TextButton(String text, String tooltip) {
        super(text, (state) -> state.setScreen(state.getScreen().concat(text)), tooltip);
    }

    public TextButton(String text) {
        super(text, (state) -> state.setScreen(state.getScreen().concat(text)));
    }

    public TextButton() {
        super();
    }

    @Override
    public CalcButton newButton(List<String> args) {
        if (args.size() > 1) return new TextButton(args.getFirst(), args.get(1));
        return new TextButton(args.getFirst());
    }
}
