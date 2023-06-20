package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;


public class PhantomRestricterModuleImpl implements PhantomRestricterModule, Listener {

	private PlugCubeBuildersIn plugin;
	
	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}
	
	@EventHandler
	public void spawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL && event.getEntityType() == EntityType.PHANTOM) {
			World world = event.getLocation().getWorld();
			long time = world.getTime();
			int day = (int) (time / 24000);
			if (day % 8 != 0) {
				// Phantoms can only spawn on full moon nights
				event.setCancelled(true);
			}
		}
	}
	
}
