package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.backends.PermissionBackend;
import ru.tehkode.permissions.backends.memory.MemoryBackend;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsExModuleImpl implements PermissionsExModule {

	private PlugCubeBuildersIn plugin;
	private PermissionsEx pex;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		this.pex = (PermissionsEx) plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}

	@Override
	public PermissionsExGroupImpl getGroup(String name) {
		PermissionGroup g = pex.getPermissionsManager().getGroup(name);
		return wrap(g);
	}

	@Override
	public List<PermissionsExGroup> getGroups() {
		List<PermissionGroup> gl = pex.getPermissionsManager().getGroupList();
		List<PermissionsExGroup> list = new ArrayList<>(gl.size());
		for (PermissionGroup g : gl) {
			list.add(wrap(g));
		}
		return list;
	}

	@Override
	public void deleteGroup(String name) {
		pex.getPermissionsManager().resetGroup(name);
	}

	@Override
	public void erasePermissionsAndSetMemory() {
		try {
			handleMap.clear();
			PermissionManager permissionManager = pex.getPermissionsManager();
			PermissionBackend backend = permissionManager.getBackend();
			if (!(backend instanceof MemoryBackend)) {
				permissionManager.setBackend("memory");
			} else {
				//Method clearCache = PermissionManager.class.getDeclaredMethod("clearCache");
				//clearCache.setAccessible(true);
				//clearCache.invoke(permissionManager);
				Field groupsField = PermissionManager.class.getDeclaredField("groups");
				groupsField.setAccessible(true);
				Map groups = (Map) groupsField.get(permissionManager);
				Field usersField = PermissionManager.class.getDeclaredField("users");
				usersField.setAccessible(true);
				Map users = (Map) usersField.get(permissionManager);
				groups.clear();
				users.clear();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void setGroups(UUID player, List<String> groups) {
		PermissionManager permissionManager = pex.getPermissionsManager();
		PermissionUser user = permissionManager.getUser(player);
		List<PermissionGroup> gr = new ArrayList<>(groups.size());
		for (String group : groups) {
			PermissionGroup g = permissionManager.getGroup(group);
			gr.add(g);
		}
		user.setParents(gr, null);
	}

	@Override
	public List<String> getGroups(UUID player) {
		PermissionManager permissionManager = pex.getPermissionsManager();
		PermissionUser user = permissionManager.getUser(player);
		List<PermissionGroup> gr = user.getParents();
		List<String> groups = new ArrayList<>(gr.size());
		for (PermissionGroup g : gr) {
			groups.add(g.getName());
		}
		return groups;
	}

	private final Map<PermissionGroup, PermissionsExGroupImpl> handleMap = new WeakHashMap<>();

	private PermissionsExGroupImpl wrap(PermissionGroup handle) {
		PermissionsExGroupImpl result = handleMap.get(handle);
		if (result == null) {
			handleMap.put(handle, result = new PermissionsExGroupImpl(handle));
		}
		return result;
	}

	public class PermissionsExGroupImpl implements PermissionsExGroup<PermissionsExGroupImpl> {

		private final PermissionGroup handle;

		public PermissionsExGroupImpl(PermissionGroup handle) {
			this.handle = handle;
		}

		@Override
		public String getName() {
			return handle.getName();
		}

		@Override
		public void setParent(String parent) {
			setParents(parent);
		}

		@Override
		public void setParents(String... parents) {
			List<PermissionGroup> l = new ArrayList<>(parents.length);
			for (String p : parents) {
				l.add(pex.getPermissionsManager().getGroup(p));
			}
			handle.setParents(l);
		}

		@Override
		public void setParent(PermissionsExGroupImpl parent) {
			setParents(parent);
		}

		@Override
		public void setParents(PermissionsExGroupImpl... parents) {
			List<PermissionGroup> l = new ArrayList<>(parents.length);
			for (PermissionsExGroupImpl p : parents) {
				l.add(p.handle);
			}
			handle.setParents(l);
		}

		@Override
		public List<PermissionsExGroup> getParents() {
			List<PermissionGroup> parents = handle.getParents();
			List<PermissionsExGroup> l = new ArrayList<>(parents.size());
			for (PermissionGroup p : parents) {
				l.add(wrap(p));
			}
			return l;
		}

		@Override
		public void addPermission(String permission) {
			handle.addPermission(permission);
		}

		@Override
		public void deletePermission(String permission) {
			handle.removePermission(permission);
		}

		@Override
		public void addPermission(String permission, String world) {
			handle.addPermission(permission, world);
		}

		@Override
		public void deletePermission(String permission, String world) {
			handle.removePermission(permission, world);
		}

		@Override
		public List<String> getPermissions(String world) {
			return handle.getOwnPermissions(world);
		}

		@Override
		public Map<String, List<String>> getPermissions() {
			return handle.getAllPermissions();
		}
	}
}
