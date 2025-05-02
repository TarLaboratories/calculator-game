package com.calcgame.main;

import com.calcgame.main.buttons.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.core.PyComplex;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import com.calcgame.main.buttons.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class GameState {
    private static final Logger LOGGER = LogManager.getLogger();
    protected Random random = new Random();
    protected List<Integer> random_sequence = new ArrayList<>();
    protected int cur_random_i = 0;
    protected Map<String, CalcButton> button_types = Map.of("text", new TextButton(), "func", new FuncButton());
    protected Map<String, Map<String, String>> mod_files = new HashMap<>();
    protected Map<String, Runnable> on_round_start = new HashMap<>();
    protected List<CalcButton> all_buttons = new ArrayList<>();
    protected Map<String, CalcButton> button_lookup = new HashMap<>();
    protected List<CalcButton> sellable_buttons = new ArrayList<>();
    protected ButtonCollection buttons;
    protected ShopRerollButton reroll_button;
    protected NextRoundButton next_round_button;
    protected UndoButton undo_button;
    protected RedoButton redo_button;
    protected PyComplex goal;
    protected int shop_slots = 6;
    protected int infinity_shop_slots = 1;
    protected String screen;
    protected PyComplex money = new PyComplex(0);
    protected boolean inShop = false;
    protected ButtonCollection shop;
    protected long current_round = 0;
    protected RenderType renderType;
    protected Frame window;
    protected Panel overlay;
    protected Label calc_screen, goal_label, money_label;
    protected List<Label> tooltip_labels;
    protected Panel tooltip_bg;
    protected List<Action> undo_stack = new ArrayList<>();
    protected Action temp_action = Action.forUndo(() -> {});
    protected int cur_undo_stack_i = -1;
    protected long seed;

    /**
     * Constructs a new GameState and starts the game.
     */
    public GameState() {
        LOGGER.info("Creating a new game state");
        seed = random.nextLong();
        random.setSeed(seed);
        prepareRender();
        loadMods();
        addSystemButtons();
        prepareCalculatorRender();
        nextRound();
    }

    /**
     * Adds vital buttons to the game (e.g. '=', 'UNDO', 'REDO', 'Reroll', etc.)
     */
    public void addSystemButtons() {
        all_buttons.add(new CalculateButton());
        buttons.add(all_buttons.getLast(), Properties.count(1.).infinity());
        button_lookup.put("=", all_buttons.getLast());
        reroll_button = new ShopRerollButton();
        next_round_button = new NextRoundButton();
        button_lookup.put(reroll_button.getString(), reroll_button);
        button_lookup.put(next_round_button.getString(), next_round_button);
        undo_button = new UndoButton();
        redo_button = new RedoButton();
        buttons.add(undo_button, Properties.count(1).infinity());
        buttons.add(redo_button, Properties.count(1).infinity());
    }

    /**
     * Prepares the window for rendering, creating all necessary frame and label objects
     */
    public void prepareRender() {
        renderType = RenderType.WINDOW;
        window = new Frame();
        window.setTitle("Calculator Game");
        window.setSize(600, 600);
        window.setLayout(null);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LOGGER.info("Exiting...");
                window.dispose();
            }
        });
        window.setVisible(true);
        overlay = new Panel();
        overlay.setLayout(null);
        overlay.setSize(600, 600);
        overlay.setVisible(false);
        tooltip_labels = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            Label tooltip_label = new Label();
            tooltip_label.setBackground(Color.GRAY);
            tooltip_label.setVisible(false);
            window.add(tooltip_label);
            tooltip_labels.add(tooltip_label);
        }
        tooltip_bg = new Panel();
        tooltip_bg.setBackground(Color.GRAY);
        tooltip_bg.setVisible(false);
        window.add(tooltip_bg);
        buttons = new ButtonCollection(getCalculatorDimensions(), this);
    }

    /**
     * Loads all mods from the mod folder (./mods), and starting buttons from them
     */
    public void loadMods() {
        LOGGER.info("Loading mods...");
        File mods = new File("mods");
        if (!mods.isDirectory()) LOGGER.warn("Invalid mods directory ({})!", mods.getAbsolutePath());
        String[] mod_list = mods.list();
        if (mod_list != null) {
            for (String mod_id : mod_list) {
                LOGGER.info("Loading mod '{}'", mod_id);
                File mod_folder = new File(mods, mod_id);
                mod_files.put(mod_id, new HashMap<>());
                for (File file : Objects.requireNonNull(mod_folder.listFiles())) {
                    try {
                        LOGGER.info("Loading file '{}' from mod '{}'", file.getName(), mod_id);
                        Scanner scanner = new Scanner(file);
                        mod_files.get(mod_id).put(file.getName(), scanner.useDelimiter("$").next());
                    } catch (FileNotFoundException ignored) {}
                }
                File config = new File(mod_folder, "config.json");
                if (config.exists() && config.isFile()) loadConfig(config, mod_id);
            }
            all_buttons.forEach((b) -> button_lookup.put(b.getString(), b));
            sellable_buttons.addAll(all_buttons);
            for (String mod_id : mod_list) {
                File mod_folder = new File(mods, mod_id);
                File config = new File(mod_folder, "config.json");
                if (config.exists() && config.isFile()) loadStartingButtons(config, mod_id);
            }
        }
    }

    /**
     * Prepares the calculator for rendering by creating all necessary label objects
     */
    public void prepareCalculatorRender() {
        calc_screen = new Label();
        Rectangle pos = getScreenDimensions();
        pos.x = (int) Math.round(pos.getCenterX());
        pos.width = pos.width/2;
        calc_screen.setBounds(pos);
        calc_screen.setAlignment(Label.RIGHT);
        goal_label = new Label();
        pos = getScreenDimensions();
        pos.width = pos.width/2;
        pos.x = getButtonPadding();
        goal_label.setBounds(pos);
        goal_label.setAlignment(Label.LEFT);
        money_label = new Label("$" + numToString(money));
        pos = getScreenDimensions();
        pos.x = getShopDimensions().x;
        money_label.setBounds(pos);
        window.add(overlay);
        window.add(calc_screen);
        window.add(goal_label);
        window.add(money_label);
    }

    /**
     * Loads all mod configuration, except starting buttons, from a file
     * @param file the file to load from
     * @param mod_id the id of the mod that supplied the file
     */
    public void loadConfig(File file, String mod_id) {
        LOGGER.info("Loading config file from mod '{}'", mod_id);
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
                for (Iterator<String> it = button_config.keys(); it.hasNext(); ) {
                    String button_type = it.next();
                    if (button_types.containsKey(button_type)) {
                        JSONArray arr = button_config.getJSONArray(button_type);
                        for (int i = 0; i < arr.length(); i++) {
                            if (arr.get(i) instanceof JSONArray args) {
                                List<String> str_args = new ArrayList<>();
                                for (Object o : args) {
                                    if (mod_files.get(mod_id).containsKey((String) o)) str_args.add(mod_files.get(mod_id).get(o));
                                    else str_args.add((String) o);
                                }
                                all_buttons.add(button_types.get(button_type).newButton(str_args));
                            } else all_buttons.add(button_types.get(button_type).newButton(List.of(arr.getString(i))));
                        }
                    } else LOGGER.warn("Unknown button type: {}", button_type);
                }
            }
            if (!obj.isNull("functions")) {
                JSONObject funcs = obj.getJSONObject("functions");
                for (Iterator<String> it = funcs.keys(); it.hasNext(); ) {
                    String func_name = it.next();
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
            LOGGER.info("Loading mod '{}' completed", mod_id);
        } catch (FileNotFoundException | JSONException e) {
            LOGGER.warn("Unable to load mod config from file '{}': {}", file.getName(), e);
        }
    }

    /**
     * Loads the starting buttons from a mod config file
     * @param file the file to load from
     * @param mod_id the mod id of the mod that supplied this file
     */
    public void loadStartingButtons(File file, String mod_id) {
        try {
            LOGGER.info("Loading starting buttons from mod '{}'", mod_id);
            Scanner scanner = new Scanner(file);
            String s = scanner.useDelimiter("$").next();
            JSONObject obj = new JSONObject(s);
            if (!obj.isNull("starting_buttons")) {
                JSONArray arr = obj.getJSONArray("starting_buttons");
                arr.forEach((o) -> buttons.add(button_lookup.get((String) o), Properties.count(2.)));
            }
            LOGGER.info("Loading starting buttons from mod '{}' completed", mod_id);
        } catch (FileNotFoundException | JSONException e) {
            LOGGER.warn("Unable to load starting buttons from mod config file '{}': {}", file.getName(), e);
        }
    }

    /**
     * Refreshes the shop, or creates it if it didn't exist.
     * Appends the (undoable) refresh action to the last action (being) executed.
     */
    public void refreshShop() {
        JSONObject old_shop;
        if (shop != null) {
            old_shop = shop.toJSON();
            shop.destroy();
        } else {
            old_shop = null;
        }
        appendToLastAction(new Action() {
            @Override
            public void redo() {
                LOGGER.info("Refreshing shop");
                shop = new ButtonCollection(getShopDimensions(), GameState.this);
                for (int i = 0; i < shop_slots; i++) {
                    CalcButton button = getRandomButton();
                    shop.add(button, Properties.price(getPrice(button)));
                }
                for (int i = 0; i < infinity_shop_slots; i++) {
                    CalcButton button = getRandomButton();
                    shop.add(button, Properties.price(getPrice(button).__mul__(new PyComplex(5.)).__complex__()).infinity());
                }
                shop.add(reroll_button, Properties.price(1.));
                shop.add(next_round_button, Properties.price(0.));
            }

            @Override
            public void undo() {
                LOGGER.info("Unrefreshing shop");
                if (shop != null) shop.destroy();
                if (old_shop != null) {
                    shop = ButtonCollection.fromJSON(old_shop, GameState.this);
                    shop.render();
                }
            }

            @Override
            public boolean undoable() {
                return true;
            }
        }).redo();

    }

    /**
     * Starts the next round, closing the shop and increasing the round counter.
     * Generates one action, that is not undoable
     */
    public void nextRound() {
        inShop = false;
        setGoal(new PyComplex(random.nextInt(100)));
        setScreen(Integer.toString(random.nextInt(100)));
        if (shop != null) shop.destroy();
        current_round++;
        on_round_start.forEach((_, f) -> f.run());
        doAction(Action.forFunction(() -> {}));
    }

    /**
     * Ends the current round, and displays the shop.
     * Generates one action, that is not undoable.
     */
    public void endRound() {
        LOGGER.info("Round #{} ended", current_round);
        appendToLastAction(Action.forFunction(() -> {})); //block undo further than round end
        inShop = true;
        addMoney(Math.min(((int) getMoney().real)/5, 5));
        addMoney(1.);
        refreshShop();
    }

    /**
     * Adds the specified amount to the player's money
     */
    public void addMoney(int x) {
        addMoney((double) x);
    }

    /**
     * Adds the specified amount to the player's money
     */
    public void addMoney(Double x) {
        setMoney(new PyComplex(getMoney().real + x, getMoney().imag));
    }

    /**
     * Adds the specified amount to the player's money
     */
    public void addMoney(PyComplex x) {
        setMoney(getMoney().__add__(x).__complex__());
    }

    /**
     * @return a random, currently sellable button
     */
    public CalcButton getRandomButton() {
        return sellable_buttons.get(randint(0, sellable_buttons.size()));
    }

    /**
     * @return the price of this button, if it were in the shop
     */
    public PyComplex getPrice(CalcButton button) {
        return button.getPrice(this);
    }

    /**
     * @return the distance between buttons
     */
    public int getButtonPadding() {
        return switch (renderType) {
            case WINDOW -> 10;
            case CONSOLE -> 1;
        };
    }

    /**
     * @return the bounds of the shop
     */
    public Rectangle getShopDimensions() {
        return switch (renderType) {
            case WINDOW -> new Rectangle(window.getWidth() - 350, getCalculatorDimensions().y, 200, 600);
            case CONSOLE -> new Rectangle(8, 8);
        };
    }

    /**
     * @return the bounds of the calculator
     */
    public Rectangle getCalculatorDimensions() {
        return switch (renderType) {
            case WINDOW -> new Rectangle(0, window.getInsets().top + 20, 300, 600);
            case CONSOLE -> new Rectangle(8, 8);
        };
    }

    /**
     * @return the bounds of the calculator screen
     */
    public Rectangle getScreenDimensions() {
        return switch (renderType) {
            case WINDOW -> new Rectangle(0, window.getInsets().top, buttons.getWidth(), 20);
            case CONSOLE -> new Rectangle(8, 1);
        };
    }

    /**
     * @return the string currently displayed on the calculator screen
     */
    public String getScreen() {
        return screen;
    }

    /**
     * Sets the string on the calculator screen to the specified string
     * @param s the string to display, does not have to be a valid mathematical expression
     */
    public void setScreen(String s) {
        if (s.startsWith("0") && s.length() > 1) s = s.substring(1);
        if (s.endsWith("+0j)") && s.startsWith("(")) s = s.substring(1, s.length() - 4);
        screen = s;
        calc_screen.setText(s);
        try {
            if (!inShop && Objects.equals(screen, numToString(getGoal()))) endRound();
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Sets the amount of money the player has
     */
    public void setMoney(PyComplex money) {
        this.money = money;
        money_label.setText("$" + numToString(money));
    }

    /**
     * @return the amount of money the player currently has
     */
    public PyComplex getMoney() {
        return money;
    }

    /**
     * Set the number, that the player is required to reach to end this round
     */
    public void setGoal(PyComplex goal) {
        this.goal = goal;
        goal_label.setText("Goal: %s".formatted(numToString(goal)));
    }

    /**
     * Reduce the player's money by the specified amount
     */
    public void subMoney(PyComplex price) {
        this.setMoney(this.getMoney().__sub__(price).__complex__());
    }

    /**
     * @return A human-readable string representing this number. This should be preferred over {@link PyComplex#toString()}
     */
    public String numToString(PyComplex x) {
        if (x.real == 0 && x.imag == 0) return "0";
        if (x.__cmp__(PyComplex.Inf) == 0) return "Infinity";
        String tmp = x.toString();
        if (tmp.endsWith("+0j)") && tmp.startsWith("(")) tmp = tmp.substring(1, tmp.length() - 4);
        return tmp;
    }

    /**
     * @return the number that the player is required to reach to end this round
     */
    public PyComplex getGoal() {
        return goal;
    }

    /**
     * @return the {@link RenderType} currently used
     */
    public RenderType getRenderType() {
        return renderType;
    }

    /**
     * @return a {@link ButtonCollection} of buttons currently on the calculator
     */
    public ButtonCollection getCurrentButtons() {
        return buttons;
    }

    /**
     * @return a list of all buttons registered
     */
    public List<CalcButton> getAllButtons() {
        return all_buttons;
    }

    /**
     * @return the Frame object, inside which everything is rendered
     */
    public Frame getWindow() {
        return window;
    }

    /**
     * @return the list of buttons that can appear in the shop
     */
    public List<CalcButton> getSellableButtons() {
        return sellable_buttons;
    }

    /**
     * @return a button that was registered under the specified name
     */
    public CalcButton getButton(String name) {
        return button_lookup.get(name);
    }

    /**
     * Returns a random element from the specified list.
     */
    public Object randomChoice(List<Object> in) {
        return in.get(randint(0, in.size()));
    }

    /**
     * Returns a random number between {@code min} (inclusive) and {@code max} (exclusive)
     */
    public int randint(int min, int max) {
        if (cur_random_i == random_sequence.size()) {
            random_sequence.add(random.nextInt(min, max));
            cur_random_i++;
            return random_sequence.getLast();
        } else {
            cur_random_i++;
            return random_sequence.get(cur_random_i - 1);
        }
    }

    /**
     * Registers a Python function to be executed at start of round.
     * @param id The id to register the function under. It's only effect is to be used in {@link GameState#removeOnRoundStart(String)} later.
     * @param f The Python function to register. Must be a callable python object.
     */
    public void onRoundStart(String id, PyObject f) {
        on_round_start.put(id, f::__call__);
    }

    /**
     * Removes the function that was previously registered using {@link GameState#onRoundStart(String, PyObject)}
     * @param id the id of the function to remove
     */
    public void removeOnRoundStart(String id) {
        on_round_start.remove(id);
    }

    /**
     * Has a {@code a/b} chance to return {@code true}. Uses {@link GameState#randint(int, int)}
     */
    public boolean chance(int a, int b) {
        return this.randint(0, b) < a;
    }

    /**
     * Renders a tooltip with the specified contents at the specified position
     * @param text The contents of the tooltip to render. Will be line-wrapped to fit into the specified bounds.
     * @param bounds the position and size of the tooltip to render
     */
    public void setTooltip(String text, Rectangle bounds) {
        FontMetrics m = tooltip_labels.getFirst().getFontMetrics(tooltip_labels.getFirst().getFont());
        StringBuilder tmp_line = new StringBuilder();
        Scanner idk = new Scanner(text);
        int cur_label = 0;
        Rectangle cur_bounds = new Rectangle(bounds), bg_bounds = new Rectangle(bounds);
        cur_bounds.height = m.getHeight();
        while (idk.hasNext()) {
            String next = idk.next();
            if (m.stringWidth(tmp_line + next + " ") < bounds.width) tmp_line.append(next).append(" ");
            else {
                Label tooltip_label = tooltip_labels.get(cur_label);
                tooltip_label.setText(tmp_line.toString());
                tooltip_label.setBounds(cur_bounds);
                tooltip_label.setVisible(true);
                tmp_line = new StringBuilder(next + " ");
                cur_bounds.y += cur_bounds.height + 1;
                bg_bounds.height = Math.max(bg_bounds.height, cur_bounds.y + cur_bounds.height - bg_bounds.y);
                cur_label++;
            }
        }
        Label tooltip_label = tooltip_labels.get(cur_label);
        tooltip_label.setText(tmp_line.toString());
        tooltip_label.setBounds(cur_bounds);
        tooltip_label.setVisible(true);
        tooltip_bg.setBounds(bg_bounds);
        tooltip_bg.setVisible(true);
    }

    /**
     * Removes any tooltip currently rendered
     */
    public void removeTooltip() {
        tooltip_bg.setVisible(false);
        tooltip_labels.forEach((l) -> l.setVisible(false));
    }

    /**
     * Invokes {@link Action#undo()} of the action currently selected in the undo stack, and moves the selection
     * to the previous action.
     */
    public void undo() {
        if (cur_undo_stack_i >= 0 && undo_stack.get(cur_undo_stack_i).undoable()) {
            undo_stack.get(cur_undo_stack_i).undo();
            cur_undo_stack_i--;
        } else if (cur_undo_stack_i == -1) LOGGER.debug("Cannot undo action, as there is nothing left to undo");
        else LOGGER.debug("Cannot undo action, as it is marked as not undoable");
    }

    /**
     * Moves the selection to the next action in the undo stack, and invokes {@link Action#redo()} of the newly selected action.
     */
    public void redo() {
        if (cur_undo_stack_i + 1 < undo_stack.size()) {
            cur_undo_stack_i++;
            undo_stack.get(cur_undo_stack_i).redo();
        }
    }

    /**
     * Appends the specified action to the undo stack (at the position after the currently selected action), then executes it. Also appends a
     * reset of this {@code GameState} random state (to it's state before the action is executed) to the undo function
     * @param action the action to execute and save to the undo stack
     */
    public void doAction(Action action) {
        LOGGER.trace("Doing action");
        int old_random_i = cur_random_i;
        action = action.andThen(temp_action).andThen(Action.forUndo(() -> {
            LOGGER.debug("Undoing changes to random: {} -> {}", cur_random_i, old_random_i);
            cur_random_i = old_random_i;
        }));
        temp_action = Action.forUndo(() -> {});
        if (undo_stack.size() > cur_undo_stack_i + 1) undo_stack.removeLast();
        cur_undo_stack_i++;
        undo_stack.add(action);
        action.redo();
        LOGGER.debug("Random changed: {} -> {}", old_random_i, cur_random_i);
    }

    /**
     * Appends the specified {@code Action} to the last action executed (or currently being executed)
     * by the means of {@link Action#andThen(Action)}. It will not be executed, unless it is undone and then redone.
     * The returned action is safe to {@link Action#redo()} without invoking {@link GameState#doAction(Action)}
     * @param action the {@code Action} to append
     * @return the input action
     */
    public Action appendToLastAction(Action action) {
        if (undo_stack.isEmpty()) return action;
        undo_stack.set(cur_undo_stack_i, undo_stack.get(cur_undo_stack_i).andThen(action));
        return action;
    }

    /**
     * When {@link GameState#doAction(Action)} is invoked next time, the specified {@code Action}
     * will be appended to the {@code doAction parameter} (by the means of {@link Action#andThen(Action)}), before it is executed.
     * @param action the {@code Action} to append
     */
    public void appendToNextAction(Action action) {
        temp_action = temp_action.andThen(action);
    }

    /**
     * @return the context of the last executed {@code Action}
     */
    public ActionContext getCurrentActionContext() {
        if (undo_stack.isEmpty()) return null;
        return undo_stack.get(cur_undo_stack_i).getContext();
    }

    public enum RenderType {
        CONSOLE,
        WINDOW
    }
}
