package root;

import org.json.JSONArray;
import org.json.JSONObject;
import org.python.core.PyComplex;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import root.buttons.*;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class GameState {
    private static final Logger LOGGER = Logger.getLogger("GameState");
    public Random random = new Random();
    public Map<String, CalcButton> button_types = Map.of("text", new TextButton(), "func", new FuncButton());
    public List<CalcButton> all_buttons = new ArrayList<>();
    public Map<String, CalcButton> button_lookup = new HashMap<>();
    public List<CalcButton> non_infinity_buttons = new ArrayList<>();
    public ButtonCollection buttons;
    public ShopRerollButton reroll_button;
    public NextRoundButton next_round_button;
    public PyComplex goal;
    public int shop_slots = 6;
    public int infinity_shop_slots = 1;
    protected String screen;
    protected PyComplex money = new PyComplex(0);
    protected boolean inShop = false;
    public ButtonCollection shop;
    public long current_round = 0;
    public RenderType renderType;
    public PrintStream out = System.out;
    public Frame window;
    public Panel overlay;
    public Label calc_screen, goal_label, money_label;

    public GameState() {
        prepareRender();
        loadMods();
        all_buttons.add(new CalculateButton());
        buttons.add(all_buttons.getLast(), CalcButton.Properties.count(1.).infinity());
        prepareCalculatorRender();
        nextRound();
    }

    public void prepareRender() {
        renderType = RenderType.WINDOW;
        window = new Frame();
        window.setTitle("Calculator Game");
        window.setSize(600, 600);
        window.setLayout(null);
        window.setVisible(true);
        overlay = new Panel();
        overlay.setLayout(null);
        overlay.setSize(600, 600);
        overlay.setVisible(false);
        buttons = new ButtonCollection(getCalculatorDimensions(), this);
    }

    public void loadMods() {
        File mods = new File("mods");
        if (!mods.isDirectory()) LOGGER.warning("Invalid mods directory (%s)!".formatted(mods.getAbsolutePath()));
        String[] mod_list = mods.list();
        if (mod_list != null) {
            for (String mod_id : mod_list) {
                File mod_folder = new File(mods, mod_id);
                File config = new File(mod_folder, "config.json");
                if (config.exists() && config.isFile()) loadConfig(config);
            }
            all_buttons.forEach((b) -> button_lookup.put(b.getString(), b));
            non_infinity_buttons.addAll(all_buttons);
            for (String mod_id : mod_list) {
                File mod_folder = new File(mods, mod_id);
                File config = new File(mod_folder, "config.json");
                if (config.exists() && config.isFile()) loadStartingButtons(config);
            }
        }
    }

    public void prepareCalculatorRender() {
        reroll_button = new ShopRerollButton();
        next_round_button = new NextRoundButton();
        calc_screen = new Label();
        Rectangle screen_pos = getScreenDimensions();
        screen_pos.x = (int) Math.round(screen_pos.getCenterX());
        screen_pos.width = screen_pos.width/2;
        calc_screen.setBounds(screen_pos);
        calc_screen.setAlignment(Label.RIGHT);
        goal_label = new Label();
        screen_pos = getScreenDimensions();
        screen_pos.width = screen_pos.width/2;
        screen_pos.x = getButtonPadding();
        goal_label.setBounds(screen_pos);
        goal_label.setAlignment(Label.LEFT);
        money_label = new Label("$" + numToString(money));
        screen_pos = getScreenDimensions();
        screen_pos.x = getShopDimensions().x;
        money_label.setBounds(screen_pos);
        window.add(overlay);
        window.add(calc_screen);
        window.add(goal_label);
        window.add(money_label);
    }

    public void loadConfig(File file) {
        try {
            Scanner scanner = new Scanner(file);
            String s = scanner.useDelimiter("$").next();
            JSONObject obj = new JSONObject(s);
            if (!obj.isNull("operations")) {
                JSONObject ops = obj.getJSONObject("operations");
                for (String op : ops.keySet()) {
                    JSONObject op_json = ops.getJSONObject(op);
                    String op_code = op_json.getString("function");
                    int priority = op_json.isNull("priority") ? 1 : op_json.getInt("priority");
                    try (PythonInterpreter py = new PythonInterpreter()) {
                        try {
                            py.exec(op_code);
                            PyObject op_func = py.get("f");
                            if (op_func == null || !op_func.isCallable()) throw new RuntimeException();
                            CalculateButton.addOperation(op, priority, (a, b) -> {
                                py.set("__a", a);
                                py.set("__b", b);
                                return op_func.__call__(py.get("__a"), py.get("__b")).__complex__();
                            });
                        } catch (RuntimeException ignored) {
                            CalculateButton.addOperation(op, priority, (a, b) -> {
                                py.set("a", a);
                                py.set("b", b);
                                return py.eval(op_code).__complex__();
                            });
                        }
                    }
                }
            }
            if (!obj.isNull("buttons")) {
                JSONObject button_config = obj.getJSONObject("buttons");
                for (String button_type : button_config.keySet()) {
                    if (button_types.containsKey(button_type)) {
                        JSONArray arr = button_config.getJSONArray(button_type);
                        for (int i = 0; i < arr.length(); i++) {
                            if (arr.get(i) instanceof JSONArray args) {
                                List<String> str_args = new ArrayList<>();
                                for (Object o : args) str_args.add((String) o);
                                all_buttons.add(button_types.get(button_type).newButton(str_args));
                            } else all_buttons.add(button_types.get(button_type).newButton(List.of(arr.getString(i))));
                        }
                    } else LOGGER.warning("Unknown button type: %s".formatted(button_type));
                }
            }
            if (!obj.isNull("functions")) {
                JSONObject funcs = obj.getJSONObject("functions");
                for (String func_name : funcs.keySet()) {
                    String f_code = funcs.getString(func_name);
                    try (PythonInterpreter py = new PythonInterpreter()) {
                        try {
                            py.exec(f_code);
                            PyObject op_func = py.get("f");
                            if (op_func == null || !op_func.isCallable()) throw new RuntimeException();
                            CalculateButton.addFunction(func_name, (x) -> {
                                py.set("__x", x);
                                return op_func.__call__(py.get("__x")).__complex__();
                            });
                        } catch (RuntimeException ignored) {
                            CalculateButton.addFunction(func_name, (x) -> {
                                py.set("x", x);
                                return py.eval(f_code).__complex__();
                            });
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.warning("Unable to load config from file (file not found): " + file.getName());
        }
    }

    public void loadStartingButtons(File file) {
        try {
            Scanner scanner = new Scanner(file);
            String s = scanner.useDelimiter("$").next();
            JSONObject obj = new JSONObject(s);
            if (!obj.isNull("starting_buttons")) {
                JSONArray arr = obj.getJSONArray("starting_buttons");
                arr.forEach((o) -> buttons.add(button_lookup.get((String) o), CalcButton.Properties.count(2.)));
            }
        } catch (FileNotFoundException e) {
            LOGGER.warning("Unable to load config from file (file not found): " + file.getName());
        }
    }

    public void refreshShop() {
        if (shop != null) shop.destroy();
        shop = new ButtonCollection(getShopDimensions(), this);
        for (int i = 0; i < shop_slots; i++) {
            CalcButton button = getRandomButton();
            shop.add(button, CalcButton.Properties.price(getPrice(button)));
        }
        for (int i = 0; i < infinity_shop_slots; i++) {
            CalcButton button = getRandomButton();
            shop.add(button, CalcButton.Properties.price(getPrice(button).__mul__(new PyComplex(5.)).__complex__()).infinity());
        }
        shop.add(reroll_button, CalcButton.Properties.price(1.));
        shop.add(next_round_button, CalcButton.Properties.price(0.));
    }

    public void nextRound() {
        inShop = false;
        setGoal(new PyComplex(random.nextInt(100)));
        setScreen(Integer.toString(random.nextInt(100)));
        if (shop != null) shop.destroy();
        current_round++;
    }

    public void endRound() {
        inShop = true;
        addMoney(Math.min(((int) getMoney().real)/5, 5));
        addMoney(1.);
        refreshShop();
    }

    public void addMoney(int x) {
        addMoney((double) x);
    }

    public void addMoney(Double x) {
        setMoney(new PyComplex(getMoney().real + x, getMoney().imag));
    }

    public CalcButton getRandomButton() {
        return non_infinity_buttons.get(random.nextInt(non_infinity_buttons.size()));
    }

    public PyComplex getPrice(CalcButton button) {
        return button.getPrice(this);
    }

    public int getButtonPadding() {
        return switch (renderType) {
            case WINDOW -> 10;
            case CONSOLE -> 1;
        };
    }

    public Rectangle getShopDimensions() {
        return switch (renderType) {
            case WINDOW -> new Rectangle(300, 50, 200, 600);
            case CONSOLE -> new Rectangle(8, 8);
        };
    }

    public Rectangle getCalculatorDimensions() {
        return switch (renderType) {
            case WINDOW -> new Rectangle(0, window.getInsets().top + 20, 300, 600);
            case CONSOLE -> new Rectangle(8, 8);
        };
    }

    public Rectangle getScreenDimensions() {
        return switch (renderType) {
            case WINDOW -> new Rectangle(0, window.getInsets().top, buttons.getWidth(), 20);
            case CONSOLE -> new Rectangle(8, 1);
        };
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String s) {
        if (s.startsWith("0") && s.length() > 1) s = s.substring(1);
        screen = s;
        calc_screen.setText(s);
        try {
            if (!inShop && Objects.equals(screen, numToString(goal))) endRound();
        } catch (NumberFormatException ignored) {}
    }

    public void setMoney(PyComplex money) {
        this.money = money;
        money_label.setText("$" + numToString(money));
    }

    public PyComplex getMoney() {
        return money;
    }

    public void setGoal(PyComplex goal) {
        this.goal = goal;
        goal_label.setText("Goal: %s".formatted(numToString(goal)));
    }

    public void subMoney(PyComplex price) {
        this.setMoney(this.getMoney().__sub__(price).__complex__());
    }

    public String numToString(PyComplex x) {
        if (x.real == 0 && x.imag == 0) return "0";
        if (x.__cmp__(PyComplex.Inf) == 0) return "Infinity";
        String tmp = x.toString();
        if (tmp.endsWith("+0j)") && tmp.startsWith("(")) tmp = tmp.substring(1, tmp.length() - 4);
        return tmp;
    }

    public enum RenderType {
        CONSOLE,
        WINDOW
    }
}
