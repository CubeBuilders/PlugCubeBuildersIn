package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PrismModuleImpl implements PrismModule {

	private PlugCubeBuildersIn plugin;
	private Plugin prism;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		prism = plugin.getServer().getPluginManager().getPlugin("Prism");
	}

	@Override
	public void kill() {
	}

	private long nextPrismClean;
	private int pretick = 0;
	private boolean prismPassed = false;

	@Override
	public void tick() {
		if (pretick < 40) {
			pretick += 1;
			if (pretick == 40) {
				nextPrismClean = System.currentTimeMillis() + 10000L;
			}
			return;
		}
		if (!prism.isEnabled()) {
			for (int i = 0; i < 10; i++) {
				System.out.println("Prism has crashed! Shutting server down!");
			}
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "stop");
		} else if (!prismPassed) {
			prismPassed = true;
			System.out.println("Prism launched OK");
		}
		if (System.currentTimeMillis() >= nextPrismClean) {
			nextPrismClean = nextPrismClean + 3600000L;
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pr purge before:14d");
		}
	}
}
