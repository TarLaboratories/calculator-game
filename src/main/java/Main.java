import com.calcgame.main.GameState;

/**
 * Exists solely for the {@link Main#main(String[])} method
 */
public class Main {
    /**
     * Private constructor to prevent accidental instantiation of this class
     */
    private Main() {}

    /**
     * Launches the game
     * @param args command-line arguments, currently ignored
     */
    public static void main(String[] args) {
        GameState state = new GameState();
    }
}