package com.darksoldier1404.dpcf.functions;

import com.darksoldier1404.dpcf.obj.FishRank;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.utils.NBT;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
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
        plugin.fishRankData.values().forEach(fr -> player.sendMessage("§e- " + fr.getName() + " §7(길이: " + fr.getMinLength() + " - " + fr.getMaxLength() + ", 가격: " + fr.getDefaultPrice() + ")"));
    }

    public static ItemStack getRandomFishItem() {
        if (plugin.fishRankData.isEmpty()) {
            return null;
        }
        int totalWeight = plugin.fishRankData.values().stream().mapToInt(FishRank::getWeight).sum();
        int randomWeight = (int) (Math.random() * totalWeight);
        for (FishRank fr : plugin.fishRankData.values()) {
            randomWeight -= fr.getWeight();
            if (randomWeight < 0) {
                DInventory inv = fr.getInventory();
                if (inv.getAllPageItems().isEmpty()) {
                    return null;
                }
                return applyInfo(inv.getAllPageItems().get((int) (Math.random() * inv.getAllPageItems().size())).clone(), fr);
            }
        }
        return null;
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
}
