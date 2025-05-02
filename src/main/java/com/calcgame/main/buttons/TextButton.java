package com.calcgame.main.buttons;

import java.util.ArrayList;
import java.util.List;

public class TextButton extends FuncButton {
    /**
     * @param args arguments for creating the button in the following format:<br>
     *             {@code 0} - the text to be displayed<br>
     *             {@code 1} - the tooltip of the button to be displayed (optional)
     * @return a new {@code CalcButton} that appends the text displayed on it to the calculator screen when clicked
     */
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
