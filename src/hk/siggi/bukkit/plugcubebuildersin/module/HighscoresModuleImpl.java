package hk.siggi.bukkit.plugcubebuildersin.module;

import com.mojang.authlib.GameProfile;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.highscores.Strike;
import hk.siggi.bukkit.plugcubebuildersin.nms.NMSUtil;
import hk.siggi.bukkit.plugcubebuildersin.util.CBMath;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import hk.siggi.bukkit.plugcubebuildersin.world.WorldBlock;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

public final class HighscoresModuleImpl implements HighscoresModule, Listener {

	private static final UUID Siggi = UUID.fromString("490F3781-8A53-447F-9058-C0F820182FFF");
	//private static final UUID Christy = UUID.fromString("6E3CBBBA-E741-4A3B-8C96-BA82189ABD32");
	private static final UUID CubeBuildersGirl = UUID.fromString("4F6A3A30-7663-405B-A2B3-8AA4667057C9");

	private final Map<WorldBlock, ScoreboardView> scoreboardViews = new HashMap<>();
	private final Map<String, ScoreboardImpl> scoreboards = new HashMap<>();
	private boolean mainMetadataChanged = false;

	private PlugCubeBuildersIn plugin;

	private void addScoreboard(ScoreboardImpl scoreboard) {
		scoreboards.put(scoreboard.name, scoreboard);
	}

