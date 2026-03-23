package com.darksoldier1404.dpcf.functions;

import com.darksoldier1404.dpcf.events.custom.FishingSuccessEvent;
import com.darksoldier1404.dpcf.obj.FUser;
import com.darksoldier1404.dpcf.obj.FishRank;
import com.darksoldier1404.dpcf.obj.FishingMinigameSession;
import com.darksoldier1404.dppc.api.essentials.MoneyAPI;
import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.api.placeholder.PlaceholderBuilder;
import com.darksoldier1404.dppc.utils.NBT;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.darksoldier1404.dpcf.CustomFishing.plugin;

public class DPCFFunction {

    private static final Map<UUID, FishingMinigameSession> activeSessions = new HashMap<>();

    /**
     * 낚시 미니게임을 시작한다.
     * 3~5개의 좌클릭/우클릭 액션을 랜덤 생성하고, UI를 표시하며 5초 타이머를 시작한다.
     *
     * @param player 낚시한 플레이어
     * @param fish   잡힌 물고기 아이템
     */
    public static void startMinigame(Player player, ItemStack fish) {
        // 3~5개 랜덤 액션 생성 (true=좌클릭, false=우클릭)
        int count = 3 + ThreadLocalRandom.current().nextInt(3);
        List<Boolean> actions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            actions.add(ThreadLocalRandom.current().nextBoolean());
        }

        FishingMinigameSession session = new FishingMinigameSession(player.getUniqueId(), fish, actions);
        activeSessions.put(player.getUniqueId(), session);

        // 초기 UI 표시
        showMinigameUI(player, session);

