package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class ActionLocation {

	public final String world;
	public final double x, y, z;
	public final float pitch, yaw;

	public ActionLocation(Location location) {
		this(
				location.getWorld().getName(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getPitch(),
				location.getYaw()
		);
	}

	public ActionLocation(String world, double x, double y, double z, float pitch, float yaw) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ActionLocation) {
			return equals((ActionLocation) other);
		} else {
			return false;
		}
	}

	public boolean equals(ActionLocation other) {
		if (other == null) {
			return false;
		}
		return (world == null ? (other.world == null) : world.equals(other.world))
				&& x == other.x
				&& y == other.y
				&& z == other.z
				&& pitch == other.pitch
				&& yaw == other.yaw;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.world);
		hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 79 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
		hash = 79 * hash + (int) (Double.doubleToLongBits(this.pitch) ^ (Double.doubleToLongBits(this.pitch) >>> 32));
		hash = 79 * hash + (int) (Double.doubleToLongBits(this.yaw) ^ (Double.doubleToLongBits(this.yaw) >>> 32));
		return hash;
	}

	public Chunk getBukkitChunk() {
		return Bukkit.getWorld(world).getChunkAt(((int) Math.floor(x)) >> 4, ((int) Math.floor(z)) >> 4);
	}

	public Block getBukkitBlock() {
		return Bukkit.getWorld(world).getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
	}

	public Location getBukkitLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	@Override
	public String toString() {
		return "ActionLocation(" + world + ", " + (Math.round(x*100.0)/100.0) + ", " + (Math.round(y*100.0)/100.0) + ", " + (Math.round(z*100.0)/100.0) + ")";
	}
}
