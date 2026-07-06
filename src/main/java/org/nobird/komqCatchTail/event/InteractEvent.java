package org.nobird.komqCatchTail.event;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.nobird.komqCatchTail.Color;
import org.nobird.komqCatchTail.KomqCatchTail;

public class InteractEvent implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!KomqCatchTail.isGameStarted) return;
        if (!event.getAction().isRightClick()) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player p = event.getPlayer();
        Color.Colors myColor = KomqCatchTail.playerColors.get(p.getName());
        if (myColor == null) return;

        ItemStack mainHand = p.getInventory().getItemInMainHand();
        if (!mainHand.getType().equals(Material.DIAMOND)) return;

        event.setCancelled(true);

        // 다이아몬드 1개 소모
        mainHand.setAmount(mainHand.getAmount() - 1);
        p.getInventory().setItemInMainHand(mainHand);

        // 현재 타겟 팀의 대가리 플레이어 위치를 가져옴
        Color.Colors targetColor = Color.nextColor(KomqCatchTail.players, myColor);
        String targetLeaderName = KomqCatchTail.players.get(targetColor).getFirst();
        Player target = org.bukkit.Bukkit.getPlayer(targetLeaderName);

        if (target == null || !target.isOnline()) {
            p.sendMessage("타겟이 현재 오프라인 상태입니다.");
            return;
        }

        if (!p.getWorld().equals(target.getWorld())) {
            p.sendMessage("타겟이 다른 월드에 있습니다.");
            return;
        }

        // 자신 → 타겟 방향으로 파티클 생성
        Vector dir = target.getEyeLocation().toVector()
                .subtract(p.getEyeLocation().toVector());

        if (dir.lengthSquared() == 0) return; // 같은 위치에 있으면 정규화 불가
        dir.normalize();

        for (int i = 0; i < 20; i++) {
            p.getWorld().spawnParticle(
                    Particle.SOUL_FIRE_FLAME,
                    p.getEyeLocation().add(dir.clone().multiply(1 + i / 5.0)),
                    5, 0, 0, 0, 0
            );
        }
    }
}
