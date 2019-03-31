package hk.siggi.bukkit.plugcubebuildersin.module;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PermissionsExModule extends Module {
	public PermissionsExGroup getGroup(String name);
	public List<PermissionsExGroup> getGroups();
	public void deleteGroup(String name);
	public void erasePermissionsAndSetMemory();
	public void setGroups(UUID player, List<String> groups);
	public List<String> getGroups(UUID player);
	public interface PermissionsExGroup<G extends PermissionsExGroup> {
		public String getName();
		public void setParent(String parent);
		public void setParents(String... parent);
		public void setParent(G parent);
		public void setParents(G... parent);
		public List<PermissionsExGroup> getParents();
		public void addPermission(String permission);
		public void deletePermission(String permission);
		public void addPermission(String permission, String world);
		public void deletePermission(String permission, String world);
		public List<String> getPermissions(String world);
		public Map<String,List<String>> getPermissions();
	}
}
