package com.calcgame.main;

import com.calcgame.main.buttons.Properties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.python.core.PyComplex;
import com.calcgame.main.buttons.CalcButton;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of rendered buttons, that have count, price and other properties
 */
public class ButtonCollection extends ArrayList<ButtonCollection.Button> {
    protected int width, height, x, y, cur_height, max_width;
    protected HashMap<Coordinate, Button> buttons_by_coords = new HashMap<>();
    protected GameState state;

    private ButtonCollection() {
        super();
    }

    /**
     * Constructs a new collection
     * @param rectangle the bounds of the rendered collection
     * @param state the current game state
     */
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
        Properties properties;
    }

    /**
     * Represents the coordinates of a rendered button, relative to the first button in a {@code ButtonCollection}
     */
    public static class Coordinate {
        public int x, y;

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

    /**
     * (Re)renders all buttons in this collection.
     */
    public void render() {
        this.forEach((button) -> button.button.render(state, button.properties));
    }

    /**
     * (Re)renders only the button at the specified coordinates.
     */
    public void render(Coordinate coords) {
        if (!buttons_by_coords.containsKey(coords)) throw new UnsupportedOperationException("Cannot render a button at a non-initialized position");
        buttons_by_coords.get(coords).button.render(state, buttons_by_coords.get(coords).properties);
    }

    /**
     * Erases all buttons in this collection, does NOT actually destroy or remove anything.
     */
    public void destroy() {
        this.forEach((button) -> button.button.destroy(state, button.properties));
        this.max_width = 0;
    }

    /**
     * Adds a button to this collection, regardless of whether it already exists in it or not
     */
    public void add(CalcButton button, Properties properties) {
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
        button.onAdd(state, properties, new PyComplex(0));
        this.add(tmp_button);
    }

    /**
     * Adds 1 to the count of the specified button if it already exists, or adds it if not.
     */
    public void add(CalcButton button) {
        add(button, new PyComplex(1));
    }

    /**
     * Adds {@code count} to the count of the specified button if it already exists, or adds it if not.
     */
    public void add(CalcButton button, PyComplex count) {
        for (Button b : this) {
            if (b.button == button) {
                assert b.properties.count != null;
                PyComplex old_count = new PyComplex(b.properties.count.real, b.properties.count.imag);
                b.properties.count = b.properties.count.__add__(count).__complex__();
                button.render(state, b.properties);
                button.onAdd(state, b.properties, old_count);
                return;
            }
        }
        add(button, Properties.count(count));
    }

    /**
     * Equivalent to {@link ButtonCollection#add(CalcButton, PyComplex)}. It is here mainly to avoid conflicts with the inherited {@code add} method and for mods
     */
    public void addCount(CalcButton button, PyComplex count) {
        this.add(button, count);
    }

    /**
     * @return the width, supplied in the constructor
     */
    public int getWidth() {
        return max_width;
    }

    /**
     * @return the coordinates of any button that is equal to the specified button.
     */
    public Coordinate getCoords(CalcButton button) {
        for (Coordinate i : buttons_by_coords.keySet()) {
            if (buttons_by_coords.get(i).button == button) return i;
        }
        throw new IndexOutOfBoundsException("The requested button doesn't exist in this collection");
    }

    /**
     * @return the button at the specified coordinates, or {@code null} if there is nothing at these coordinates
     */
    public CalcButton getButton(Coordinate coords) {
        if (buttons_by_coords.get(coords) == null) return null;
        return buttons_by_coords.get(coords).button;
    }

    /**
     * Sets the button at the specified coordinates to the provided one, and keeps the properties of the old button.
     */
    public void setButton(Coordinate coords, CalcButton button) {
        if (getButton(coords) == null) throw new UnsupportedOperationException("Cannot set a button at a non-initialized position");
        buttons_by_coords.get(coords).button = button;
        button.render(state, buttons_by_coords.get(coords).properties);
    }

    /**
     * @return all neighbouring coordinates, where a button exists.
     */
    public List<Coordinate> getNeighbourCoords(Coordinate coords) {
        ArrayList<Coordinate> out = new ArrayList<>();
        if (getButton(coords.left()) != null) out.add(coords.left());
        if (getButton(coords.right()) != null) out.add(coords.right());
        if (getButton(coords.up()) != null) out.add(coords.up());
        if (getButton(coords.down()) != null) out.add(coords.down());
        return out;
    }

    /**
     * @return a list of all buttons in this collection
     */
    public List<CalcButton> getButtons() {
        ArrayList<CalcButton> out = new ArrayList<>();
        this.forEach((b) -> out.add(b.button));
        return out;
    }

    /**
     * Sets the bounds of this rendered collection, and rerenders it
     */
    public void setDimensions(Rectangle d) {
        destroy();
        x = d.x;
        y = d.y;
        width = d.width;
        height = d.height;
        forEach((button -> add(button.button, button.properties)));
    }

    /**
     * Serialises this object.
     */
    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("width", width);
        out.put("height", height);
        out.put("x", x);
        out.put("y", y);
        out.put("cur_height", cur_height);
        out.put("max_width", max_width);
        JSONArray buttons_json = new JSONArray();
        this.forEach((b) -> buttons_json.put(Map.of("button", b.button.getString(), "properties", b.properties.toJSON())));
        out.put("buttons", buttons_json);
        return out;
    }

    /**
     * Deserializes this object
     * @param state the current game state for button lookup
     */
    public static ButtonCollection fromJSON(JSONObject o, GameState state) {
        ButtonCollection out = new ButtonCollection();
        out.state = state;
        out.width = o.getInt("width");
        out.height = o.getInt("height");
        out.x = o.getInt("x");
        out.y = o.getInt("y");
        out.cur_height = o.getInt("cur_height");
        out.max_width = o.getInt("max_width");
        o.getJSONArray("buttons").forEach((_o) -> {
            if (_o instanceof JSONObject obj) {
                out.add(state.getButton(obj.getString("button")), Properties.fromJSON(obj.getJSONObject("properties")));
            }
        });
        return out;
    }
}
