package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class WarpPortalsModuleImpl implements WarpPortalsModule {

	private PlugCubeBuildersIn plugin = null;
	private int tick = 0;

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
		if (tick == 0) {
			cleanup();
		}
		tick++;
		if (tick >= 864000) { // 12 hours worth of ticks
			tick = 0;
		}
	}

	private void cleanup() {
		long now = System.currentTimeMillis();
		long expire = now - (43200000L); // 12 hours ago
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss");
			File wp = new File(plugin.getDataFolder().getParentFile(), "WarpPortals");
			File[] ff = wp.listFiles();
			if (ff == null) {
				return;
			}
			for (File f : ff) {
				String name = f.getName();
				if (name.startsWith("portals_") && name.endsWith(".bac")) {
					try {
						String dt = name.substring(8, name.length() - 4);
						long date = sdf.parse(dt).getTime();
						if (date < expire) {
							f.delete();
						}
					} catch (ParseException e) {
					}
				}
			}
		} catch (Exception e) {
		}
	}

}
