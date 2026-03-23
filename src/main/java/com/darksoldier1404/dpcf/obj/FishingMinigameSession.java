package com.darksoldier1404.dpcf.obj;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

/**
 * 낚시 미니게임 세션을 관리하는 클래스.
 * 플레이어별로 한 개의 세션이 생성되며, 클릭 액션 시퀀스와 타이머를 포함한다.
 * actions: true = 좌클릭, false = 우클릭
 */
public class FishingMinigameSession {

    /** 미니게임 제한 시간 (밀리초) */
    public static final long DURATION_MS = 5000L;

    private final UUID playerUUID;
    private final ItemStack caughtFish;
    private final List<Boolean> actions; // true = 좌클릭, false = 우클릭
    private int currentIndex;
    private BukkitTask timerTask;
    private final long expireTime;
    /** 낚시 이벤트를 유발한 우클릭을 미니게임 입력으로 처리하지 않도록 스킵하는 플래그 */

    public FishingMinigameSession(UUID playerUUID, ItemStack caughtFish, List<Boolean> actions) {
        this.playerUUID = playerUUID;
        this.caughtFish = caughtFish;
        this.actions = actions;
        this.currentIndex = 0;
        this.expireTime = System.currentTimeMillis() + DURATION_MS;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public ItemStack getCaughtFish() {
        return caughtFish;
    }

    public List<Boolean> getActions() {
        return actions;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void nextAction() {
        currentIndex++;
    }

    public boolean isComplete() {
        return currentIndex >= actions.size();
    }

    public long getRemainingMs() {
        return Math.max(0, expireTime - System.currentTimeMillis());
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    public BukkitTask getTimerTask() {
        return timerTask;
    }

    public void setTimerTask(BukkitTask timerTask) {
        this.timerTask = timerTask;
    }

    public Boolean getCurrentAction() {
        return actions.get(currentIndex);
    }
}

