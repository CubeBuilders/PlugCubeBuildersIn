package hk.siggi.bukkit.plugcubebuildersin.module;

import com.lishid.openinv.OpenInv;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.entity.Player;

public class OpenInvModuleImpl implements OpenInvModule {

	private PlugCubeBuildersIn plugin;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}
	
	@Override
	public boolean getSilentChest(Player p) {
		return OpenInv.getPlayerSilentChestStatus(p);
	}

	@Override
	public void setSilentChest(Player p, boolean on) {
		OpenInv.setPlayerSilentChestStatus(p, on);
	}

}
