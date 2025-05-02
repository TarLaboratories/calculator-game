package com.calcgame.main.buttons;

import com.calcgame.main.ButtonCollection;
import com.calcgame.main.Utils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.python.core.PyComplex;
import org.python.core.PyDictionary;

import java.awt.*;

public class Properties {
    public int x;
    public int y;
    @Nullable
    public PyComplex count;
    @Nullable
    public PyComplex price;
    public boolean infinity = false, sold = false;
    public Button rendered_button;
    public Label rendered_price, rendered_count;
    public ButtonCollection collection;
    public ButtonCollection.Coordinate pos;
    public PyDictionary data = new PyDictionary();

    /**
     * Creates a new Properties object that only has the price set
     */
    public static Properties price(PyComplex price) {
        Properties properties = new Properties();
        properties.price = price;
        return properties;
    }

    /**
     * Creates a new Properties object that only has the count set
     */
    public static Properties count(PyComplex count) {
        Properties properties = new Properties();
        properties.count = count;
        return properties;
    }

    /**
     * Creates a new Properties object that only has the price set
     */
    public static Properties price(double price) {
        return price(new PyComplex(price));
    }

    /**
     * Creates a new Properties object that only has the count set
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
