package root.buttons;

import java.util.ArrayList;
import java.util.List;

public class TextButton extends FuncButton {
    @Override
    public CalcButton newButton(List<String> args) {
        List<String> funcArgs = new ArrayList<>();
        funcArgs.add(args.getFirst());
        funcArgs.add("state.setScreen(state.getScreen()+'%s')".formatted(args.getFirst()));
        if (args.size() > 1) funcArgs.add(args.get(1));
        else funcArgs.add(null);
        funcArgs.add("state.setScreen(state.getScreen()[:-%d])".formatted(args.getFirst().length()));
        return super.newButton(funcArgs);
    }
}
