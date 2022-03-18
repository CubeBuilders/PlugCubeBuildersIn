package hk.siggi.bukkit.plugcubebuildersin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.projectiles.ProjectileSource;

public class VanishProtectionListener implements Listener {
	private final PlugCubeBuildersIn plugin;
	public VanishProtectionListener(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	private void handle(Player p, Cancellable event) {
		if (p == null)
			return;
		if (plugin.isVanishProtected(p))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event) {
		handle(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true)
	public void blockMultiPlace(BlockMultiPlaceEvent event) {
		handle(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent event) {
		handle(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerDropItem(PlayerDropItemEvent event) {
		handle(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerInteract(PlayerInteractEvent event) {
		handle(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		handle(event.getPlayer(), event);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerPickupItem(EntityPickupItemEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity instanceof Player)
			handle((Player) entity, event);
	}

	@EventHandler(ignoreCancelled = true)
	public void hangingBreakByPlayer(HangingBreakByEntityEvent event) {
		Entity remover = event.getRemover();
		if (remover instanceof Player)
			handle((Player) remover, event);
	}

	@EventHandler(ignoreCancelled = true)
	public void entityDamage(EntityDamageEvent event) {
		Entity smacked = event.getEntity();
		if ((smacked instanceof Player)) {
			handle((Player) smacked, event);
		}
		if ((event instanceof EntityDamageByEntityEvent)) {
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
			Entity damager = ev.getDamager();
			Player player = null;
			if ((damager instanceof Player)) {
				player = (Player) damager;
			} else if ((damager instanceof Projectile)) {
				Projectile projectile = (Projectile) damager;
				ProjectileSource shooter = projectile.getShooter();
				if (shooter instanceof Player) {
					player = (Player) shooter;
				}
			}
			handle(player, event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void entityTarget(EntityTargetEvent event) {
		Entity target = event.getTarget();
		if (target instanceof Player) {
			// This is a special case, do not allow /vp to override whether a mob targets a vanished player.
			// So we check if the player is vanished rather than if they have vanish protection.
			if (plugin.isVanished((Player) target)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void vehicleDestroy(VehicleDestroyEvent event) {
		Entity entity = event.getAttacker();
		if (entity instanceof Player) {
			handle((Player) entity, event);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void spectatorVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof Player) {
			handle((Player) entity, event);
		}
	}
}
