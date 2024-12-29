package com.wish.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SlimAPIReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public SlimAPIReloadEvent() {
        super();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
