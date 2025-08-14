package org.nobird.comqCatchTail.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.nobird.comqCatchTail.ComqCatchTail;

public class MoveEvent implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();

        if (!ComqCatchTail.isGameStarted) {return;}

        if (ComqCatchTail.isPlayerRespawning.get(p.getName())) {
            event.setCancelled(true);
        }
    }
}