        // 매 틱(1/20초)마다 액션바 갱신 및 타임아웃 체크
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            FishingMinigameSession s = activeSessions.get(player.getUniqueId());
            if (s == null) return;
            if (s.isExpired()) {
                endMinigame(player, false);
                return;
            }
            updateActionBar(player, s);
        }, 1L, 1L);

        session.setTimerTask(task);
    }

    /**
     * 플레이어의 클릭 입력을 처리한다.
     * 올바른 클릭이면 다음 액션으로, 틀리면 실패 처리한다.
     *
     * @param player      클릭한 플레이어
     * @param isLeftClick 좌클릭 여부 (false = 우클릭)
     */
    public static void processClick(Player player, boolean isLeftClick) {
        FishingMinigameSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        if (session.isExpired()) {
            endMinigame(player, false);
            return;
        }

        boolean required = session.getCurrentAction(); // true=좌, false=우
        if (required == isLeftClick) {
            // 정답
            session.nextAction();
            if (session.isComplete()) {
                endMinigame(player, true);
            } else {
                showMinigameUI(player, session);
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1f, 1.5f);
            }
        } else {
            // 오답
            endMinigame(player, false);
        }
    }

    /**
     * 미니게임을 종료한다. 성공 시 물고기 지급, 실패 시 물고기 소멸.
     *
     * @param player  플레이어
     * @param success 성공 여부
     */
    public static void endMinigame(Player player, boolean success) {
        FishingMinigameSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) return;

        if (session.getTimerTask() != null) {
            session.getTimerTask().cancel();
        }

        // Title / ActionBar 초기화
        player.sendTitle("", "", 0, 1, 5);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));

        if (success) {
            ItemStack fish = session.getCaughtFish();
            player.getInventory().addItem(fish);
            String rank = NBT.getStringTag(fish, "dpcf_rank");
            player.sendMessage(plugin.prefix + "§a물고기를 낚았습니다! §e"
                    + fish.getItemMeta().getDisplayName() + " §a(§e" + rank + "§a)");
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1f, 2f);
            player.sendTitle("§a§l낚시 성공!", "§f물고기를 낚아 올렸습니다!", 5, 40, 15);
            Bukkit.getPluginManager().callEvent(new FishingSuccessEvent(player, fish));
        } else {
            player.sendMessage(plugin.prefix + "§c물고기가 도망쳤습니다!");
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 1f, 0.8f);
            player.sendTitle("§c§l낚시 실패!", "§7물고기가 도망쳤습니다...", 5, 40, 15);
        }
    }

    /**
     * 플레이어가 현재 미니게임 중인지 반환한다.
     */
    public static boolean isInMinigame(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * 서버 종료 또는 플러그인 비활성화 시 모든 세션을 정리한다.
     */
    public static void clearAllSessions() {
        for (FishingMinigameSession session : activeSessions.values()) {
            if (session.getTimerTask() != null) {
                session.getTimerTask().cancel();
            }
        }
        activeSessions.clear();
    }

    // ─── 미니게임 UI ────────────────────────────────────────────────────────────

    /**
     * 현재 단계의 Title(요구 액션)과 Subtitle(진행 상황)을 표시한다.
     */
    private static void showMinigameUI(Player player, FishingMinigameSession session) {
        boolean isLeft = session.getCurrentAction();
        String actionTitle = isLeft ? "§e§l◀  좌클릭 !" : "§e§l▶  우클릭 !";
        String progress = buildProgressSubtitle(session);
        player.sendTitle(actionTitle, progress, 0, 60, 5);
        updateActionBar(player, session);
    }

    /**
     * 액션 시퀀스 진행 상황을 색상으로 표시하는 문자열 생성.
     * §a = 완료(초록), §e§l = 현재(노란 굵기), §7 = 미도달(회색)
     */
    private static String buildProgressSubtitle(FishingMinigameSession session) {
        List<Boolean> actions = session.getActions();
        int current = session.getCurrentIndex();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            if (i > 0) sb.append("  ");
            String symbol = actions.get(i) ? plugin.getConfig().getString("Settings.symbol.left") : plugin.getConfig().getString("Settings.symbol.right");
            if (i < current) {
                sb.append("§a").append(symbol);        // 완료 - 초록
            } else if (i == current) {
                sb.append("§e").append(symbol);     // 현재 - 노란 굵기
            } else {
                sb.append("§7").append(symbol);        // 대기 - 회색
            }
        }
        return sb.toString();
    }

    /**
     * 액션바에 남은 시간을 게이지 바 형태로 표시한다.
     * 색상: 초록(>60%) → 노란(>30%) → 빨간(≤30%)
     */
    private static void updateActionBar(Player player, FishingMinigameSession session) {
        long remaining = session.getRemainingMs();
        double ratio = remaining / (double) FishingMinigameSession.DURATION_MS;
        int filled = (int) Math.ceil(ratio * 10);

        String color = filled > 6 ? "§a" : (filled > 3 ? "§e" : "§c");
        StringBuilder bar = new StringBuilder("§8[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? color + "█" : "§8█");
        }
        bar.append("§8]  ").append(color).append(String.format("%.1f", remaining / 1000.0)).append("§f초");

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(bar.toString()));
    }

    // ─── 기존 기능 ────────────────────────────────────────────────────────────

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
        sm.setDisplayName("§a물고기 판매 &7(쉬프트-우클릭으로 모두 팔기)");
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

    public static void sellAllItems(Player p) {
        Inventory inv = p.getInventory();
        int totalPrice = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && !item.getType().isAir() && NBT.hasTagKey(item, "dpcf_price")) {
                totalPrice += NBT.getIntegerTag(item, "dpcf_price") * item.getAmount();
                inv.setItem(i, null);
            }
        }
        if (totalPrice <= 0) {
            p.sendMessage(plugin.prefix + "판매할 물고기가 없습니다.");
            return;
        }
        MoneyAPI.addMoney(p, totalPrice);
        p.sendMessage(plugin.prefix + "물고기를 판매하여 §e" + totalPrice + "§a원을 얻었습니다.");
    }

    public static void initPlayer(Player p) {
        if (!plugin.udata.containsKey(p.getUniqueId())) {
            plugin.udata.put(p.getUniqueId(), new FUser(0, p.getName()));
        } else {
            plugin.udata.get(p.getUniqueId()).setName(p.getName());
        }
    }

    public static void savePlayer(Player p) {
        if (plugin.udata.containsKey(p.getUniqueId())) {
            plugin.udata.save(p.getUniqueId());
        }
    }

    public static void initPlaceholder() {
        new PlaceholderBuilder.Builder(plugin)
                .identifier("dpcf")
                .author("DEAD_POOLIO_")
                .version("1.0.0")
                .onRequest((player, context) -> {
                    if (context.equalsIgnoreCase("total_fishing")) {
                        return String.valueOf(plugin.udata.get(player.getUniqueId()).getTotalFishing());
                    }
                    // top ten
                    if (context.contains("top_fishing_")) {
                        String[] split = context.split("top_fishing_");
                        if (split.length == 2) {
                            try {
                                int rank = Integer.parseInt(split[1]);
                                return getTopPlayTime(rank);
                            } catch (NumberFormatException e) {
                                return "0";
                            }
                        }
                    }
                    if (context.contains("top_name_")) {
                        String[] split = context.split("top_name_");
                        if (split.length == 2) {
                            try {
                                int rank = Integer.parseInt(split[1]);
                                return getTopPlayUsername(rank);
                            } catch (NumberFormatException e) {
                                return "none";
                            }
                        }
                    }
                    return "0";
                })
                .build();
    }

    public static void initTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, DPCFFunction::sort, 0L, 100L);
    }

    public static void sort() {
        plugin.sort = plugin.udata.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().getTotalFishing(), e1.getValue().getTotalFishing()))
                .toList();
    }

    public static String getTopPlayUsername(int rank) {
        plugin.sort = plugin.sort.stream()
                .filter(entry -> !Bukkit.getOfflinePlayer(entry.getKey()).isOp())
                .toList();
        if (rank <= 0 || rank > plugin.sort.size()) {
            return "none";
        }
        return plugin.sort.get(rank - 1).getValue().getName();
    }

    public static String getTopPlayTime(int rank) {
        plugin.sort = plugin.sort.stream()
                .filter(entry -> !Bukkit.getOfflinePlayer(entry.getKey()).isOp())
                .toList();
        if (rank <= 0 || rank > plugin.sort.size()) {
            return "0";
        }
        String time = String.valueOf(plugin.sort.get(rank - 1).getValue().getTotalFishing());
        return time;
    }
}
