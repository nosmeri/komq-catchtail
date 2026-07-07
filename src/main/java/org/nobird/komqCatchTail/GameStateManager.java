package org.nobird.komqCatchTail;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * 게임 진행상황을 gamestate.yml 파일에 저장하고 불러오는 클래스입니다.
 *
 * 저장 항목:
 * - isGameStarted     : 게임 진행 여부
 * - players           : 팀별 플레이어 목록  (팀색상 → [이름, ...])
 * - playerColors      : 플레이어별 팀 색상  (이름 → 팀색상)
 * - isPlayerRespawning: 리스폰 대기 여부    (이름 → true/false)
 * - playerMaxHealth   : 플레이어별 최대체력 (이름 → double)
 */
public class GameStateManager {

    private static final String FILE_NAME = "gamestate.yml";

    private static File getFile() {
        return new File(KomqCatchTail.getInstance().getDataFolder(), FILE_NAME);
    }

    // ─────────────────────────────────────────────────────────
    // 저장
    // ─────────────────────────────────────────────────────────

    /**
     * 현재 게임 상태를 파일에 저장합니다.
     * 게임이 시작되지 않은 상태라면 파일을 삭제합니다.
     */
    public static void save() {
        if (!KomqCatchTail.isGameStarted) {
            clear();
            return;
        }

        FileConfiguration config = new YamlConfiguration();
        config.set("isGameStarted", true);

        // players: 팀색상 → 멤버 리스트
        for (var entry : KomqCatchTail.players.entrySet()) {
            config.set("players." + entry.getKey().name(), entry.getValue());
        }

        // playerColors: 플레이어이름 → 팀색상 이름
        for (var entry : KomqCatchTail.playerColors.entrySet()) {
            config.set("playerColors." + entry.getKey(), entry.getValue().name());
        }

        // isPlayerRespawning: 플레이어이름 → boolean
        for (var entry : KomqCatchTail.isPlayerRespawning.entrySet()) {
            config.set("isPlayerRespawning." + entry.getKey(), entry.getValue());
        }

        // playerMaxHealth: 현재 온라인 플레이어의 최대체력 저장
        for (var entry : KomqCatchTail.playerColors.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null) {
                double maxHp = Objects.requireNonNull(
                        p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
                config.set("playerMaxHealth." + entry.getKey(), maxHp);
            }
        }

        try {
            KomqCatchTail.getInstance().getDataFolder().mkdirs();
            config.save(getFile());
        } catch (IOException e) {
            KomqCatchTail.getInstance().getLogger()
                    .log(Level.SEVERE, "게임 상태를 저장하는 데 실패했습니다.", e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // 불러오기
    // ─────────────────────────────────────────────────────────

    /**
     * 저장된 게임 상태를 파일에서 불러옵니다.
     * 파일이 없거나 게임이 시작되지 않은 상태이면 아무것도 하지 않습니다.
     */
    public static void load() {
        File file = getFile();
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.getBoolean("isGameStarted", false)) return;

        // players 복원
        KomqCatchTail.players = new HashMap<>();
        if (config.isConfigurationSection("players")) {
            for (String colorName : Objects.requireNonNull(config.getConfigurationSection("players")).getKeys(false)) {
                Color.Colors color = parseColor(colorName);
                if (color == null) continue;
                List<String> members = config.getStringList("players." + colorName);
                KomqCatchTail.players.put(color, new ArrayList<>(members));
            }
        }

        // playerColors 복원
        KomqCatchTail.playerColors = new HashMap<>();
        if (config.isConfigurationSection("playerColors")) {
            for (String playerName : Objects.requireNonNull(config.getConfigurationSection("playerColors")).getKeys(false)) {
                Color.Colors color = parseColor(config.getString("playerColors." + playerName));
                if (color != null) KomqCatchTail.playerColors.put(playerName, color);
            }
        }

        // isPlayerRespawning 복원
        KomqCatchTail.isPlayerRespawning = new HashMap<>();
        if (config.isConfigurationSection("isPlayerRespawning")) {
            for (String playerName : Objects.requireNonNull(config.getConfigurationSection("isPlayerRespawning")).getKeys(false)) {
                KomqCatchTail.isPlayerRespawning.put(playerName,
                        config.getBoolean("isPlayerRespawning." + playerName, false));
            }
        }

        // playerMaxHealth 복원: 온라인 플레이어에게 즉시 적용, 오프라인은 캐시에 보관
        if (config.isConfigurationSection("playerMaxHealth")) {
            for (String playerName : Objects.requireNonNull(config.getConfigurationSection("playerMaxHealth")).getKeys(false)) {
                double maxHp = config.getDouble("playerMaxHealth." + playerName, 20.0);
                Player p = Bukkit.getPlayer(playerName);
                if (p != null) {
                    Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(maxHp);
                }
                // 오프라인 플레이어 체력은 playerJoinEvent에서 적용하기 위해 캐시 저장
                KomqCatchTail.savedMaxHealth.put(playerName, maxHp);
            }
        }

        // 스코어보드 팀 복원
        restoreScoreboardTeams();

        KomqCatchTail.isGameStarted = true;
        KomqCatchTail.getInstance().getLogger().info("게임 상태를 복원했습니다.");
    }

    // ─────────────────────────────────────────────────────────
    // 삭제
    // ─────────────────────────────────────────────────────────

    /**
     * 저장된 게임 상태 파일을 삭제합니다. (게임 종료 시 호출)
     */
    public static void clear() {
        File file = getFile();
        if (file.exists()) {
            file.delete();
        }
    }

    // ─────────────────────────────────────────────────────────
    // 유틸리티
    // ─────────────────────────────────────────────────────────

    /** 스코어보드 팀을 players 맵 기준으로 다시 등록합니다. */
    private static void restoreScoreboardTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (var entry : KomqCatchTail.players.entrySet()) {
            String teamName = entry.getKey().name().toLowerCase();
            org.bukkit.scoreboard.Team sbTeam = scoreboard.getTeam(teamName);
            if (sbTeam == null) sbTeam = scoreboard.registerNewTeam(teamName);
            for (String memberName : entry.getValue()) {
                sbTeam.addEntry(memberName);
            }
        }
    }

    /** 색상 이름 문자열을 Colors 열거형으로 안전하게 변환합니다. */
    private static Color.Colors parseColor(String name) {
        if (name == null) return null;
        try {
            return Color.Colors.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
