package org.nobird.komqCatchTail.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.nobird.komqCatchTail.KomqCatchTail;

public class St implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        KomqCatchTail.isGameStarted = true;
        return true;
    }
}
