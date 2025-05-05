package com.calcgame.main;

/**
 * An exception to be thrown in an event listener to interrupt the execution of the event
 */
public class EventInterrupt extends RuntimeException {
    /**
     * Constructs an event interrupt
     * @param message the message to log when the interrupt is caught
     */
    public EventInterrupt(String message) {
        super(message);
    }
}
