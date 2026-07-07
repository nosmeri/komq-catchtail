package org.nobird.komqCatchTail;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.nobird.komqCatchTail.command.St;
import org.nobird.komqCatchTail.command.StartGame;
import org.nobird.komqCatchTail.command.Team;
import org.nobird.komqCatchTail.event.DeathEvent;
import org.nobird.komqCatchTail.event.InteractEvent;
import org.nobird.komqCatchTail.event.MoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class KomqCatchTail extends JavaPlugin implements Listener {

    /** 팀별 플레이어 목록: 팀색상 → [플레이어이름, ...] */
    public static Map<Color.Colors, ArrayList<String>> players = new HashMap<>();

    /** 플레이어별 팀 색상: 플레이어이름 → 팀색상 */
    public static Map<String, Color.Colors> playerColors = new HashMap<>();

    /** 플레이어별 리스폰 대기 여부: 플레이어이름 → true/false */
    public static Map<String, Boolean> isPlayerRespawning = new HashMap<>();

    /**
     * 오프라인 플레이어의 최대 체력 캐시.
     * 서버 재시작 후 플레이어가 접속할 때 체력을 복원하는 데 사용됩니다.
     */
    public static Map<String, Double> savedMaxHealth = new HashMap<>();

    public static int playerCount;
    public static boolean isGameStarted = false;

    private static KomqCatchTail instance;

    public static KomqCatchTail getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        getServer().getPluginManager().registerEvents(new InteractEvent(), this);
        getServer().getPluginManager().registerEvents(new MoveEvent(), this);
        getServer().getPluginManager().registerEvents(this, this); // PlayerJoinEvent

        getCommand("start-game").setExecutor(new StartGame());
        getCommand("teams").setExecutor(new Team());
        getCommand("st").setExecutor(new St());

        Repeat.startHeartbeat();

        // 저장된 게임 상태 복원
        GameStateManager.load();

        getLogger().info("플러그인 활성화");
    }

    @Override
    public void onDisable() {
        // 비정상 종료 대비: 현재 상태를 저장
        GameStateManager.save();
        Repeat.stopHeartbeat();
        getLogger().info("플러그인 비활성화");
    }

    /**
     * 오프라인이었던 플레이어가 재접속할 때 최대 체력을 복원합니다.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Double maxHp = savedMaxHealth.get(player.getName());
        if (maxHp != null) {
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH))
                    .setBaseValue(maxHp);
            savedMaxHealth.remove(player.getName());
        }
    }
}
