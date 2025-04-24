package com.calcgame.main;

import org.python.core.PyComplex;
import com.calcgame.main.buttons.CalcButton;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ButtonCollection extends ArrayList<ButtonCollection.Button> {
    protected int width, height, x, y, cur_height, max_width;
    protected HashMap<Coordinate, Button> buttons_by_coords = new HashMap<>();
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

    public static class Coordinate {
        int x, y;

        public Coordinate() {
            this.x = 0;
            this.y = 0;
        }

        public Coordinate(Coordinate c) {
            this.x = c.x;
            this.y = c.y;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Coordinate that = (Coordinate) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        public Coordinate left() {
            Coordinate out = new Coordinate(this);
            out.x--;
            return out;
        }

        public Coordinate right() {
            Coordinate out = new Coordinate(this);
            out.x++;
            return out;
        }

        public Coordinate up() {
            Coordinate out = new Coordinate(this);
            out.y--;
            return out;
        }

        public Coordinate down() {
            Coordinate out = new Coordinate(this);
            out.y++;
            return out;
        }
    }

    public void render() {
        this.forEach((button) -> button.button.render(state, button.properties));
    }

    public void render(Coordinate coords) {
        if (!buttons_by_coords.containsKey(coords)) throw new UnsupportedOperationException("Cannot render a button at a non-initialized position");
        buttons_by_coords.get(coords).button.render(state, buttons_by_coords.get(coords).properties);
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
        Coordinate cur = new Coordinate();
        Coordinate prev = this.size() > 0 ? getCoords(this.getLast().button) : null;
        if (this.size() > 0) {
            last_x = this.getLast().properties.x + this.getLast().button.getWidth(state, properties) + state.getButtonPadding();
            last_y = this.getLast().properties.y;
            cur.x = prev.x + 1;
            cur.y = prev.y;
        } else {
            last_x = x + state.getButtonPadding();
            last_y = y + state.getButtonPadding();
            cur.x = 0;
            cur.y = 0;
        }
        if (last_x + button.getWidth(state, properties) + state.getButtonPadding() > x + width) {
            last_x = x + state.getButtonPadding();
            last_y += cur_height + state.getButtonPadding();
            cur.x = 0;
            cur.y++;
            cur_height = button.getHeight(state, properties);
        } else cur_height = Math.max(cur_height, button.getHeight(state, properties));
        tmp_button.properties.x = last_x;
        tmp_button.properties.y = last_y;
        tmp_button.properties.collection = this;
        tmp_button.properties.pos = cur;
        this.buttons_by_coords.put(cur, tmp_button);
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

    public void addCount(CalcButton button, PyComplex count) {
        this.add(button, count);
    }

    public int getWidth() {
        return max_width;
    }

    public Coordinate getCoords(CalcButton button) {
        for (Coordinate i : buttons_by_coords.keySet()) {
            if (buttons_by_coords.get(i).button == button) return i;
        }
        throw new IndexOutOfBoundsException("The requested button doesn't exist in this collection");
    }

    public CalcButton getButton(Coordinate coords) {
        if (buttons_by_coords.get(coords) == null) return null;
        return buttons_by_coords.get(coords).button;
    }

    public void setButton(Coordinate coords, CalcButton button) {
        if (getButton(coords) == null) throw new UnsupportedOperationException("Cannot set a button at a non-initialized position");
        buttons_by_coords.get(coords).button = button;
        button.render(state, buttons_by_coords.get(coords).properties);
    }

    public List<Coordinate> getNeighbourCoords(CalcButton button) {
        Coordinate coords = getCoords(button);
        ArrayList<Coordinate> out = new ArrayList<>();
        if (getButton(coords.left()) != null) out.add(coords.left());
        if (getButton(coords.right()) != null) out.add(coords.right());
        if (getButton(coords.up()) != null) out.add(coords.up());
        if (getButton(coords.down()) != null) out.add(coords.down());
        return out;
    }

    public List<CalcButton> getButtons() {
        ArrayList<CalcButton> out = new ArrayList<>();
        this.forEach((b) -> out.add(b.button));
        return out;
    }
}
