package root.buttons;

import org.python.core.PyComplex;
import root.GameState;

import java.awt.*;
import java.util.List;

public class TextButton implements CalcButton {
    protected final String text;

    public TextButton() {
        this.text = null;
    }

    public TextButton(String text) {
        this.text = text;
    }

    @Override
    public void onClick(GameState state, Properties properties) {
        if (text == null) throw new UnsupportedOperationException("This button was initialised without text, and can only be used for constructing other buttons");
        if (properties.price != null) {
            if (state.getMoney().__cmp__(properties.price) == -1 || state.getMoney().imag < properties.price.imag) return;
            state.subMoney(properties.price);
            state.buttons.add(this, properties.infinity ? PyComplex.Inf : new PyComplex(1));
            properties.sold = true;
            if (properties.rendered_button != null) state.window.remove(properties.rendered_button);
            if (properties.rendered_price != null) state.window.remove(properties.rendered_price);
            if (properties.rendered_count != null) state.window.remove(properties.rendered_count);
            if (properties.infinity) state.non_infinity_buttons.remove(this);
        } else if (properties.count != null) {
            if (!properties.infinity && properties.count.real == 0 && properties.count.imag == 0) return;
            if (!properties.infinity) properties.count.real--;
            state.setScreen(state.getScreen().concat(text));
        }
        render(state, properties);
    }

    @Override
    public void render(GameState state, Properties properties) {
        switch (state.renderType) {
            case CONSOLE -> {
                //TODO write render for console
            }
            case WINDOW -> {
                Window window = state.window;
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
                properties.rendered_button = b;
                window.add(b);
            }
        }
    }

    @Override
    public void destroy(GameState state, Properties properties) {
        if (properties.rendered_button != null) state.window.remove(properties.rendered_button);
        if (properties.rendered_count != null) state.window.remove(properties.rendered_count);
        if (properties.rendered_price != null) state.window.remove(properties.rendered_price);
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
    public CalcButton newButton(List<String> args) {
        return new TextButton(args.getFirst());
    }

    @Override
    public String getString() {
        return text;
    }
}
