package hk.siggi.bukkit.plugcubebuildersin;

import hk.siggi.bukkit.plugcubebuildersin.module.EssentialsModule;
import hk.siggi.bukkit.plugcubebuildersin.module.FactionsModule;
import hk.siggi.bukkit.plugcubebuildersin.module.PermissionsExModule;
import hk.siggi.bukkit.plugcubebuildersin.module.SkyblockModule;
import hk.siggi.bukkit.plugcubebuildersin.music.MusicPlayer;
import hk.siggi.bukkit.plugcubebuildersin.nms.ChatSetting;
import hk.siggi.bukkit.plugcubebuildersin.playerstate.PlayerState;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlayerSession {

	public final PlugCubeBuildersIn plugin;
	public final Player player;

	public boolean forceShowAll = false;

	public long nextWarnChatSetting = 0L;
	public ChatSetting lastChatSetting = null;

	public boolean didAutoBan = false;

	public boolean signEdit = false;
	public Block signEditBlock = null;

	public Block secretCodeBlock = null;
	public int secretCodeRandomNumber = 0;

	public double speedUpMultiplier = 1.0;

	public boolean moved = false;
	int autobanGracePeriod;
	public long unvanishTime = 0L;
	public boolean vanishProtection = true;
	
	public long loginTime = 0L;
	
	private int backupTimeSinceRest = 0;

	PlayerSession(PlugCubeBuildersIn plugin, Player player) {
		groupList.add("default");
		this.plugin = plugin;
		this.player = player;
	}

	public MusicPlayer musicPlayer = null;

	private final List<String> groupList = new ArrayList<>();

	private boolean staffPerms = false;
	public PlayerState staffPermsOffState = null;

	public boolean getStaffPerms() {
		return staffPerms;
	}

	public void setStaffPerms(boolean newStaffPerms) {
		if (staffPerms && !newStaffPerms) {
			staffPermsOff();
		}
		if (!staffPerms && newStaffPerms) {
			staffPermsOn();
		}
	}

	private static File getStaffStateDir() {
		File f = new File(PlugCubeBuildersIn.getInstance().getDataFolder(), "staffstate");
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}

	private static File getStaffStateFile(Player p) {
		return getStaffStateFile(p.getUniqueId());
	}

	private static File getStaffStateFile(UUID uuid) {
		return new File(getStaffStateDir(), uuid.toString().replace("-", "") + ".json");
	}

	public boolean canUseStaffToggle() {
		for (String group : groupList) {
			if (isStaffRestrictedGroup(group)) {
				return true;
			}
		}
		return false;
	}

	private boolean isStaffRestrictedGroup(String group) {
		return group.equals("moderator")
				|| group.equals("admin")
				|| group.equals("manager")
				|| group.equals("coowner")
				|| group.equals("owner");
	}

	private void staffPermsOn() {
		staffPerms = true;
		staffPermsOffState = new PlayerState(player, player.getLocation());
		PlayerState staffPermsOnState;
		try {
			String stateJson = Util.readToString(getStaffStateFile(player));
			staffPermsOnState = PlayerState.fromJson(stateJson);
		} catch (Exception e) {
			staffPermsOnState = new PlayerState(player, player.getLocation());
			staffPermsOnState.blankState();
		}
		updateGroups();
		FactionsModule fac = plugin.getModule(FactionsModule.class);
		if (fac != null) {
			fac.setBypassing(player, true);
		}
		SkyblockModule sky = plugin.getModule(SkyblockModule.class);
		if (sky != null) {
			sky.setBypassing(player, true);
		}
		backupTimeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
		player.setStatistic(Statistic.TIME_SINCE_REST, 0); // stop phantoms
		staffPermsOnState.apply(player, GameMode.SURVIVAL, true);

		// teleport is not really necessary for turning on.
		// just for turning off when we return the player to original state.
		//staffPermsOnState.teleport(player);
	}

	private void staffPermsOff() {
		staffPerms = false;
		PlayerState staffPermsOnState = new PlayerState(player, player.getLocation());
		try {
			String stateJson = staffPermsOnState.toJson();
			Util.writeToFile(getStaffStateFile(player), stateJson);
		} catch (Exception e) {
		}
		if (staffPermsOffState == null) {
			staffPermsOffState = new PlayerState(player, player.getLocation());
			staffPermsOffState.blankState();
		}
		player.setOp(false);
		updateGroups();
		FactionsModule fac = plugin.getModule(FactionsModule.class);
		if (fac != null) {
			fac.setBypassing(player, false);
		}
		SkyblockModule sky = plugin.getModule(SkyblockModule.class);
		if (sky != null) {
			sky.setBypassing(player, false);
		}
		EssentialsModule ess = plugin.getModule(EssentialsModule.class);
		if (ess != null && ess.isGodmode(player)) {
			ess.setGodmode(player, false);
		}
		staffPermsOffState.apply(player, GameMode.SURVIVAL, true);
		staffPermsOffState.teleport(player);

		player.setStatistic(Statistic.TIME_SINCE_REST, backupTimeSinceRest); // restore previous statistic for phantoms
	}

	public void logout() {
		setStaffPerms(false);
	}

	public void setGroups(String[] groupList) {
		this.groupList.clear();
		this.groupList.addAll(Arrays.asList(groupList));
		updateGroups();
	}

	public void updateGroups() {
		boolean restrictStaffPrivileges = false;
		if (!staffPerms && PlugCubeBuildersIn.getInstance().staffPrivilegeToggle) {
			restrictStaffPrivileges = true;
		}
		List<String> newList = new LinkedList<>();
		for (String str : groupList) {
			String groupToAdd = str;
			if (restrictStaffPrivileges && isStaffRestrictedGroup(groupToAdd)) {
				groupToAdd = "helper";
			}
			if (!newList.contains(groupToAdd)) {
				newList.add(groupToAdd);
			}
		}
		plugin.updateBungeeGroups(player, newList);
		plugin.getModule(PermissionsExModule.class).setGroups(player.getUniqueId(), newList);
//		StringBuilder gl = new StringBuilder();
//		for (String str : newList) {
//			if (gl.length() > 0) {
//				gl.append(",");
//			}
//			gl.append(str);
//		}
//		String groupListStr = gl.toString();
//		player.getServer().dispatchCommand(player.getServer().getConsoleSender(), "pex user " + player.getName() + " group set " + groupListStr);
	}

	private String visibilityGroup = "";

	public void setVisibilityGroup(String group) {
		if (group == null) {
			group = "";
		}
		visibilityGroup = group;
	}

	public String getVisibilityGroup() {
		return visibilityGroup;
	}
	
	public boolean canHandleVanish(){
		long now = System.currentTimeMillis();
		return now - loginTime >= 1000L;
	}
}
