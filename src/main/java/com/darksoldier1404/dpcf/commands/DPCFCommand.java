package com.darksoldier1404.dpcf.commands;

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
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING)
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    DPCFFunction.editItems(player, rank);
                    return true;
                });
        builder.beginSubCommand("length", "/dpcf length <rank> <minLength> <maxLength> - set length range for a rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING)
                .withArgument(ArgumentIndex.ARG_1, ArgumentType.INTEGER)
                .withArgument(ArgumentIndex.ARG_2, ArgumentType.INTEGER)
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    int minLength = args.getInteger(ArgumentIndex.ARG_1);
                    int maxLength = args.getInteger(ArgumentIndex.ARG_2);
                    DPCFFunction.setLengthRange(player, rank, minLength, maxLength);
                    return true;
                });
        builder.beginSubCommand("delete", "/dpcf delete <rank> - delete a rank")
                .withPermission("dpcf.admin")
                .withArgument(ArgumentIndex.ARG_0, ArgumentType.STRING)
                .executesPlayer((player, args) -> {
                    String rank = args.getString(ArgumentIndex.ARG_0);
                    DPCFFunction.deleteRank(player, rank);
                    return true;
                });
        builder.beginSubCommand("list", "/dpcf list - list all ranks")
                .withPermission("dpcf.admin")
                .executesPlayer((player, args) -> {
                    DPCFFunction.listRanks(player);
                    return true;
                });

        builder.beginSubCommand("opensell", "/dpcf opensell - open the sell GUI")
                .executesPlayer((player, args) -> {
                    DPCFFunction.openShop(player);
                    return true;
                });

        builder.beginSubCommand("reload", "/dpcf reload - reload the plugin")
                .withPermission("dpcf.admin")
                .executesPlayer((player, args) -> {
                    DPCFFunction.init();
                    player.sendMessage("§aCustomFishing plugin reloaded.");
                    return true;
                });

        builder.build("dpcf");
    }
}
