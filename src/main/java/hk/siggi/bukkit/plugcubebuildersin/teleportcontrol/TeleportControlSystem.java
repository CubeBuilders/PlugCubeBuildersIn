package hk.siggi.bukkit.plugcubebuildersin.teleportcontrol;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TeleportControlSystem {

	public boolean playerTPA(Player from, Player to, boolean tpaHere);

	public boolean goHome(Player player, Location home);

	public boolean setHome(Player player);
}
