package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.MobUtility;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MobLimiterModuleImpl implements MobLimiterModule, Listener {

	private PlugCubeBuildersIn plugin;
	private final Map<MobRegion, Set<LivingEntity>> entitiesSpawnedInChunks = new HashMap<>();
	private final Set<LivingEntity> empty = Collections.unmodifiableSet(new HashSet<>());
	private static final int REGION_SIZE = 3;
	private static final int REGION_SIZE_Y = 3;
	private static final int MAX_REGION_Y = 256 / (1 << REGION_SIZE_Y);

	private void clean() {
		for (Iterator<Map.Entry<MobRegion, Set<LivingEntity>>> regionIt = entitiesSpawnedInChunks.entrySet().iterator(); regionIt.hasNext();) {
			Map.Entry<MobRegion, Set<LivingEntity>> mapEntry = regionIt.next();
			Set<LivingEntity> entitySet = mapEntry.getValue();
			for (Iterator<LivingEntity> entityIt = entitySet.iterator(); entityIt.hasNext();) {
				LivingEntity entity = entityIt.next();
				if (!entity.isValid()) {
					entityIt.remove();
				}
			}
			if (entitySet.isEmpty()) {
				regionIt.remove();
			}
		}
	}

	private void trackEntity(LivingEntity entity, MobRegion chunk) {
		Set<LivingEntity> entitySet = entitiesSpawnedInChunks.get(chunk);
		if (entitySet == null) {
			entitiesSpawnedInChunks.put(chunk, entitySet = new HashSet<>());
		}
		entitySet.add(entity);
	}

	private Set<LivingEntity> getEntitiesSpawnedHere(MobRegion region, int regionDistance, int regionDistanceY) {
		if (regionDistance == 0 && regionDistanceY == 0) {
			Set<LivingEntity> set = entitiesSpawnedInChunks.get(region);
			if (set == null) {
				return empty;
			}
			return set;
		}
		HashSet<LivingEntity> result = new HashSet<>();
		int minX = region.x - regionDistance;
		int maxX = region.x + regionDistance;
		int minY = Math.max(0, region.y - regionDistanceY);
		int maxY = Math.min(MAX_REGION_Y, region.y + regionDistanceY);
		int minZ = region.z - regionDistance;
		int maxZ = region.z + regionDistance;
		for (int z = minZ; z <= maxZ; z++) {
			for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x <= maxX; x++) {
					Set<LivingEntity> set = entitiesSpawnedInChunks.get(new MobRegion(x, y, z));
					if (set != null) {
						result.addAll(set);
					}
				}
			}
		}
		return result;
	}

	private String getString(Chunk chunk) {
		return chunk.getWorld() + "," + chunk.getX() + "," + chunk.getZ();
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		SpawnReason spawnReason = event.getSpawnReason();
		boolean skipCheck = false;
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		for (StackTraceElement ele : stackTrace) {
			if (ele.getClassName().endsWith(".CommandSummon")) {
				skipCheck = true;
				// allow the /minecraft:summon command to bypass the restrictions
				// when spawning using that command, it uses SpawnReason.NATURAL for some reason
				break;
			}
		}
		if (spawnReason == SpawnReason.DISPENSE_EGG || spawnReason == SpawnReason.SPAWNER_EGG) {
			// if we reached this point, we've already allowed it.
			// see other places that allowSpawn is called
			skipCheck = true;
		}
		if (!skipCheck) {
			EntityType type = event.getEntity().getType();
			if (!allowSpawn(type, event.getLocation(), spawnReason)) {
				event.setCancelled(true);
				return;
			}
		}
		trackEntity(event.getEntity(), new MobRegion(event.getLocation()));
	}

	@EventHandler
	public void itemDispense(BlockDispenseEvent event) {
		Block block = event.getBlock();
		BlockData data = block.getBlockData();
		if (!(data instanceof Dispenser)) {
			return;
		}
		ItemStack item = event.getItem();
		if (!MobUtility.isEgg(item.getType())) {
			return;
		}
		Dispenser dispenser = (Dispenser) data;
		BlockFace facing = dispenser.getFacing();
		Block spawnPoint = block.getRelative(facing);
		Location pos = spawnPoint.getLocation().add(0.5, 0.0, 0.5);
		EntityType type = MobUtility.getMob(item.getType());
		if (!allowSpawn(type, pos, SpawnReason.DISPENSE_EGG)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void itemUse(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		ItemStack item = event.getItem();
		if (item == null) {
			return;
		}
		if (!MobUtility.isEgg(item.getType())) {
			return;
		}
		Block block = event.getClickedBlock();
		BlockFace facing = event.getBlockFace();
		Block spawnPoint = block.getRelative(facing);
		Location pos = spawnPoint.getLocation().add(0.5, 0.0, 0.5);
		EntityType type = MobUtility.getMob(item.getType());
		if (!allowSpawn(type, pos, SpawnReason.SPAWNER_EGG)) {
			event.setCancelled(true);
			Player p = event.getPlayer();
			p.sendMessage(ChatColor.RED + "There are too many mobs here!");
		}
	}

	private void filter(Set<LivingEntity> entities, boolean monster) {
		if (monster) {
			entities.removeIf((entity) -> !(entity instanceof Monster));
		} else {
			entities.removeIf((entity) -> (entity instanceof Monster));
		}
	}

	private boolean allowSpawn(EntityType type, Location loc, SpawnReason spawnReason) {
		int limit = 6;
		int limitNearby = 30;
		int nearDistance = 2;
		int nearDistanceY = 1;
		boolean isMonster = Monster.class.isAssignableFrom(type.getEntityClass());
		if (isMonster) {
			nearDistance = 3;
			nearDistanceY = 4;
		}
		switch (spawnReason) {
			case BREEDING:
				limit = 10;
				limitNearby = 25;
				nearDistance = 1;
				nearDistanceY = 1;
				break;
			case SPAWNER:
				limit = 10;
				limitNearby = -1;
				break;
			case BUILD_IRONGOLEM:
			case BUILD_SNOWMAN:
			case BUILD_WITHER:
				limit = 10;
				limitNearby = -1;
				break;
			case CURED:
			case CUSTOM: // plugins
			case DROWNED:
			case ENDER_PEARL:
			case INFECTION:
			case JOCKEY:
			case LIGHTNING:
			case MOUNT:
			case PATROL:
			case RAID:
			case REINFORCEMENTS:
			case SHEARED:
			case SHOULDER_ENTITY:
			case SLIME_SPLIT:
			case TRAP:
			case VILLAGE_DEFENSE:
			case VILLAGE_INVASION:
				limit = -1;
				break;
		}
		clean();
		if (limit == -1) {
			return true;
		}
		MobRegion region = new MobRegion(loc);
		Set<LivingEntity> spawnedHere = getEntitiesSpawnedHere(region, 0, 0);
		filter(spawnedHere, isMonster);
		if (spawnedHere.size() > limit) {
			return false;
		}
		if (limitNearby == -1) {
			return true;
		}
		Set<LivingEntity> spawnedNear = getEntitiesSpawnedHere(region, nearDistance, nearDistanceY);
		if (spawnedNear.size() >= limitNearby) {
			return false;
		}
		return true;
	}

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}

	private class MobRegion {

		private final int x;
		private final int y;
		private final int z;

		public MobRegion(Location location) {
			this(location.getBlockX() >> REGION_SIZE, location.getBlockY() >> REGION_SIZE_Y, location.getBlockZ() >> REGION_SIZE);
		}

		public MobRegion(Block block) {
			this(block.getX() >> REGION_SIZE, block.getY() >> REGION_SIZE_Y, block.getZ() >> REGION_SIZE);
		}

		public MobRegion(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 29 * hash + this.x;
			hash = 29 * hash + this.y;
			hash = 29 * hash + this.z;
			return hash;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof MobRegion)) {
				return false;
			}
			MobRegion o = (MobRegion) other;
			return x == o.x && y == o.y && z == o.z;
		}

		public MobRegion getRelative(int x, int y, int z) {
			int newX = this.x + x;
			int newY = this.y + y;
			int newZ = this.z + z;
			if (newY < 0 || newY >= MAX_REGION_Y) {
				return null;
			}
			return new MobRegion(newX, newY, newZ);
		}
	}
}
