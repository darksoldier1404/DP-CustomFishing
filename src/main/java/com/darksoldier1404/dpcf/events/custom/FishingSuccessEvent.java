package com.darksoldier1404.dpcf.events.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FishingSuccessEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;
    private final ItemStack caughtItem;

    public FishingSuccessEvent(Player player, ItemStack caughtItem) {
        this.player = player;
        this.caughtItem = caughtItem;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getCaughtItem() {
        return caughtItem;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
