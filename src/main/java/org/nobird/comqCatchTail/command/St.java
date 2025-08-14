package org.nobird.comqCatchTail.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nobird.comqCatchTail.ComqCatchTail;

public class St implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ComqCatchTail.isGameStarted=true;
        return false;
    }
}
