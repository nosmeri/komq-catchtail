package org.nobird.komqCatchTail.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.nobird.komqCatchTail.Color;
import org.nobird.komqCatchTail.GameStateManager;
import org.nobird.komqCatchTail.KomqCatchTail;

import java.util.ArrayList;

public class Team implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "사용법: /teams <팀번호>");
            return true;
        }

        int colorIndex;
        try {
            colorIndex = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "숫자를 입력해주세요.");
            return true;
        }

        Color.Colors color = Color.Colors.fromInteger(colorIndex);
        if (color == null) {
            player.sendMessage(ChatColor.RED + "유효하지 않은 팀 번호입니다. (0~7)");
            return true;
        }

        // 플레이어 팀 등록
        KomqCatchTail.isPlayerRespawning.put(player.getName(), false);
        KomqCatchTail.players.computeIfAbsent(color, k -> new ArrayList<>()).add(player.getName());
        KomqCatchTail.playerColors.put(player.getName(), color);

        // 스코어보드 팀에 추가 (없을 경우 생성)
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = color.name().toLowerCase();
        org.bukkit.scoreboard.Team sbTeam = scoreboard.getTeam(teamName);
        if (sbTeam == null) sbTeam = scoreboard.registerNewTeam(teamName);
        sbTeam.addEntry(player.getName());

        // 팀 변경 시 즉시 저장
        GameStateManager.save();

        player.sendMessage(ChatColor.GREEN + color.getLabel() + " 팀에 참가했습니다.");
        return true;
    }
}
