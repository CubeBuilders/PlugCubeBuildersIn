package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.VariableListener;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.getInfo;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.removeItem;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.setInfo;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.setNameAndLore;
import hk.siggi.bukkit.plugcubebuildersin.serverwatcherapi.ServerWatcherAPI;
import hk.siggi.bukkit.plugcubebuildersin.vanish.PlayerVanisher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

// delete classicMinigames.txt from official server to enable this module
public class MinigameHubModuleImpl implements MinigameHubModule, VariableListener, Listener, PlayerVanisher {

	PlugCubeBuildersIn plugin;
	private final List<MinigameSession> sessions = new ArrayList<>();
	private final Map<UUID, MinigamePlayer> players = new HashMap<>();
	private boolean enabled = false;
	private boolean registeredEvents = false;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		plugin.addVariableListener(this);
		if (!registeredEvents) {
			registeredEvents = true;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
		enabled = true;
	}

	@Override
	public void kill() {
		enabled = false;
		plugin.removeVariableListener(this);
		for (Iterator<MinigameSession> it = sessions.iterator(); it.hasNext();) {
			MinigameSession session = it.next();
			it.remove();
			session.close();
		}
		players.clear();
	}

	@Override
	public void tick() {
		for (Iterator<MinigameSession> it = sessions.iterator(); it.hasNext();) {
			MinigameSession session = it.next();
			if (!session.isOpen()) {
				it.remove();
			} else if (session.getPlayers().length == 0) {
				session.close();
				it.remove();
			} else {
				session.tick();
			}
		}
		for (Iterator<MinigamePlayer> it = players.values().iterator(); it.hasNext();) {
			MinigamePlayer player = it.next();
			if (player.hasExpired()) {
				it.remove();
			}
		}
		for (Iterator<Inventory> it = inventoryHandlers.keySet().iterator(); it.hasNext();) {
			Inventory inv = it.next();
			if (inv.getViewers().isEmpty()) {
				it.remove();
			}
		}
	}

	@Override
	public void receivedVariable(String variable, String value) {
		int dotPos = variable.indexOf(".");
		if (dotPos == -1) {
			return;
		}
		String server = variable.substring(0, dotPos);
		variable = variable.substring(dotPos + 1);
		MinigameSession session = null;
		for (MinigameSession asession : sessions) {
			String aserver = asession.getServer();
			if (aserver != null && aserver.equals(server)) {
				session = asession;
				break;
			}
		}
		if (session == null) {
			return;
		}
		if (variable.equals("status")) {
			session.serverStatusUpdated(Integer.parseInt(value));
		}
	}

	@Override
	public void receivedMessage(String from, byte[] data) {
		MinigameSession session = null;
		for (MinigameSession asession : sessions) {
			String aserver = asession.getServer();
			if (aserver != null && aserver.equals(from)) {
				session = asession;
				break;
			}
		}
		if (session == null) {
			return;
		}
		// do something
	}

	public MinigamePlayer getPlayer(Player p) {
		return getPlayer(p.getUniqueId());
	}

	public MinigamePlayer getPlayer(UUID p) {
		MinigamePlayer pl = players.get(p);
		if (pl == null || pl.hasExpired()) {
			players.put(p, pl = new MinigamePlayer(p));
		}
		return pl;
	}

	public List<MinigameSession> getSessionsMatching(MinigameController controller) {
		return getSessionsMatching(controller.getClass());
	}