	private void load() {
		File f = new File(getScoreboardDir(), "scoreboard.properties");
		FileInputStream in = null;
		try {
			if (f.exists()) {
				in = new FileInputStream(f);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
					try {
						int eqPos = line.indexOf("=");
						String key = line.substring(0, eqPos).trim();
						String val = line.substring(eqPos + 1).trim();
						if (key.startsWith("view-")) {
							String[] csv = key.substring(5).split(",");
							if (csv.length == 4) {
								String world = csv[0];
								String x = csv[1];
								String y = csv[2];
								String z = csv[3];
								String[] csvVal = val.split(",");
								if (csvVal.length >= 2) {
									String scoreboard = csvVal[0];
									String position = csvVal[1];
									boolean personalSign = false;
									if (csvVal.length >= 3) {
										personalSign = Integer.parseInt(csvVal[2]) != 0;
									}
									scoreboardViews.put(
											new WorldBlock(
													world,
													Integer.parseInt(x),
													Integer.parseInt(y),
													Integer.parseInt(z)
											),
											new ScoreboardView(
													scoreboard,
													Integer.parseInt(position),
													personalSign
											)
									);
								}
							}
						} else if (key.equals("lastreset")) {
							lastScoreboardReset = Long.parseLong(val);
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (IOException | NumberFormatException e) {
			plugin.reportProblem("Couldn't load scoreboard.properties", e);
		} finally {
			tryClose(in);
		}
		for (ScoreboardImpl scoreboard : scoreboards.values()) {
			scoreboard.load();
		}
		saveOnNextUpdate = false;
	}

	private void save() {
		if (mainMetadataChanged) {
			File f = new File(getScoreboardDir(), "scoreboard.properties");
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(f);
				out.write(("lastreset=" + lastScoreboardReset + "\n").getBytes());
				for (WorldBlock block : scoreboardViews.keySet()) {
					ScoreboardView view = scoreboardViews.get(block);
					out.write(("view-" + block.world + "," + block.x + "," + block.y + "," + block.z + "=" + view.scoreboard + "," + view.position + "," + (view.personalSign ? "1" : "0") + "\n").getBytes());
				}
				mainMetadataChanged = false;
			} catch (IOException e) {
				plugin.reportProblem("Couldn't save scoreboard.properties", e);
			} finally {
				tryClose(out);
			}
		}
		for (ScoreboardImpl scoreboard : scoreboards.values()) {
			scoreboard.save();
		}
	}

	private ScoreboardView calculateNewView(Block block) {
		String scoreboard = null;
		int lowest = Integer.MAX_VALUE;
		WorldBlock lowestBlock = null;
		ScoreboardView lowestView = null;
		BlockFace[] checks = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};
		for (BlockFace face : checks) {
			if ((face == BlockFace.UP && block.getY() >= 255)
					|| (face == BlockFace.DOWN && block.getY() <= 0)) {
				continue;
			}
			Block blockCheck = block.getRelative(face);
			WorldBlock bl = new WorldBlock(blockCheck);
			ScoreboardView view = scoreboardViews.get(bl);
			if (view == null) {
				continue;
			}
			if (scoreboard != null && !scoreboard.equals(view.scoreboard)) {
				return null;
			}
			scoreboard = view.scoreboard;
			if (view.position < lowest) {
				lowest = view.position;
				lowestBlock = bl;
				lowestView = view;
			}
		}
		if (lowest == Integer.MAX_VALUE) {
			return null;
		}
		if (lowestBlock != null && lowestView != null) {
			ScoreboardView newForLowest = new ScoreboardView(lowestView.scoreboard, lowestView.position, false);
			scoreboardViews.put(lowestBlock, newForLowest);
			setup(lowestBlock.getBukkitBlock(), newForLowest);
		}
		return new ScoreboardView(scoreboard, lowest + 1, true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void placeSign(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		if (!p.hasPermission("hk.siggi.plugcubebuildersin.highscoresign")) {
			return;
		}
		Block block = event.getBlock();
		BlockState state = block.getState();
		if (state instanceof Sign) {
			ItemStack itemPlace = event.getItemInHand();
			ItemMeta meta = itemPlace.getItemMeta();
			String displayName = meta.getDisplayName();
			if (displayName.equals("§6Scoreboard")) {
				ScoreboardView view = calculateNewView(block);
				if (view != null) {
					setup(block, view);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void signChange(SignChangeEvent event) {
		Player p = event.getPlayer();
		Block block = event.getBlock();
		if (event.getLine(0).equalsIgnoreCase("[SB]")) {
			if (!p.hasPermission("hk.siggi.plugcubebuildersin.highscoresign")) {
				event.setLine(0, "[SB]");
				event.setLine(1, "Error:");
				event.setLine(2, "No Permission");
				event.setLine(3, "");
				return;
			}
			ScoreboardView view;
			String inf1 = event.getLine(1);
			String inf2 = event.getLine(2);
			String inf3 = event.getLine(3);
			if (!inf1.isEmpty() && inf2.isEmpty()) {
				inf2 = "1";
			}
			if (inf1.isEmpty()) {
				view = calculateNewView(block);
			} else {
				String sb = inf1;
				int pos = 1;
				try {
					pos = Integer.parseInt(inf2);
				} catch (Exception e) {
				}
				view = new ScoreboardView(sb, pos, false);
			}
			if (view == null) {
				event.setLine(0, "[SB]");
				event.setLine(1, "Error:");
				event.setLine(2, "invalid");
				event.setLine(3, "");
				return;
			}
			if (inf3.equals("1")) {
				view = new ScoreboardView(view.scoreboard, view.position, true);
			}
			event.setLine(0, "[SB]");
			event.setLine(1, view.scoreboard);
			event.setLine(2, Integer.toString(view.position));
			event.setLine(3, (view.personalSign ? "1" : "0"));
			ScoreboardImpl scoreboard = getScoreboard(view.scoreboard);
			if (scoreboard == null) {
				event.setLine(0, "[SB]");
				event.setLine(1, "Error:");
				event.setLine(2, "invalid");
				event.setLine(3, "");
				return;
			}
			setup(block, view);
		}
	}

	private void setup(Block block, ScoreboardView view) {
		World world = block.getWorld();
		int blockX = block.getX();
		int blockY = block.getY();
		int blockZ = block.getZ();
		scoreboardViews.put(new WorldBlock(block), view);
		mainMetadataChanged = true;
		int distanceAny = Integer.MAX_VALUE;
		int distanceUntaken = Integer.MAX_VALUE;
		Skull skullAny = null;
		Skull skullUntaken = null;
		Skull skullMatch = null;
		for (int z = blockZ - 1; z <= blockZ + 1; z++) {
			int diffX = z == blockZ ? 1 : 0;
			for (int x = blockX - diffX; x <= blockX + diffX; x++) {
				int diffY = (z == blockZ || x == blockX) ? 1 : 0;
				for (int y = blockY - diffY; y <= blockY + diffY; y++) {
					if (y < 0 || y > 255) {
						continue;
					}
					Block b = world.getBlockAt(x, y, z);
					BlockState state = b.getState();
					if (state instanceof Skull) {
						Skull skull = (Skull) state;
						int dist = Math.abs(x - blockX) + Math.abs(y - blockY) + Math.abs(z - blockZ);
						if (dist < distanceAny) {
							distanceAny = dist;
							skullAny = skull;
						}
						WorldBlock wb = new WorldBlock(b);
						if (!scoreboardViews.containsKey(wb) && dist < distanceUntaken) {
							distanceUntaken = dist;
							skullUntaken = skull;
						}
						ScoreboardView checkView = scoreboardViews.get(wb);
						if (checkView != null
								&& checkView.scoreboard.equals(view.scoreboard)
								&& checkView.position == view.position) {
							skullMatch = skull;
						}
					}
				}
			}
		}
		if (skullMatch != null) {
			Block bb = skullMatch.getBlock();
			scoreboardViews.put(new WorldBlock(bb), view);
			mainMetadataChanged = true;
		} else if (skullUntaken != null) {
			Block bb = skullUntaken.getBlock();
			scoreboardViews.put(new WorldBlock(bb), view);
			mainMetadataChanged = true;
		} else if (skullAny != null) {
			Block bb = skullAny.getBlock();
			scoreboardViews.put(new WorldBlock(bb), view);
			mainMetadataChanged = true;
		}
		saveOnNextUpdate = true;
		requireUpdate = true;
	}

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		//this week scoreboards
		addScoreboard(new ScoreboardImpl("bounty", "Most Wanted", "$", null, null));
		addScoreboard(new ScoreboardImpl("topkiller", "Top Killer", null, " hp", " hp"));
		addScoreboard(new ScoreboardImpl("topminer", "Top Miner", null, " ore", " ores"));
		addScoreboard(new TopPlayerScoreboard("topplayer", "Top Player"));
		addScoreboard(new TravelScoreboard("toptraveller", "Top Traveller"));
		addScoreboard(new ScoreboardImpl("mostdeaths", "Most Deaths", null, " death", " deaths"));
		addScoreboard(new ScoreboardImpl("blockplace", "Block Placer", null, " block", " blocks"));
		addScoreboard(new ScoreboardImpl("blockbreak", "Block Breaker", null, " block", " blocks"));

		//last week scoreboards
		addScoreboard(new ScoreboardImpl("LWtopkiller", "Top Killer", null, " hp", " hp"));
		addScoreboard(new ScoreboardImpl("LWtopminer", "Top Miner", null, " ore", " ores"));
		addScoreboard(new TopPlayerScoreboard("LWtopplayer", "Top Player"));
		addScoreboard(new TravelScoreboard("LWtoptraveller", "Top Traveller"));
		addScoreboard(new ScoreboardImpl("LWmostdeaths", "Most Deaths", null, " death", " deaths"));
		addScoreboard(new ScoreboardImpl("LWblockplace", "Block Placer", null, " block", " blocks"));
		addScoreboard(new ScoreboardImpl("LWblockbreak", "Block Breaker", null, " block", " blocks"));

		load();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void kill() {
		save();
	}

	private int ticksSincePlayerUpdate = 0;
	private int ticksSinceUpdate = 0;
	private boolean requireUpdate = false;
	private boolean saveOnNextUpdate = false;
	private long lastScoreboardReset = 0L;
	private long nextScoreboardReset = 0L;

	@Override
	public void tick() {
		boolean requireSave = false;
		long now = System.currentTimeMillis();
		if (nextScoreboardReset == 0L) {
			GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
			cal.setTimeInMillis(now);
			while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				cal.add(Calendar.DATE, -1);
			}
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long lastResetShouldBe = cal.getTimeInMillis();
			cal.add(Calendar.DATE, 7);
			long nextResetShouldBe = cal.getTimeInMillis();
			if (lastScoreboardReset < lastResetShouldBe) {
				nextScoreboardReset = lastResetShouldBe;
			} else {
				nextScoreboardReset = nextResetShouldBe;
			}
		}
		if (now >= nextScoreboardReset) {
			nextScoreboardReset = 0L;
			lastScoreboardReset = now;
			resetScoreboards();
			mainMetadataChanged = true;
			requireSave = true;
		}
		ticksSinceUpdate += 1;
		if (ticksSinceUpdate >= (requireUpdate ? 100 : 1200)) {
			ticksSinceUpdate = 0;
			ticksSincePlayerUpdate = 100;
			requireUpdate = false;
			updatePlayerTimes();
			updateScoreboardViews();
			if (saveOnNextUpdate) {
				saveOnNextUpdate = false;
				requireSave = true;
			}
		}
		ticksSincePlayerUpdate += 1;
		if (ticksSincePlayerUpdate >= 100) {
			updatePersonalSigns();
		}
		if (requireSave) {
			save();
		}
	}

	private void resetScoreboards() {
		for (ScoreboardImpl scoreboard : scoreboards.values()) {
			if (!scoreboard.name.startsWith("LW")) {
				ScoreboardImpl lw = scoreboards.get("LW" + scoreboard.name);
				if (lw == null) {
					continue;
				}
				lw.clear();
				for (ScoreboardImpl.ScoreImpl score : scoreboard.getScores()) {
					lw.setScore(score.player, score.score);
				}
			}
		}
		for (ScoreboardImpl scoreboard : scoreboards.values()) {
			if (scoreboard.name.equals("bounty")) {
				continue;
			}
			if (!scoreboard.name.startsWith("LW")) {
				scoreboard.clear();
			}
		}
	}

	@EventHandler
	public void blockBroken(BlockBreakEvent event) {
		// prevent accidentally breaking scoreboard view
		Block block = event.getBlock();
		if (scoreboardViews.containsKey(new WorldBlock(block))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void handlePhysics(BlockPhysicsEvent event) {
		// prevent accidentally breaking scoreboard view
		Block block = event.getBlock();
		if (scoreboardViews.containsKey(new WorldBlock(block))) {
			event.setCancelled(true);
		}
	}

	@Override
	public ScoreboardImpl getScoreboard(String name) {
		return scoreboards.get(name);
	}

	private File scoreboardDir = null;

	private File getScoreboardDir() {
		if (scoreboardDir == null) {
			scoreboardDir = new File(plugin.getDataFolder(), "scoreboard");
			if (!scoreboardDir.isDirectory()) {
				if (scoreboardDir.exists()) {
					scoreboardDir.delete();
				}
				scoreboardDir.mkdirs();
			}
		}
		return scoreboardDir;
	}

	private File getScoreboardFile(String name) {
		return new File(getScoreboardDir(), name + ".txt");
	}

	private void updateScoreboardViews() {
		for (ScoreboardImpl scoreboard : scoreboards.values()) {
			scoreboard.recalculateTopList();
		}
		for (Iterator<WorldBlock> it = scoreboardViews.keySet().iterator(); it.hasNext();) {
			WorldBlock block = it.next();
			if (block.isChunkLoaded()) {
				if (!updateScoreboardView(block, scoreboardViews.get(block))) {
					it.remove();
					mainMetadataChanged = true;
				}
			}
		}
	}

	private boolean updateScoreboardView(WorldBlock worldBlock, ScoreboardView view) {
		ScoreboardImpl scoreboard = getScoreboard(view.scoreboard);
		if (scoreboard == null) {
			return false;
		}
		ScoreboardImpl.ScoreImpl score = scoreboard.getScoreAtPosition(view.position);
		Block block = worldBlock.getBukkitBlock();
		BlockState state = block.getState();
		if (state instanceof Sign) {
			Sign sign = (Sign) state;
			try {
				if (score == null) {
					sign.setLine(0, "--Empty--");
					sign.setLine(1, "");
					sign.setLine(2, "");
					sign.setLine(3, "");
				} else {
					sign.setLine(0, scoreboard.title + " " + view.position);
					String name = plugin.getUUIDCache().getNameFromUUID(score.player);
					if (name == null) {
						name = "<#" + (score.player.toString().replace("-", "").substring(0, 8).toUpperCase()) + ">";
					}
					if (name.length() > 16) {
						name = name.substring(0, 16);
					}
					sign.setLine(1, name);
					sign.setLine(2, scoreboard.scoreToString1(score));
					sign.setLine(3, scoreboard.scoreToString2(score));
				}
				sign.update();
			} catch (Exception e) {
				plugin.reportProblem("Could not update sign for highscores", e);
			}
			return true;
		} else if (state instanceof Skull) {
			Skull skull = (Skull) state;
			try {
				GameProfile profile = null;
				if (score != null) {
					profile = plugin.getGameProfile(new GameProfile(score.player, null));
				}
				if (profile == null) {
					profile = plugin.getGameProfile(new GameProfile((view.position % 2 == 1) ? Siggi : CubeBuildersGirl, null));
				}
				plugin.setGameProfile(skull, profile);
				skull.update();
			} catch (Exception e) {
				plugin.reportProblem("Could not update skull for highscores", e);
			}
			return true;
		} else {
			return false;
		}
	}

	public class ScoreboardView {

		public final String scoreboard;
		public final int position;
		public final boolean personalSign;

		public ScoreboardView(String scoreboard, int position, boolean personalSign) {
			if (scoreboard == null) {
				throw new NullPointerException("scoreboard can't be null");
			} else if (position <= 0) {
				throw new IllegalArgumentException("position must be 1 or greater");
			}
			this.scoreboard = scoreboard;
			this.position = position;
			this.personalSign = personalSign;
		}
	}

	public class ScoreboardImpl implements Scoreboard {

		private final Map<UUID, ScoreImpl> scores = new HashMap<>();
		private final String name, title;
		private final String prefix, suffix, suffixPlural;
		private List<ScoreImpl> calculated = null;
		private Map<UUID, Integer> playerToPosition = null;
		private boolean scoresChanged = false;

		public ScoreboardImpl(String name, String title, String prefix, String suffix, String suffixPlural) {
			if (name == null) {
				throw new NullPointerException("name can't be null");
			}
			if (title == null) {
				throw new NullPointerException("title can't be null");
			}
			this.name = name;
			this.title = title;
			if (suffixPlural == null) {
				suffixPlural = suffix;
			} else if (suffix == null) {
				suffix = suffixPlural;
			}
			this.prefix = prefix;
			this.suffix = suffix;
			this.suffixPlural = suffixPlural;
		}

		public void load() {
			File f = getScoreboardFile(name);
			if (!f.exists()) {
				return;
			}
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
				String line;
				while ((line = reader.readLine()) != null) {
					try {
						int eqPos = line.indexOf("=");
						if (eqPos == -1) {
							continue;
						}
						String key = line.substring(0, eqPos).trim();
						String val = line.substring(eqPos + 1).trim();
						UUID player = Util.uuidFromString(key);
						long score = Long.parseLong(val);
						setScore(player, score);
					} catch (Exception e) {
					}
				}
				scoresChanged = false;
			} catch (Exception e) {
				plugin.reportProblem("Couldn't load scoreboard " + name, e);
			} finally {
				tryClose(fis);
			}
		}

		public void save() {
			if (!scoresChanged) {
				return;
			}
			File f = getScoreboardFile(name);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
				for (ScoreImpl score : scores.values()) {
					String line = score.player.toString().replace("-", "") + "=" + score.score + "\n";
					fos.write(line.getBytes());
				}
				scoresChanged = false;
			} catch (Exception e) {
				plugin.reportProblem("Couldn't save scoreboard " + name, e);
			} finally {
				tryClose(fos);
			}
		}

		@Override
		public void clear() {
			scoresChanged = true;
			scores.clear();
		}

		@Override
		public void setScore(UUID player, long score) {
			saveOnNextUpdate = true;
			scoresChanged = true;
			if (score == 0L) {
				scores.remove(player);
			} else {
				scores.put(player, new ScoreImpl(player, score));
			}
		}

		@Override
		public long incrementScore(UUID player, long increment) {
			long newScore = getScore(player) + increment;
			setScore(player, newScore);
			return newScore;
		}

		@Override
		public long getScore(UUID player) {
			ScoreImpl score = scores.get(player);
			if (score == null) {
				return 0L;
			} else {
				return score.score;
			}
		}

		@Override
		public int getPositionForPlayer(UUID player) {
			if (playerToPosition == null) {
				return -1;
			}
			Integer i = playerToPosition.get(player);
			if (i == null) {
				return -1;
			}
			return i;
		}

		@Override
		public ScoreImpl getScoreAtPosition(int position) {
			if (calculated == null) {
				return null;
			}
			try {
				return calculated.get(position - 1);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}

		@Override
		public void recalculateTopList() {
			calculated = getScores();
			playerToPosition = new HashMap<>();
			int s = calculated.size();
			for (int i = 0; i < s; i++) {
				ScoreImpl get = calculated.get(i);
				playerToPosition.put(get.player, i + 1);
			}
		}

		@Override
		public List<ScoreImpl> getScores() {
			List<ScoreImpl> result = new ArrayList<>(scores.size());
			result.addAll(scores.values());
			result.sort(new Comparator<ScoreImpl>() {
				@Override
				public int compare(ScoreImpl o1, ScoreImpl o2) {
					if (o1.score > o2.score) {
						return -1;
					} else if (o2.score > o1.score) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			return result;
		}

		public String scoreToString1(ScoreImpl score) {
			return scoreToString1(score.score);
		}

		public String scoreToString1(long score) {
			return (prefix == null ? "" : prefix) + Long.toString(score) + (suffix == null ? "" : (score == 1L ? suffix : suffixPlural));
		}

		public String scoreToString2(ScoreImpl score) {
			return scoreToString2(score.score);
		}

		public String scoreToString2(long score) {
			return "";
		}

		public class ScoreImpl implements Score {

			public final UUID player;
			public final long score;

			public ScoreImpl(UUID player, long score) {
				this.player = player;
				this.score = score;
			}

			@Override
			public UUID getPlayer() {
				return player;
			}

			@Override
			public long getScore() {
				return score;
			}
		}
	}

	public class TravelScoreboard extends ScoreboardImpl {

		public TravelScoreboard(String name, String title) {
			super(name, title, null, null, null);
		}

		@Override
		public String scoreToString1(long distance) {
			double m = ((double) distance) / 100.0;
			double km = m / 1000.0;
			if (m < 1000.0) {
				return (Math.floor(m * 100.0) / 100.0) + " m";
			}
			if (km < 1000.0) {
				return (Math.floor(km * 100.0) / 100.0) + " km";
			}
			return (Math.floor(km * 10.0) / 10.0) + " km";
		}

		@Override
		public String scoreToString2(long distance) {
			double inches = ((double) distance) / 2.54;
			double feet = inches / 12.0;
			double yard = feet / 3.0;
			double mile = feet / 5280.0;
			if (yard < 1000.0) {
				return (Math.floor(yard * 100.0) / 100.0) + " yards";
			}
			if (mile < 1000.0) {
				return (Math.floor(mile * 100.0) / 100.0) + " miles";
			}
			return (Math.floor(mile * 10.0) / 10.0) + " miles";
		}
	}

	public class TopPlayerScoreboard extends ScoreboardImpl {

		public TopPlayerScoreboard(String name, String title) {
			super(name, title, null, null, null);
		}

		@Override
		public String scoreToString1(long milliseconds) {
			long seconds = milliseconds / 1000;
			long minutes = seconds / 60;
			long hours = minutes / 60;
			minutes %= 60;
			return hours + " hr " + minutes + " min";
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerMined(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player p = event.getPlayer();
		if (p == null) {
			return;
		}
		Material type = event.getBlock().getType();
		Material glowingRedstone = getGlowingRedstone();
		Material quartzOre = getQuartzOre();
		if (type == Material.IRON_ORE
				|| type == Material.GOLD_ORE
				|| type == Material.DIAMOND_ORE
				|| type == Material.LAPIS_ORE
				|| type == Material.EMERALD_ORE
				|| type == Material.COAL_ORE
				|| type == Material.REDSTONE_ORE
				|| (glowingRedstone != null && type == glowingRedstone)
				|| type == quartzOre
				|| type == Material.OBSIDIAN) {
			getScoreboard("topminer").incrementScore(event.getPlayer().getUniqueId(), 1L);
		}
	}

	private Material glowingRedstoneMaterial = null;
	private boolean gotGlowingRedstoneMaterial = false;

	public Material getGlowingRedstone() {
		if (!gotGlowingRedstoneMaterial) {
			gotGlowingRedstoneMaterial = true;
			try {
				glowingRedstoneMaterial = Material.getMaterial("GLOWING_REDSTONE_ORE");
			} catch (Throwable t) {
			}
		}
		return glowingRedstoneMaterial;
	}

	private Material quartzOreMaterial = null;

	public Material getQuartzOre() {
		if (quartzOreMaterial == null) {
			try {
				quartzOreMaterial = Material.NETHER_QUARTZ_ORE;
			} catch (Throwable t) {
			}
		}
		return quartzOreMaterial;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerAttacked(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Entity damager = event.getDamager();
		if (damager == null) {
			return;
		}
		Player player;
		if (!(damager instanceof Player)) {
			if (!(damager instanceof Projectile)) {
				return;
			}
			Projectile projectile = (Projectile) damager;
			ProjectileSource source = projectile.getShooter();
			if (!(source instanceof Player)) {
				return;
			}
			player = (Player) source;
		} else {
			player = (Player) damager;
		}
		int damage = (int) Math.ceil(event.getDamage());
		getScoreboard("topkiller").incrementScore(player.getUniqueId(), damage);
		Entity victimEntity = event.getEntity();
		if (victimEntity == null) {
			return;
		}
		if (!(victimEntity instanceof Player)) {
			return;
		}
		Player victim = (Player) victimEntity;
		long now = System.currentTimeMillis();
		Strike strike = new Strike(player, victim, now);
		plugin.lastStrikes.put(victim, strike);
		plugin.lastAttacks.put(player, strike);
		ArrayList<Strike> recent = plugin.recentAttacks.get(player);
		if (recent == null) {
			plugin.recentAttacks.put(player, recent = new ArrayList<>());
		}
		recent.add(strike);
		for (int i = 0; i < recent.size(); i++) {
			Strike s = recent.get(i);
			if (now - s.time > plugin.pvpTimer) {
				recent.remove(i);
				i--;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerDeath(PlayerDeathEvent event) {
		Player p = event.getEntity();
		getScoreboard("mostdeaths").incrementScore(p.getUniqueId(), 1L);
		highscoresworld:
		try {
			Strike lastStrike = (Strike) plugin.lastStrikes.remove(p);
			if (lastStrike == null) {
				break highscoresworld;
			}
			if (System.currentTimeMillis() - lastStrike.time < 10000L) {
				ScoreboardImpl bountyScoreboard = getScoreboard("bounty");
				long bounty = bountyScoreboard.getScore(p.getUniqueId());
				if (bounty > 0) {
					if (lastStrike.attacker.isOnline()) {
						if (lastStrike.attacker != p) {
							try {
								BankModule money = plugin.getBankModule();
								money.giveMoney(lastStrike.attacker, bounty);
								bountyScoreboard.setScore(p.getUniqueId(), 0L);
								event.setDeathMessage(ChatColor.GREEN + lastStrike.attacker.getName() + " has killed " + event.getEntity().getName() + " and receives a cash reward of $" + bounty + "!");
							} catch (Exception e) {
								plugin.reportProblem("Error giving money for bounty", e);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			plugin.reportProblem("Error processing bounty for death", e);
		}
		plugin.lastAttacks.remove((Player) event.getEntity());
		plugin.lastStrikes.remove((Player) event.getEntity());
		plugin.recentAttacks.remove((Player) event.getEntity());
		plugin.lastPvPCommand.remove((Player) event.getEntity());
	}

	@EventHandler
	public void playerMoving(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (p.isFlying()) {
			return;
		}
		try {
			Location from = event.getFrom();
			Location to = event.getTo();
			double distanceMoved = CBMath.sqrt(from.distanceSquared(to));
			getScoreboard("toptraveller").incrementScore(p.getUniqueId(), (long) Math.floor(distanceMoved * 100.0));
		} catch (Exception e) {
		}
	}
	private final Map<Player, Long> lastPlayerTimeUpdate = new WeakHashMap<>();

	private void updatePlayerTimes() {
		long now = System.currentTimeMillis();
		for (Player p : Bukkit.getOnlinePlayers()) {
			Long joinL = lastPlayerTimeUpdate.get(p);
			lastPlayerTimeUpdate.put(p, now);
			if (joinL == null) {
				return;
			}
			long join = joinL;
			long timeOnline = now - join;
			getScoreboard("topplayer").incrementScore(p.getUniqueId(), timeOnline);
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		lastPlayerTimeUpdate.put(event.getPlayer(), System.currentTimeMillis());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		Long joinL = lastPlayerTimeUpdate.get(p);
		if (joinL == null) {
			return;
		}
		long join = joinL;
		long timeOnline = System.currentTimeMillis() - join;
		getScoreboard("topplayer").incrementScore(p.getUniqueId(), timeOnline);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event) {
		Player p = event.getPlayer();
		getScoreboard("blockplace").incrementScore(p.getUniqueId(), 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent event) {
		Player p = event.getPlayer();
		getScoreboard("blockbreak").incrementScore(p.getUniqueId(), 1L);
	}

	public void updatePersonalSigns() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			UUID uuid = p.getUniqueId();
			String name = p.getName();
			List<WorldBlock> nearbySigns = getNearbySigns(p.getLocation());
			for (WorldBlock wb : nearbySigns) {
				ScoreboardView view = scoreboardViews.get(wb);
				if (!view.personalSign) {
					continue;
				}
				ScoreboardImpl sb = getScoreboard(view.scoreboard);
				int position = sb.getPositionForPlayer(uuid);
				if (position <= view.position) {
					continue;
				}
				ScoreboardImpl.ScoreImpl score = sb.getScoreAtPosition(position);
				Block block = wb.getBukkitBlock();
				BlockState state = block.getState();
				if (state instanceof Sign) {
					p.sendSignChange(
							block.getLocation(),
							new String[]{
								sb.title + " " + position,
								name,
								sb.scoreToString1(score),
								sb.scoreToString2(score)
							}
					);
				} else if (state instanceof Skull) {
					Skull skull = (Skull) state;
					GameProfile profile = plugin.getGameProfile(new GameProfile(uuid, name));
					NMSUtil.get().sendPacket(p, NMSUtil.get().createPacket(skull, profile));
				}
			}
		}
	}

	public List<WorldBlock> getNearbySigns(Location loc) {
		List<WorldBlock> listOfBlocks = new ArrayList<>(scoreboardViews.size());
		String wn = loc.getWorld().getName();
		double maxDistance = 20.0;
		maxDistance = maxDistance * maxDistance;
		for (WorldBlock wb : scoreboardViews.keySet()) {
			if (wb.world.equals(wn) && wb.getBukkitLocation(true).distanceSquared(loc) <= maxDistance) {
				listOfBlocks.add(wb);
			}
		}
		return listOfBlocks;
	}
}
