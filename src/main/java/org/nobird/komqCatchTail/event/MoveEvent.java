package org.nobird.komqCatchTail.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.nobird.komqCatchTail.KomqCatchTail;

public class MoveEvent implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!KomqCatchTail.isGameStarted) return;

        Player p = event.getPlayer();

        // isPlayerRespawning 맵에 없는 플레이어는 기본값 false로 처리 (NPE 방지)
        if (KomqCatchTail.isPlayerRespawning.getOrDefault(p.getName(), false)) {
            event.setCancelled(true);
        }
    }
}
