package com.darksoldier1404.dpcf.obj;

/**
 * 낚시 대회 참가자의 개인 진행 데이터.
 * - LENGTH 대회: value = 잡은 물고기 중 최대 길이(cm)
 * - MOSTCATCH 대회: value = 낚은 물고기 총 마리 수
 */
public class ContestEntry {
    private final String playerName;
    private int value;

    public ContestEntry(String playerName) {
        this.playerName = playerName;
        this.value = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /** MOSTCATCH 용: 카운트를 1 증가시킨다. */
    public void increment() {
        this.value++;
    }
}

