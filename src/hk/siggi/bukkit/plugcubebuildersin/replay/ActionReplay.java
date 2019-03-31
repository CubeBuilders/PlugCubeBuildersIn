package hk.siggi.bukkit.plugcubebuildersin.replay;

import hk.siggi.bukkit.plugcubebuildersin.module.Module;
import org.bukkit.entity.Player;

public interface ActionReplay extends Module {
	public boolean isPlayerWatchingReplay(Player p);
}
