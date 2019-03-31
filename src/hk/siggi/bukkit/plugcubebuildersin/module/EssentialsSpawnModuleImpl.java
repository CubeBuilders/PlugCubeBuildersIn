package hk.siggi.bukkit.plugcubebuildersin.module;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.spawn.EssentialsSpawn;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EssentialsSpawnModuleImpl implements EssentialsSpawnModule {

	public PlugCubeBuildersIn plugin;
	public Essentials essentials;
	public EssentialsSpawn essentialsSpawn;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
		essentialsSpawn = (EssentialsSpawn) plugin.getServer().getPluginManager().getPlugin("EssentialsSpawn");
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}

	@Override
	public Location getSpawn(Player p) {
		User user = essentials.getUser(p);
		String group = user.getGroup();
		return essentialsSpawn.getSpawn(group);
	}
}
