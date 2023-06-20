package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.personalspace.PersonalSpace;
import hk.siggi.bukkit.personalspace.Space;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import java.util.UUID;

public class PersonalSpaceModuleImpl implements PersonalSpaceModule {

	public PlugCubeBuildersIn plugin;
	public PersonalSpace personalSpace;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		personalSpace = (PersonalSpace) plugin.getServer().getPluginManager().getPlugin("PersonalSpace");
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}

	@Override
	public boolean loadWorld(String name) {
		if (name.startsWith("spaces/space_")) {
			String id = name.substring(13);
			String subid = "";
			int underscore = id.indexOf("_");
			if (underscore >= 0) {
				subid = id.substring(underscore);
				id = id.substring(0, underscore);
			}
			UUID spaceId = Util.uuidFromString(id);
			Space space = personalSpace.getSpace(spaceId);
			if (space != null) {
				space.load();
				return true;
			}
		}
		return false;
	}
}
