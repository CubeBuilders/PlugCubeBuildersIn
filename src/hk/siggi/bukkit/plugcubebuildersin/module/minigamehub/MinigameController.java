package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Superclass of MinigameController -- DO NOT RELY on static fields in your
 * subclasses as they won't always stick due to the class being unloaded when
 * minigame isn't currently in use.
 *
 * @author Siggi
 * @param <M> the class to use for the map
 */
public abstract class MinigameController<M extends MinigameMap> implements MinigameVoteCallback {

	private WeakReference<MinigameSession> session = null;
	private boolean autoReady = false;
	private M map = null;

	/**
	 * Get the name of this minigame.
	 *
	 * @return the name
	 */
	public abstract String getDisplayName();

	/**
	 * Get the display icon for this minigame.
	 *
	 * @return the minigame's display icon.
	 */
	public abstract ItemStack getDisplayIcon();

	/**
	 * Get the maximum number of players allowed for this minigame.
	 *
	 * @return
	 */
	public abstract int getMaxPlayers();

	final void setSession(MinigameSession session) {
		this.session = session == null ? null : new WeakReference<>(session);
		sessionAttached(session);
		if (autoReady && session != null) {
			autoReady = false;
			session.readyToInitialize();
		}
	}

	public final MinigameSession getSession() {
		MinigameSession sess = session == null ? null : session.get();
		if (sess != null && sess.getMinigameController0() == this) {
			return sess;
		}
		return null;
	}

	final MinigameSession getSession0() {
		return session == null ? null : session.get();
	}

	/**
	 * Called when a session has been attached.
	 *
	 * @param session
	 */
	protected abstract void sessionAttached(MinigameSession session);

	/**
	 * Set the map used in this session.
	 *
	 * @param map the map
	 */
	protected final void setMap(M map) {
		this.map = map;
	}

	/**
	 * Get the map used in this session.
	 *
	 * @return the map
	 */
	public final M getMap() {
		return map;
	}

	/**
	 * Called when a player has entered the session or returned from the game
	 * server.
	 *
	 * @param p
	 * @param joinType
	 */
	protected abstract void playerEnteredLobby(Player p, MinigameJoinType joinType);

	/**
	 * Called when a player has left the lobby server (They may have gone to the
	 * game server, or may have logged out), or called right before playerQuit()
	 * when the player is quitting the session.
	 *
	 * @param p
	 */
	protected abstract void playerQuitLobby(Player p);

	/**
	 * Called when a player joins the session.
	 *
	 * @param player
	 */
	protected abstract void playerJoined(MinigamePlayer player);

	/**
	 * Called when a player leaves the session.
	 *
	 * @param player
	 */
	protected abstract void playerQuit(MinigamePlayer player);

	/**
	 * Call this when you're ready to initialize a game server. MinigameAPI will
	 * automatically call initialize() for you when a game server is ready to be
	 * initialized.
	 */
	protected final void readyToInitialize() {
		MinigameSession sess = getSession();
		if (sess == null) {
			autoReady = true;
		} else {
			sess.readyToInitialize();
		}
	}

	/**
	 * Call this when players have voted to change to a new minigame,
	 * MinigameAPI will present a voting screen to select a new minigame.
	 */
	protected final void changeMinigame() {
	}

	/**
	 * Set info of an ItemStack, useful for votes.
	 *
	 * @param stack the stack to set info for
	 * @param info the info to put on the stack
	 * @return the new stack
	 */
	protected final ItemStack setInfo(ItemStack stack, String info) {
		return MinigameUtil.setInfo(stack, info);
	}

	/**
	 * Retrieve the info from the ItemStack.
	 *
	 * @param stack the item stack to retrieve the info from
	 * @return the info from the item stack
	 */
	protected final String getInfo(ItemStack stack) {
		return MinigameUtil.getInfo(stack);
	}

	/**
	 * Set the name and lore on an ItemStack.
	 *
	 * @param stack the stack to set it on.
	 * @param name the name to set.
	 * @param lore the lore to set.
	 * @return the ItemStack
	 */
	protected final ItemStack setNameAndLore(ItemStack stack, String name, String... lore) {
		return MinigameUtil.setNameAndLore(stack, name, lore);
	}

