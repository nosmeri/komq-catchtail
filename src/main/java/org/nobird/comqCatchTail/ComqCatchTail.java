package org.nobird.comqCatchTail;

import org.bukkit.plugin.java.JavaPlugin;
import org.nobird.comqCatchTail.command.St;
import org.nobird.comqCatchTail.command.StartGame;
import org.nobird.comqCatchTail.command.Team;
import org.nobird.comqCatchTail.event.DeathEvent;
import org.nobird.comqCatchTail.event.InteractEvent;
import org.nobird.comqCatchTail.event.MoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public final class ComqCatchTail extends JavaPlugin {

    public static Map<Color.Colors, ArrayList<String>> players = new HashMap<>();
    public static Map<String, Color.Colors> playerColors = new HashMap<>();
    public static int playerCount;

    public static Map<String, Boolean> isPlayerRespawning = new HashMap<>();

    public static  boolean isGameStarted = false;

    private static ComqCatchTail instance;


    public static ComqCatchTail getInstance() {
        return ComqCatchTail.instance;
    }

    public void onEnable() {
        ComqCatchTail.instance = this;
        getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        getServer().getPluginManager().registerEvents(new InteractEvent(), this);
        getServer().getPluginManager().registerEvents(new MoveEvent(), this);
        getCommand("start-game").setExecutor(new StartGame());
        getCommand("teams").setExecutor(new Team());
        getCommand("st").setExecutor(new St());
        getLogger().info("플러그인 활성화");

        Repeat.doogndoognStart();

    }

    @Override
    public void onDisable() {

        getLogger().info("플러그인 비활성화");
    }
}
