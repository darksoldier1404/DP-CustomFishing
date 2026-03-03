package com.darksoldier1404.dpcf;

import com.darksoldier1404.dpcf.commands.DPCFCommand;
import com.darksoldier1404.dpcf.events.DPCFEvent;
import com.darksoldier1404.dpcf.functions.DPCFFunction;
import com.darksoldier1404.dpcf.obj.FishRank;
import com.darksoldier1404.dppc.data.DPlugin;
import com.darksoldier1404.dppc.data.DataContainer;
import com.darksoldier1404.dppc.data.DataType;

import java.util.HashSet;
import java.util.Set;

public class CustomFishing extends DPlugin {
    public static CustomFishing plugin;
    public static DataContainer<String, FishRank> fishRankData;
    public static int pricePerLength;
    public static Set<String> allowedWorlds = new HashSet<>();

    public CustomFishing() {
        super(false);
        plugin = this;
        init();
    }

    @Override
    public void onLoad() {
        fishRankData = loadDataContainer(new DataContainer<>(this, DataType.CUSTOM, "fishranks"), FishRank.class);
        pricePerLength = config.getInt("Settings.PricePerLength", 10);
    }

    @Override
    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(new DPCFEvent(), plugin);
        DPCFCommand.init();
        DPCFFunction.init();
    }

    @Override
    public void onDisable() {
        saveAllData();
    }
}
