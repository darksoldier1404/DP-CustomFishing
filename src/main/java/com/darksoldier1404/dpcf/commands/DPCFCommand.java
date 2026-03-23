package com.darksoldier1404.dpcf.commands;

import com.darksoldier1404.dpcf.enums.ContestType;
import com.darksoldier1404.dpcf.functions.ContestManager;
import com.darksoldier1404.dpcf.functions.DPCFFunction;
import com.darksoldier1404.dppc.builder.command.ArgumentIndex;
import com.darksoldier1404.dppc.builder.command.ArgumentType;
import com.darksoldier1404.dppc.builder.command.CommandBuilder;

import static com.darksoldier1404.dpcf.CustomFishing.plugin;

public class DPCFCommand {
    private static final CommandBuilder builder = new CommandBuilder(plugin);

    public static void init() {
        builder.beginSubCommand("create", "/dpcf create <rank> - create a new rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING)
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    DPCFFunction.createRank(player, rank);
                    return true;
                });
        builder.beginSubCommand("items", "/dpcf items <rank> - edit items for a rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING, plugin.fishRankData.keySet())
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    DPCFFunction.editItems(player, rank);
                    return true;
                });

        builder.beginSubCommand("price", "/dpcf price <rank> <price> - set price for a rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING, plugin.fishRankData.keySet())
                .withArgument(ArgumentIndex.ARG_1, ArgumentType.INTEGER)
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    int price = args.getInteger(ArgumentIndex.ARG_1);
                    DPCFFunction.setPrice(player, rank, price);
                    return true;
                });

        builder.beginSubCommand("length", "/dpcf length <rank> <minLength> <maxLength> - set length range for a rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING, plugin.fishRankData.keySet())
                .withArgument(ArgumentIndex.ARG_1, ArgumentType.INTEGER)
                .withArgument(ArgumentIndex.ARG_2, ArgumentType.INTEGER)
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    int minLength = args.getInteger(ArgumentIndex.ARG_1);
                    int maxLength = args.getInteger(ArgumentIndex.ARG_2);
                    DPCFFunction.setLengthRange(player, rank, minLength, maxLength);
                    return true;
                });

        builder.beginSubCommand("weight", "/dpcf weight <rank> <weight> - set weight for a rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING, plugin.fishRankData.keySet())
                .withArgument(ArgumentIndex.ARG_1, ArgumentType.INTEGER)
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    int weight = args.getInteger(ArgumentIndex.ARG_1);
                    DPCFFunction.setWeight(player, rank, weight);
                    return true;
                });

        builder.beginSubCommand("delete", "/dpcf delete <rank> - delete a rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING, plugin.fishRankData.keySet())
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    DPCFFunction.deleteRank(player, rank);
                    return true;
                });

        builder.beginSubCommand("setpriceperlength", "/dpcf setpriceperlength <priceperlength> - set price per length for all rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.INTEGER)
                .executesPlayer((player, args) -> {
                    int lengthPerPrice = args.getInteger(ArgumentIndex.ARG_0);
                    DPCFFunction.setLengthPerPrice(player, lengthPerPrice);
                    return true;
                });

        builder.beginSubCommand("list", "/dpcf list - list all ranks")
                .withPermission("dpcf.admin")
                .executesPlayer((player, args) -> {
                    DPCFFunction.listRanks(player);
                    return true;
                });

        builder.beginSubCommand("opensell", "/dpcf opensell - open the sell GUI")
                .withPermission("dpcf.sell")
                .executesPlayer((player, args) -> {
                    DPCFFunction.openShop(player);
                    return true;
                });

        builder.beginSubCommand("reload", "/dpcf reload - reload the plugin")
                .withPermission("dpcf.admin")
                .executesPlayer((player, args) -> {
                    plugin.reload();
                    DPCFFunction.init();
                    player.sendMessage("§aCustomFishing plugin reloaded.");
                    return true;
                });

        // ── 대회 시스템 ────────────────────────────────────────────────────────
        // /dpcf contest create <name> <length|mostcatch> <HH:mm>
        builder.beginSubCommand("contestcreate",
                        "/dpcf contestcreate <name> <length|mostcatch> <HH:mm> - schedule a contest")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING)
                .withArgument(ArgumentIndex.ARG_1, ArgumentType.STRING)
                .withArgument(ArgumentIndex.ARG_2, ArgumentType.STRING)
                .executesPlayer((player, args) -> {
                    String name = args.getString(ArgumentIndex.ARG_0);
                    String typeStr = args.getString(ArgumentIndex.ARG_1);
                    String time = args.getString(ArgumentIndex.ARG_2);
                    if (name == null || typeStr == null || time == null) {
                        player.sendMessage(plugin.prefix + "§c사용법: /dpcf contestcreate <이름> <length|mostcatch> <HH:mm>");
                        return true;
                    }
                    ContestType type = ContestType.fromString(typeStr);
                    if (type == null) {
                        player.sendMessage(plugin.prefix + "§c대회 종류는 §elength §c또는 §emostcatch §c이어야 합니다.");
                        return true;
                    }
                    ContestManager.scheduleContest(player, name, type, time);
                    return true;
                });

        // /dpcf contest start <length|mostcatch>
        builder.beginSubCommand("conteststart",
                        "/dpcf conteststart <length|mostcatch> - force start a contest")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING)
                .executesPlayer((player, args) -> {
                    String typeStr = args.getString(ArgumentIndex.ARG_0);
                    if (typeStr == null) {
                        player.sendMessage(plugin.prefix + "§c사용법: /dpcf conteststart <length|mostcatch>");
                        return true;
                    }
                    ContestType type = ContestType.fromString(typeStr);
                    if (type == null) {
                        player.sendMessage(plugin.prefix + "§c대회 종류는 §elength §c또는 §emostcatch §c이어야 합니다.");
                        return true;
                    }
                    if (ContestManager.isContestActive(type)) {
                        player.sendMessage(plugin.prefix + "§c이미 진행 중인 §f" + type.getDisplayName() + " §c대회가 있습니다.");
                        return true;
                    }
                    ContestManager.startContest(type.getDisplayName() + " 대회", type);
                    player.sendMessage(plugin.prefix + "§a대회를 즉시 시작했습니다.");
                    return true;
                });

        // /dpcf contest stop <length|mostcatch>
        builder.beginSubCommand("conteststop",
                        "/dpcf conteststop <length|mostcatch> - force stop a contest")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING)
                .executesPlayer((player, args) -> {
                    String typeStr = args.getString(ArgumentIndex.ARG_0);
                    if (typeStr == null) {
                        player.sendMessage(plugin.prefix + "§c사용법: /dpcf conteststop <length|mostcatch>");
                        return true;
                    }
                    ContestType type = ContestType.fromString(typeStr);
                    if (type == null) {
                        player.sendMessage(plugin.prefix + "§c대회 종류는 §elength §c또는 §emostcatch §c이어야 합니다.");
                        return true;
                    }
                    ContestManager.stopContest(player, type);
                    return true;
                });

        builder.build("dpcf");
    }
}
