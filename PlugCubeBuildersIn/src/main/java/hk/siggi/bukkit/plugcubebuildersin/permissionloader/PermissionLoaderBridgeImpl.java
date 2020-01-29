package hk.siggi.bukkit.plugcubebuildersin.permissionloader;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.module.PermissionsExModule;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

class PermissionLoaderBridgeImpl implements PermissionLoaderBridge {

	private final PlugCubeBuildersIn plugin;
	private final PermissionsExModule module;

	public PermissionLoaderBridgeImpl() {
		plugin = PlugCubeBuildersIn.getInstance();
		module = plugin.getModule(PermissionsExModule.class);
	}

	@Override
	public boolean checkPermission(String permission) {
		return false;
	}

	@Override
	public void clearPermissions() {
		module.erasePermissionsAndSetMemory();
	}

	@Override
	public void setParent(String child, String... parent) {
		module.getGroup(child).setParents(parent);
	}

	@Override
	public void addPermission(String group, String permission) {
		module.getGroup(group).addPermission(permission);
	}

	@Override
	public void finishUp() {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			plugin.getSession(p).updateGroups();
		}
	}

	@Override
	public String getValueOfVariable(String variable) {
		if (variable.equals("server")) {
			return plugin.getServerName();
		}
		return "";
	}

	@Override
	public boolean checkForPlugin(String plugin) {
		Plugin pp = this.plugin.getServer().getPluginManager().getPlugin(plugin);
		return pp != null;
	}

	@Override
	public void setupTimerTask(final PermissionLoader loader) {
		new BukkitRunnable() {
			@Override
			public void run() {
				loader.runLoop();
			}
		}.runTaskTimer(plugin, 20L, 20L);
	}

}
