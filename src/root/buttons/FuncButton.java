package root.buttons;

import org.python.core.PyComplex;
import org.python.util.PythonInterpreter;
import root.GameState;

import java.util.List;
import java.util.function.Consumer;

public class FuncButton extends TextButton {
    protected final Consumer<GameState> func;

    public FuncButton(String text, Consumer<GameState> f) {
        super(text);
        func = f;
    }

    public FuncButton() {
        super();
        func = null;
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (func == null) throw new UnsupportedOperationException("This button was initialised without arguments, and is valid only for constructing buttons");
        if (properties.price != null) {
            if (state.getMoney().__cmp__(properties.price) == -1) return;
            state.subMoney(properties.price);
            state.buttons.add(this, properties.infinity ? PyComplex.Inf : new PyComplex(1));
            properties.sold = true;
            if (properties.rendered_button != null) state.window.remove(properties.rendered_button);
            if (properties.rendered_price != null) state.window.remove(properties.rendered_price);
            if (properties.rendered_count != null) state.window.remove(properties.rendered_count);
            if (properties.infinity) state.non_infinity_buttons.remove(this);
            destroy(state, properties);
        } else if (properties.count != null) {
            if (!properties.infinity && properties.count.real == 0 && properties.count.imag == 0) return;
            if (!properties.infinity) properties.count.real--;
            func.accept(state);
        }
        render(state, properties);
    }

    @Override
    public CalcButton newButton(List<String> args) {
        try (PythonInterpreter py = new PythonInterpreter()) {
            return new FuncButton(args.getFirst(), (state) -> {
                py.set("state", state);
                py.exec(args.get(1));
            });
        }
    }
}
