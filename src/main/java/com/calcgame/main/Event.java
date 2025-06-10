package com.calcgame.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * An event that can be emitted and listened to.
 */
public class Event {
    /**
     * This event's logger, to be used only in this event's functions
     */
    private final Logger LOGGER;
    /**
     * The name of the event for logging purposes
     */
    protected final String name;
    /**
     * All listeners of this event, where keys are ids
     */
    protected Map<String, Action> listeners;
    /**
     * All private listeners of this event, invoked only when the event is emitted with their id.
     * Only one private listener may be invoked when the event is emitted, as multiple listeners cannot have the same id.
     */
    protected Map<String, Action> private_listeners;

    /**
     * Constructs a new event with the specified name
     * @param name the name of the new event, used only for logging
     */
    public Event(String name) {
        this.name = name;
        this.LOGGER = LogManager.getLogger("Event/%s".formatted(name));
        this.listeners = new HashMap<>();
        this.private_listeners = new HashMap<>();
    }

    /**
     * Invokes all listeners of this event
     * @param ctx the context to pass to all listeners
     */
    public void emit(ActionContext ctx) {
        emit(ctx, null);
    }

    /**
     * Invokes all listeners of this event, and the private listener with the specified id if it exists
     * @param ctx the context to pass to all listeners
     * @param privateListenerId the id of the private listener, ignored if {@code null}
     */
    public void emit(ActionContext ctx, @Nullable String privateListenerId) {
        Logger logger = ctx.logger() == null ? LOGGER : ctx.logger();
        if (privateListenerId != null) logger.debug("Emitting event {} with private listener id {}", name, privateListenerId);
        else logger.debug("Emitting event {}", name);
        if (ctx.data() != null) logger.debug("Event data: {}", ctx.data());
        ctx.state().appendToLastAction(new Action("%sEvent".formatted(name)) {
            @Override
            protected void redoInternal() {
                try {
                    assert getContext() != null;
                    if (privateListenerId != null && private_listeners.containsKey(privateListenerId)) getContext().state().appendToLastAction(private_listeners.get(privateListenerId)).redo();
                    listeners.forEach((id, action) -> {
                        action.setContext(ctx);
                        getContext().state().appendToLastAction(action).redo();
                    });
                } catch (EventInterrupt e) {
                    LOGGER.debug("Event interrupted: {}", e.getMessage());
                }
            }

            @Override
            protected void undoInternal() {}

            @Override
            public boolean undoable() {
                return true;
            }

            @Override
            public ActionContext getContext() {
                return ctx;
            }
        }).redo();
    }

    /**
     * Adds a listener with the specified id
     * @param listener the listener to add, if {@code null} removes the listener with the specified id
     * @param id the id of the listener to add
     */
    public void addListener(@Nullable Action listener, String id) {
        LOGGER.trace("Adding listener {} with id {}", listener == null ? "null" : listener.name, id);
        if (listener == null && !listeners.containsKey(id)) LOGGER.warn("Attempt to remove a listener that does not exist (id {})", id);
        if (listener != null && listeners.containsKey(id)) LOGGER.warn("Overwriting listener with id {}", id);
        listeners.put(id, listener);
    }

    /**
     * Removes a listener with the specified id.
     * Equivalent to {@code addListener(null, id)}
     * @param id the id of the listener to remove
     */
    public void removeListener(String id) {
        addListener(null, id);
    }
}
