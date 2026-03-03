package com.darksoldier1404.dpcf.functions;

import com.darksoldier1404.dpcf.obj.FishRank;
import com.darksoldier1404.dppc.api.essentials.MoneyAPI;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.NBT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.darksoldier1404.dpcf.CustomFishing.plugin;

public class DPCFFunction {

    public static void init() {
        plugin.allowedWorlds.clear();
        if (plugin.getConfig().contains("Settings.AllowedWorlds")) {
            plugin.getConfig().getStringList("Settings.AllowedWorlds").forEach(world -> plugin.allowedWorlds.add(world));
        }
    }

    public static boolean isExistRank(String rank) {
        return plugin.fishRankData.containsKey(rank);
    }

    public static void createRank(Player player, String rank) {
        if (isExistRank(rank)) {
            player.sendMessage(plugin.prefix + "이미 존재하는 등급입니다.");
            return;
        }
        FishRank fr = new FishRank();
        fr.setName(rank);
        fr.setDefaultPrice(100);
        fr.setMinLength(1);
        fr.setMaxLength(100);
        fr.setWeight(100);
        DInventory inv = new DInventory("Fish Rank Inventory", 54, true, true, plugin);
        inv.setPages(99);
        fr.setInventory(inv);
        plugin.fishRankData.put(rank, fr);
        plugin.fishRankData.save(rank);
        player.sendMessage(plugin.prefix + "등급이 생성되었습니다.");
    }

    public static void editItems(Player player, String rank) {
        if (!isExistRank(rank)) {
            player.sendMessage(plugin.prefix + "존재하지 않는 등급입니다.");
            return;
        }
        FishRank fr = plugin.fishRankData.get(rank);
        DInventory inv = fr.getInventory();
        inv.setChannel(1);
        inv.setObj(rank);
        inv.openInventory(player);
    }

    public static void saveItems(Player player, DInventory inv) {
        inv.applyChanges();
        String rank = (String) inv.getObj();
        if (!isExistRank(rank)) {
            player.sendMessage(plugin.prefix + "존재하지 않는 등급입니다.");
            return;
        }
        FishRank fr = plugin.fishRankData.get(rank);
        fr.setInventory(inv);
        plugin.fishRankData.put(rank, fr);
        plugin.fishRankData.save(rank);
        player.sendMessage(plugin.prefix + "등급의 아이템이 저장되었습니다.");
    }

    public static void setLengthRange(Player player, String rank, int minLength, int maxLength) {
        if (!isExistRank(rank)) {
            player.sendMessage(plugin.prefix + "존재하지 않는 등급입니다.");
            return;
        }
        FishRank fr = plugin.fishRankData.get(rank);
        if (minLength >= maxLength) {
            player.sendMessage(plugin.prefix + "최소 길이는 최대 길이보다 크거나 같을 수 없습니다.");
            return;
        }
        fr.setMinLength(minLength);
        fr.setMaxLength(maxLength);
        plugin.fishRankData.put(rank, fr);
        plugin.fishRankData.save(rank);
        player.sendMessage(plugin.prefix + "등급의 길이 범위가 설정되었습니다.");
    }

    public static void deleteRank(Player player, String rank) {
        if (!isExistRank(rank)) {
            player.sendMessage(plugin.prefix + "존재하지 않는 등급입니다.");
            return;
        }
        plugin.fishRankData.delete(rank);
        plugin.fishRankData.remove(rank);
        player.sendMessage(plugin.prefix + "등급이 삭제되었습니다.");
    }

    public static void listRanks(Player player) {
        if (plugin.fishRankData.isEmpty()) {
            player.sendMessage(plugin.prefix + "등급이 존재하지 않습니다.");
            return;
        }
        player.sendMessage(plugin.prefix + "등급 목록:");
        plugin.fishRankData.values().forEach(fr -> player.sendMessage("§e- " + fr.getName() + " §7(길이: " + fr.getMinLength() + " - " + fr.getMaxLength() + ", 가격: " + fr.getDefaultPrice() + ", 가중치: " + fr.getWeight() + ")"));
    }

