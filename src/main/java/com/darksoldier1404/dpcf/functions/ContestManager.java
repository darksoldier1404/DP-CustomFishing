package com.darksoldier1404.dpcf.functions;

import com.darksoldier1404.dpcf.enums.ContestType;
import com.darksoldier1404.dpcf.obj.ContestEntry;
import com.darksoldier1404.dpcf.obj.ContestSession;
import com.darksoldier1404.dppc.utils.NBT;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.darksoldier1404.dpcf.CustomFishing.plugin;

/**
 * 낚시 대회 시스템 전체를 관리하는 매니저.
 *
 * <ul>
 *   <li>{@link #init()} - 플러그인 활성화 시 예약 스케줄러 시작</li>
 *   <li>{@link #clearAll()} - 플러그인 비활성화 시 모든 세션/태스크 정리</li>
 *   <li>{@link #scheduleContest} - 지정한 시각(HH:mm)에 대회 자동 시작 예약</li>
 *   <li>{@link #startContest} - 즉시 대회 시작</li>
 *   <li>{@link #stopContest} - 강제 종료 (보상 없음)</li>
 *   <li>{@link #onFishCaught} - 물고기 낚시 성공 시 대회 점수 갱신</li>
 * </ul>
 */
public class ContestManager {

    /** 타입별 현재 진행 중인 세션 */
    private static final Map<ContestType, ContestSession> activeSessions = new EnumMap<>(ContestType.class);

    /** 예약된 대회 목록 */
    private static final List<ScheduledContest> scheduledContests = new ArrayList<>();

    /** 예약 체크 스케줄러 태스크 */
    private static BukkitTask scheduleTask;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ═══════════════════════════════════════════════════════════════════════
    // 내부 클래스: 예약 대회
    // ═══════════════════════════════════════════════════════════════════════

    private static class ScheduledContest {
        final String name;
        final ContestType type;
        final LocalTime time;
        /** 해당 분(minute) 안에 이미 시작됐으면 true — 다음 분이 되면 false 로 초기화 */
        boolean triggered = false;

        ScheduledContest(String name, ContestType type, LocalTime time) {
            this.name = name;
            this.type = type;
            this.time = time;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 초기화 / 정리
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 플러그인 활성화 시 호출: 매 20틱(1초)마다 예약 대회 시간을 확인한다.
     */
    public static void init() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
        }
        scheduleTask = Bukkit.getScheduler().runTaskTimer(plugin, ContestManager::checkScheduled, 20L, 20L);
    }

