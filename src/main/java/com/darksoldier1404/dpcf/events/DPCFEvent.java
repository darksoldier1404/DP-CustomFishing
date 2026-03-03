package com.darksoldier1404.dpcf.events;

import com.darksoldier1404.dpcf.functions.DPCFFunction;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.events.dinventory.DInventoryCloseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import static com.darksoldier1404.dpcf.CustomFishing.plugin;

public class DPCFEvent implements Listener {

    @EventHandler
    public void onInventoryClose(DInventoryCloseEvent e) {
        DInventory inv = e.getDInventory();
        if (inv.isValidHandler(plugin)) {
            if (inv.isValidChannel(1)) {
                DPCFFunction.saveItems((Player) e.getPlayer(), inv);
            }
        }
    }

    @EventHandler
    public void onFishing(PlayerFishEvent e) {
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        e.setCancelled(true);
        e.getHook().remove();
        Player p = e.getPlayer();
        ItemStack caught = DPCFFunction.getRandomFishItem();
        if (caught == null) {
            return;
        }
        p.getInventory().addItem(caught);
    }
}
