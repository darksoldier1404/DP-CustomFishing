package com.darksoldier1404.dpcf.obj;

import com.darksoldier1404.dppc.data.DataCargo;
import org.bukkit.configuration.file.YamlConfiguration;

public class FUser implements DataCargo {
    private int totalFishing;
    private String name;

    public FUser() {
    }

    public FUser(int totalFishing, String name) {
        this.totalFishing = totalFishing;
        this.name = name;
    }

    public int getTotalFishing() {
        return totalFishing;
    }

    public void setTotalFishing(int totalFishing) {
        this.totalFishing = totalFishing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public YamlConfiguration serialize() {
        YamlConfiguration data = new YamlConfiguration();
        data.set("totalFishing", totalFishing);
        data.set("name", name);
        return data;
    }

    @Override
    public FUser deserialize(YamlConfiguration data) {
        this.totalFishing = data.getInt("totalFishing", 0);
        this.name = data.getString("name", "none");
        return this;
    }
}
