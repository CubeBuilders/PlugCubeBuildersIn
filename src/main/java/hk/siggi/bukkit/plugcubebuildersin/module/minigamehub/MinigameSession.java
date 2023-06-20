package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.changeMinigameItem;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.getInfo;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.quitItem;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.removeItem;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.setInfo;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.setVotes;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.timeLeftItem;
import static hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameUtil.voteItem;
import hk.siggi.bukkit.plugcubebuildersin.serverwatcherapi.ServerWatcherAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * MinigameSession for group of players. NOT THREAD SAFE, everything here is
 * designed to be run on the main thread.
 *
 * @author Siggi
 */
public final class MinigameSession implements MinigameVoteCallback, MinigameInventoryHandler {

	public MinigameSession(MinigameHubModuleImpl hub, ServerWatcherAPI api) {
		this.hub = hub;
		this.api = api;
		this.uuid = UUID.randomUUID();
		this.name = hub.genSessionName();
	}
	private final MinigameHubModuleImpl hub;
	private final ServerWatcherAPI api;
	private final UUID uuid;
	private final String name;
	MinigameController controller;
	private String server = null;
	private int currentStatus = 0;
	private boolean readyToInitialize = false;
	private boolean sessionClosed = false;
	final List<MinigamePlayer> players = new ArrayList<>();

	public final UUID getUUID() {
		return uuid;
	}

	public final String getName() {
		return name;
	}

	public boolean isInGame() {
		return !sessionClosed && currentStatus == 2;
	}

	String getServer() {
		return server;
	}

	public void startServer() {
		if (server == null) {
			server = api.createServer("minigame", "Minigames");
			readyToInitialize = false;
			currentStatus = 0;
			hub.plugin.setPublicChatGroup(server, name);
		}
	}

	public void setMinigameController(MinigameController controller) {
		this.controller = controller;
		if (controller != null) {
			controller.setSession(this);
			for (Iterator<MinigamePlayer> it = players.iterator(); it.hasNext();) {
				MinigamePlayer player = it.next();
				if (player.getSession() == this) {
					controller.playerJoined(player);
					Player bkp = player.getBukkitPlayer();
					if (bkp != null) {
						controller.playerEnteredLobby(bkp, MinigameJoinType.JOIN_SESSION);
					}
				} else {
					it.remove();
				}
			}
			if (readyToInitialize && currentStatus == 1) {
				controller.initialize();
				readyToInitialize = false;
			}
		}
	}

	public MinigameController getMinigameController() {
		if (controller != null && controller.getSession0() == this) {
			return controller;
		}
		return null;
	}

	MinigameController getMinigameController0() {
		return controller;
	}

	void readyToInitialize() {
		readyToInitialize = true;
		if (currentStatus == 1) {
			if (controller != null) {
				try {
					controller.initialize();
				} catch (Exception e) {
					PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on initialize()", e);
				}
				readyToInitialize = false;
			}
		}
	}

	public void serverStatusUpdated(int status) {
		int oldStatus = currentStatus;
		currentStatus = status;
		if (status == 1 && oldStatus == 0) { // now ready to receive instructions
			if (readyToInitialize) {
				if (controller != null) {
					try {
						controller.initialize();
					} catch (Exception e) {
						PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on initialize()", e);
					}
					readyToInitialize = false;
				}
			}
		} else if (status == 2 && oldStatus == 1) { // ready to receive players
			queueCommands();
			try {
				sendCommand("clearplayers");
				for (MinigamePlayer player : players) {
					sendCommand("addplayer " + player.getUniqueId().toString());
				}
				try {
					controller.serverReady();
				} catch (Exception e) {
					PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on serverReady()", e);
				}
			} finally {
				sendCommandQueue();
			}
		} else if (status == 3 && oldStatus == 2) { // game finished
			resetAllLastSeen();
			currentStatus = 0;
			try {
				controller.gameEnded();
			} catch (Exception e) {
				PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on gameEnded()", e);
			}
			stopServer();
			startServer();
			try {
				controller.newGame();
			} catch (Exception e) {
				PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on newGame()", e);
			}
		} else if (status == -1) { // an error occurred
			resetAllLastSeen();
			try {
				controller.gameCrashed();
			} catch (Exception e) {
				PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on gameCrashed()", e);
			}
			currentStatus = 0;
			stopServer();
			startServer();
			try {
				controller.newGame();
			} catch (Exception e) {
				PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on newGame()", e);
			}
		}
	}

