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

    public static Properties price(PyComplex price) {
        Properties properties = new Properties();
        properties.price = price;
        return properties;
    }

    public static Properties count(PyComplex count) {
        Properties properties = new Properties();
        properties.count = count;
        return properties;
    }

    public static Properties price(double price) {
        return price(new PyComplex(price));
    }

    public static Properties count(double count) {
        return count(new PyComplex(count));
    }

    public Properties infinity() {
        infinity = true;
        count = PyComplex.Inf;
        return this;
    }

    public void decreaseCount() {
        if (count == null) count = new PyComplex(0.);
        count.real--;
    }

    public void increaseCount() {
        if (count == null) count = new PyComplex(0.);
        count.real++;
    }

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
