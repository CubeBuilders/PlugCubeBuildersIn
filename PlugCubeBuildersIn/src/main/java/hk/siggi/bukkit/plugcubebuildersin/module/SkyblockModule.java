package hk.siggi.bukkit.plugcubebuildersin.module;

import org.bukkit.entity.Player;

public interface SkyblockModule extends Module, WorldLoaderModule {
	public void setBypassing(Player player, boolean bypass);
	public boolean isBypassing(Player player);
}
