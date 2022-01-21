package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.world.WorldBlock;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MineWatchModuleImpl implements MineWatchModule, Listener {

	private PlugCubeBuildersIn plugin;

	private final Map<Material, String> materials = new HashMap<>();
	private final Map<WorldBlock, Long> blocksFound = new HashMap<>();

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
		materials.put(Material.IRON_ORE, "iron");
		materials.put(Material.GOLD_ORE, "gold");
		materials.put(Material.COAL_ORE, "coal");
		materials.put(Material.LAPIS_ORE, "lapis");
		materials.put(Material.DIAMOND_ORE, "diamond");
		materials.put(Material.REDSTONE_ORE, "redstone");
		materials.put(Material.EMERALD_ORE, "emerald");
		try {
			materials.put(Material.NETHER_QUARTZ_ORE, "quartz");
		} catch (Throwable t) {
			materials.put(Material.getMaterial("QUARTZ_ORE"), "quartz");
			materials.put(Material.getMaterial("GLOWING_REDSTONE_ORE"), "redstone");
		}
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
		long now = System.currentTimeMillis();
		for (Iterator<WorldBlock> iterator = blocksFound.keySet().iterator(); iterator.hasNext();) {
			WorldBlock wb = iterator.next();
			Long timeL = blocksFound.get(wb);
			if (timeL == null) {
				iterator.remove();
				continue;
			}
			long time = timeL;
			if (time + 1200000L <= now) { // 20 minutes
				iterator.remove();
			}
		}
	}

	private static final BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void blockBreakEvent(BlockBreakEvent event) {
		Block block = event.getBlock();
		Material material = block.getType();
		if (!materials.containsKey(material)) {
			return;
		}
		WorldBlock wb = new WorldBlock(block);
		if (blocksFound.containsKey(wb)) {
			return;
		}
		Set<Block> findSimilarAdjacentBlocks = findSimilarAdjacentBlocks(block);
		long now = System.currentTimeMillis();
		for (Block b : findSimilarAdjacentBlocks) {
			blocksFound.put(new WorldBlock(b), now);
		}
		Player p = event.getPlayer();
		String materialName = materials.get(material);
		int count = findSimilarAdjacentBlocks.size();
		int lightLevel = (int) block.getLightLevel();
		for (BlockFace f : faces) {
			try {
				lightLevel = Math.max(lightLevel, block.getRelative(f).getLightLevel());
			} catch (Exception e) {
			}
		}
		sendMine(p, block, materialName, count, lightLevel);
	}

	private Set<Block> findSimilarAdjacentBlocks(Block block) {
		Material material = block.getType();
		World w = block.getWorld();
		Set<Block> blocksToRecurseFrom = new HashSet<>();
		Set<Block> checkedBlocks = new HashSet<>();
		Set<Block> matchedBlocks = new HashSet<>();
		blocksToRecurseFrom.add(block);
		checkedBlocks.add(block);
		matchedBlocks.add(block);
		Set<Block> newBlocks = new HashSet<>();
		while (true) {
			for (Block b : blocksToRecurseFrom) {
				int originalX = b.getX();
				int originalY = b.getY();
				int originalZ = b.getZ();
				for (int y = Math.max(0, originalY - 1); y <= Math.min(255, originalY + 1); y++) {
					for (int z = originalZ - 1; z <= originalZ + 1; z++) {
						for (int x = originalX - 1; x <= originalX + 1; x++) {
							if (x == originalX && y == originalY && z == originalZ) {
								continue;
							}
							Block bl = w.getBlockAt(x, y, z);
							if (checkedBlocks.contains(bl)) {
								continue;
							}
							checkedBlocks.add(bl);
							Material blockType = bl.getType();
							if (isSameMaterial(material, blockType)) {
								matchedBlocks.add(bl);
								newBlocks.add(bl);
								if (matchedBlocks.size() >= 100000) {
									return matchedBlocks;
								}
							}
						}
					}
				}
			}
			if (newBlocks.isEmpty()) {
				break;
			}
			blocksToRecurseFrom.clear();
			blocksToRecurseFrom.addAll(newBlocks);
			newBlocks.clear();
		}
		return matchedBlocks;
	}

	private boolean isSameMaterial(Material material1, Material material2) {
		return material1 == material2
				|| isSameMaterial_backwardscompatible(material1, material2);
	}

	private boolean isSameMaterial_backwardscompatible(Material material1, Material material2) {
		try {
			Material redstone = Material.REDSTONE_ORE;
			Material redstoneGlow = Material.getMaterial("GLOWING_REDSTONE_ORE");
			return material1 == redstone && material2 == redstoneGlow
					|| material1 == redstoneGlow && material2 == redstone;
		} catch (Throwable t) {
		}
		return false;
	}

	private void sendMine(Player p, Block block, String ore, int count, int lightLevel) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("MineWatch");
			out.writeUTF(block.getWorld().getName());
			out.writeInt(block.getX());
			out.writeInt(block.getY());
			out.writeInt(block.getZ());
			out.writeUTF(ore);
			out.writeInt(count);
			out.writeInt(lightLevel);

			p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

}
