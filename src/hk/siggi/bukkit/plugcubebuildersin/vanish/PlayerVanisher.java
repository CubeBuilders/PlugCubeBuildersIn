package hk.siggi.bukkit.plugcubebuildersin.vanish;

import org.bukkit.entity.Player;

public interface PlayerVanisher {
	public boolean canSee(Player viewer, Player target);
}
