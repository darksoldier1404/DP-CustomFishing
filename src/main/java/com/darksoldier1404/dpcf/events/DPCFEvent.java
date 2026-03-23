package com.darksoldier1404.dpcf.events;

import com.darksoldier1404.dpcf.events.custom.FishingSuccessEvent;
import com.darksoldier1404.dpcf.functions.ContestManager;
import com.darksoldier1404.dpcf.functions.DPCFFunction;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.events.dinventory.DInventoryClickEvent;
import com.darksoldier1404.dppc.events.dinventory.DInventoryCloseEvent;
import com.darksoldier1404.dppc.utils.NBT;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.darksoldier1404.dpcf.CustomFishing.plugin;

public class DPCFEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        DPCFFunction.initPlayer(p);
        ContestManager.onPlayerJoin(p);
    }

    @EventHandler
    public void onQuit2(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        DPCFFunction.savePlayer(p);
        ContestManager.onPlayerQuit(p);
    }

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
                    if (e.getClick() == ClickType.SHIFT_RIGHT) {
                        DPCFFunction.sellAllItems(p);
                    } else {
                        DPCFFunction.sellItems(p, inv);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFishing(PlayerFishEvent e) {
        Player p = e.getPlayer();
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        if (!plugin.allowedWorlds.contains(p.getWorld().getName())) return;
        e.setCancelled(true);
        e.getHook().remove();
        ItemStack caught = DPCFFunction.getRandomFishItem();
        if (caught == null) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> DPCFFunction.startMinigame(p, caught), 1L);
    }

    private Set<UUID> cooldowns = new HashSet<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!DPCFFunction.isInMinigame(p)) return;
        e.setCancelled(true);
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (cooldowns.contains(p.getUniqueId())) return;
        // action filter
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
                e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK &&
                e.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_AIR &&
                e.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
            return;
        }
        boolean isLeftClick = e.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR ||
                e.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
        DPCFFunction.processClick(p, isLeftClick);
        cooldowns.add(p.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> cooldowns.remove(p.getUniqueId()), 1L);
    }

    /**
     * 플레이어 접속 종료 시 진행 중인 미니게임 세션을 정리한다.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (DPCFFunction.isInMinigame(p)) {
            DPCFFunction.endMinigame(p, false);
        }
    }

    @EventHandler
    public void onFishing(FishingSuccessEvent e) {
        Player p = e.getPlayer();
        // 대회 점수 갱신
        ContestManager.onFishCaught(p, e.getCaughtItem());
        if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(plugin.prefix + "인벤토리가 가득 찼습니다!");
        }
    }
}
