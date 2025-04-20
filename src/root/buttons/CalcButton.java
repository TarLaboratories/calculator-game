package root.buttons;

import org.jetbrains.annotations.Nullable;
import org.python.core.PyComplex;
import root.GameState;

import java.awt.*;
import java.util.List;

public interface CalcButton {
    void onClick(GameState state, Properties properties);
    void render(GameState state, Properties properties);
    void destroy(GameState state, Properties properties);
    PyComplex getPrice(GameState state);
    int getWidth(GameState state, Properties properties);
    int getHeight(GameState state, Properties properties);
    CalcButton newButton(List<String> args);
    String getString();

    class Properties {
        public int x;
        public int y;
        @Nullable public PyComplex count;
        @Nullable public PyComplex price;
        public boolean infinity = false, sold = false;
        public Button rendered_button;
        public Label rendered_price, rendered_count;

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
    }
}
