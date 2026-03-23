package com.darksoldier1404.dpcf.obj;

import com.darksoldier1404.dpcf.enums.ContestType;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 진행 중인 낚시 대회 세션을 담는 객체.
 */
public class ContestSession {

    private final String name;
    private final ContestType type;
    private final long startTimeMs;
    private final long durationMs;
    private final Map<UUID, ContestEntry> entries;
    private BossBar bossBar;
    private BukkitTask timerTask;

    public ContestSession(String name, ContestType type, long durationMs) {
        this.name = name;
        this.type = type;
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = durationMs;
        this.entries = new LinkedHashMap<>();
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getName() { return name; }
    public ContestType getType() { return type; }
    public Map<UUID, ContestEntry> getEntries() { return entries; }

    public BossBar getBossBar() { return bossBar; }
    public void setBossBar(BossBar bossBar) { this.bossBar = bossBar; }

    public BukkitTask getTimerTask() { return timerTask; }
    public void setTimerTask(BukkitTask timerTask) { this.timerTask = timerTask; }

    public long getDurationMs() { return durationMs; }

    // ── 시간 계산 ──────────────────────────────────────────────────────────

    public long getRemainingMs() {
        return Math.max(0, startTimeMs + durationMs - System.currentTimeMillis());
    }

    public boolean isExpired() {
        return getRemainingMs() <= 0;
    }

    // ── 참가자 관리 ────────────────────────────────────────────────────────

    /**
     * 아직 등록되지 않은 플레이어를 참가자로 추가한다.
     */
    public void addParticipant(UUID uuid, String playerName) {
        entries.putIfAbsent(uuid, new ContestEntry(playerName));
    }

    /**
     * value 내림차순으로 정렬된 순위 목록을 반환한다.
     */
    public List<Map.Entry<UUID, ContestEntry>> getSortedRankings() {
        return entries.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().getValue(), a.getValue().getValue()))
                .collect(Collectors.toList());
    }
}

