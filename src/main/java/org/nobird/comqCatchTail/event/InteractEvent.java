package org.nobird.comqCatchTail.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.nobird.comqCatchTail.Color;
import org.nobird.comqCatchTail.ComqCatchTail;

public class InteractEvent implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!ComqCatchTail.isGameStarted || !event.getAction().isRightClick()) {return;}
        if (!event.getHand().equals(EquipmentSlot.HAND)) {return;}
        if (p.getInventory().getItemInMainHand().getType().equals(Material.DIAMOND)) {
            event.setCancelled(true);
            ItemStack item = p.getInventory().getItemInMainHand();
            item.setAmount(item.getAmount() - 1);
            p.getInventory().setItemInMainHand(item);

            Player target = Bukkit.getPlayer(
                    ComqCatchTail.players.get(Color.nextColor(ComqCatchTail.players, ComqCatchTail.playerColors.get(p.getName()))).getFirst());

            if (!p.getWorld().equals(target.getWorld())) {
                p.sendMessage("타겟이 다른 월드에 있습니다");
                return;
            }

            Vector dir = target.getEyeLocation().toVector().subtract(p.getEyeLocation().toVector()).normalize();

            for (int i=0; i < 20; i++) {
                p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, p.getEyeLocation().add(dir.clone().multiply(1+i/5d)), 5,0,0,0,0);
            }
        }
    }
}