    public static ItemStack getRandomFishItem() {
        if (plugin.fishRankData.isEmpty()) {
            return null;
        }
        List<FishRank> ranks = new ArrayList<>(plugin.fishRankData.values());
        int totalWeight = ranks.stream().mapToInt(FishRank::getWeight).sum();
        if (totalWeight <= 0) {
            return null;
        }
        int randomWeight = java.util.concurrent.ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulativeWeight = 0;
        FishRank selectedRank = null;
        for (FishRank fr : ranks) {
            cumulativeWeight += fr.getWeight();
            if (randomWeight < cumulativeWeight) {
                selectedRank = fr;
                break;
            }
        }
        if (selectedRank == null) {
            return null;
        }
        DInventory inv = selectedRank.getInventory();
        List<ItemStack> items = inv.getAllPageItems();
        if (items.isEmpty()) {
            return null;
        }
        ItemStack item = items.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(items.size())).clone();
        return applyInfo(item, selectedRank);
    }

    public static ItemStack applyInfo(ItemStack item, FishRank fr) {
        ItemMeta im = item.getItemMeta();
        int length = fr.getMinLength() + (int) (Math.random() * (fr.getMaxLength() - fr.getMinLength() + 1));
        int price = fr.getDefaultPrice() + (length * plugin.pricePerLength);
        List<String> lore = im.getLore() != null ? im.getLore() : new ArrayList<>();
        lore.add("§e등급 §7: §f" + fr.getName());
        lore.add("§e길이 §7: §f" + length + "cm");
        lore.add("§e가격 §7: §f" + price);
        im.setLore(lore);
        item.setItemMeta(im);
        NBT.setStringTag(item, "dpcf_rank", fr.getName());
        NBT.setIntTag(item, "dpcf_length", length);
        NBT.setIntTag(item, "dpcf_price", price);
        return item;
    }

    public static void openShop(Player p) {
        DInventory inv = new DInventory("Fish Shop", 45, true, false, plugin);
        inv.setChannel(2);
        ItemStack[] pt = new ItemStack[9];
        ItemStack pane = new ItemStack(org.bukkit.Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.setDisplayName("§f");
        pm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        pane.setItemMeta(pm);
        NBT.setStringTag(pane, "dppc_clickcancel", "true");
        for (int i = 0; i < 9; i++) {
            pt[i] = pane;
        }
        ItemStack sellButton = new ItemStack(org.bukkit.Material.GREEN_WOOL);
        ItemMeta sm = sellButton.getItemMeta();
        sm.setDisplayName("§a물고기 판매");
        sm.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        sellButton.setItemMeta(sm);
        NBT.setStringTag(sellButton, "dpcf_sell", "true");
        pt[4] = sellButton;
        inv.setPageTools(pt);
        inv.update();
        inv.openInventory(p);
    }

    public static void returnItems(Player p, DInventory inv) {
        inv.getAllPageItems().forEach(item -> {
            if (item != null && !item.getType().isAir()) {
                HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    leftover.values().forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
                }
            }
        });
    }

    public static void updatePrticeInfo(DInventory inv) {
        inv.applyChanges();
        ItemStack sellButton = inv.getPageTools()[4];
        ItemMeta sm = sellButton.getItemMeta();
        int totalPrice = inv.getAllPageItems().stream().filter(item -> item != null && !item.getType().isAir() && NBT.hasTagKey(item, "dpcf_price")).mapToInt(item -> NBT.getIntegerTag(item, "dpcf_price") * item.getAmount()).sum();
        sm.setDisplayName("§a물고기 판매");
        sm.setLore(Arrays.asList("§e총 가격 §7: §f" + totalPrice));
        sellButton.setItemMeta(sm);
        inv.getPageTools()[4] = sellButton;
        inv.update();
    }

    public static void sellItems(Player p, DInventory inv) {
        int totalPrice = inv.getAllPageItems().stream().filter(item -> item != null && !item.getType().isAir() && NBT.hasTagKey(item, "dpcf_price")).mapToInt(item -> NBT.getIntegerTag(item, "dpcf_price") * item.getAmount()).sum();
        inv.applyAllItemChanges(pis -> {
            if (pis != null && !pis.getItem().getType().isAir() && NBT.hasTagKey(pis.getItem(), "dpcf_price")) {
                pis.setItem(null);
            }
            return pis;
        });
        if (totalPrice <= 0) {
            p.sendMessage(plugin.prefix + "판매할 물고기가 없습니다.");
            return;
        }
        inv.update();
        MoneyAPI.addMoney(p, totalPrice);
        p.sendMessage(plugin.prefix + "물고기를 판매하여 §e" + totalPrice + "§a원을 얻었습니다.");
        updatePrticeInfo(inv);
    }

    public static void setWeight(Player player, String rank, int weight) {
        if (!isExistRank(rank)) {
            player.sendMessage(plugin.prefix + "존재하지 않는 등급입니다.");
            return;
        }
        FishRank fr = plugin.fishRankData.get(rank);
        fr.setWeight(weight);
        plugin.fishRankData.put(rank, fr);
        plugin.fishRankData.save(rank);
        player.sendMessage(plugin.prefix + "등급의 가중치가 설정되었습니다.");
    }

    public static void setPrice(Player player, String rank, int price) {
        if (!isExistRank(rank)) {
            player.sendMessage(plugin.prefix + "존재하지 않는 등급입니다.");
            return;
        }
        FishRank fr = plugin.fishRankData.get(rank);
        fr.setDefaultPrice(price);
        plugin.fishRankData.put(rank, fr);
        plugin.fishRankData.save(rank);
        player.sendMessage(plugin.prefix + "등급의 기본 가격이 설정되었습니다.");
    }

    public static void setLengthPerPrice(Player player, int lengthPerPrice) {
        plugin.pricePerLength = lengthPerPrice;
        plugin.getConfig().set("Settings.PricePerLength", lengthPerPrice);
        plugin.saveConfig();
        player.sendMessage(plugin.prefix + "길이당 가격이 §e" + lengthPerPrice + "§a원으로 설정되었습니다.");
    }
}
