package hk.siggi.bukkit.plugcubebuildersin.module;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.LocationFlag;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class WorldGuardModuleImpl implements WorldGuardModule {

	private PlugCubeBuildersIn plugin;
	private WorldGuardPlugin worldGuard;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
		setupWorldGuardFlags();
	}

	@Override
	public void init() {
		worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	}

	@Override
	public void kill() {
	}

	private int pretick = 0;
	private boolean worldGuardPassed = false;

	@Override
	public void tick() {
		if (pretick < 40) {
			pretick += 1;
			return;
		}
		ApplicableRegionSet worldGuardRegionSet = getWorldGuardRegionSet(plugin.getServer().getWorlds().get(0).getSpawnLocation());
		if (worldGuardRegionSet == null) {
			for (int i = 0; i < 10; i++) {
				System.out.println("WorldGuard has crashed! Shutting server down!");
			}
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "stop");
		} else if (!worldGuardPassed) {
			worldGuardPassed = true;
			System.out.println("WorldGuard launched OK");
		}
	}

	@Override
	public Location getQuitLocation(Location location) {
		try {
			com.sk89q.worldedit.Location worldEditLocation = getWorldGuardRegionSet(location).getFlag(quitLocation);
			if (worldEditLocation != null) {
				com.sk89q.worldedit.Vector position = worldEditLocation.getPosition();
				Location loc = new Location(plugin.getServer().getWorld(worldEditLocation.getWorld().getName()), position.getX(), position.getY(), position.getZ(), worldEditLocation.getYaw(), worldEditLocation.getPitch());
				return loc;
			}
		} catch (Exception e) {
		}
		return null;
	}

	private boolean addWorldGuardFlag(Flag<?> flag) {
		addFlag:
		try {
			Plugin p = (Plugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
			if (p == null) {
				break addFlag;
			}
			try {
				WorldGuardPlugin worldguard = (WorldGuardPlugin) p;
				worldguard.getFlagRegistry().register(flag);
				return true;
			} catch (Throwable t) {
				try {
					Field flags = DefaultFlag.class.getDeclaredField("flagsList");
					Field modifiers = Field.class.getDeclaredField("modifiers");
					modifiers.setAccessible(true);
					modifiers.setInt(flags, flags.getModifiers() & ~Modifier.FINAL);
					Flag<?>[] oldFlagsList = DefaultFlag.flagsList;
					Flag<?>[] newFlagsList = new Flag<?>[oldFlagsList.length + 1];
					System.arraycopy(oldFlagsList, 0, newFlagsList, 0, oldFlagsList.length);
					newFlagsList[newFlagsList.length - 1] = flag;
					flags.set(null, newFlagsList);
					return true;
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	private LocationFlag quitLocation = null;

	private void setupWorldGuardFlags() {
		if (quitLocation == null) {
			quitLocation = new LocationFlag("quit");
			addWorldGuardFlag(quitLocation);
		}
	}

	private ApplicableRegionSet getWorldGuardRegionSet(Location location) {
		try {
			return worldGuard.getGlobalRegionManager().get(location.getWorld()).getApplicableRegions(BukkitUtil.toVector(location));
		} catch (Exception e) {
			return null;
		}
	}
}
