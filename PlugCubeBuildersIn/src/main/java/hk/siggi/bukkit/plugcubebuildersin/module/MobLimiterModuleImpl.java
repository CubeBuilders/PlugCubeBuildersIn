package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.ArrayList;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
	private final Set<LivingEntity> trackedEntities = new HashSet<>();
	private final Map<LivingEntity, String> entityToSpawnedChunk = new HashMap<>();
	private final Map<String, ArrayList<LivingEntity>> entitiesSpawnedInChunks = new HashMap<>();

	private void clean() {
		for (Iterator<LivingEntity> it = trackedEntities.iterator(); it.hasNext();) {
			LivingEntity entity = it.next();
			if (!entity.isValid()) {
				String chunkString = entityToSpawnedChunk.get(entity);
				entityToSpawnedChunk.remove(entity);
				it.remove();
				if (chunkString != null) {
					ArrayList<LivingEntity> entityList = entitiesSpawnedInChunks.get(chunkString);
					if (entityList != null) {
						entityList.remove(entity);
						if (entityList.isEmpty()) {
							entitiesSpawnedInChunks.remove(chunkString);
						}
					}
				}
			}
		}
	}

	private void addEntity(LivingEntity entity, Chunk chunk) {
		if (trackedEntities.contains(entity)) {
			return;
		}
		trackedEntities.add(entity);
		String chunkString = getString(chunk);
		entityToSpawnedChunk.put(entity, chunkString);
		ArrayList<LivingEntity> entityList = entitiesSpawnedInChunks.get(chunkString);
		if (entityList == null) {
			entitiesSpawnedInChunks.put(chunkString, entityList = new ArrayList<>());
		}
		entityList.add(entity);
	}

	private LivingEntity[] getEntitiesSpawnedInChunk(String chunkString) {
		ArrayList<LivingEntity> entityList = entitiesSpawnedInChunks.get(chunkString);
		if (entityList == null) {
			return new LivingEntity[0];
		}
		return entityList.toArray(new LivingEntity[entityList.size()]);
	}

	private String getString(Chunk chunk) {
		return chunk.getWorld() + "," + chunk.getX() + "," + chunk.getZ();
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		SpawnReason spawnReason = event.getSpawnReason();
		boolean skipCheck = false;
		if (spawnReason == SpawnReason.NATURAL) {
			StackTraceElement[] stackTrace = new Throwable().getStackTrace();
			for (StackTraceElement ele : stackTrace) {
				if (ele.getClassName().endsWith(".CommandSummon")) {
					skipCheck = true;
					// allow the /minecraft:summon command to bypass the restrictions
					// when spawning using that command, it uses SpawnReason.NATURAL for some reason
					break;
				}
			}
		}
		if (spawnReason == SpawnReason.DISPENSE_EGG || spawnReason == SpawnReason.SPAWNER_EGG) {
			// if we reached this point, we've already allowed it.
			// see other places that allowSpawn is called
			skipCheck = true;
		}
		if (!skipCheck) {
			if (!allowSpawn(event.getLocation(), spawnReason)) {
				event.setCancelled(true);
				return;
			}
		}
		addEntity(event.getEntity(), event.getLocation().getChunk());
	}

	@EventHandler
	public void itemDispense(BlockDispenseEvent event) {
		Block block = event.getBlock();
		BlockData data = block.getBlockData();
		if (!(data instanceof Dispenser)) {
			return;
		}
		if (!isSpawnEgg(event.getItem())) {
			return;
		}
		Dispenser dispenser = (Dispenser) data;
		BlockFace facing = dispenser.getFacing();
		Block spawnPoint = block.getRelative(facing);
		Location pos = spawnPoint.getLocation().add(0.5, 0.0, 0.5);
		if (!allowSpawn(pos, SpawnReason.DISPENSE_EGG)) {
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
		if (!isSpawnEgg(item)) {
			return;
		}
		Block block = event.getClickedBlock();
		BlockFace facing = event.getBlockFace();
		Block spawnPoint = block.getRelative(facing);
		Location pos = spawnPoint.getLocation().add(0.5, 0.0, 0.5);
		if (!allowSpawn(pos, SpawnReason.SPAWNER_EGG)) {
			event.setCancelled(true);
			Player p = event.getPlayer();
			p.sendMessage(ChatColor.RED + "There are too many mobs here!");
		}
	}

	private boolean isSpawnEgg(ItemStack item) {
		String n = item.getType().name();
		if (n.endsWith("_SPAWN_EGG") /*1.13 & later*/ || n.equals("MONSTER_EGG") /*1.12 & earlier*/) {
			return true;
		}
		return false;
	}

	private boolean allowSpawn(Location loc, SpawnReason spawnReason) {
		int limit = 8;
		switch (spawnReason) {
			case BREEDING:
			case BUILD_IRONGOLEM:
			case BUILD_SNOWMAN:
			case BUILD_WITHER:
				limit = 15;
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
		Chunk chunk = loc.getChunk();
		String chunkString = getString(chunk);
		Entity[] entities = chunk.getEntities();
		LivingEntity[] spawnedLivingEntities = getEntitiesSpawnedInChunk(chunkString);
		int entityCount = 0;
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity) {
				entityCount += 1;
			}
		}
		int spawnedCount = 0;
		for (LivingEntity spawnedLivingEntity : spawnedLivingEntities) {
			boolean c = false;
			for (Entity entity : entities) {
				if (entity == spawnedLivingEntity) {
					c = true;
				}
			}
			if (c) {
				continue;
			}
			spawnedCount += 1;
		}
		return entityCount < limit && spawnedCount < limit;
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
}
