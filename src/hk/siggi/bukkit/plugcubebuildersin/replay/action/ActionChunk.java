package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class ActionChunk {

	public final String world;
	public final int x, z;

	public ActionChunk(String world, int x, int z) {
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public ActionChunk(Block block) {
		this(block.getWorld().getName(), block.getX() >> 4, block.getZ() >> 4);
	}

	public ActionChunk(Location loc) {
		this(loc.getWorld().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
	}
	
	public ActionChunk(Chunk chunk) {
		this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}

	public ActionChunk(ActionBlock block) {
		this(block.world, block.x >> 4, block.z >> 4);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ActionChunk) {
			return equals((ActionChunk) other);
		} else {
			return false;
		}
	}

	public boolean equals(ActionChunk other) {
		if (other == null) {
			return false;
		}
		return (world == null ? (other.world == null) : world.equals(other.world))
				&& x == other.x
				&& z == other.z;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 37 * hash + Objects.hashCode(this.world);
		hash = 37 * hash + this.x;
		hash = 37 * hash + this.z;
		return hash;
	}

	public Chunk getBukkitChunk() {
		return Bukkit.getWorld(world).getChunkAt(x, z);
	}
	public boolean isLoaded() {
		return Bukkit.getWorld(world).isChunkLoaded(x, z);
	}

	@Override
	public String toString() {
		return "ActionChunk(" + world + ", " + x + ", " + z + ")";
	}
}