	public List<MinigameSession> getSessionsMatching(Class<? extends MinigameController> clazz) {
		String clazzName = clazz.getName();
		List<MinigameSession> matched = new LinkedList<>();
		for (MinigameSession session : sessions) {
			if (session.isOpen() && session.controller != null && session.controller.getClass().getName().equals(clazzName)) {
				matched.add(session);
			}
		}
		return matched;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void join(final PlayerJoinEvent event) {
		if (!enabled) {
			return;
		}
		final Player p = event.getPlayer();
		final MinigamePlayer player = getPlayer(p);
		final MinigameSession session = player.getSession();
		MinigameUtil.removeItem(p, MinigameUtil.menuItem);
		MinigameUtil.removeItem(p, MinigameUtil.voteItem);
		MinigameUtil.giveItem(p, 0, MinigameUtil.menuItem);
		if (session != null) {
			if (session.controller != null) {
				plugin.setPublicChatGroup(p, session.getName());
				plugin.getSession(p).setVisibilityGroup(session.getName());
				session.controller.playerEnteredLobby(p, MinigameJoinType.ENTER_LOBBY_SERVER);
				if (session.isVoting()) {
					MinigameUtil.giveItem(p, 1, MinigameUtil.voteItem);
					new BukkitRunnable() {

						@Override
						public void run() {
							session.openVoteInventory(p);
						}
					}.runTask(plugin);
				}
			}
		} else {
			new BukkitRunnable() {

				@Override
				public void run() {
					openMinigameMenu(p);
				}
			}.runTask(plugin);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void quit(PlayerQuitEvent event) {
		if (!enabled) {
			return;
		}
		Player p = event.getPlayer();
		MinigamePlayer player = getPlayer(p);
		player.updateLastSeen();
		MinigameSession session = player.getSession();
		if (session != null && session.controller != null) {
			session.controller.playerQuitLobby(p);
		}
	}

	private final Map<Inventory, MinigameInventoryHandler> inventoryHandlers = new HashMap<>();

	void setInventoryHandler(Inventory inv, MinigameInventoryHandler handler) {
		inventoryHandlers.put(inv, handler);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerClickInventory(InventoryClickEvent event) {
		if (!enabled) {
			return;
		}
		InventoryView view = event.getView();
		Inventory inv = view.getTopInventory();
		MinigameInventoryHandler handler = inventoryHandlers.get(inv);
		if (handler != null) {
			handler.inventoryClick(event);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerCloseInventory(InventoryCloseEvent event) {
		if (!enabled) {
			return;
		}
		InventoryView view = event.getView();
		Inventory inv = view.getTopInventory();
		MinigameInventoryHandler handler = inventoryHandlers.get(inv);
		if (handler != null) {
			handler.inventoryClose(event);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerInteract(PlayerInteractEvent event) {
		if (!enabled) {
			return;
		}
		Action action = event.getAction();
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			if (processItemClick(event.getPlayer())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerInteract(PlayerInteractEntityEvent event) {
		if (!enabled) {
			return;
		}
		if (processItemClick(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void dropItem(PlayerDropItemEvent event) {
		if (!enabled) {
			return;
		}
		ItemStack stack = event.getItemDrop().getItemStack();
		if (MinigameUtil.isSpecialItem(stack)) {
			event.setCancelled(true);
		}
	}

	public boolean processItemClick(Player p) {
		ItemStack itemInMainHand = p.getInventory().getItemInMainHand();
		if (MinigameUtil.menuItem.test(itemInMainHand)) {
			openMinigameMenu(p);
			return true;
		} else if (MinigameUtil.voteItem.test(itemInMainHand)) {
			MinigamePlayer pl = getPlayer(p);
			MinigameSession sess = pl.getSession();
			if (sess == null || !sess.isVoting()) {
				removeItem(p, MinigameUtil.voteItem);
			} else {
				sess.openVoteInventory(p);
			}
			return true;
		}
		return false;
	}

	void openMinigameMenu(final Player p) {
		final MinigamePlayer pl = getPlayer(p);
		MinigameSession session = pl.getSession();
		if (session == null) {
			openMinigameSelectorMenu(p);
		} else {
			openMinigameSessionMenu(p);
		}
	}

	private void openMinigameSelectorMenu(final Player p) {
		final MinigamePlayer pl = getPlayer(p);
		final Inventory inv = Bukkit.createInventory(null, 54, "Minigames");

		List<MinigameController> minigameControllers = MinigameControllerLoader.getMinigameControllers();
		int slot = 0;
		for (int i = 0; i < minigameControllers.size() && slot < 53; i++) {
			MinigameController minigameController = minigameControllers.get(i);
			ItemStack stack = minigameController.getDisplayIcon();
			stack = setInfo(stack, "create:" + minigameController.getClass().getName());
			setNameAndLore(stack, ChatColor.RESET + minigameController.getDisplayName(), ChatColor.RESET + "Create a new session");
			inv.setItem(slot, stack);
			slot += 1;
		}

		slot = ((int) Math.ceil(((double) slot) / 9.0)) * 9;
		for (int i = 0; i < sessions.size() && slot < 53; i++) {
			try {
				MinigameSession session = sessions.get(sessions.size() - 1 - i);
				String skin = null;
				String skinSignature = null;
				UUID uuid = null;
				String name = null;
				try {
					uuid = session.players.get(0).getUniqueId();
					GameProfile profile = plugin.getGameProfile(new GameProfile(uuid, null));
					name = profile.getName();
					Collection<Property> textures = profile.getProperties().get("textures");
					Property texturesProperty = textures.iterator().next();
					skin = texturesProperty.value();
					skinSignature = texturesProperty.signature();
				} catch (Exception e) {
				}
				ItemStack skull = plugin.createSkull(uuid, name, skin, skinSignature);
				skull = setInfo(skull, "join:" + session.getUUID().toString());
				String gameName = "Session";
				if (session.controller != null) {
					gameName = session.controller.getDisplayName();
				}
				List<String> lore = new LinkedList<>();
				for (MinigamePlayer player : session.players) {
					UUID u = player.getUniqueId();
					String n = plugin.getUUIDCache().getNameFromUUID(u);
					if (n == null) {
						n = u.toString();
					}
					lore.add(ChatColor.RESET + n);
				}
				setNameAndLore(skull, ChatColor.RESET + "Join " + gameName, lore.toArray(new String[lore.size()]));
				inv.setItem(slot, skull);
				slot += 1;
			} catch (Exception e) {
			}
		}

		ItemStack quitItem = new ItemStack(Material.BARRIER);
		quitItem = setInfo(quitItem, "quit");
		quitItem = setNameAndLore(quitItem, "Quit to Lobby");
		inv.setItem(53, quitItem);

		InventoryView view = p.openInventory(inv);
		Inventory topInventory = view.getTopInventory();
		setInventoryHandler(topInventory, new MinigameInventoryHandler() {

			@Override
			public void inventoryClick(InventoryClickEvent event) {
				event.setCancelled(true);
				if (event.getClickedInventory() != event.getView().getTopInventory()) {
					return;
				}
				int slot = event.getSlot();
				if (slot >= 54) {
					return;
				}
				ItemStack item = inv.getItem(slot);
				String info = getInfo(item);
				if (info.startsWith("create:")) {
					String className = info.substring(7);
					MinigameController controller = null;
					for (MinigameController c : MinigameControllerLoader.getMinigameControllers()) {
						if (c.getClass().getName().equals(className)) {
							controller = c;
							break;
						}
					}
					if (controller == null) {
						setNameAndLore(item, ChatColor.RED + "An error has occurred! :/");
					} else {
						p.closeInventory();
						MinigameSession session = new MinigameSession(MinigameHubModuleImpl.this, new ServerWatcherAPI("127.0.0.1"));
						sessions.add(session);
						session.setMinigameController(controller);
						pl.joinSession(session);
						session.startServer();
						try {
							controller.newGame();
						} catch (Exception e) {
							PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on newGame()", e);
						}
					}
				} else if (info.startsWith("join:")) {
					UUID sessionID = UUID.fromString(info.substring(5));
					MinigameSession sess = null;
					for (MinigameSession s : sessions) {
						if (s.getUUID().equals(sessionID)) {
							sess = s;
							break;
						}
					}
					if (sess == null) {
						setNameAndLore(item, ChatColor.RED + "This session is no longer available");
						inv.setItem(slot, item);
					} else if (sess.isFull()) {
						setNameAndLore(item, ChatColor.RED + "This session is full");
						inv.setItem(slot, item);
					} else {
						p.closeInventory();
						pl.joinSession(sess);
					}
				} else if (info.equals("quit")) {
					plugin.quitToLobby(p);
				}
			}

			@Override
			public void inventoryClose(InventoryCloseEvent event) {
			}
		});
	}

	private void openMinigameSessionMenu(final Player p) {
		final MinigamePlayer pl = getPlayer(p);
		final Inventory inv = Bukkit.createInventory(null, 9, "Minigame Session");

		ItemStack changeMinigameItem = new ItemStack(getDoorItem());
		changeMinigameItem = setInfo(changeMinigameItem, "change");
		changeMinigameItem = setNameAndLore(changeMinigameItem, "Quit this Session");
		inv.setItem(3, changeMinigameItem);

		ItemStack quitItem = new ItemStack(Material.BARRIER);
		quitItem = setInfo(quitItem, "quit");
		quitItem = setNameAndLore(quitItem, "Quit to Lobby");
		inv.setItem(5, quitItem);

		InventoryView view = p.openInventory(inv);
		Inventory topInventory = view.getTopInventory();
		setInventoryHandler(topInventory, new MinigameInventoryHandler() {

			@Override
			public void inventoryClick(InventoryClickEvent event) {
				event.setCancelled(true);
				if (event.getClickedInventory() != event.getView().getTopInventory()) {
					return;
				}
				int slot = event.getSlot();
				ItemStack item = inv.getItem(slot);
				if (item == null) {
					return;
				}
				String info = getInfo(item);
				if (info.equals("change")) {
					pl.quitSession();
					openMinigameSelectorMenu(p);
				} else if (info.equals("quit")) {
					pl.quitSession();
					plugin.quitToLobby(p);
				}
			}

			@Override
			public void inventoryClose(InventoryCloseEvent event) {
			}
		});
	}

	/**
	 * 1.12/1.13 compatibility workaround.
	 *
	 * @return
	 */
	private static Material getDoorItem() {
		if (doorItem == null) {
			try {
				doorItem = Material.OAK_DOOR;
			} catch (Throwable t) {
				doorItem = Material.getMaterial("WOOD_DOOR");
			}
		}
		return doorItem;
	}
	private static Material doorItem;

	String genSessionName() {
		int id = 1;
		String newName;
		while (getSessionByName(newName = "mg" + toStr(id, 3)) != null) {
			id += 1;
		}
		return newName;
	}

	private MinigameSession getSessionByName(String name) {
		for (MinigameSession session : sessions) {
			if (session.getName().equals(name)) {
				return session;
			}
		}
		return null;
	}

	private String toStr(int number, int minDigits) {
		String s = Integer.toString(number);
		while (s.length() < minDigits) {
			s = "0" + s;
		}
		return s;
	}

	@EventHandler
	public void bungeeCordReady(PlayerRegisterChannelEvent e) {
		if (e.getChannel().equals("BungeeCord")) {
			Player p = e.getPlayer();
			MinigamePlayer player = getPlayer(p);
			MinigameSession session = player.getSession();
			if (session != null && session.controller != null) {
				plugin.setPublicChatGroup(p, session.getName());
			}
		}
	}

	@Override
	public boolean canSee(Player viewer, Player target) {
		MinigamePlayer v = getPlayer(viewer);
		MinigamePlayer t = getPlayer(target);
		MinigameSession sv = v.getSession();
		MinigameSession st = t.getSession();
		return sv == null ? st == null : sv.equals(st);
	}
}
