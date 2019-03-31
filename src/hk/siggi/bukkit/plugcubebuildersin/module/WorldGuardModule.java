package hk.siggi.bukkit.plugcubebuildersin.module;

import org.bukkit.Location;

public interface WorldGuardModule extends Module {
	public Location getQuitLocation(Location location);
}
