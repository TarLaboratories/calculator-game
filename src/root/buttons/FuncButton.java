package root.buttons;

import org.python.core.PyComplex;
import org.python.util.PythonInterpreter;
import root.GameState;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class FuncButton implements CalcButton {
    protected final Consumer<GameState> func;
    protected final Consumer<GameState> undo;
    protected final String text;
    protected final String tooltip;

    public FuncButton(String text, Consumer<GameState> f, Consumer<GameState> undo, String tooltip) {
        this.text = text;
        this.tooltip = tooltip;
        func = f;
        this.undo = undo;
    }

    public FuncButton(String text, Consumer<GameState> f, String tooltip) {
        this.text = text;
        this.tooltip = tooltip;
        func = f;
        this.undo = null;
    }

    public FuncButton(String text, Consumer<GameState> f) {
        this.text = text;
        this.tooltip = null;
        func = f;
        this.undo = null;
    }

    public FuncButton() {
        text = null;
        tooltip = null;
        func = null;
        undo = null;
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (func == null) throw new UnsupportedOperationException("This button was initialised without arguments, and is valid only for constructing buttons");
        if (properties.price != null) {
            if (state.getMoney().__cmp__(properties.price) == -1) return;
            state.subMoney(properties.price);
            state.getCurrentButtons().add(this, properties.infinity ? PyComplex.Inf : new PyComplex(1));
            properties.sold = true;
            if (properties.rendered_button != null) state.getWindow().remove(properties.rendered_button);
            if (properties.rendered_price != null) state.getWindow().remove(properties.rendered_price);
            if (properties.rendered_count != null) state.getWindow().remove(properties.rendered_count);
            if (properties.infinity) state.getSellableButtons().remove(this);
            destroy(state, properties);
        } else if (properties.count != null) {
            if (!properties.infinity && properties.count.real == 0 && properties.count.imag == 0) return;
            if (!properties.infinity) properties.count.real--;
            render(state, properties);
            func.accept(state);
        }
    }

    @Override
    public CalcButton newButton(List<String> args) {
        try (PythonInterpreter py = new PythonInterpreter()) {
            Consumer<GameState> f = (state) -> {
                py.set("state", state);
                py.exec(args.get(1));
            };
            if (args.size() > 2) return new FuncButton(args.getFirst(), f, args.get(2));
            return new FuncButton(args.getFirst(), f);
        }
    }

    @Override
    public void render(GameState state, Properties properties) {
        switch (state.getRenderType()) {
            case CONSOLE -> {
                //TODO write render for console
            }
            case WINDOW -> {
                Window window = state.getWindow();
                if (properties.sold) return;
                if (properties.rendered_button != null) {
                    window.remove(properties.rendered_button);
                }
                Rectangle bounds = new Rectangle(properties.x, properties.y, getWidth(state, properties), getHeight(state, properties));
                if (properties.count != null) {
                    if (properties.rendered_count != null) {
                        window.remove(properties.rendered_count);
                    }
                    Label l = new Label(state.numToString(properties.count));
                    l.setBounds(bounds.x, bounds.y + 3*bounds.height/4, bounds.width, bounds.height/4);
                    l.setAlignment(Label.RIGHT);
                    properties.rendered_count = l;
                    window.add(l);
                }
                if (properties.price != null) {
                    if (properties.rendered_price != null) {
                        window.remove(properties.rendered_price);
                    }
                    Label l = new Label("$" + state.numToString(properties.price));
                    l.setBounds(bounds.x, bounds.y, bounds.width, bounds.height/4);
                    l.setAlignment(Label.LEFT);
                    properties.rendered_price = l;
                    window.add(l);
                }
                Button b = new Button(text);
                b.setBounds(bounds);
                b.addActionListener((_) -> onClick(state, properties));
                bounds.x += bounds.width + state.getButtonPadding()/2;
                bounds.width *= 3;
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (tooltip != null) state.setTooltip(tooltip, bounds);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        state.removeTooltip();
                    }
                });
                properties.rendered_button = b;
                window.add(b);
            }
        }
    }

    @Override
    public void destroy(GameState state, Properties properties) {
        if (properties.rendered_button != null) state.getWindow().remove(properties.rendered_button);
        if (properties.rendered_count != null) state.getWindow().remove(properties.rendered_count);
        if (properties.rendered_price != null) state.getWindow().remove(properties.rendered_price);
    }

    @Override
    public PyComplex getPrice(GameState state) {
        return new PyComplex(1);
    }

    @Override
    public int getWidth(GameState state, Properties properties) {
        return 50;
    }

    @Override
    public int getHeight(GameState state, Properties properties) {
        return 50;
    }

    @Override
    public String getString() {
        return text;
    }
}
