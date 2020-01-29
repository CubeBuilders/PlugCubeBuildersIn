package hk.siggi.bukkit.plugcubebuildersin.module;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface EssentialsSpawnModule extends Module {
	public Location getSpawn(Player p);
}
