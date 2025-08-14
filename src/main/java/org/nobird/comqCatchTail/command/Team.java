package org.nobird.comqCatchTail.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.nobird.comqCatchTail.Color;
import org.nobird.comqCatchTail.ComqCatchTail;

import java.util.ArrayList;
import java.util.Arrays;

public class Team implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {return false;}
        Player player = (Player) sender;

        ComqCatchTail.isPlayerRespawning.put(player.getName(), false);
        ComqCatchTail.players.put(Color.Colors.fromInteger(Integer.parseInt(args[0])), new ArrayList<>(Arrays.asList(player.getName())));
        ComqCatchTail.playerColors.put(player.getName(), Color.Colors.fromInteger(Integer.parseInt(args[0])));
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "team join "+Color.Colors.fromInteger(Integer.parseInt(args[0])).name().toLowerCase()+" "+player.getName());

        return false;
    }
}
