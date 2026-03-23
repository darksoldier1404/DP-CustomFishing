package com.darksoldier1404.dpcf;

import com.darksoldier1404.dpcf.commands.DPCFCommand;
import com.darksoldier1404.dpcf.events.DPCFEvent;
import com.darksoldier1404.dpcf.functions.ContestManager;
import com.darksoldier1404.dpcf.functions.DPCFFunction;
import com.darksoldier1404.dpcf.obj.FUser;
import com.darksoldier1404.dpcf.obj.FishRank;
import com.darksoldier1404.dppc.data.DPlugin;
import com.darksoldier1404.dppc.data.DataContainer;
import com.darksoldier1404.dppc.data.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomFishing extends DPlugin {
    public static CustomFishing plugin;
    public static DataContainer<String, FishRank> fishRankData;
    public static DataContainer<UUID, FUser> udata;
    public static int pricePerLength;
    public static List<String> allowedWorlds = new ArrayList<>();
    public static List<Map.Entry<UUID, FUser>> sort = new ArrayList<>();

    public CustomFishing() {
        super(false);
        plugin = this;
        init();
    }

    @Override
    public void onLoad() {
        fishRankData = loadDataContainer(new DataContainer<>(this, DataType.CUSTOM, "fishranks"), FishRank.class);
        udata = loadDataContainer(new DataContainer<>(this, DataType.CUSTOM, "users"), FUser.class);
        pricePerLength = config.getInt("Settings.PricePerLength", 10);
        DPCFFunction.initPlaceholder();
    }

    @Override
    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new DPCFEvent(), plugin);
        DPCFCommand.init();
        DPCFFunction.init();
        DPCFFunction.initTask();
        ContestManager.init();
    }

    @Override
    public void onDisable() {
        ContestManager.clearAll();
        saveAllData();
    }
}
