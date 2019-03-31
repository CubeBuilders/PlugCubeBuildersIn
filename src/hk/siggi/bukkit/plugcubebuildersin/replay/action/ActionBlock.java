package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class ActionBlock {

	public final String world;
	public final int x, y, z;

	public ActionBlock(Block block) {
		this(
				block.getWorld().getName(),
				block.getX(),
				block.getY(),
				block.getZ()
		);
	}

	public ActionBlock(Location location) {
		this(
				location.getWorld().getName(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ()
		);
	}

	public ActionBlock(String world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ActionBlock) {
			return equals((ActionBlock) other);
		} else {
			return false;
		}
	}

	public boolean equals(ActionBlock other) {
		if (other == null) {
			return false;
		}
		return (world == null ? (other.world == null) : world.equals(other.world))
				&& x == other.x
				&& y == other.y
				&& z == other.z;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + Objects.hashCode(this.world);
		hash = 67 * hash + this.x;
		hash = 67 * hash + this.y;
		hash = 67 * hash + this.z;
		return hash;
	}

	public Chunk getBukkitChunk() {
		return Bukkit.getWorld(world).getChunkAt(x >> 4, z >> 4);
	}

	public Block getBukkitBlock() {
		return Bukkit.getWorld(world).getBlockAt(x, y, z);
	}

	public Location getBukkitLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	@Override
	public String toString() {
		return "ActionBlock(" + world + ", " + x + ", " + y + ", " + z + ")";
	}
}
