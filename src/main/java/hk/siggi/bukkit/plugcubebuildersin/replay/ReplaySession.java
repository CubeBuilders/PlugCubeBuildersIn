package hk.siggi.bukkit.plugcubebuildersin.replay;

import com.mojang.authlib.GameProfile;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.Action;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ActionBlock;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ActionChunk;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ActionLocation;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ArmswingAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.BlockChangeAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ChatAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.LoginAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.LogoutAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.MoveAction;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import hk.siggi.statues.Statue;
import hk.siggi.statues.Statues;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ReplaySession {

	Player starter;

	public ReplaySession(ActionRecorder recorder, long startTime) {
		this.recorder = recorder;
		this.offset = startTime;
		this.track = new ReplayTrack(recorder, startTime);
		readingFromLiveRecording = true;
	}

	public ReplaySession(Action[] actions) {
		this.recorder = null;
		this.offset = actions[0].time;
		this.track = new ReplayTrack(actions);
		readingFromLiveRecording = false;
	}
	private final ActionRecorder recorder;
	private ReplayTrack track;
	private boolean playing = false;
	private boolean reverse = false;
	private long offset = 0L;
	private final boolean readingFromLiveRecording;
	private double speed = 1.0;

	public void play() {
		play(1.0);
	}

	public void play(double speed) {
		long now = System.currentTimeMillis();
		if (!playing) {
			// Transition from Stopped to Playing
			offset = ((long) (now * speed)) - offset;
			this.speed = speed;
		} else if (reverse || this.speed != speed) {
			stop();
			play(speed);
			return;
		}
		playing = true;
		reverse = false;
	}

	public void stop() {
		long now = System.currentTimeMillis();
		if (playing) {
			if (reverse) {
				// Transition from Reversing to Stopped
				offset = offset - ((long) (now * speed));
			} else {
				// Transition from Playing to Stopped
				offset = ((long) (now * speed)) - offset;
			}
		}
		playing = false;
		reverse = false;
		speed = 1.0;
	}

	public void reverse() {
		reverse(1.0);
	}

	public void reverse(double speed) {
		long now = System.currentTimeMillis();
		if (!playing) {
			// Transition from Stopped to Reversing
			offset = offset + ((long) (now * speed));
			this.speed = speed;
		} else {
			if (!reverse || this.speed != speed) {
				stop();
				reverse(speed);
				return;
			}
		}
		playing = true;
		reverse = true;
	}

	private boolean trackLoadAndUnload(long time) {
		long minimumLoad;
		long maximumLoad;
		if (speed <= 2.0) {
			minimumLoad = 5000L;
		} else if (speed <= 5.0) {
			minimumLoad = 10000L;
		} else if (speed <= 10.0) {
			minimumLoad = 60000L;
		} else if (speed <= 20.0) {
			minimumLoad = 120000L;
		} else if (speed <= 30.0) {
			minimumLoad = 300000L;
		} else {
			minimumLoad = 600000L;
		}
		maximumLoad = (60000L * 30L);
		return track.loadAndUnload(time, minimumLoad, maximumLoad);
	}

	public void seek(long time) {
		if (playing) {
			stop();
		}
		try {
			visibleToArray = getVisibleTo();
			long difference = offset - time;
			if (readingFromLiveRecording && Math.abs(difference) > (60000L * 30L)) {
				first = true;
				resetState();
				track = new ReplayTrack(recorder, time);
				offset = time;
			} else {
				if (time < offset) {
					// seek backwards
					trackLoadAndUnload(time);
					Action current = track.current();
					while (current != null && current.time > time) {
						if (!(current instanceof ChatAction) && !(current instanceof ArmswingAction)) {
							playAction(current, true);
						}
						current = track.previous();
					}
					trackLoadAndUnload(time);
					offset = time;
				} else if (time > offset) {
					// seek forwards
					trackLoadAndUnload(time);
					Action next = track.peekNext();
					while (next != null && next.time <= time) {
						if (!(next instanceof ChatAction) && !(next instanceof ArmswingAction)) {
							playAction(next, false);
						}
						track.next();
						next = track.peekNext();
					}
					trackLoadAndUnload(time);
					offset = time;
				}
			}
		} finally {
			visibleToArray = null;
		}
	}

	public void seek(final Player commandSender, final UUID player, final boolean backwards, final Class<? extends Action> matchClass) {
		if (readingFromLiveRecording) {
			if (commandSender != null) {
				commandSender.sendMessage(ChatColor.GOLD + "Running asynchronous search...");
			}
			final long[] bounds = recorder.getLogBounds();
			final long timeOffset = getCurrentTimeOffset();
			new BukkitRunnable() {
				@Override
				public void run() {
					asyncSeek(commandSender, player, backwards, matchClass, bounds, timeOffset);
				}
			}.runTaskAsynchronously(PlugCubeBuildersIn.getInstance());
			return;
		}
		long seekPoint = -1L;
		long originalOffset = getCurrentTimeOffset();
		try {
			track.bookmark();
			if (backwards) {
				while (true) {
					Action action = track.previous();
					if (action == null) {
						break;
					}
					if (action.time == originalOffset) {
						continue;
					}
					if ((player == null || player.equals(action.player))
							&& (matchClass == null || matchClass.isAssignableFrom(matchClass))) {
						seekPoint = action.time;
						break;
					}
				}
			} else {
				while (true) {
					Action action = track.next();
					if (action == null) {
						break;
					}
					if (action.time == originalOffset) {
						continue;
					}
					if ((player == null || player.equals(action.player))
							&& (matchClass == null || matchClass.isAssignableFrom(action.getClass()))) {
						seekPoint = action.time;
						break;
					}
				}
			}
		} finally {
			track.unbookmark();
		}
		seekResult(commandSender, player, backwards, seekPoint);
	}

	private void asyncSeek(final Player commandSender, final UUID player, final boolean backwards, final Class<? extends Action> matchClass, final long[] bounds, long originalOffset) {
		long seekPoint = -1L;
		long currentLog = ActionRecorder.getLogNumber(originalOffset);
		if (backwards) {
			if (currentLog > bounds[1]) {
				currentLog = bounds[1];
			}
		} else {
			if (currentLog < bounds[0]) {
				currentLog = bounds[0];
			}
		}
		while (seekPoint == -1L) {
			if (currentLog < bounds[0] || currentLog > bounds[1]) {
				break;
			}
			Action[] log = recorder.readLogFile(currentLog);
			if (backwards) {
				for (int i = log.length - 1; i >= 0; i--) {
					if (log[i].time < originalOffset) {
						if ((player == null || player.equals(log[i].player))
								&& (matchClass == null || matchClass.isAssignableFrom(log[i].getClass()))) {
							seekPoint = log[i].time;
							break;
						}
					}
				}
				currentLog -= 1;
			} else {
				for (int i = 0; i < log.length; i++) {
					if (log[i].time > originalOffset) {
						if ((player == null || player.equals(log[i].player))
								&& (matchClass == null || matchClass.isAssignableFrom(log[i].getClass()))) {
							seekPoint = log[i].time;
							break;
						}
					}
				}
				currentLog += 1;
			}
		}
		final long finalSeekPoint = seekPoint;
		new BukkitRunnable() {
			@Override
			public void run() {
				seekResult(commandSender, player, backwards, finalSeekPoint);
			}
		}.runTask(PlugCubeBuildersIn.getInstance());
	}

	private void seekResult(Player commandSender, UUID player, boolean backwards, long seekPoint) {
		if (seekPoint == -1L) {
			if (commandSender != null) {
				commandSender.sendMessage(ChatColor.RED + "Seek failed: No action found.");
			}
			return;
		}
		if (commandSender != null) {
			sendMessage(ChatColor.GOLD + commandSender.getName() + ": Seeking replay " + (backwards ? "back to last" : "forward to next") + " move of " + PlugCubeBuildersIn.getInstance().getUUIDCache().getNameFromUUID(player) + ".");
		}
		boolean wasPlaying = playing;
		double oldSpeed = speed;
		if (wasPlaying) {
			stop();
		}
		seek(seekPoint);
		if (wasPlaying) {
			if (backwards) {
				reverse(oldSpeed);
			} else {
				play(oldSpeed);
			}
		}
	}

	public void kill() {
		stop();
		for (Iterator<Statue> it = statues.values().iterator(); it.hasNext();) {
			Statue statue = it.next();
			statue.delete();
			it.remove();
		}
	}

	private boolean first = true;

	public void tick() {
		try {
			visibleToArray = getVisibleTo();
			{
				boolean terminate;
				if (starter == null || !starter.isOnline()) {
					terminate = true;
				} else {
					terminate = true;
					for (Player p : visibleToArray) {
						if (p == starter) {
							terminate = false;
							break;
						}
					}
				}
				if (terminate) {
					starter = null;
					sendMessage(ChatColor.RED + "The Replay Owner left the replay session. The replay has been terminated.");
					sendHotbarMessage(ChatColor.RED + "Replay was terminated.");
					for (Player p : visibleToArray) {
						removeVisibleTo(p);
					}
					return;
				}
			}
			long currentOffset = getCurrentTimeOffset();
			if ((playing && trackLoadAndUnload(currentOffset)) || first) {
				first = false;
				for (Iterator<Action> it = track.reverseIterateToCurrent(); it.hasNext();) {
					Action action = it.next();
					if (first) {
						if (!(action instanceof ChatAction) && !(action instanceof ArmswingAction)) {
							playAction(action, true);
						}
					} else {
						if (action instanceof BlockChangeAction) {
							playAction(action, true);
						}
					}
				}
			}
			String msg = (playing
					? ((reverse ? (ChatColor.RED + "Reverse") : (ChatColor.GREEN + "Playing")) + ((speed == 1.0) ? "" : (" " + speed + "x")))
					: (ChatColor.DARK_RED + "Stopped")) + ChatColor.YELLOW + " {time:" + currentOffset + "}";
			sendHotbarMessage(msg);
			if (playing) {
				if (reverse) {
					if (track.current() == null && track.peekPrevious() == null) {
						if (!readingFromLiveRecording || currentOffset <= recorder.getEarliestRecordedHistory()) {
							sendMessage(ChatColor.GOLD + "Reached beginning of recording");
							stop();
						}
						return;
					}
					while (true) {
						Action action = track.current();
						if (action != null) {
							if (action.time <= currentOffset) {
								break;
							}
							playAction(action, true);
						}
						if (track.previous() == null) {
							if (!readingFromLiveRecording || currentOffset <= recorder.getEarliestRecordedHistory()) {
								sendMessage(ChatColor.GOLD + "Reached beginning of recording");
								stop();
							}
							return;
						}
					}
				} else {
					if (track.peekNext() == null) {
						if (!readingFromLiveRecording || (speed > 1.0 && currentOffset > System.currentTimeMillis())) {
							sendMessage(ChatColor.GOLD + "Reached end of recording");
							stop();
						}
						return;
					}
					while (true) {
						if (track.peekNext() == null) {
							if (!readingFromLiveRecording || (speed > 1.0 && currentOffset > System.currentTimeMillis())) {
								sendMessage(ChatColor.GOLD + "Reached end of recording");
								stop();
							}
							return;
						}
						Action action = track.next();
						if (action != null) {
							if (action.time > currentOffset) {
								track.previous();
								break;
							}
						}
						playAction(action, false);
					}
				}
			}
		} finally {
			visibleToArray = null;
		}
	}

	public long getCurrentTimeOffset() {
		long now = System.currentTimeMillis();
		if (playing) {
			if (reverse) {
				return offset - ((long) (now * speed));
			} else {
				return ((long) (now * speed)) - offset;
			}
		} else {
			return offset;
		}
	}

	Map<UUID, Statue> statues = new HashMap<>();
	private final List<WeakReference<Player>> visibleTo = new LinkedList<>();
	private Player[] visibleToArray = null;

	public Player[] getVisibleTo() {
		List<Player> l = new LinkedList<>();
		for (Iterator<WeakReference<Player>> it = visibleTo.iterator(); it.hasNext();) {
			WeakReference<Player> pp = it.next();
			Player p = pp.get();
			if (p == null) {
				it.remove();
				continue;
			}
			if (!p.isOnline()) {
				it.remove();
				continue;
			}
			l.add(p);
		}
		return l.toArray(new Player[l.size()]);
	}

	public void addVisibleTo(Player p) {
		add:
		{
			for (Iterator<WeakReference<Player>> it = visibleTo.iterator(); it.hasNext();) {
				WeakReference<Player> pp = it.next();
				Player ppp = pp.get();
				if (ppp == null) {
					it.remove();
					continue;
				}
				if (!ppp.isOnline()) {
					it.remove();
					continue;
				}
				if (p == ppp) {
					break add;
				}
			}
			visibleTo.add(new WeakReference<>(p));
			updatePlayer(p, null, new ActionChunk(p.getLocation()));
		}
		for (Statue statue : statues.values()) {
			statue.addVisibleTo(p);
		}
	}

	public void removeVisibleTo(Player p) {
		updatePlayer(p, new ActionChunk(p.getLocation()), null);
		for (Iterator<WeakReference<Player>> it = visibleTo.iterator(); it.hasNext();) {
			WeakReference<Player> pp = it.next();
			Player ppp = pp.get();
			if (ppp == null || ppp == p) {
				it.remove();
				continue;
			}
			if (!ppp.isOnline()) {
				it.remove();
				continue;
			}
		}
		for (Statue statue : statues.values()) {
			statue.removeVisibleTo(p);
		}
	}

	public boolean isVisibleTo(Player p) {
		boolean result = false;
		for (Iterator<WeakReference<Player>> it = visibleTo.iterator(); it.hasNext();) {
			WeakReference<Player> pp = it.next();
			Player ppp = pp.get();
			if (ppp == null) {
				it.remove();
				continue;
			}
			if (!ppp.isOnline()) {
				it.remove();
				continue;
			}
			if (p == ppp) {
				result = true;
			}
		}
		return result;
	}

	private void playAction(Action action, boolean reverse) {
		if (action instanceof ArmswingAction) {
			ArmswingAction armswing = (ArmswingAction) action;
			Statue statue = statues.get(action.player);
			if (statue != null) {
				statue.armswing();
			}
		} else if (action instanceof ChatAction) {
			ChatAction chat = (ChatAction) action;

		} else if (action instanceof LoginAction) {
			LoginAction login = (LoginAction) action;
			if (reverse) {
				removeStatue(login.player);
			} else {
				putStatue(login.player, login.location);
			}
		} else if (action instanceof LogoutAction) {
			LogoutAction logout = (LogoutAction) action;
			if (reverse) {
				putStatue(logout.player, logout.location);
			} else {
				removeStatue(logout.player);
			}
		} else if (action instanceof MoveAction) {
			MoveAction move = (MoveAction) action;
			putStatue(move.player, reverse ? move.from : move.to);
		} else if (action instanceof BlockChangeAction) {
			BlockChangeAction blockChange = (BlockChangeAction) action;
			try {
				Material mat;
				byte dat;
				if (reverse) {
					mat = blockChange.from;
					dat = blockChange.fromData;
				} else {
					mat = blockChange.to;
					dat = blockChange.toData;
				}
				setBlock(blockChange.block, mat, dat);
				String worldStr = blockChange.block.world;
				World world = Bukkit.getWorld(worldStr);
				if (world != null) {
					for (Player p : visibleToArray) {
						sendBlock(p, blockChange.block, mat, dat);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private void putStatue(UUID player, ActionLocation location) {
		try {
			Statue statue = statues.get(player);
			if (statue == null) {
				PlugCubeBuildersIn plugin = PlugCubeBuildersIn.getInstance();
				GameProfile originalProfile = plugin.getGameProfile(new GameProfile(player, ""));
				String newName = "[R]" + originalProfile.getName();
				newName = newName.substring(0, Math.min(newName.length(), 16));
				GameProfile modifiedProfile = new GameProfile(modifyUUID(player), newName);
				plugin.copyProperties(originalProfile, modifiedProfile);
				statue = new Statue(modifiedProfile, null, visibleToArray);
				statue.save = false;
				statue.move(location.world, location.x, location.y, location.z, location.pitch, location.yaw);
				statue.alwaysShownOnPlayerList = true;
				Statues.getInstance().add(statue);
				statues.put(player, statue);
			} else {
				statue.move(location.world, location.x, location.y, location.z, location.pitch, location.yaw);
			}
		} catch (Exception e) {
			PlugCubeBuildersIn.getInstance().reportProblem("Could not put statue", e);
		}
	}

	private void removeStatue(UUID player) {
		Statue statue = statues.remove(player);
		if (statue != null) {
			statue.delete();
		}
	}

	private UUID modifyUUID(UUID player) {
		String str = player.toString().replace("-", "");
		str = str.substring(0, 12) + "F" + str.substring(13);
		return Util.uuidFromString(str);
	}

	public boolean teleport(Player player, UUID statueId) {
		Statue statue = statues.get(statueId);
		if (statue == null) {
			player.sendMessage(ChatColor.RED + "That player is currently not visible in the replay at this time.");
			return false;
		}
		PlugCubeBuildersIn.getInstance().loadWorld(statue.world);
		try {
			player.teleport(Statues.getLocation(statue));
			player.sendMessage(ChatColor.RED + "Teleporting");
			return true;
		} catch (Exception e) {
			player.sendMessage(ChatColor.RED + "The world that player is in is currently unavailable.");
			return false;
		}
	}

	private String getDefaultTimeZone(Player p) {
		return "GMT";
	}

	public void sendMessage(String message) {
		Player[] pp = visibleToArray == null ? getVisibleTo() : visibleToArray;
		for (Player p : pp) {
			p.sendMessage(customizeMessage(p, message));
		}
	}

	public void sendHotbarMessage(String message) {
		PlugCubeBuildersIn plugin = PlugCubeBuildersIn.getInstance();
		Player[] pp = visibleToArray == null ? getVisibleTo() : visibleToArray;
		for (Player p : pp) {
			plugin.addHotbar(p, "replay", customizeMessage(p, message));
		}
	}

	private static final Pattern timePattern = Pattern.compile("\\{time:([0-9]*)\\}");

	private String customizeMessage(Player p, String message) {
		String customizeMessage = message;
		String timezone = null;
		Matcher matcher = timePattern.matcher(customizeMessage);
		while (matcher.find()) {
			if (timezone == null) {
				timezone = getDefaultTimeZone(p);
			}
			String time = matcher.group(1);
			int start = matcher.start();
			int end = matcher.end();
			String timeFormatted = Util.unparseTime(Long.parseLong(time), timezone);
			String newStr = customizeMessage.substring(0, start) + timeFormatted + customizeMessage.substring(end);
			int newPos = start + timeFormatted.length();
			matcher.reset(newStr);
			matcher.region(newPos, newStr.length());
			customizeMessage = newStr;
		}
		return customizeMessage;
	}

	private final Map<ActionChunk, Map<ActionBlock, BlockInfo>> chunks = new HashMap<>();

	private void setBlock(ActionBlock block, Material material, byte data) {
		ActionChunk chunk = new ActionChunk(block);
		Map<ActionBlock, BlockInfo> chunkInfo = chunks.get(chunk);
		if (chunkInfo == null) {
			chunks.put(chunk, chunkInfo = new HashMap<>());
		}
		chunkInfo.put(block, new BlockInfo(material, data));
	}

	private void resetState() {
		for (Player p : getVisibleTo()) {
			updatePlayer(p, new ActionChunk(p.getLocation()), null);
		}
		chunks.clear();
		for (Statue statue : statues.values()) {
			statue.delete();
		}
		statues.clear();
	}

	private final int replayRenderDistance = 8;

	private void sendBlock(Player p, ActionBlock block, Material material, byte data) {
		ActionChunk chunk = new ActionChunk(p.getLocation());
		ActionChunk chunk2 = new ActionChunk(block);
		int distance = chunk.world.equals(chunk2.world)
				? Math.max(Math.abs(chunk.x - chunk2.x), Math.abs(chunk.z - chunk2.z))
				: Integer.MAX_VALUE;
		if (distance <= replayRenderDistance) {
			p.sendBlockChange(block.getBukkitLocation(), material, data);
		}
	}

	private void revertBlock(Player p, ActionBlock block) {
		if (p.getWorld().getName().equals(block.world)) {
			ActionChunk ac = new ActionChunk(block);
			if (ac.isLoaded()) {
				Block b = block.getBukkitBlock();
				p.sendBlockChange(block.getBukkitLocation(), b.getType(), b.getData());
			}
		}
	}

	void playerMoved(PlayerMoveEvent event) {
		final Player p = event.getPlayer();
		final ActionChunk from = new ActionChunk(event.getFrom());
		final ActionChunk to = new ActionChunk(event.getTo());
		final BukkitRunnable runnable = new BukkitRunnable() {

			@Override
			public void run() {
				updatePlayer(p, from, to);
			}
		};
		if (event instanceof PlayerTeleportEvent) {
			runnable.runTask(PlugCubeBuildersIn.getInstance());
		} else {
			runnable.run();
		}
	}

	private void updatePlayer(Player p, ActionChunk from, ActionChunk to) {
		if (from == to || (from != null && to != null && from.equals(to))) {
			return;
		}
		List<ActionChunk> oldChunks = new LinkedList<>();
		List<ActionChunk> newChunks = new LinkedList<>();
		for (int z = -replayRenderDistance; z <= replayRenderDistance; z++) {
			for (int x = -replayRenderDistance; x <= replayRenderDistance; x++) {
				if (from != null) {
					oldChunks.add(new ActionChunk(from.world, from.x + x, from.z + z));
				}
				if (to != null) {
					newChunks.add(new ActionChunk(to.world, to.x + x, to.z + z));
				}
			}
		}
		if (from != null && to != null) {
			for (Iterator<ActionChunk> it = newChunks.iterator(); it.hasNext();) {
				ActionChunk ch = it.next();
				if (oldChunks.remove(ch)) {
					it.remove();
				}
			}
		}
		if (from != null) {
			for (ActionChunk chunk : oldChunks) {
				Map<ActionBlock, BlockInfo> chunkBlocks = chunks.get(chunk);
				if (chunkBlocks != null) {
					for (ActionBlock block : chunkBlocks.keySet()) {
						revertBlock(p, block);
					}
				}
			}
		}
		if (to != null) {
			for (ActionChunk chunk : newChunks) {
				Map<ActionBlock, BlockInfo> chunkBlocks = chunks.get(chunk);
				if (chunkBlocks != null) {
					for (ActionBlock block : chunkBlocks.keySet()) {
						BlockInfo info = chunkBlocks.get(block);
						p.sendBlockChange(block.getBukkitLocation(), info.material, info.data);
					}
				}
			}
		}
	}

	private static class BlockInfo {

		public final Material material;
		public final byte data;

		public BlockInfo(Material material, byte data) {
			this.material = material;
			this.data = data;
		}
	}
}
