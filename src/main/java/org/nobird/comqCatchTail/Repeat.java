package org.nobird.comqCatchTail;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Repeat {
    public static void doogndoognStart() {
        ComqCatchTail plugin = ComqCatchTail.getInstance();

        new BukkitRunnable() {
            @Override
            public void run() {
                doogndoognStart();
            }
        }.runTaskLater(plugin, 10L);

        if (!ComqCatchTail.isGameStarted) { return; }

        if (ComqCatchTail.players.size()==1) {
            Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "게임을 종료합니다.");
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.sendTitle(Color.getLeftColor(ComqCatchTail.players).getFirst().getColor() + ComqCatchTail.players.get(Color.getLeftColor(ComqCatchTail.players).getFirst()).getFirst() + "님이 우승하셨습니다.", "");
            }
            ComqCatchTail.isGameStarted = false;
        }

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            List<Entity> near = p.getNearbyEntities(50.0D, 50.0D, 50.0D);
            for (Entity e : near) {
                if (e instanceof Player) {
                    Player nearplayer = (Player) e;
                    if (Color.nextColor(ComqCatchTail.players ,ComqCatchTail.playerColors.get(nearplayer.getName())).equals(ComqCatchTail.playerColors.get(p.getName()))) {
                        //두근두근
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f,1.0f);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "[ ♥ ]"));
                    }
                }
            }
        }
    }
}
