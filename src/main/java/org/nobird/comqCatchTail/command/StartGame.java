package org.nobird.comqCatchTail.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.nobird.comqCatchTail.Color;
import org.nobird.comqCatchTail.ComqCatchTail;

import java.util.*;
import net.kyori.adventure.text.Component;
public class StartGame implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        startGame(Integer.parseInt(args[0]));
        return false;
    }

    private void startGame(int teamC) {
        ComqCatchTail.playerCount = Bukkit.getOnlinePlayers().size();

        ComqCatchTail.players = new HashMap<>();
        ComqCatchTail.playerColors = new HashMap<>();
        ComqCatchTail.isPlayerRespawning = new HashMap<>();

        List<Integer> temp = new ArrayList<>();

        for (int i=0; i < ComqCatchTail.playerCount; i++) { temp.add(i); }

        Collections.shuffle(temp);

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

        for (int i=0; i < ComqCatchTail.playerCount; i++) {
            Color.Colors teamColor = Color.Colors.fromInteger(i /teamC);
            ComqCatchTail.isPlayerRespawning.put(onlinePlayers.get(i).getName(), false);
            ArrayList<String> newTeam = new ArrayList<>();
            if ((float)(i /teamC) != i/((float)teamC)) {
                newTeam.addAll(ComqCatchTail.players.get(teamColor));
            }
            newTeam.add(onlinePlayers.get(temp.get(i)).getName());
            ComqCatchTail.players.put(teamColor, newTeam);
            ComqCatchTail.playerColors.put(onlinePlayers.get(temp.get(i)).getName(), teamColor);
        }

        for (int i = 0; i<500;i++){
            Bukkit.broadcast(Component.empty());
        }

        for (Map.Entry<Color.Colors, ArrayList<String>> entrySet : ComqCatchTail.players.entrySet()) {
            for (String pla : entrySet.getValue()) {
                Player p = Bukkit.getPlayer(pla);

                assert p != null : "player is null";

                p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 100));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 100));

                //초기화
                p.setMaxHealth(20);
                p.setHealth(p.getMaxHealth());
                Bukkit.getScoreboardManager().getMainScoreboard().getTeam(entrySet.getKey().name().toLowerCase()).addPlayer(p);

                p.sendMessage(ChatColor.YELLOW + "당신의 팀: " +
                        entrySet.getKey().getColor() + entrySet.getKey().getLabel());
                p.sendMessage(ChatColor.YELLOW + "당신의 팀 대가리: " +
                        entrySet.getKey().getColor() + ComqCatchTail.players.get(entrySet.getKey()).getFirst());
                p.sendMessage(ChatColor.YELLOW + "당신의 팀원: " +
                        entrySet.getKey().getColor() + ComqCatchTail.players.get(entrySet.getKey()).toString());
                p.sendMessage(ChatColor.YELLOW + "당신의 타겟은 " +
                        Color.nextColor(ComqCatchTail.players, entrySet.getKey()).getColor() + Color.nextColor(ComqCatchTail.players, entrySet.getKey()).getLabel() +
                        ChatColor.YELLOW + "입니다.");
                p.sendMessage(ChatColor.GREEN + "게임을 시작합니다.");
            }
        }

        ComqCatchTail.isGameStarted=true;
    }
}
