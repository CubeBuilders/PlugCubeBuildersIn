package hk.siggi.bukkit.plugcubebuildersin.module;

import org.bukkit.entity.Player;

public interface OpenInvModule extends Module {
	public boolean getSilentChest(Player p);
	public void setSilentChest(Player p, boolean on);
}