	/**
	 * Call a vote by presenting an Inventory to players. The bottom row must be
	 * left empty for MinigameAPI use. When the voting has completed, the tag
	 * you provided and the item that was clicked on by most players will be
	 * passed in voteCompleted(String tag, ItemStack stack)
	 *
	 * @param tag
	 * @param inventory
	 */
	protected final void callVote(String tag, Inventory inventory) {
		MinigameSession sess = getSession();
		if (sess != null) {
			sess.callVote(tag, inventory, this);
		}
	}

	/**
	 * When vote has completed, MinigameAPI will call this method.
	 *
	 * @param tag
	 * @param stack
	 */
	@Override
	public abstract void voteCompleted(String tag, ItemStack stack);

	/**
	 * Cancel a vote.
	 */
	protected final void cancelVote() {
		MinigameSession sess = getSession();
		if (sess != null) {
			sess.closeVoting();
		}
	}

	/**
	 * Send all players in the lobby to the minigame server.
	 */
	protected final void sendAllPlayers() {
		MinigameSession sess = getSession();
		if (sess != null) {
			sess.sendAllPlayers();
		}
	}

	/**
	 * Send this player to the minigame server.
	 *
	 * @param p Send this player to the minigame server.
	 */
	protected final void sendPlayer(Player p) {
		MinigameSession sess = getSession();
		if (sess != null) {
			sess.sendPlayer(p);
		}
	}

	/**
	 * Send some commands to the minigame server to initialize it. MinigameAPI
	 * will call this after you call readyToInitialize()
	 */
	protected abstract void initialize();

	/**
	 * The server is ready, you can now send players.
	 */
	protected abstract void serverReady();

	/**
	 * Start a new game, you can either start the game immediately, or ask
	 * players to choose game settings.
	 */
	protected abstract void newGame();

	/**
	 * The game has ended, now get players to vote for the next map. If the
	 * session is a private session, an option to switch to vote to change
	 * minigames MUST be given as well.
	 */
	protected abstract void gameEnded();

	/**
	 * Called when the game server has failed for any reason.
	 */
	protected void gameCrashed() {
		gameEnded();
	}

	protected final void sendCommand(String... command) {
		MinigameSession sess = getSession();
		if (sess != null) {
			sess.sendCommand(command);
		}
	}

	/**
	 * Called every tick.
	 */
	protected abstract void tick();

	protected final boolean isInGame() {
		MinigameSession sess = getSession();
		if (sess != null) {
			return sess.isInGame();
		}
		return false;
	}

	protected final M getMap(String mapName) {
		for (M m : getMaps()) {
			if (m.getName().equals(mapName)) {
				return m;
			}
		}
		return null;
	}

	protected abstract List<M> getMaps();

	protected final List<M> loadMaps(String gameName, MinigameMapLoader<M> mapLoader) {
		LinkedList<M> maps = new LinkedList<>();
		try {
			File dir = new File(new File("../mgmaps"), gameName);
			File[] listFiles = dir.listFiles();
			for (File f : listFiles) {
				try {
					M m = mapLoader.loadMap(f);
					if (m != null) {
						maps.add(m);
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
		return maps;
	}

	/**
	 * Call a vote to select a map. When voting is completed, voteCompleted()
	 * will be called with the result.
	 */
	protected final void callMapVote() {
		MinigameSession sess = getSession();
		if (sess != null) {
			Inventory inv = Bukkit.createInventory(null, 54, "Vote For The Next Map");
			for (M m : getMaps()) {
				ItemStack icon = m.getIcon();
				List<String> theLore = new LinkedList<>();
				for (MinigameMapArtist artist : m.getArtists()) {
					theLore.add("Artist: " + artist.toString());
				}
				icon = setNameAndLore(icon, ChatColor.RESET + m.getName(), theLore.toArray(new String[theLore.size()]));
				icon = setInfo(icon, m.getName());
				inv.addItem(icon);
			}
			sess.callVote("map", inv, (String tag, ItemStack result) -> {
				String info = getInfo(result);
				if (info == null) {
					callMapVote();
					return;
				}
				M m = getMap(info);
				if (m == null) {
					callMapVote();
					return;
				}
				mapVoteResult(m);
			});
		}
	}

	protected abstract void mapVoteResult(M map);
}
