package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.util.CBMath;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedHaxModuleImpl implements SpeedHaxModule, Listener {

	private PlugCubeBuildersIn plugin;
	private boolean enabled = false;
	private boolean registeredEvents = false;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		if (plugin.getServerName().startsWith("quake")) {
			return;
		}
		if (!registeredEvents) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
			registeredEvents = true;
		}
		enabled = true;
	}

	@Override
	public void kill() {
		enabled = false;

	}

	@Override
	public void tick() {
	}

	private final Map<Player, SpeedHaxTracker> trackers = new WeakHashMap<>();

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		removeTracker(p);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerMoved(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (p.getVehicle() != null) {
			return;
		}
		long now = System.currentTimeMillis();
		SpeedHaxTracker tracker = getTracker(p);
		double[] speed = tracker.track(event.getTo());
		long earliest = tracker.getEarliest();
		long timeSinceEarliest = now - earliest;
			int speedPotion = 0;
			try {
				for (PotionEffect pe : p.getActivePotionEffects()) {
					if (pe.getType() == PotionEffectType.SPEED) {
						speedPotion = pe.getAmplifier() + 1;
					}
				}
			} catch (Exception e) {
			}
		if (timeSinceEarliest >= 200L) {
			double speed3D = speed[0];
			double speedXZ = speed[1];
			double speedY = speed[2];
			GameMode gameMode = p.getGameMode();
			boolean fly = p.isFlying();
			boolean gliding = p.isGliding();
			plugin.reportSpeeder(p, fly, gliding, gameMode, speedPotion, speed3D, speedXZ, speedY);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleported(PlayerTeleportEvent event) {
		Player p = event.getPlayer();
		getTracker(p).clear();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerEnterVehicle(VehicleEnterEvent event) {
		Entity entered = event.getEntered();
		if (entered instanceof Player) {
			Player p = (Player) entered;
			getTracker(p).clear();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerExitVehicle(VehicleExitEvent event) {
		LivingEntity exited = event.getExited();
		if (exited instanceof Player) {
			Player p = (Player) exited;
			getTracker(p).clear();
		}
	}

	private SpeedHaxTracker getTracker(Player p) {
		SpeedHaxTracker tracker = trackers.get(p);
		if (tracker == null) {
			trackers.put(p, tracker = new SpeedHaxTracker(p));
		}
		return tracker;
	}

	private void removeTracker(Player p) {
		trackers.remove(p);
	}

	private static class SpeedHaxTracker {

		private final Player p;

		private SpeedHaxTracker(Player p) {
			this.p = p;
		}

		private final List<SpeedHaxTrack> tracks = new LinkedList<>();

		private long getEarliest() {
			long now = System.currentTimeMillis();
			long stale = now - 1000L;
			for (Iterator<SpeedHaxTrack> it = tracks.iterator(); it.hasNext();) {
				SpeedHaxTrack track = it.next();
				if (track.time <= stale || track.time > now) {
					it.remove();
				} else {
					return track.time;
				}
			}
			return -1L;
		}

		private double[] track(Location to) {
			long now = System.currentTimeMillis();
			long stale = now - 1000L;
			{
				int s = tracks.size();
				if (s >= 1 && tracks.get(s - 1).time >= now - 25) {
					tracks.remove(s - 1);
				}
			}
			tracks.add(new SpeedHaxTrack(p, this, to, now));
			int count = 0;
			double totalSpeed3D = 0.0;
			double totalSpeedXZ = 0.0;
			double totalSpeedY = 0.0;
			SpeedHaxTrack prev = null;
			for (Iterator<SpeedHaxTrack> it = tracks.iterator(); it.hasNext();) {
				SpeedHaxTrack track = it.next();
				if (track.time <= stale || track.time > now) {
					it.remove();
				} else {
					if (prev != null) {
						double[] distance = track.distance(prev);
						long timeDifference = track.time - prev.time;
						double timeInSec = ((double) timeDifference) / 1000.0;
						totalSpeed3D += (distance[0] / timeInSec);
						totalSpeedXZ += (distance[1] / timeInSec);
						totalSpeedY += (distance[2] / timeInSec);
						count += 1;
					}
					prev = track;
				}
			}
			return new double[]{
				totalSpeed3D / ((double) count),
				totalSpeedXZ / ((double) count),
				totalSpeedY / ((double) count)
			};
		}

		private void clear() {
			tracks.clear();
		}
	}

	private static class SpeedHaxTrack {

		private final Player p;
		private final SpeedHaxTracker tracker;
		public final double x, y, z;
		public final long time;

		private SpeedHaxTrack(Player p, SpeedHaxTracker tracker, Location loc, long time) {
			this(p, tracker, loc.getX(), loc.getY(), loc.getZ(), time);
		}

		private SpeedHaxTrack(Player p, SpeedHaxTracker tracker, double x, double y, double z, long time) {
			this.p = p;
			this.tracker = tracker;
			this.x = x;
			this.y = y;
			this.z = z;
			this.time = time;
		}

		private double[] distance(SpeedHaxTrack otherTrack) {
			double distanceX = x - otherTrack.x;
			double distanceY = y - otherTrack.y;
			double distanceZ = z - otherTrack.z;
			double distanceSquared3D = (distanceX * distanceX) + (distanceY * distanceY) + (distanceZ * distanceZ);
			double distanceSquaredXZ = (distanceX * distanceX) + (distanceZ * distanceZ);
			return new double[]{sqrt(distanceSquared3D), sqrt(distanceSquaredXZ), distanceY};
		}

	}

	private static double sqrt(double val) {
		double sqrt = CBMath.sqrt(val);
		if (val != val) {
			return Math.sqrt(val);
		}
		return sqrt;
	}
}
