package com.calcgame.main;

/**
 * A class containing only static constants, which are the ids of all base events.
 * Prefer using these constants to string literals.
 */
public class Events {
    /**
     * A private constructor to prevent accidental instantiation of this class
     */
    private Events() {}

    /**
     * Emitted after any button is added to any {@link ButtonCollection}
     */
    public static final String ADD = "add";
    /**
     * Emitted after any button on the calculator is clicked
     */
    public static final String CLICK = "click";
    /**
     * Emitted after the round is started (e.g. the shop is already destroyed)
     */
    public static final String ROUND_START = "roundStart";
    /**
     * Emitted after the round is ended, but before the shop is created
     */
    public static final String ROUND_END = "roundEnd";
    /**
     * Emitted after the shop is rerolled
     */
    public static final String REROLL = "reroll";
    /**
     * Emitted after any button is bought
     */
    public static final String BUY = "buy";
}
