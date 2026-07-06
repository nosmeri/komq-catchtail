package org.nobird.komqCatchTail;

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

public final class KomqCatchTail extends JavaPlugin {

    public static Map<Color.Colors, ArrayList<String>> players = new HashMap<>();
    public static Map<String, Color.Colors> playerColors = new HashMap<>();
    public static Map<String, Boolean> isPlayerRespawning = new HashMap<>();
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

        getCommand("start-game").setExecutor(new StartGame());
        getCommand("teams").setExecutor(new Team());
        getCommand("st").setExecutor(new St());

        Repeat.startHeartbeat();

        getLogger().info("플러그인 활성화");
    }

    @Override
    public void onDisable() {
        Repeat.stopHeartbeat();
        getLogger().info("플러그인 비활성화");
    }
}
