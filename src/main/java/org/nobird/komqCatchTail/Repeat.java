package org.nobird.komqCatchTail;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class Repeat {

    private static BukkitTask heartbeatTask = null;

    /**
     * 두근두근 하트비트 태스크를 시작합니다.
     * 이미 실행 중이면 중복 실행을 방지합니다.
     */
    public static void startHeartbeat() {
        if (heartbeatTask != null) return;

        KomqCatchTail plugin = KomqCatchTail.getInstance();

        heartbeatTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    /**
     * 하트비트 태스크를 중지합니다. (플러그인 비활성화 시 호출)
     */
    public static void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
            heartbeatTask = null;
        }
    }

    /**
     * 매 틱마다 실행되는 게임 상태 체크 로직입니다.
     * - 남은 팀이 1개면 게임 종료
     * - 가까운 타겟이 주변에 있으면 두근두근 효과
     */
    private static void tick() {
        if (!KomqCatchTail.isGameStarted) return;

        // 남은 팀이 1팀이면 게임 종료
        if (KomqCatchTail.players.size() == 1) {
            Color.Colors winnerColor = Color.getLeftColors(KomqCatchTail.players).getFirst();
            String winnerName = KomqCatchTail.players.get(winnerColor).getFirst();

            Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "게임을 종료합니다.");
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                player.sendTitle(winnerColor.getColor() + winnerName + "님이 우승하셨습니다.", "");
            }
            KomqCatchTail.isGameStarted = false;
            GameStateManager.clear(); // 게임 종료 → 저장 파일 삭제
            return;
        }

        // 타겟 팀이 근처에 있으면 두근두근 효과
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Color.Colors myColor = KomqCatchTail.playerColors.get(p.getName());
            if (myColor == null) continue;

            Color.Colors targetColor = Color.nextColor(KomqCatchTail.players, myColor);
            List<Entity> nearbyEntities = p.getNearbyEntities(50.0, 50.0, 50.0);

            for (Entity entity : nearbyEntities) {
                if (!(entity instanceof Player nearPlayer)) continue;

                Color.Colors nearColor = KomqCatchTail.playerColors.get(nearPlayer.getName());
                if (targetColor.equals(nearColor)) {
                    // 두근두근 효과
                    p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 1.0f);
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "[ ♥ ]"));
                    break; // 한 명 발견하면 충분
                }
            }
        }
    }
}
