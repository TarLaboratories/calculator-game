package root;

import org.python.core.PyComplex;
import root.buttons.CalcButton;

import java.awt.*;
import java.util.ArrayList;

public class ButtonCollection extends ArrayList<ButtonCollection.Button> {
    protected int width, height, x, y, cur_height, max_width;
    protected GameState state;

    public ButtonCollection(Rectangle rectangle, GameState state) {
        super();
        this.width = rectangle.width;
        this.height = rectangle.height;
        this.x = rectangle.x;
        this.y = rectangle.y;
        this.state = state;
    }

    protected static class Button {
        CalcButton button;
        CalcButton.Properties properties;
    }

    public void render() {
        this.forEach((button) -> button.button.render(state, button.properties));
    }

    public void destroy() {
        this.forEach((button) -> button.button.destroy(state, button.properties));
        this.max_width = 0;
    }

    public void add(CalcButton button, CalcButton.Properties properties) {
        Button tmp_button = new Button();
        tmp_button.button = button;
        tmp_button.properties = properties;
        int last_x, last_y;
        if (this.size() > 0) {
            last_x = this.getLast().properties.x + this.getLast().button.getWidth(state, properties) + state.getButtonPadding();
            last_y = this.getLast().properties.y;
        } else {
            last_x = x + state.getButtonPadding();
            last_y = y + state.getButtonPadding();
        }
        if (last_x + button.getWidth(state, properties) + state.getButtonPadding() > x + width) {
            last_x = x + state.getButtonPadding();
            last_y += cur_height + state.getButtonPadding();
            cur_height = button.getHeight(state, properties);
        } else cur_height = Math.max(cur_height, button.getHeight(state, properties));
        tmp_button.properties.x = last_x;
        tmp_button.properties.y = last_y;
        max_width = Math.max(last_x + button.getWidth(state, properties) - x, max_width);
        button.render(state, properties);
        this.add(tmp_button);
    }

    public void add(CalcButton button) {
        add(button, new PyComplex(1));
    }

    public void add(CalcButton button, PyComplex count) {
        for (Button b : this) {
            if (b.button == button) {
                assert b.properties.count != null;
                b.properties.count = b.properties.count.__add__(count).__complex__();
                button.render(state, b.properties);
                return;
            }
        }
        add(button, CalcButton.Properties.count(count));
    }

    public int getWidth() {
        return max_width;
    }
}
