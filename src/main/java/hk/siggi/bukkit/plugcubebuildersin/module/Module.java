package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;

public interface Module {
	public void load(PlugCubeBuildersIn plugin);
	public void init();
	public void kill();
	public void tick();
}
