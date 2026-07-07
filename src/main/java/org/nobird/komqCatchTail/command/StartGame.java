package org.nobird.komqCatchTail.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.nobird.komqCatchTail.Color;
import org.nobird.komqCatchTail.GameStateManager;
import org.nobird.komqCatchTail.KomqCatchTail;

import java.util.*;

public class StartGame implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "사용법: /start-game <팀당인원수>");
            return true;
        }

        int teamSize;
        try {
            teamSize = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "숫자를 입력해주세요.");
            return true;
        }

        if (teamSize <= 0) {
            sender.sendMessage(ChatColor.RED + "팀당 인원수는 1 이상이어야 합니다.");
            return true;
        }

        startGame(teamSize);
        return true;
    }

    private void startGame(int teamSize) {
        int onlineCount = Bukkit.getOnlinePlayers().size();
        KomqCatchTail.playerCount = onlineCount;

        // 게임 상태 초기화
        KomqCatchTail.players = new HashMap<>();
        KomqCatchTail.playerColors = new HashMap<>();
        KomqCatchTail.isPlayerRespawning = new HashMap<>();

        // 플레이어 순서 셔플
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < onlineCount; i++) indices.add(i);
        Collections.shuffle(indices);

        // 팀 배정
        for (int i = 0; i < onlineCount; i++) {
            Color.Colors teamColor = Color.Colors.fromInteger(i / teamSize);
            if (teamColor == null) break; // 색상 범위 초과 시 중단

            Player player = onlinePlayers.get(indices.get(i));
            KomqCatchTail.isPlayerRespawning.put(player.getName(), false);

            KomqCatchTail.players
                    .computeIfAbsent(teamColor, k -> new ArrayList<>())
                    .add(player.getName());
            KomqCatchTail.playerColors.put(player.getName(), teamColor);
        }

        // 채팅창 비우기
        for (int i = 0; i < 500; i++) {
            Bukkit.broadcast(Component.empty());
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // 팀별 설정 및 안내 메시지
        for (Map.Entry<Color.Colors, ArrayList<String>> entry : KomqCatchTail.players.entrySet()) {
            Color.Colors teamColor = entry.getKey();
            ArrayList<String> members = entry.getValue();
            Color.Colors targetColor = Color.nextColor(KomqCatchTail.players, teamColor);

            // 스코어보드 팀 생성 (없을 경우)
            String teamName = teamColor.name().toLowerCase();
            Team sbTeam = scoreboard.getTeam(teamName);
            if (sbTeam == null) sbTeam = scoreboard.registerNewTeam(teamName);

            for (String memberName : members) {
                Player p = Bukkit.getPlayer(memberName);
                if (p == null) continue;

                // 체력 초기화
                Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
                p.setHealth(20.0);

                // 포션 효과 적용 (포화 + 즉시 체력 회복)
                p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 100));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 100));

                // 스코어보드 팀에 추가
                sbTeam.addEntry(p.getName());

                // 안내 메시지
                p.sendMessage(ChatColor.YELLOW + "당신의 팀: " + teamColor.getColor() + teamColor.getLabel());
                p.sendMessage(ChatColor.YELLOW + "당신의 팀 대가리: " + teamColor.getColor() + members.getFirst());
                p.sendMessage(ChatColor.YELLOW + "당신의 팀원: " + teamColor.getColor() + members);
                p.sendMessage(ChatColor.YELLOW + "당신의 타겟은 " + targetColor.getColor() + targetColor.getLabel() + ChatColor.YELLOW + "입니다.");
                p.sendMessage(ChatColor.GREEN + "게임을 시작합니다.");
            }
        }

        KomqCatchTail.isGameStarted = true;
        GameStateManager.save();
    }
}
