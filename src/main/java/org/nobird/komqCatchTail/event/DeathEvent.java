package org.nobird.komqCatchTail.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nobird.komqCatchTail.Color;
import org.nobird.komqCatchTail.GameStateManager;
import org.nobird.komqCatchTail.KomqCatchTail;

import java.util.ArrayList;
import java.util.Objects;

public class DeathEvent implements Listener {

    @EventHandler
    public void onDamaged(EntityDamageEvent e) {
        if (!KomqCatchTail.isGameStarted) return;
        if (!(e.getEntity() instanceof Player p)) return;

        Color.Colors playerColor = KomqCatchTail.playerColors.get(p.getName());
        if (playerColor == null) return; // 게임에 등록되지 않은 플레이어

        boolean isAttackedByPlayer = e.getDamageSource().getCausingEntity() instanceof Player;
        Player attacker = null;

        if (isAttackedByPlayer) {
            attacker = (Player) e.getDamageSource().getCausingEntity();
            Color.Colors attackerColor = KomqCatchTail.playerColors.get(attacker.getName());
            if (attackerColor == null) { e.setCancelled(true); return; }

            // 공격자의 다음 타겟 팀이 피격자 팀이 아니면 데미지 취소
            Color.Colors attackerTarget = Color.nextColor(KomqCatchTail.players, attackerColor);
            if (!attackerTarget.equals(playerColor)) {
                e.setCancelled(true);
                return;
            }
        }

        // 죽음 처리: 체력이 받은 데미지보다 적거나 같으면 사망
        if (p.getHealth() > e.getFinalDamage()) return;

        final Player finalAttacker = attacker;

        // 토템 해킹: 부활의 토템을 오프핸드에 임시로 넣어 사망 애니메이션만 재생
        ItemStack offHand = p.getInventory().getItemInOffHand().clone();
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));

        new BukkitRunnable() {
            @Override
            public void run() {
                p.getInventory().setItemInOffHand(offHand);
                p.setHealth(p.getMaxHealth());
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1));
            }
        }.runTaskLater(KomqCatchTail.getInstance(), 1L);

        ArrayList<String> deadTeam = KomqCatchTail.players.get(playerColor);
        boolean isLeader = deadTeam != null && deadTeam.getFirst().equals(p.getName());

        if (isAttackedByPlayer && isLeader && finalAttacker != null) {
            // 대가리(팀장)를 처치한 경우: 타겟 팀 전체를 공격자 팀으로 흡수
            handleLeaderKill(p, finalAttacker, playerColor);
        } else {
            // 꼬리(팀원)를 처치한 경우: 30초 리스폰 대기
            handleTailKill(p);
        }
    }

    /**
     * 팀장을 처치한 경우의 처리:
     * - 공격자의 최대 체력 증가
     * - 피격 팀 전체가 공격자 팀으로 이동
     * - 팀 전체의 최대 체력을 공격자와 동기화
     */
    private void handleLeaderKill(Player dead, Player attacker, Color.Colors deadColor) {
        ArrayList<String> deadTeam = KomqCatchTail.players.get(deadColor);
        Color.Colors attackerColor = KomqCatchTail.playerColors.get(attacker.getName());

        // 공격자 체력 증가
        double bonus = 2.0 * deadTeam.size();
        double newMaxHealth = Objects.requireNonNull(attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue() + bonus;
        Objects.requireNonNull(attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(newMaxHealth);

        attacker.sendMessage("목표를 처치하는데 성공하였습니다! " + deadColor.getColor() + dead.getName() + ChatColor.WHITE + "님이 꼬리가 됩니다.");

        // 피격 팀원 전원을 공격자 팀으로 이동
        String attackerTeamName = attackerColor.name().toLowerCase();
        org.bukkit.scoreboard.Team sbTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(attackerTeamName);

        for (String memberName : deadTeam) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null) {
                member.sendMessage("주인이 처치당하였습니다! 앞으로 " + attackerColor.getColor() + attacker.getName() + ChatColor.WHITE + "님의 꼬리가 됩니다.");
                Objects.requireNonNull(member.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(newMaxHealth);
                if (member.getHealth() > newMaxHealth) member.setHealth(newMaxHealth);
            }
            KomqCatchTail.playerColors.put(memberName, attackerColor);
            if (sbTeam != null) sbTeam.addEntry(memberName);
        }

        // 공격자 팀에 피격 팀원 추가 후 피격 팀 제거
        ArrayList<String> attackerTeam = new ArrayList<>(KomqCatchTail.players.get(attackerColor));
        attackerTeam.addAll(deadTeam);
        KomqCatchTail.players.put(attackerColor, attackerTeam);
        KomqCatchTail.players.remove(deadColor);

        // 팀 구조 변경 즉시 저장
        GameStateManager.save();
    }

    /**
     * 꼬리(팀원)를 처치한 경우의 처리:
     * - 30초 뒤 리스폰
     */
    private void handleTailKill(Player dead) {
        dead.sendMessage("사망하여 30초 뒤에 부활합니다.");
        KomqCatchTail.isPlayerRespawning.put(dead.getName(), true);

        // 리스폰 대기 상태 저장
        GameStateManager.save();

        new BukkitRunnable() {
            @Override
            public void run() {
                KomqCatchTail.isPlayerRespawning.put(dead.getName(), false);
                dead.sendMessage("리스폰!!");
                // 리스폰 완료 후 저장
                GameStateManager.save();
            }
        }.runTaskLater(KomqCatchTail.getInstance(), 20L * 30);
    }
}
