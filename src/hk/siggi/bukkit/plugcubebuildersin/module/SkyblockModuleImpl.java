package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import hk.siggi.bukkit.skyblock.Island;
import hk.siggi.bukkit.skyblock.Skyblock;
import java.util.UUID;
import org.bukkit.entity.Player;

public class SkyblockModuleImpl implements SkyblockModule {

	public PlugCubeBuildersIn plugin;
	public Skyblock skyblock;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		skyblock = (Skyblock) plugin.getServer().getPluginManager().getPlugin("Skyblock");
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}

	@Override
	public boolean loadWorld(String name) {
		if (name.startsWith("islands/island_")) {
			String id = name.substring(15);
			String subid = "";
			int underscore = id.indexOf("_");
			if (underscore >= 0) {
				subid = id.substring(underscore);
				id = id.substring(0, underscore);
			}
			UUID islandId = Util.uuidFromString(id);
			Island island = skyblock.getIslandByUUID(islandId);
			if (island != null) {
				island.load();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setBypassing(Player player, boolean bypassing) {
		skyblock.setBypassing(player, bypassing);
	}
	
	@Override
	public boolean isBypassing(Player player) {
		return skyblock.isBypassing(player);
	}
}