	void addPlayer(MinigamePlayer player) {
		players.add(player);
		if (currentStatus == 2) {
			sendCommand("addplayer " + player.getUniqueId().toString());
		}
		Player bkp = player.getBukkitPlayer();
		if (controller != null) {
			controller.playerJoined(player);
			if (bkp != null) {
				hub.plugin.setPublicChatGroup(bkp, name);
				hub.plugin.getSession(bkp).setVisibilityGroup(name);
				controller.playerEnteredLobby(bkp, MinigameJoinType.JOIN_SESSION);
			}
		}
		if (isVoting()) {
			MinigameUtil.giveItem(bkp, 1, MinigameUtil.voteItem);
			openVoteInventory(bkp);
		}
	}

	void removePlayer(MinigamePlayer player) {
		votes.remove(player);
		if (players.contains(player)) {
			players.remove(player);
			if (currentStatus == 2) {
				sendCommand("delplayer " + player.getUniqueId().toString());
			}
			if (controller != null) {
				Player bkp = player.getBukkitPlayer();
				if (bkp != null) {
					controller.playerQuitLobby(bkp);
					removeItem(bkp, voteItem);
				}
				controller.playerQuit(player);
			}
		}
		Player bkp = player.getBukkitPlayer();
		if (bkp != null) {
			hub.plugin.setPublicChatGroup(bkp, null);
			hub.plugin.getSession(bkp).setVisibilityGroup(null);
		}
	}

	public MinigamePlayer[] getPlayers() {
		for (Iterator<MinigamePlayer> it = players.iterator(); it.hasNext();) {
			MinigamePlayer player = it.next();
			if (player.getSession() != this) {
				it.remove();
				if (controller != null) {
					controller.playerQuit(player);
				}
			}
		}
		return players.toArray(new MinigamePlayer[players.size()]);
	}

	void tick() {
		if (controller != null) {
			controller.tick();
		}
		if (voteInventory == null) {
			if (!votes.isEmpty()) {
				votes.clear();
			}
			if (!voteInventories.isEmpty()) {
				voteInventories.clear();
			}
		} else {
			processVote();
		}
	}

	public boolean isOpen() {
		return !sessionClosed;
	}

	public void close() {
		closeVoting();
		sessionClosed = true;
		stopServer();
	}

	public void sendAllPlayers() {
		for (MinigamePlayer pl : players) {
			Player bkp = pl.getBukkitPlayer();
			if (bkp != null) {
				sendPlayer(bkp);
			}
		}
	}

	public void sendPlayer(Player p) {
		if (server != null) {
			hub.plugin.sendToServer(p, server, null);
		}
	}

	private void stopServer() {
		if (server != null) {
			sendCommand("allquit");
			api.stopOnEmpty(server);
			server = null;
		}
	}

	private final List<String> queuedCommands = new LinkedList<>();
	private boolean queueCommands = false;

	private void queueCommands() {
		queueCommands = true;
	}

	private void sendCommandQueue() {
		String[] commands = queuedCommands.toArray(new String[queuedCommands.size()]);
		queuedCommands.clear();
		queueCommands = false;
		sendCommand(commands);
	}

