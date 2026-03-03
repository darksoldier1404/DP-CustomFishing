package com.darksoldier1404.dpcf.events;

import com.darksoldier1404.dpcf.functions.DPCFFunction;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.events.dinventory.DInventoryClickEvent;
import com.darksoldier1404.dppc.events.dinventory.DInventoryCloseEvent;
import com.darksoldier1404.dppc.utils.NBT;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
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
                return;
            }
            if (inv.isValidChannel(2)) {
                DPCFFunction.returnItems((Player) e.getPlayer(), inv);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(DInventoryClickEvent e) {
        DInventory inv = e.getDInventory();
        if (inv.isValidHandler(plugin)) {
            if (inv.isValidChannel(2)) {
                e.setCancelled(true);
                Player p = (Player) e.getWhoClicked();
                ItemStack item = e.getCurrentItem();
                if (item == null) {
                    return;
                }
                if (NBT.hasTagKey(item, "dpcf_price")) {
                    if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
                        inv.addItem(item.clone());
                        item.setAmount(0);
                    } else {
                        p.getInventory().addItem(item.clone());
                        item.setAmount(0);
                    }
                    DPCFFunction.updatePrticeInfo(inv);
                    return;
                }
                if (NBT.hasTagKey(item, "dpcf_sell")) {
                    DPCFFunction.sellItems(p, inv);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onFishing(PlayerFishEvent e) {
        Player p = e.getPlayer();
        if (!plugin.allowedWorlds.contains(p.getWorld().getName())) return;
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        e.setCancelled(true);
        e.getHook().remove();
        ItemStack caught = DPCFFunction.getRandomFishItem();
        if (caught == null) {
            return;
        }
        p.getInventory().addItem(caught);
    }
}
