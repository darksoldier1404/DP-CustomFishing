package com.darksoldier1404.dpcf.obj;

import com.darksoldier1404.dppc.api.inventory.DInventory;
import com.darksoldier1404.dppc.data.DataCargo;
import org.bukkit.configuration.file.YamlConfiguration;

import static com.darksoldier1404.dpcf.CustomFishing.plugin;

public class FishRank implements DataCargo {
    private String name;
    private int defaultPrice;
    private int minLength;
    private int maxLength;
    private int weight;
    private DInventory inventory;

    public FishRank() {
    }

    public FishRank(String name, int defaultPrice, int minLength, int maxLength, int weight, DInventory inventory) {
        this.name = name;
        this.defaultPrice = defaultPrice;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.weight = weight;
        this.inventory = inventory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(int defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public DInventory getInventory() {
        return inventory;
    }

    public void setInventory(DInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public YamlConfiguration serialize() {
        YamlConfiguration data = new YamlConfiguration();
        data.set("name", name);
        data.set("defaultPrice", defaultPrice);
        data.set("minLength", minLength);
        data.set("maxLength", maxLength);
        data.set("weight", weight);
        inventory.serialize(data);
        return data;
    }

    @Override
    public FishRank deserialize(YamlConfiguration data) {
        this.name = data.getString("name");
        this.defaultPrice = data.getInt("defaultPrice");
        this.minLength = data.getInt("minLength");
        this.maxLength = data.getInt("maxLength");
        this.weight = data.getInt("weight");
        this.inventory = new DInventory("Fish Rank Inventory", 54, true, true, plugin);
        this.inventory.setPages(99);
        inventory.deserialize(data);
        return this;
    }
}
