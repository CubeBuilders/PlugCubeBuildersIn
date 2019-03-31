package hk.siggi.bukkit.plugcubebuildersin.replay;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.Action;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ActionBlock;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ActionLocation;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ArmswingAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.BlockChangeAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.LoginAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.LogoutAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.MoveAction;
import hk.siggi.bukkit.plugcubebuildersin.util.ReadingIterator;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class ActionRecorder implements Listener {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

	private static final long recordInterval = 600000L; // every 10 minutes

	public static final long getLogNumber(long time) {
		return (time - (time % recordInterval)) / recordInterval;
	}

	public static final long getLogTime(long logNumber) {
		return logNumber * recordInterval;
	}

	private final ActionReplayImpl replay;
	private final PlugCubeBuildersIn plugin;
	private boolean killed = false;
	private long earliestRecordedHistory;

	public long getEarliestRecordedHistory() {
		return earliestRecordedHistory;
	}

	public ActionRecorder(ActionReplayImpl replay, PlugCubeBuildersIn plugin) {
		this.replay = replay;
		this.plugin = plugin;
		earliestRecordedHistory = System.currentTimeMillis();
		try {
			long[] bounds = getLogBounds();
			if (bounds[0] != Long.MAX_VALUE) {
				Action[] log = readLogFile(bounds[0]);
				earliestRecordedHistory = log[0].time;
			}
		} catch (Exception e) {
			plugin.reportProblem("Could not determine earliest recorded history", e);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerMove(PlayerMoveEvent event) {
		recordAction(
				new MoveAction(System.currentTimeMillis(),
						event.getPlayer().getUniqueId(),
						new ActionLocation(event.getFrom()),
						new ActionLocation(event.getTo()),
						false
				)
		);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerTeleport(PlayerTeleportEvent event) {
		recordAction(
				new MoveAction(System.currentTimeMillis(),
						event.getPlayer().getUniqueId(),
						new ActionLocation(event.getFrom()),
						new ActionLocation(event.getTo()),
						true
				)
		);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerAnimation(PlayerAnimationEvent event) {
		recordAction(
				new ArmswingAction(
						System.currentTimeMillis(),
						event.getPlayer().getUniqueId()
				)
		);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerBlockPlace(BlockPlaceEvent event) {
		recordAction(
				new BlockChangeAction(
						System.currentTimeMillis(),
						event.getPlayer().getUniqueId(),
						new ActionBlock(event.getBlock()),
						event.getBlockReplacedState().getType(),
						event.getBlockReplacedState().getData().getData(),
						event.getBlock().getType(),
						event.getBlock().getData()
				)
		);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerBlockBreak(BlockBreakEvent event) {
		recordAction(
				new BlockChangeAction(
						System.currentTimeMillis(),
						event.getPlayer().getUniqueId(),
						new ActionBlock(event.getBlock()),
						event.getBlock().getType(),
						event.getBlock().getData(),
						Material.AIR,
						(byte) 0
				)
		);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		recordAction(
				new LoginAction(
						System.currentTimeMillis(),
						p.getUniqueId(),
						new ActionLocation(p.getLocation())
				)
		);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		recordAction(
				new LogoutAction(
						System.currentTimeMillis(),
						p.getUniqueId(),
						new ActionLocation(p.getLocation())
				)
		);
	}

	private long lastLogNumber = -1L;
	private File lastLogFile = null;
	private OutputStream outputStream = null;

	private void closeOutputStream() {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception e) {
		} finally {
			outputStream = null;
			if (lastLogFile != null) {
				checkAndFixFile(lastLogFile, true);
				Util.compressFile(lastLogFile);
			}
		}
	}

	private OutputStream getOutputStream(long logNumber) {
		dontMakeNewOutputStream:
		{
			makeNewOutputStream:
			{
				if (outputStream == null) {
					break makeNewOutputStream;
				}
				if (lastLogNumber == logNumber) {
					break dontMakeNewOutputStream;
				}
				closeOutputStream();
			}
			boolean success = false;
			try {
				File f = getFileForLog(logNumber);
				checkAndFixFile(f, false);
				outputStream = new FileOutputStream(f, true);
				if (f.length() == 0) {
					outputStream.write(0x20);
				}
				success = true;
				lastLogFile = f;
				lastLogNumber = logNumber;
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (!success) {
					outputStream = null;
				}
			}
		}
		return outputStream;
	}

	private File getActionLogDir() {
		File actionLogDir = new File(plugin.getDataFolder(), "actionlog");
		if (!actionLogDir.isDirectory()) {
			if (actionLogDir.exists()) {
				actionLogDir.delete();
			}
			actionLogDir.mkdirs();
		}
		return actionLogDir;
	}

	private File getFileForLog(long logNumber) {
		File actionLogDir = getActionLogDir();
		return new File(actionLogDir, logNumber + ".json");
	}

	private void checkAndFixFile(File f, boolean shouldBeLocked) {
		// Check file first, instead of just going ahead and fixing it even if it's already fixed.
		boolean needsFix = false;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");
			raf.seek(0);
			int a = raf.read();
			raf.seek(raf.length() - 1);
			int b = raf.read();
			if (shouldBeLocked) {
				if (a != (int) '[' || b != (int) ']') {
					needsFix = true;
				}
			} else {
				if (a != (int) ' ' || b != (int) ',') {
					needsFix = true;
				}
			}
		} catch (IOException e) {
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (Exception e) {
				}
			}
		}
		if (needsFix) {
			fixFile(f, shouldBeLocked);
		}
	}

	private void fixFile(File f, boolean shouldBeLocked) {
		int a = (int) '[';
		int b = (int) ']';
		if (!shouldBeLocked) {
			a = (int) ' ';
			b = (int) ',';
		}
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "rw");
			raf.seek(0);
			raf.write(a);
			raf.seek(raf.length() - 1);
			raf.write(b);
		} catch (IOException e) {
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private void recordAction(Action action) {
		if (killed) {
			return;
		}
		writeLock.lock();
		try {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				String str = action.toJson();
				String str2 = ",";

				baos.write(str.getBytes());
				baos.write(str2.getBytes());

				OutputStream out = getOutputStream(getLogNumber(action.time));
				out.write(baos.toByteArray());
			} catch (Exception e) {
			}
		} finally {
			writeLock.unlock();
		}
	}

	private ReadJsonResult readLogFileJson(long logFile, long skipBytes) {
		readLock.lock();
		try {
			RandomAccessFile in = null;
			long newSkipBytes = 0L;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				in = new RandomAccessFile(getFileForLog(logFile), "r");
				in.seek(skipBytes);
				int c;
				byte[] b = new byte[8388608];
				while ((c = in.read(b, 0, b.length)) != -1) {
					baos.write(b, 0, c);
				}
				newSkipBytes = in.length() - 1L;
			} catch (Exception e) {
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
					}
				}
			}
			byte[] result = baos.toByteArray();
			if (result.length < 2) {
				return new ReadJsonResult("[]", 0L);
			}
			result[0] = (byte) ((int) '[');
			result[result.length - 1] = (byte) ((int) ']');
			return new ReadJsonResult(new String(result), newSkipBytes);
		} finally {
			readLock.unlock();
		}
	}

	public Action[] readLogFile(long logFile) {
		try {
			ReadJsonResult result = readLogFileJson(logFile, 0L);
			return Action.getGson().fromJson(result.json, Action[].class);
		} catch (Exception e) {
			return new Action[0];
		}
	}

	public ReadResult readLogFileExtended(long logFile, long skipBytes) {
		try {
			ReadJsonResult result = readLogFileJson(logFile, skipBytes);
			return new ReadResult(Action.getGson().fromJson(result.json, Action[].class), result.fileOffset);
		} catch (Exception e) {
			return new ReadResult(new Action[0], 0L);
		}
	}

	private class ReadJsonResult {

		public final String json;
		public final long fileOffset;

		ReadJsonResult(String json, long fileOffset) {
			this.json = json;
			this.fileOffset = fileOffset;
		}
	}

	public class ReadResult {

		public final Action[] actions;
		public final long fileOffset;

		ReadResult(Action[] actions, long fileOffset) {
			this.actions = actions;
			this.fileOffset = fileOffset;
		}
	}

	public long[] getLogBounds() {
		long low = Long.MAX_VALUE;
		long high = Long.MIN_VALUE;
		File actionLogDir = getActionLogDir();
		File[] ff = actionLogDir.listFiles();
		for (File f : ff) {
			String name = f.getName();
			if (name.endsWith(".json")) {
				try {
					long x = Long.parseLong(name.substring(0, name.length() - 5));
					low = Math.min(x, low);
					high = Math.max(x, high);
				} catch (Exception e) {
				}
			}
		}
		return new long[]{low, high};
	}

	public LinkedList<Action> getActions(long startTime, long endTime) {
		LinkedList<Action> actions = new LinkedList<>();
		long first = getLogNumber(startTime);
		long last = getLogNumber(endTime);
		try {
			for (long i = first; i <= last; i++) {
				for (Action action : readLogFile(i)) {
					if (action.time >= startTime && action.time <= endTime) {
						actions.add(action);
					}
				}
			}
		} catch (Exception e) {
		}
		return actions;
	}

	public Iterable<Action> iterateActions(final long first, final long log) {
		return new Iterable<Action>() {
			@Override
			public Iterator<Action> iterator() {
				return ActionRecorder.this.iterator(first, log);
			}
		};
	}

	public Iterator<Action> iterator(final long first, final long last) {
		return new ReadingIterator<Action>() {
			private Action[] actions = null;
			private int nextAction = -1;
			private long currentLogFile = first - 1L;
			private boolean ended = false;

			@Override
			protected Action read() throws NoSuchElementException {
				if (ended) {
					throw new NoSuchElementException();
				}
				while (actions == null || nextAction >= actions.length) {
					currentLogFile += 1;
					if (currentLogFile > last) {
						ended = true;
						throw new NoSuchElementException();
					}
					actions = readLogFile(currentLogFile);
					nextAction = 0;
				}
				Action result = actions[nextAction];
				nextAction += 1;
				return result;
			}
		};
	}

	void init() {
		killed = false;
		File dir = getActionLogDir();
		File[] ff = dir.listFiles();
		for (File f : ff) {
			String name = f.getName();
			if (name.endsWith(".json")) {
				try {
					long x = Long.parseLong(name.substring(0, name.length() - 5));
				} catch (Exception e) {
					continue;
				}
				checkAndFixFile(f, true);
			}
		}
		long now = System.currentTimeMillis();
		for (Player p : Bukkit.getOnlinePlayers()) {
			recordAction(
					new LoginAction(
							now,
							p.getUniqueId(),
							new ActionLocation(p.getLocation())
					)
			);
		}
	}

	void kill() {
		long now = System.currentTimeMillis();
		for (Player p : Bukkit.getOnlinePlayers()) {
			recordAction(
					new LogoutAction(
							now,
							p.getUniqueId(),
							new ActionLocation(p.getLocation())
					)
			);
		}
		killed = true;
		closeOutputStream();
	}

	private long lastCleanup = 0L;

	void tick() {
		if (lastCleanup < System.currentTimeMillis() - (60000L * 30L)) {
			lastCleanup = System.currentTimeMillis();
			cleanup();
		}
	}

	private void cleanup() {
		long now = System.currentTimeMillis();
		long expired = now - (86400000L * 7L);
		File actionLogDir = getActionLogDir();
		File[] ff = actionLogDir.listFiles();
		boolean didDelete = false;
		for (File f : ff) {
			String name = f.getName();
			if (name.endsWith(".json")) {
				try {
					long x = Long.parseLong(name.substring(0, name.length() - 5));
					long t = (x - 1) * recordInterval;
					if (t < expired) {
						f.delete();
						didDelete = true;
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		if (didDelete) {
			long newLow = getLogBounds()[0];
			Action[] log = readLogFile(newLow);
			if (log.length > 0) {
				earliestRecordedHistory = log[0].time;
			}
		}
	}
}