	void sendCommand(String... command) {
		if (queueCommands) {
			queuedCommands.addAll(Arrays.asList(command));
			return;
		}
		try {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < command.length; i++) {
				if (i != 0) {
					sb.append("\n");
				}
				sb.append(command[i]);
			}
			hub.plugin.getVariableServerConnection().sendMessage(server, sb.toString().getBytes());
		} catch (Exception e) {
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (!sessionClosed) {
				close();
				PlugCubeBuildersIn.getInstance().reportProblem("Leaking MinigameSession cleaned by JVM Garbage Collector -- always close() the MinigameSession to release server resources when no longer needed instead of letting the JVM Garbage Collector take care of it later!", new RuntimeException("Leaking MinigameSession"));
			}
		} catch (Exception e) {
		}
		super.finalize();
	}

	private void callMinigameVote() {
		Inventory newInv = Bukkit.createInventory(null, 54, "Vote For The Next Minigame");
		for (MinigameController c : MinigameControllerLoader.getMinigameControllers()) {
			ItemStack item = c.getDisplayIcon();
			item = setInfo(item, c.getClass().getName());
			newInv.addItem(item);
		}
		callVote("nextgame", newInv, this);
	}

	@Override
	public void voteCompleted(String tag, ItemStack item) {
		if (tag.equals("nextgame")) {
			String className = getInfo(item);
			MinigameController newController = null;
			for (MinigameController c : MinigameControllerLoader.getMinigameControllers()) {
				if (c.getClass().getName().equals(className)) {
					newController = c;
					break;
				}
			}
			if (newController == null) {
				callMinigameVote();
			} else {
				setMinigameController(newController);
				try {
					newController.newGame();
				} catch (Exception e) {
					PlugCubeBuildersIn.getInstance().reportProblem("Minigame " + controller.getDisplayName() + " Exception on newGame()", e);
				}
			}
		}
	}

	private final long voteTimer = 30000L;

	private String voteTag = null;
	private Inventory voteInventory = null;
	private MinigameVoteCallback voteCallback = null;
	private long voteTimeout = 0L;
	private final Map<MinigamePlayer, Integer> votes = new HashMap<>();
	private final Map<Inventory, Player> voteInventories = new HashMap<>();

	void callVote(String tag, Inventory inventory, MinigameVoteCallback callback) {
		this.voteTag = tag;
		this.voteInventory = inventory;
		this.voteCallback = callback;
		this.voteTimeout = -1L;
		votes.clear();
		voteInventories.clear();
		if (controller != null) {
			voteInventory.setItem(voteInventory.getSize() - 2, changeMinigameItem());
		}
		voteInventory.setItem(voteInventory.getSize() - 1, quitItem());
		for (MinigamePlayer pl : players) {
			Player bkp = pl.getBukkitPlayer();
			if (bkp != null) {
				MinigameUtil.giveItem(bkp, 1, MinigameUtil.voteItem);
				openVoteInventory(bkp);
			}
		}
	}

	boolean isVoting() {
		return voteInventory != null;
	}

	void openVoteInventory(Player p) {
		if (voteInventory == null) {
			return;
		}
		InventoryView view = p.openInventory(voteInventory);
		Inventory topInventory = view.getTopInventory();
		voteInventories.put(topInventory, p);
		hub.setInventoryHandler(topInventory, this);
	}

	Player getVoter(Inventory inv) {
		return voteInventories.get(inv);
	}

	private void processVote() {
		long now = System.currentTimeMillis();
		if (players.size() > 1 && voteTimeout == -1L) {
			if (votes.size() > 0) {
				voteTimeout = now + voteTimer;
			}
		}
		for (Iterator<MinigamePlayer> it = votes.keySet().iterator(); it.hasNext();) {
			MinigamePlayer pl = it.next();
			if (pl.hasExpired()) {
				it.remove();
			}
		}
		if (votes.size() >= players.size()) {
			if (voteTimeout == -1L || voteTimeout - now > 5000L) {
				voteTimeout = now + 5000L;
			}
		}
		long timeLeft = voteTimeout == -1L ? -1 : voteTimeout - now;
		int timeLeftSecs = timeLeft == -1L ? -1 : ((int) Math.ceil(((double) timeLeft) / 1000.0));
		voteInventory.setItem(voteInventory.getSize() - 9, timeLeftItem(timeLeftSecs));
		for (int i = 0; i < voteInventory.getSize() - 9; i++) {
			ItemStack stack = voteInventory.getItem(i);
			if (stack != null && stack.getType() != Material.AIR) {
				voteInventory.setItem(i, setVotes(stack, countVotes(i)));
			}
		}
		if (controller != null) {
			int i = voteInventory.getSize() - 2;
			ItemStack stack = voteInventory.getItem(i);
			if (stack != null && stack.getType() != Material.AIR) {
				voteInventory.setItem(i, setVotes(stack, countVotes(i)));
			}
		}
		if (voteTimeout != -1L && now >= voteTimeout) {
			for (Iterator<Inventory> it = voteInventories.keySet().iterator(); it.hasNext();) {
				Inventory inv = it.next();
				Player p = voteInventories.get(inv);
				if (!p.isOnline() || p.getOpenInventory().getTopInventory() != inv) {
					it.remove();
				}
			}
			int[] totals = new int[voteInventory.getSize()];
			for (int i = 0; i < totals.length; i++) {
				totals[i] = 0;
			}
			for (Integer i : votes.values()) {
				try {
					totals[i] += 1;
				} catch (Exception e) {
				}
			}
			int highestTotal = 0;
			for (int total : totals) {
				highestTotal = Math.max(highestTotal, total);
			}
			List<ItemStack> winners = new LinkedList<>();
			for (int i = 0; i < totals.length; i++) {
				if (totals[i] == highestTotal) {
					try {
						ItemStack item = voteInventory.getItem(i);
						if (item != null && item.getType() != Material.AIR) {
							winners.add(item);
						}
					} catch (Exception e) {
					}
				}
			}
			ItemStack winningItem;
			if (winners.isEmpty()) {
				winningItem = new ItemStack(Material.AIR);
			} else if (winners.size() == 1) {
				winningItem = winners.get(0);
			} else {
				winningItem = winners.get((int) Math.floor(Math.random() * winners.size()));
			}
			String tag = voteTag;
			MinigameVoteCallback callback = voteCallback;
			closeVoting();
			if (getInfo(winningItem).equals("choosenewminigame")) {
				controller = null;
				callMinigameVote();
			} else {
				callback.voteCompleted(tag, winningItem);
			}
		}
	}

	void closeVoting() {
		voteTag = null;
		voteInventory = null;
		voteCallback = null;
		voteTimeout = 0L;
		votes.clear();
		for (MinigamePlayer pl : players) {
			Player bkp = pl.getBukkitPlayer();
			if (bkp != null) {
				bkp.closeInventory();
				removeItem(bkp, voteItem);
			}
		}
		voteInventories.clear();
	}

	boolean hasVoted(MinigamePlayer player) {
		return votes.containsKey(player);
	}

	Inventory getVoteInventory() {
		return voteInventory;
	}

	void vote(MinigamePlayer player, int vote) {
		votes.put(player, vote);
	}

	private int countVotes(int slot) {
		int v = 0;
		for (Integer i : votes.values()) {
			if (i == slot) {
				v += 1;
			}
		}
		return v;
	}

	@Override
	public void inventoryClick(InventoryClickEvent event) {
		if (voteInventory != null) {
			event.setCancelled(true);
			if (event.getClickedInventory() != event.getView().getTopInventory()) {
				return;
			}
			Player p = (Player) event.getWhoClicked();
			MinigamePlayer mp = hub.getPlayer(p);
			int slot = event.getSlot();
			if (slot < 0) {
				return;
			}
			int size = voteInventory.getSize();
			if (slot == size - 9) { // non-clickable slot
				return;
			} else if (slot == size - 1) { // quit
				mp.quitSession();
				hub.openMinigameMenu(p);
				return;
			}
			ItemStack stack = voteInventory.getItem(slot);
			if (stack == null || stack.getType() == Material.AIR) {
				return;
			}
			if (slot < size || (controller != null && slot == size - 2)) {
				vote(mp, slot);
			}
		}
	}

	@Override
	public void inventoryClose(InventoryCloseEvent event) {
	}

	boolean isFull() {
		MinigameController cont = getMinigameController();
		if (cont == null) {
			return false;
		}
		return players.size() >= cont.getMaxPlayers();
	}

	private void resetAllLastSeen() {
		for (MinigamePlayer player : players) {
			player.updateLastSeen();
		}
	}
}
