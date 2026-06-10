package com.darksoldier1404.dpcf.enums;

public enum ContestType {
    LENGTH("가장 긴 물고기 낚기"),
    MOSTCATCH("가장 많이 잡기");

    private final String displayName;

    ContestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * config.yml 내 키 이름으로 반환 (예: "Length", "MostCatch")
     */
    public String getConfigKey() {
        return this == LENGTH ? "Length" : "MostCatch";
    }

    /**
     * "length" / "mostcatch" 문자열로부터 변환. null 안전.
     */
    public static ContestType fromString(String s) {
        if (s == null) return null;
        switch (s.toLowerCase()) {
            case "length":    return LENGTH;
            case "mostcatch": return MOSTCATCH;
            default:          return null;
        }
    }
}

