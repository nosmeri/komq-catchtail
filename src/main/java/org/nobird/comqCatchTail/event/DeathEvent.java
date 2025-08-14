package org.nobird.comqCatchTail.event;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nobird.comqCatchTail.Color;
import org.nobird.comqCatchTail.ComqCatchTail;

import java.util.ArrayList;

public class DeathEvent implements Listener {

    @EventHandler
    public void OnDamaged(EntityDamageEvent e) {
        ComqCatchTail comqCatchTail = ComqCatchTail.getInstance();
        if (!ComqCatchTail.isGameStarted) { return; }

        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player)e.getEntity();
        Player attacker = p;
        boolean isPlayer = e.getDamageSource().getCausingEntity() instanceof Player;

        //다른사람이 때렸을때
        if (isPlayer) {
            attacker = (Player) e.getDamageSource().getCausingEntity();
            if (!Color.nextColor(ComqCatchTail.players, ComqCatchTail.playerColors.get(attacker.getName())).equals(ComqCatchTail.playerColors.get(p.getName()))) { e.setCancelled(true); return; }
        }

        //상대, 엔티티가 떄렸을때
        if (((Player) e.getEntity()).getHealth() <= e.getFinalDamage()) {  //죽음

            ItemStack offHand = p.getInventory().getItemInOffHand().clone();
            p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));

            new BukkitRunnable() {
                @Override
                public void run() {
                    p.getInventory().setItemInOffHand(offHand);
                    p.setHealth(p.getMaxHealth());
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*5, 1));
                }
            }.runTaskLater(comqCatchTail, 1L);

            //대가리
            if (isPlayer && ComqCatchTail.players.get(ComqCatchTail.playerColors.get(p.getName())).getFirst().equals(p.getName())) {
                attacker.setMaxHealth(attacker.getMaxHealth()-2*ComqCatchTail.players.get(ComqCatchTail.playerColors.get(p.getName())).size());
                attacker.sendMessage("목표를 처치하는데 성공하였습니다! " + ComqCatchTail.playerColors.get(p.getName()).getColor() + p.getName() + ChatColor.WHITE + "님이 꼬리가 됩니다.");

                //플레이어의 이전 팀
                Color.Colors before = ComqCatchTail.playerColors.get(p.getName());
                //마크 팀 변경, playerColors 변경
                for (String teams : ComqCatchTail.players.get(ComqCatchTail.playerColors.get(p.getName()))) {
                    p.sendMessage("주인이 처치당하였습니다! 앞으로 " + ComqCatchTail.playerColors.get(attacker.getName()).getColor() + attacker.getName() + ChatColor.WHITE + "님의 꼬리가 됩니다.");
                    ComqCatchTail.playerColors.put(teams, ComqCatchTail.playerColors.get(attacker.getName()));

                    Bukkit.getScoreboardManager().getMainScoreboard().getTeam(ComqCatchTail.playerColors.get(attacker.getName()).name().toLowerCase()).addPlayer(Bukkit.getPlayer(teams));
                }

                //attacker 팀에 추가
                ArrayList<String> atTeam = new ArrayList<>();
                atTeam.addAll(ComqCatchTail.players.get(ComqCatchTail.playerColors.get(attacker.getName())));
                atTeam.addAll(ComqCatchTail.players.get(before));
                ComqCatchTail.players.put(ComqCatchTail.playerColors.get(attacker.getName()), atTeam);
                ComqCatchTail.players.remove(before);

                //팀원의 최대체력 감소

                double maxHealth = attacker.getMaxHealth();

                for (String teams : ComqCatchTail.players.get(ComqCatchTail.playerColors.get(p.getName()))) {
                    Bukkit.getPlayer(teams).setMaxHealth(maxHealth);
                }
            } else { //꼬리 죽임
                p.sendMessage("사망하여 30초 뒤에 부활합니다.");
                ComqCatchTail.isPlayerRespawning.put(p.getName(), true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        ComqCatchTail.isPlayerRespawning.put(p.getName(), false);
                        p.sendMessage("리스폰!!");
                    }
                }.runTaskLater(ComqCatchTail.getInstance(), 20L*30);
            }
        }
    }
}
