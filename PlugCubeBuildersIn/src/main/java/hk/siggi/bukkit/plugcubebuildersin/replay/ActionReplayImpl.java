package hk.siggi.bukkit.plugcubebuildersin.replay;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.Action;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ActionReplayImpl implements ActionReplay, Listener {

	private PlugCubeBuildersIn plugin = null;
	private ActionRecorder recorder = null;
	private final List<ReplaySession> sessions = new LinkedList<>();

	public ActionRecorder getRecorder() {
		return recorder;
	}

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		recorder = new ActionRecorder(this, plugin);
		recorder.init();
		plugin.getServer().getPluginManager().registerEvents(recorder, plugin);
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		PluginCommand replayCommand = plugin.getCommand("replay");
		ReplayCommand rpc = new ReplayCommand(plugin, this);
		replayCommand.setExecutor(rpc);
		replayCommand.setTabCompleter(rpc);
	}

	@Override
	public void kill() {
		recorder.kill();
	}

	@Override
	public void tick() {
		recorder.tick();
		for (Iterator<ReplaySession> it = sessions.iterator(); it.hasNext();) {
			ReplaySession session = it.next();
			if (session.getVisibleTo().length == 0) {
				it.remove();
			} else {
				try {
					session.tick();
				} catch (Exception e) {
					System.err.println("Exception when ticking ReplaySession");
					e.printStackTrace();
				}
			}
		}
	}

	public ReplaySession getSession(Player p) {
		for (ReplaySession session : sessions) {
			if (session.isVisibleTo(p)) {
				return session;
			}
		}
		return null;
	}

	void addSession(ReplaySession session) {
		if (!sessions.contains(session)) {
			sessions.add(session);
		}
	}

	public static void sortActions(Action[] actions) {
		Arrays.sort(actions, new Comparator<Action>() {
			@Override
			public int compare(Action a1, Action a2) {
				if (a1.time < a2.time) {
					return -1;
				} else if (a1.time > a2.time) {
					return 1;
				} else {
					return 0;
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerMoved(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		for (ReplaySession session : sessions) {
			if (session.isVisibleTo(p)) {
				session.playerMoved(event);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleported(PlayerTeleportEvent event) {
		playerMoved(event);
	}
	
	@Override
	public boolean isPlayerWatchingReplay(Player p) {
		ReplaySession session = getSession(p);
		return session != null;
	}
}
