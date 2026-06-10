package com.darksoldier1404.dpcf.obj;

import com.darksoldier1404.dpcf.enums.ContestType;
import com.darksoldier1404.dppc.data.DataCargo;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * 반복 대회 등록 정보를 디스크에 직렬화/역직렬화하기 위한 DataCargo 구현체.
 * DataContainer&lt;String, ScheduledContestData&gt; 의 값으로 사용되며,
 * 키(key)는 대회 이름(name)과 동일하다.
 */
public class ScheduledContestData implements DataCargo {

    private String name;
    /** ContestType.name() 문자열 — "LENGTH" 또는 "MOSTCATCH" */
    private String type;
    /** HH:mm 포맷의 시작 시각 */
    private String time;

    public ScheduledContestData() {}

    public ScheduledContestData(String name, ContestType type, String time) {
        this.name = name;
        this.type = type.name();
        this.time = time;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public String getTime() { return time; }

    public ContestType getContestType() {
        return ContestType.valueOf(type);
    }

    @Override
    public YamlConfiguration serialize() {
        YamlConfiguration data = new YamlConfiguration();
        data.set("name", name);
        data.set("type", type);
        data.set("time", time);
        return data;
    }

    @Override
    public ScheduledContestData deserialize(YamlConfiguration data) {
        this.name = data.getString("name", "");
        this.type = data.getString("type", ContestType.LENGTH.name());
        this.time = data.getString("time", "00:00");
        return this;
    }
}