    /**
     * 플러그인 비활성화 시 호출: 모든 세션과 태스크를 정리한다.
     */
    public static void clearAll() {
        if (scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        for (ContestSession session : activeSessions.values()) {
            if (session.getTimerTask() != null) session.getTimerTask().cancel();
            if (session.getBossBar() != null)   session.getBossBar().removeAll();
        }
        activeSessions.clear();
        scheduledContests.clear();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 예약 체크
    // ═══════════════════════════════════════════════════════════════════════

    private static void checkScheduled() {
        LocalTime now = LocalTime.now();
        for (ScheduledContest sc : scheduledContests) {
            boolean isTime = now.getHour() == sc.time.getHour()
                    && now.getMinute() == sc.time.getMinute();
            if (isTime && !sc.triggered) {
                sc.triggered = true;
                startContest(sc.name, sc.type);
            }
            // 분이 지나면 트리거 초기화 (다음날 재사용)
            if (!isTime && sc.triggered) {
                sc.triggered = false;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 공개 API
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 지정한 시각(HH:mm)에 대회를 자동으로 시작하도록 예약한다.
     *
     * @param player   명령 실행 플레이어 (피드백 수신용)
     * @param name     대회 이름
     * @param type     대회 종류
     * @param timeStr  시작 시각 (예: "14:30")
     */
    public static void scheduleContest(Player player, String name, ContestType type, String timeStr) {
        LocalTime time;
        try {
            time = LocalTime.parse(timeStr, TIME_FMT);
        } catch (Exception e) {
            player.sendMessage(plugin.prefix + "§c시간 형식이 올바르지 않습니다. §eHH:mm §c형식으로 입력해주세요. (예: §e14:30§c)");
            return;
        }
        scheduledContests.add(new ScheduledContest(name, type, time));
        player.sendMessage(plugin.prefix + "§a대회 예약 완료! §e" + time.format(TIME_FMT)
                + "§a에 §f'" + name + "' §a(" + type.getDisplayName() + ") §a대회가 시작됩니다.");
        broadcast(plugin.prefix + "§6§l낚시 대회 §f'" + name + "'§6이(가) §e"
                + time.format(TIME_FMT) + "§6에 시작 예정입니다! §a낚시를 준비하세요!");
    }

    /**
     * 대회를 즉시 시작한다.
     *
     * @param name 대회 이름
     * @param type 대회 종류
     */
    public static void startContest(String name, ContestType type) {
        if (activeSessions.containsKey(type)) {
            broadcast(plugin.prefix + "§c이미 §f" + type.getDisplayName() + " §c대회가 진행 중입니다.");
            return;
        }

        int durationMinutes = plugin.getConfig().getInt("Contest.Duration", 30);
        long durationMs = durationMinutes * 60_000L;

        ContestSession session = new ContestSession(name, type, durationMs);
        activeSessions.put(type, session);

        // 보스바 생성
        BossBar bossBar = Bukkit.createBossBar(
                buildBossBarTitle(name, durationMs / 1000),
                BarColor.YELLOW,
                BarStyle.SEGMENTED_10
        );
        bossBar.setProgress(1.0);
        session.setBossBar(bossBar);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        // 전체 방송
        broadcast("§7----------------------------------------------------");
        broadcast(" §6§l낚시 대회 시작!");
        broadcast(" §f대회명 §7: §e" + name);
        broadcast(" §f종류   §7: §a" + type.getDisplayName());
        broadcast(" §f시간   §7: §e" + durationMinutes + "분");
        broadcast("§7----------------------------------------------------");

        // 타이머 태스크 (20틱 = 1초)
        BukkitTask timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ContestSession s = activeSessions.get(type);
            if (s == null) return;
            long remaining = s.getRemainingMs();
            if (remaining <= 0) {
                endContest(s, true);
                return;
            }
            double progress = remaining / (double) durationMs;
            BossBar bar = s.getBossBar();
            if (bar != null) {
                bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                bar.setTitle(buildBossBarTitle(s.getName(), remaining / 1000));
            }
        }, 20L, 20L);
        session.setTimerTask(timerTask);
    }

    /**
     * 진행 중인 대회를 강제 종료한다. 보상은 지급되지 않는다.
     *
     * @param player 명령 실행 플레이어 (피드백 수신용)
     * @param type   대회 종류
     */
    public static void stopContest(Player player, ContestType type) {
        ContestSession session = activeSessions.get(type);
        if (session == null) {
            player.sendMessage(plugin.prefix + "§c진행 중인 §f" + type.getDisplayName() + " §c대회가 없습니다.");
            return;
        }
        endContest(session, false);
        player.sendMessage(plugin.prefix + "§a대회를 강제 종료했습니다.");
        broadcast(plugin.prefix + "§c§l낚시 대회 §f'" + session.getName() + "'§c이(가) 강제 종료되었습니다.");
    }

    /**
     * 물고기를 낚았을 때 대회 점수를 갱신한다.
     * FishingSuccessEvent 에서 호출된다.
     *
     * @param player 낚시한 플레이어
     * @param fish   낚은 물고기 아이템 (dpcf_length NBT 태그 포함)
     */
    public static void onFishCaught(Player player, ItemStack fish) {
        if (activeSessions.isEmpty()) return;
        for (ContestSession session : activeSessions.values()) {
            session.addParticipant(player.getUniqueId(), player.getName());
            ContestEntry entry = session.getEntries().get(player.getUniqueId());
            if (session.getType() == ContestType.LENGTH) {
                int length = NBT.getIntegerTag(fish, "dpcf_length");
                if (length > entry.getValue()) {
                    entry.setValue(length);
                }
            } else if (session.getType() == ContestType.MOSTCATCH) {
                entry.increment();
            }
            // 참가 시점에 보스바에 추가 (뒤늦게 접속한 경우 대비)
            BossBar bar = session.getBossBar();
            if (bar != null && !bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
        }
    }

    /**
     * 플레이어 접속 시 진행 중인 모든 대회 보스바에 추가한다.
     */
    public static void onPlayerJoin(Player player) {
        for (ContestSession session : activeSessions.values()) {
            if (session.getBossBar() != null) {
                session.getBossBar().addPlayer(player);
            }
        }
    }

    /**
     * 플레이어 퇴장 시 진행 중인 모든 대회 보스바에서 제거한다.
     */
    public static void onPlayerQuit(Player player) {
        for (ContestSession session : activeSessions.values()) {
            if (session.getBossBar() != null) {
                session.getBossBar().removePlayer(player);
            }
        }
    }

    /**
     * 해당 타입의 대회가 현재 진행 중인지 반환한다.
     */
    public static boolean isContestActive(ContestType type) {
        return activeSessions.containsKey(type);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 대회 종료 처리
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 대회를 종료하고, giveRewards 가 true 이면 보상을 지급하고 결과를 방송한다.
     */
    private static void endContest(ContestSession session, boolean giveRewards) {
        activeSessions.remove(session.getType());

        if (session.getTimerTask() != null) session.getTimerTask().cancel();
        if (session.getBossBar() != null)   session.getBossBar().removeAll();

        if (!giveRewards) return;

        List<Map.Entry<UUID, ContestEntry>> rankings = session.getSortedRankings();
        if (rankings.isEmpty()) {
            broadcast(plugin.prefix + "§e대회가 종료되었지만 참가자가 없습니다.");
            return;
        }

        // 상위 3명 이름 결정
        String first  = !rankings.isEmpty()       ? rankings.get(0).getValue().getPlayerName() : "-";
        String second = rankings.size() >= 2 ? rankings.get(1).getValue().getPlayerName() : "-";
        String third  = rankings.size() >= 3 ? rankings.get(2).getValue().getPlayerName() : "-";

        // ── 전체 채팅 방송 ────────────────────────────────────────────────
        broadcast("§7----------------------------------------------------");
        broadcast(" §6§l" + session.getType().getDisplayName() + " 낚시대회");
        broadcast("");
        broadcast(" §a§l1등 §7: §f" + first);
        broadcast(" §a§l2등 §7: §f" + second);
        broadcast(" §a§l3등 §7: §f" + third);
        broadcast("§7----------------------------------------------------");

        // ── 참가자 개인 메시지 + 보상 ─────────────────────────────────────
        String cfgBase = "Contest.Rewards." + session.getType().getConfigKey();

        // top 3 보상 처리 (최대 3명)
        Set<UUID> remaining = new HashSet<>(session.getEntries().keySet());
        int top = Math.min(3, rankings.size());
        for (int i = 0; i < top; i++) {
            UUID uuid = rankings.get(i).getKey();
            remaining.remove(uuid);

            String rankLabel;
            String rewardKey;
            if      (i == 0) { rankLabel = "§a§l1등"; rewardKey = "1st"; }
            else if (i == 1) { rankLabel = "§a§l2등"; rewardKey = "2nd"; }
            else             { rankLabel = "§a§l3등"; rewardKey = "3rd"; }

            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                sendPersonalResult(p, session, first, second, third, rankLabel);
                giveReward(p, cfgBase + "." + rewardKey);
            }
        }

        // 참가보상 (순위권 밖)
        for (UUID uuid : remaining) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                sendPersonalResult(p, session, first, second, third, "§b§l참가보상");
                giveReward(p, cfgBase + ".participation");
            }
        }
    }

    /** 개인 결과 메시지를 해당 플레이어에게 전송한다. */
    private static void sendPersonalResult(Player player, ContestSession session,
                                           String first, String second, String third,
                                           String rankLabel) {
        player.sendMessage("§7----------------------------------------------------");
        player.sendMessage(" §6§l" + session.getType().getDisplayName() + " 낚시대회");
        player.sendMessage("");
        player.sendMessage(" §a§l1등 §7: §f" + first);
        player.sendMessage(" §a§l2등 §7: §f" + second);
        player.sendMessage(" §a§l3등 §7: §f" + third);
        player.sendMessage("");
        player.sendMessage(" §b§l당신의 등수 §7: §f" + rankLabel);
        player.sendMessage("§7----------------------------------------------------");
    }

    /** config.yml 에 설정된 보상 커맨드를 콘솔로 실행한다. */
    private static void giveReward(Player player, String configPath) {
        List<String> commands = plugin.getConfig().getStringList(configPath + ".commands");
        for (String cmd : commands) {
            String replaced = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replaced);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 유틸리티
    // ═══════════════════════════════════════════════════════════════════════

    /** 보스바 제목 포맷: "§6§l대회명 §f: §e MM:SS" */
    private static String buildBossBarTitle(String name, long totalSeconds) {
        return "§6§l" + name + " §f: §e" + formatMMSS(totalSeconds);
    }

    /** 초 → "MM:SS" 문자열 변환 */
    private static String formatMMSS(long totalSeconds) {
        long min = totalSeconds / 60;
        long sec = totalSeconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    /**
     * 전체 온라인 플레이어에게 메시지를 전송한다.
     * {@code Bukkit.broadcastMessage} 의 deprecated 대체 메서드.
     */
    private static void broadcast(String msg) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }
}
