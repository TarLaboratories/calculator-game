package com.calcgame.main.buttons;

import com.calcgame.main.ButtonCollection;
import com.calcgame.main.Utils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.python.core.PyComplex;
import org.python.core.PyDictionary;

import java.awt.*;

/**
 * The properties of a button in a {@link ButtonCollection}
 */
public class Properties {
    /**
     * Constructs a properties object with all fields as {@code null}
     */
    public Properties() {}
    /**
     * The coordinates where the button is rendered (in px)
     */
    public int x, y;

    /**
     * The number of clicks remaining (or {@code null} if the button is in the shop)
     */
    @Nullable
    public PyComplex count;

    /**
     * The price of the button (or {@code null} if it is not in the shop)
     */
    @Nullable
    public PyComplex price;

    /**
     * Whether the button can be clicked infinitely
     */
    public boolean infinity = false;

    /**
     * Whether the button has been sold and should not be rendered
     */
    public boolean sold = false;

    /**
     * The rendered button object (or {@code null} if it has not been rendered yet)
     */
    public Button rendered_button;

    /**
     * The rendered price label object (or {@code null} if it has not been rendered yet)
     */
    public Label rendered_price;

    /**
     * The rendered count label object (or {@code null} if it has not been rendered yet)
     */
    public Label rendered_count;

    /**
     * The collection to which the button belongs
     */
    public ButtonCollection collection;

    /**
     * The position of the button in the collection
     */
    public ButtonCollection.Coordinate pos;

    /**
     * Other (mainly modded) data about the button
     */
    public PyDictionary data = new PyDictionary();

    /**
     * Creates a new Properties object that only has the price set
     * @param price what to set the price to
     * @return the new Properties object
     */
    public static Properties price(PyComplex price) {
        Properties properties = new Properties();
        properties.price = price;
        return properties;
    }

    /**
     * Creates a new Properties object that only has the count set
     * @param count what to set the count to
     * @return a properties object with the specified count
     */
    public static Properties count(PyComplex count) {
        Properties properties = new Properties();
        properties.count = count;
        return properties;
    }

    /**
     * Creates a new Properties object that only has the price set
     * @param price what to set the price to
     * @return a properties object with the specified price
     */
    public static Properties price(double price) {
        return price(new PyComplex(price));
    }

    /**
     * Creates a new Properties object that only has the count set
     * @param count what to set the count to
     * @return a properties object with the specified count
     */
    public static Properties count(double count) {
        return count(new PyComplex(count));
    }

    /**
     * Modifies this object to have an infinite count
     * @return this
     */
    public Properties infinity() {
        infinity = true;
        count = PyComplex.Inf;
        return this;
    }

    /**
     * Decreases the count by one.
     */
    public void decreaseCount() {
        if (count == null) count = new PyComplex(0.);
        count.real--;
    }

    /**
     * Increases the count by one.
     */
    public void increaseCount() {
        if (count == null) count = new PyComplex(0.);
        count.real++;
    }

    /**
     * Creates a new Properties object, that has the same values as the provided object
     * @param properties the object to copy from
     * @return a copy
     */
    public static Properties copy(Properties properties) {
        Properties p = new Properties();
        p.count = properties.count;
        p.price = properties.price;
        p.rendered_price = properties.rendered_price;
        p.rendered_button = properties.rendered_button;
        p.rendered_count = properties.rendered_count;
        p.x = properties.x;
        p.y = properties.y;
        p.infinity = properties.infinity;
        p.sold = properties.sold;
        return p;
    }

    /**
     * Serialises this object
     * @return the serialised object
     */
    public JSONObject toJSON() {
        JSONObject out = new JSONObject();
        out.put("count", Utils.toJSON(count));
        out.put("price", Utils.toJSON(price));
        out.put("x", x);
        out.put("y", y);
        out.put("infinity", infinity);
        out.put("sold", sold);
        return out;
    }

    /**
     * Deserializes this object
     * @param o the object to deserialize
     * @return the deserialized object
     */
    public static Properties fromJSON(JSONObject o) {
        Properties out = new Properties();
        out.count = Utils.fromJSON(o.getJSONObject("count"));
        out.price = Utils.fromJSON(o.getJSONObject("price"));
        out.x = o.getInt("x");
        out.y = o.getInt("y");
        out.infinity = o.getBoolean("infinity");
        out.sold = o.getBoolean("sold");
        return out;
    }
}
