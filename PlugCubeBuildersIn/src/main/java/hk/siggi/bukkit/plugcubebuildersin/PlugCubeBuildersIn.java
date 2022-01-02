package hk.siggi.bukkit.plugcubebuildersin;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import hk.siggi.bukkit.nbt.NBTCompound;
import hk.siggi.bukkit.nbt.NBTList;
import hk.siggi.bukkit.nbt.NBTTool;
import hk.siggi.bukkit.nbt.NBTUtil;
import hk.siggi.bukkit.plugcubebuildersin.commands.BountyCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.ClearChatCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.CloseTheEnd;
import hk.siggi.bukkit.plugcubebuildersin.commands.DebugCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.DonateCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.FactionResetCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.HelpCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.InactiveCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.MusicCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.PingCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.PongCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.PvPCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.QuitCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.RenderDistanceCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.SignEditCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.SnapToGrid;
import hk.siggi.bukkit.plugcubebuildersin.commands.SpeedUpCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.StaffToggleCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.TellAllRawCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.VPCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.VoteCommand;
import hk.siggi.bukkit.plugcubebuildersin.commands.WorldSpawnCommand;
import hk.siggi.bukkit.plugcubebuildersin.highscores.Strike;
import hk.siggi.bukkit.plugcubebuildersin.module.BankModule;
import hk.siggi.bukkit.plugcubebuildersin.module.ErrorReportModule;
import hk.siggi.bukkit.plugcubebuildersin.module.EssentialsModule;
import hk.siggi.bukkit.plugcubebuildersin.module.EssentialsSpawnModule;
import hk.siggi.bukkit.plugcubebuildersin.module.FactionsModule;
import hk.siggi.bukkit.plugcubebuildersin.module.Module;
import hk.siggi.bukkit.plugcubebuildersin.module.OpenInvModule;
import hk.siggi.bukkit.plugcubebuildersin.module.VaultModule;
import hk.siggi.bukkit.plugcubebuildersin.module.WorldLoaderModule;
import hk.siggi.bukkit.plugcubebuildersin.music.BlockPlayer;
import hk.siggi.bukkit.plugcubebuildersin.music.MusicPlayer;
import hk.siggi.bukkit.plugcubebuildersin.nms.BrigadierUtil;
import hk.siggi.bukkit.plugcubebuildersin.nms.ChatSetting;
import hk.siggi.bukkit.plugcubebuildersin.nms.NMSUtil;
import hk.siggi.bukkit.plugcubebuildersin.permissionloader.PermissionLoader;
import hk.siggi.bukkit.plugcubebuildersin.punisher.PunisherModule;
import hk.siggi.bukkit.plugcubebuildersin.replay.ActionReplay;
import hk.siggi.bukkit.plugcubebuildersin.skins.SkinServerHandler;
import hk.siggi.bukkit.plugcubebuildersin.teleportcontrol.FactionsTeleportControlSystem;
import hk.siggi.bukkit.plugcubebuildersin.teleportcontrol.TeleportControlSystem;
import hk.siggi.bukkit.plugcubebuildersin.util.CommandThrowableCatchingProxy;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import hk.siggi.bukkit.plugcubebuildersin.vanish.PlayerVanisher;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

public class PlugCubeBuildersIn extends JavaPlugin implements Listener, PluginMessageListener, VariableServerConnection.Listener {

	private final ArrayList<String> corruptedChunks = new ArrayList<>();
	private SkinServerHandler skinServerHandler;

	private int tick = 0;

	public void corruptedChunk(String worldName, int chunkX, int chunkZ) {
		String chunkID = worldName + "," + chunkX + "," + chunkZ;
		if (!corruptedChunks.contains(chunkID)) {
			corruptedChunks.add(chunkID);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void loadingACorruptedChunk(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		String worldName = chunk.getWorld().getName();
		int chunkX = chunk.getX();
		int chunkZ = chunk.getZ();
		int regionX = chunkX >> 5;
		int regionZ = chunkZ >> 5;
		String chunkID = worldName + "," + chunkX + "," + chunkZ;
		if (!corruptedChunks.contains(chunkID)) {
			return;
		}
		File dir = new File("corruptedregions" + File.separator + worldName);
		if (!dir.isDirectory()) {
			if (dir.isFile()) {
				dir.mkdirs();
			}
		}
		File dest = new File(dir, "r." + regionX + "," + regionZ + ".mca");
		for (int i = 1; dest.exists(); i++) {
			dest = new File(dir, "r." + regionX + "," + regionZ + "." + i + ".mca");
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(worldName + File.separator + "region" + File.separator + "r." + regionX + "," + regionZ + ".mca");
			out = new FileOutputStream(dest);
			byte[] b = new byte[4096];
			int c;
			while ((c = in.read(b, 0, b.length)) != -1) {
				out.write(b, 0, c);
			}
			in.close();
			out.close();
		} catch (Exception e) {
		} finally {
			tryClose(in, out);
		}
	}

	@EventHandler
	public void loadChunk(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		for (Entity entity : chunk.getEntities()) {
			if (entity instanceof Item) {
				Item item = (Item) entity;
				ItemStack stack = item.getItemStack();
				if (stack != null) {
					if (isHackedItem(stack)) {
						item.remove();
					}
				}
			}
		}
	}

	private static PlugCubeBuildersIn instance = null;
	private UUIDCache uuidCache = null;
	private NicknameCache nicknameCache = null;
	private PlayerNameHandler playerNameHandler = null;
	private boolean bountyIsEnabled = false;
	private boolean doNotKeepSpawnLoaded = false;
	private boolean externalPlayerIndicator = false;
	private boolean updatingPlayerCount = false;
	private String masterAddress = "127.0.0.1";
	private final HashMap<UUID, PlayerSession> playerSessions = new HashMap<>();

	public PlayerSession getSession(Player p) {
		PlayerSession playerSession = playerSessions.get(p.getUniqueId());
		if (playerSession == null && p.isOnline()) {
			playerSessions.put(p.getUniqueId(), playerSession = new PlayerSession(this, p));
		}
		return playerSession;
	}

	public PlayerSession getSession(UUID p) {
		return playerSessions.get(p);
	}

	public boolean bountyIsEnabled() {
		return bountyIsEnabled;
	}

	public UUIDCache getUUIDCache() {
		return uuidCache;
	}

	public NicknameCache getNicknameCache() {
		return nicknameCache;
	}

	public PlayerNameHandler getPlayerNameHandler() {
		return playerNameHandler;
	}

	@Deprecated
	public String getPlayer(UUID uuid) {
		return getUUIDCache().getNameFromUUID(uuid);
	}

	@Deprecated
	public UUID getUUID(String player) {
		return getUUIDCache().getUUIDFromName(player);
	}

	public static PlugCubeBuildersIn getInstance() {
		return instance;
	}
	private boolean spawnOnLogin = false;
	public boolean staffPrivilegeToggle = false;
	private final Properties loginTimes = new Properties();
	private boolean runningRunnable = false;
	private String[] spawnEggProtectionWorlds = null;

	public final Map<Player, Strike> lastStrikes = new HashMap<>();
	public final Map<Player, Strike> lastAttacks = new HashMap<>();
	public final Map<Player, ArrayList<Strike>> recentAttacks = new HashMap<>();
	public final Map<Player, Long> lastPvPCommand = new HashMap<>();

	public String autoCompleteName(String name) {
		if (name == null) {
			return null;
		}
		List<Player> list = getServer().matchPlayer(name);
		if (list.size() == 1) {
			return ((Player) list.get(0)).getName();
		}
		return name;
	}

	public static String getFinalArg(String[] args, int start) {
		StringBuilder bldr = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) {
				bldr.append(" ");
			}
			bldr.append(args[i]);
		}
		return bldr.toString();
	}

	private String serverName = null;
	private boolean pluginEnabled = false;

	public String getServerName() {
		return serverName;
	}

	@Override
	public void onLoad() {
		instance = this;
		serverName = System.getProperty("cubebuildersserver");
		loadSetupModules();
	}

	private boolean didSetupModulesLoad = false;
	private boolean didSetupModulesEnable = false;
	private final List<Module> modules = new ArrayList<>();

	private void loadSetupModules() {
		if (didSetupModulesLoad) {
			return;
		}
		didSetupModulesLoad = true;
		setupModule("ErrorReportModuleImpl");
	}

	private void enableSetupModules() {
		if (didSetupModulesEnable) {
			return;
		}
		didSetupModulesEnable = true;
		if (setupModule("Factions", "FactionsModuleImpl")) {
			teleportControlSystem = new FactionsTeleportControlSystem(this);
		}
		setupModule("Essentials", "EssentialsModuleImpl");
		setupModule("EssentialsSpawn", "EssentialsSpawnModuleImpl");
		setupModule("Vault", "VaultModuleImpl");
		setupModule("Prism", "PrismModuleImpl");
		setupModule("PermissionsEx", "PermissionsExModuleImpl");
		setupModule("PersonalSpace", "PersonalSpaceModuleImpl");
		setupModule("Skyblock", "SkyblockModuleImpl");
		setupModule("Statues", "hk.siggi.bukkit.plugcubebuildersin.replay.ActionReplayImpl");
		setupModule("MobLimiterModuleImpl");
		setupModule("PhantomRestricterModuleImpl");
		setupModule("HighscoresModuleImpl");
		setupModule("WarpPortals", "WarpPortalsModuleImpl");
		setupModule("hk.siggi.bukkit.plugcubebuildersin.punisher.PunisherModuleImpl");
		setupModule("MineWatchModuleImpl");
		setupModule("SpeedHaxModuleImpl");
		String sname = getServerName();
		if (sname != null && sname.equalsIgnoreCase("minigames")) {
			File f = new File(getDataFolder(), "classicMinigames.txt");
			if (!f.exists()) {
				setupModule("hk.siggi.bukkit.plugcubebuildersin.module.minigamehub.MinigameHubModuleImpl");
			}
		}
	}

	private boolean setupModule(String moduleName) {
		return setupModule(null, moduleName);
	}

	private boolean setupModule(String pluginName, String moduleName) {
		if (pluginName != null) {
			if (getServer().getPluginManager().getPlugin(pluginName) == null) {
				System.out.println("Skipping " + moduleName + " because plugin " + pluginName + " was not found.");
				return false;
			}
		}
		try {
			Class<Module> clazz = (Class<Module>) Class.forName((moduleName.contains(".") ? "" : "hk.siggi.bukkit.plugcubebuildersin.module.") + moduleName);
			Module newInstance = clazz.newInstance();
			newInstance.load(this);
			modules.add(newInstance);
			System.out.println("Loaded Module " + moduleName);
			return true;
		} catch (Exception | LinkageError e) {
		}
		System.out.println("Module " + moduleName + " was not loaded");
		return false;
	}

	public <M extends Module> M getModule(Class<M> clazz) {
		for (Module m : modules) {
			if (clazz.isAssignableFrom(m.getClass())) {
				return (M) m;
			}
		}
		return null;
	}

	public <M extends Module> List<M> getModules(Class<M> clazz) {
		List<M> ml = new ArrayList<>();
		for (Module m : modules) {
			if (clazz.isAssignableFrom(m.getClass())) {
				ml.add((M) m);
			}
		}
		return ml;
	}

	public BankModule getBankModule() {
		try {
			VaultModule vault = getModule(VaultModule.class);
			if (vault != null) {
				return vault;
			}
		} catch (Exception | LinkageError e) {
		}
		try {
			EssentialsModule ess = getModule(EssentialsModule.class);
			if (ess != null) {
				return ess;
			}
		} catch (Exception | LinkageError e) {
		}
		return null;
	}

	private SimpleCommandMap commandMap = null;

	private SimpleCommandMap getCommandMap() {
		if (commandMap == null) {
			try {
				Server serv = getServer();
				Class craftServerClass = serv.getClass();
				Method method = craftServerClass.getMethod("getCommandMap");
				SimpleCommandMap result = (SimpleCommandMap) method.invoke(serv);
				commandMap = result;
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}
		return commandMap;
	}

	@Override
	public void onEnable() {
		try {
			InactiveCommand nullCommand = new InactiveCommand(this);
			SimpleCommandMap map = getCommandMap();
			Collection<Command> commands = map.getCommands();
			for (Command command : commands) {
				if (command instanceof PluginCommand) {
					PluginCommand pluginCommand = (PluginCommand) command;
					if (pluginCommand.getPlugin() == this) {
						pluginCommand.setExecutor(nullCommand);
					}
				}
			}
		} catch (Exception e) {
		}
		enableSetupModules();
		for (Iterator<Module> it = modules.iterator(); it.hasNext();) {
			Module m = it.next();
			try {
				m.init();
				System.out.println("Initialized Module " + m.getClass().getName());
			} catch (Exception e) {
				reportProblem("Error initializing Module " + m.getClass().getName(), e);
				it.remove();
			}
		}
		externalPlayerIndicator = false;
		joinMessage = quitMessage = null;
		{
			File variableServerMove = new File(getDataFolder(), "variableServerAddress.txt");
			File targetFile = new File(getDataFolder(), "master.txt");
			if (variableServerMove.exists() && !targetFile.exists()) {
				variableServerMove.renameTo(targetFile);
			}
		}
		try {
			File file = new File(getDataFolder(), "master.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			masterAddress = reader.readLine();
		} catch (Exception e) {
		}
		if (masterAddress == null) {
			masterAddress = "127.0.0.1";
		}
		try {
			File file = new File(getDataFolder(), "joinMessages.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			joinMessage = reader.readLine();
			quitMessage = reader.readLine();
		} catch (Exception e) {
		}
		try {
			File file = new File(getDataFolder(), "blockeditems.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("=")) {
					blockedItems.add(line);
				}
			}
		} catch (Exception e) {
		}
		try {
			File file = new File(getDataFolder(), "lobbyServer.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			lobbyServer = reader.readLine();
			lobbyWarp = reader.readLine();
		} catch (Exception e) {
		}
		spawnOnLogin = false;
		try {
			File file = new File(getDataFolder(), "spawnOnLogin.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			spawnOnLogin = reader.readLine() != null;
		} catch (Exception e) {
		}
		bountyIsEnabled = false;
		try {
			File file = new File(getDataFolder(), "bountyIsEnabled.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			bountyIsEnabled = reader.readLine() != null;
		} catch (Exception e) {
		}
		doNotKeepSpawnLoaded = false;
		try {
			File file = new File(getDataFolder(), "doNotKeepSpawnLoaded.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			doNotKeepSpawnLoaded = reader.readLine() != null;
		} catch (Exception e) {
		}
		if (doNotKeepSpawnLoaded) {
			for (World w : getServer().getWorlds()) {
				w.setKeepSpawnInMemory(false);
			}
		}
		pvpLogPunishment = false;
		try {
			File file = new File(getDataFolder(), "pvpLogPunishment.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			pvpLogPunishment = reader.readLine() != null;
		} catch (Exception e) {
		}
		ArrayList<VariableUpdater> vu = new ArrayList<>();
		try {
			File file = new File(getDataFolder(), "variables.txt");
			if (file.exists()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line;
				while ((line = reader.readLine()) != null) {
					int x = line.indexOf("=");
					if (x == -1) {
						continue;
					}
					String signInfo = line.substring(0, x).trim();
					String variable = line.substring(x + 1).trim();
					try {
						String[] pieces = signInfo.split(",");
						String worldName = pieces[0];
						int blockX = Integer.parseInt(pieces[1]);
						int blockY = Integer.parseInt(pieces[2]);
						int blockZ = Integer.parseInt(pieces[3]);
						int lineNumber = Integer.parseInt(pieces[4]);
						vu.add(new VariableUpdater(getServer().getWorld(worldName).getBlockAt(blockX, blockY, blockZ), lineNumber, variable));
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			reportProblem("Couldn't load variables.txt", e);
		}
		variableUpdaters = vu.toArray(new VariableUpdater[vu.size()]);
		File dataFolder = getDataFolder();
		if (!dataFolder.isDirectory()) {
			if (dataFolder.exists()) {
				dataFolder.delete();
			}
			dataFolder.mkdirs();
		}
		uuidCache = new UUIDCache(new File(getDataFolder(), "UUIDs.txt"));
		nicknameCache = new NicknameCache();
		playerNameHandler = new PlayerNameHandler(this);
		loginTimes.clear();
		runningRunnable = false;
		PluginManager pm = getServer().getPluginManager();
		getCommand("help").setExecutor(new HelpCommand(this, serverName));
		DonateCommand dc = new DonateCommand(this);
		getCommand("donate").setExecutor(dc);
		getCommand("buy").setExecutor(dc);
		getCommand("vote").setExecutor(new VoteCommand(this));
		getCommand("bounty").setExecutor(new BountyCommand(this));
		getCommand("worldspawn").setExecutor(new WorldSpawnCommand(this));
		getCommand("factionreset").setExecutor(new FactionResetCommand(this));
		getCommand("snaptogrid").setExecutor(new SnapToGrid(this));
		getCommand("closetheend").setExecutor(new CloseTheEnd(this));
		getCommand("cc").setExecutor(new ClearChatCommand(this));
		getCommand("quit").setExecutor(new QuitCommand(this));
		getCommand("pvp").setExecutor(new PvPCommand(this, serverName));
		getCommand("tellallraw").setExecutor(new TellAllRawCommand(this));
		getCommand("signedit").setExecutor(new SignEditCommand(this));
		getCommand("speedup").setExecutor(new SpeedUpCommand(this));
		getCommand("music").setExecutor(new MusicCommand(this));
		getCommand("renderdistance").setExecutor(new RenderDistanceCommand(this));
		getCommand("st").setExecutor(new StaffToggleCommand(this));
		getCommand("vp").setExecutor(new VPCommand(this));
		getCommand("dbg").setExecutor(new DebugCommand(this));
		getCommand("ping").setExecutor(new PingCommand(this));
		getCommand("pong").setExecutor(new PongCommand(this));
		pm.registerEvents(this, this);
		getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		try {
			ArrayList worlds = new ArrayList();
			File f = new File(getDataFolder(), "spawneggworlds.txt");
			if (f.exists()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) {
						continue;
					}
					if (worlds.contains(line)) {
						continue;
					}
					worlds.add(line);
				}
			}
			spawnEggProtectionWorlds = (String[]) worlds.toArray(new String[worlds.size()]);
		} catch (Exception e) {
			reportProblem("Couldn't load spawneggworlds.txt", e);
			spawnEggProtectionWorlds = new String[0];
		}
		connectVariableServer();
		pluginEnabled = true;
		updatePlayers(getServer().getOnlinePlayers().size());
		File skinServerFile = new File(getDataFolder(), "skinserver.txt");
		if (skinServerFile.exists()) {
			skinServerHandler = new SkinServerHandler(this, skinServerFile);
			skinServerHandler.start();
		}
		File blockplayersFile = new File(getDataFolder(), "blockplayers.txt");
		if (blockplayersFile.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(blockplayersFile)));
				String line;
				while ((line = reader.readLine()) != null) {
					try {
						String[] parts = line.split(",");
						String world = parts[0];
						double x = Double.parseDouble(parts[1]);
						double y = Double.parseDouble(parts[2]);
						double z = Double.parseDouble(parts[3]);
						String folder = parts[4];
						blockPlayers.add(new BlockPlayer(this, world, x, y, z, folder));
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {
					}
				}
			}
		}
		File staffToggleFile = new File(getDataFolder(), "stafftoggle.txt");
		if (staffToggleFile.exists()) {
			staffPrivilegeToggle = true;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				PlugCubeBuildersIn.this.tick();
			}
		}.runTaskTimer(this, 1, 1);
		new BukkitRunnable() {
			@Override
			public void run() {
				perSecond();
			}
		}.runTaskTimer(this, 20L, 20L);
		new BukkitRunnable() {
			@Override
			public void run() {
				updateNamesCache();
			}
		}.runTaskTimerAsynchronously(this, 300L, 36000L);
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					SimpleCommandMap map = getCommandMap();
					Collection<Command> commands = map.getCommands();
					for (Command command : commands) {
						if (command instanceof PluginCommand) {
							PluginCommand pluginCommand = (PluginCommand) command;
							CommandThrowableCatchingProxy.setup(PlugCubeBuildersIn.this, pluginCommand);
						}
					}
				} catch (Exception e) {
				}
			}
		}.runTaskLater(this, 20L);
		permissionLoader = new PermissionLoader(new File(getDataFolder(), "permissions.txt"));
	}

	private PermissionLoader permissionLoader;

	public PermissionLoader getPermissionLoader() {
		return permissionLoader;
	}

	@Override
	public void onDisable() {
		for (Module m : modules) {
			try {
				m.kill();
				System.out.println("Killed Module " + m.getClass().getName());
			} catch (Exception e) {
				reportProblem("Couldn't kill Module " + m.getClass().getName(), e);
			}
		}
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerSession sess = getSession(p);
			sess.setStaffPerms(false); // revert to regular inventory if the server is shutting down while players still online
		}
		pluginEnabled = false;
		cubeTokens = getCubeTokens = chargeCubeTokens = giveCubeTokens = null;
		updatingPlayerCount = true;
		setVariable("players", "---------------");
		setVariable("status", "Server Offline");
		updatingPlayerCount = false;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000L);
					variableServer.stop();
				} catch (Exception e) {
				}
			}
		}).start();
		variableServer = null;
		commandMap = null;
	}

	// This method is run every 20 ticks. (There are 20 ticks in a second!)
	private void perSecond() {
		NMSUtil nms = NMSUtil.get();
		long now = System.currentTimeMillis();
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerSession session = getSession(p);
			ChatSetting chatSetting = nms.getChatSetting(p);
			if (session.nextWarnChatSetting < now
					|| session.lastChatSetting != chatSetting) {
				if (chatSetting == ChatSetting.ON) {
					// Good
					session.nextWarnChatSetting = 0L;
				} else if (chatSetting == ChatSetting.COMMANDS_ONLY) {
					session.nextWarnChatSetting = now + 15000L;
					p.sendMessage(ChatColor.RED + "ATTENTION!  IMPORTANT!");
					p.sendMessage(ChatColor.RED + "ATTENTION!  IMPORTANT!");
					p.sendMessage(ChatColor.WHITE + "The chat setting is currently set to Commands-Only!  Open the "
							+ ChatColor.GOLD + "Pause menu"
							+ ChatColor.WHITE + " then go to "
							+ ChatColor.GOLD + "Options"
							+ ChatColor.WHITE + " then "
							+ ChatColor.GOLD + "Chat Settings"
							+ ChatColor.WHITE + " and then set "
							+ ChatColor.GOLD + "Chat"
							+ ChatColor.WHITE + " to "
							+ ChatColor.GOLD + "Shown"
							+ ChatColor.WHITE + ".");
					p.sendMessage(ChatColor.WHITE + "This might explain why you get " + ChatColor.RED + "Cannot send chat message" + ChatColor.WHITE + " on every server.  You're not muted, you just have the chat disabled!");
					p.sendMessage(ChatColor.RED + "ATTENTION!  IMPORTANT!");
					p.sendMessage(ChatColor.RED + "ATTENTION!  IMPORTANT!");
				} else if (chatSetting == ChatSetting.OFF) {
					if (session.lastChatSetting == ChatSetting.COMMANDS_ONLY
							|| session.lastChatSetting == ChatSetting.ON) {
						session.nextWarnChatSetting = now + 15000L;
						// give 15 seconds grace period in case the player
						// is currently changing their setting.
					} else {
						p.kickPlayer("CubeBuilders requires Multiplayer Chat to be enabled. Go to Options > Chat Settings, and then set Chat to Shown.");
					}
				}
				session.lastChatSetting = chatSetting;
			}
		}
		updatePlayerVisibility();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getGameMode() == GameMode.CREATIVE) {
				// stops phantoms spawning for creative mode
				p.setStatistic(Statistic.TIME_SINCE_REST, 0);
			}
		}
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerSession sess = getSession(p);
			ActionReplay ar = getModule(ActionReplay.class);
			boolean replaying = ar == null ? false : ar.isPlayerWatchingReplay(p);
			boolean vanished = isVanished(p);
			boolean canUseStaffToggle = sess.canUseStaffToggle();
			boolean staffPerms = sess.getStaffPerms();
			if (!staffPerms && staffPrivilegeToggle && vanished && canUseStaffToggle) {
				sess.setStaffPerms(true);
				p.sendMessage(ChatColor.GREEN + "Switching to Staff Mode automatically since you're vanished.");
			}
			if (staffPerms) {
				// stops phantoms spawning for people in staff mode
				p.setStatistic(Statistic.TIME_SINCE_REST, 0);
				addHotbar(p, "staffmode", "Staff Mode");
			} else {
				removeHotbar(p, "staffmode");
			}
		}
	}

	private void updateNamesCache() {
		List<UUID> uuidL = new LinkedList<>();
		List<String> nameL = new LinkedList<>();
		List<String> nickL = new LinkedList<>();
		try {
			URL url = new URL("http://" + masterAddress + ":20857/api/allnames");
			URLConnection c = url.openConnection();
			HttpURLConnection connection = (HttpURLConnection) c;
			JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
			if (reader.peek() == JsonToken.BEGIN_ARRAY) {
				reader.beginArray();
				while (reader.peek() != JsonToken.END_ARRAY) {
					if (reader.peek() != JsonToken.BEGIN_OBJECT) {
						reader.skipValue();
						continue;
					}
					reader.beginObject();
					UUID uuid = null;
					String name = null;
					String nick = null;
					while (reader.peek() != JsonToken.END_OBJECT) {
						if (reader.peek() != JsonToken.NAME) {
							reader.skipValue();
							continue;
						}
						String key = reader.nextName();
						if (reader.peek() != JsonToken.STRING) {
							reader.skipValue();
							continue;
						}
						if (key.equals("uuid")) {
							try {
								uuid = UUID.fromString(reader.nextString());
							} catch (Exception e) {
							}
						}
						if (key.equals("name")) {
							name = reader.nextString();
						}
						if (key.equals("nick")) {
							nick = reader.nextString();
						}
					}
					reader.endObject();
					if (uuid != null && name != null) {
						uuidL.add(uuid);
						nameL.add(name);
						nickL.add(nick);
					}
				}
				reader.endArray();
			} else {
				return;
			}
		} catch (Exception e) {
			return;
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				UUIDCache uc = getUUIDCache();
				try {
					uc.preventSaving();
					NicknameCache nc = getNicknameCache();
					Iterator<UUID> uuidIt = uuidL.iterator();
					Iterator<String> nameIt = nameL.iterator();
					Iterator<String> nickIt = nickL.iterator();
					while (uuidIt.hasNext()) {
						UUID uuid = uuidIt.next();
						String name = nameIt.next();
						String nick = nickIt.next();
						uc.storeToCache(name, uuid);
						nc.setNicknameCache(uuid, nick);
					}
				} finally {
					uc.resumeSaving();
				}
			}
		}.runTask(this);
	}

	private final List<VariableListener> variableListeners = new LinkedList<>();

	public void addVariableListener(VariableListener listener) {
		if (!variableListeners.contains(listener)) {
			variableListeners.add(listener);
		}
	}

	public void removeVariableListener(VariableListener listener) {
		if (!variableListeners.contains(listener)) {
			variableListeners.add(listener);
		}
	}

	public VariableListener[] getVariableListeners() {
		return variableListeners.toArray(new VariableListener[variableListeners.size()]);
	}

	private VariableServerConnection variableServer = null;

	private void connectVariableServer() {
		variableServer = new VariableServerConnection(masterAddress);
		variableServer.addListener(this);
		variableServer.setName(getServerName());
	}

	public VariableServerConnection getVariableServerConnection() {
		return variableServer;
	}

	@Override
	public void disconnectedVariableServer(VariableServerConnection c) {
		if (c != variableServer) {
			return;
		}
		variableServer = null;
		c.stop();
		final PlugCubeBuildersIn pcbi = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000L);
				} catch (Exception e) {
				}
				variableServer = new VariableServerConnection(masterAddress);
				variableServer.addListener(pcbi);
			}
		}).start();
	}
	private SimpleDateFormat simpleDateFormat = null;

	public SimpleDateFormat getDateFormat() {
		if (simpleDateFormat == null) {
			try {
				simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
			} catch (Exception e) {
			}
		}
		return simpleDateFormat;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void mobSpawned(CreatureSpawnEvent cse) {
		CreatureSpawnEvent.SpawnReason reason = cse.getSpawnReason();
		if (reason == CreatureSpawnEvent.SpawnReason.CUSTOM
				|| reason == CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE
				|| reason == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT
				|| reason == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
				|| reason == CreatureSpawnEvent.SpawnReason.BREEDING
				|| reason == CreatureSpawnEvent.SpawnReason.LIGHTNING) {
			return;
		}
		Entity entity = cse.getEntity();
		World world = entity.getWorld();
		boolean spawnEggWorld = false;
		try {
			for (String spawnEggProtectionWorld : spawnEggProtectionWorlds) {
				if (world.getName().equals(spawnEggProtectionWorld)) {
					spawnEggWorld = true;
					break;
				}
			}
		} catch (Exception e) {
		}
		if (spawnEggWorld) {
			if (reason != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
				cse.setCancelled(true);
				return;
			}
			Location loc = cse.getLocation();
			List list = world.getLivingEntities();
			LivingEntity[] entities = (LivingEntity[]) list.toArray(new LivingEntity[0]);
			int entityCount = 0;
			int totalEntityCount = 0;
			double score = 0.0;
			for (LivingEntity ent : entities) {
				double distanceSquared = ent.getLocation().distanceSquared(loc);
				score += 1.0 / Math.max(1.0, distanceSquared);
				if (distanceSquared < (400.0 /* 20 squared */)) {
					entityCount += 1;
				}
				totalEntityCount += 1;
			}
			if (score >= 3.0) {
				cse.setCancelled(true);
			} else if (entityCount >= 15) {
				cse.setCancelled(true);
			} else if (totalEntityCount >= 5000) {
				cse.setCancelled(true);
			}
		}
	}

	public final Map<Player, Integer> suicideCost = new HashMap<>();
	public final Map<Player, Long> lastTypedSuicide = new HashMap<>();
	public final Map<Player, Integer> suicideCount = new HashMap<>();
	public final Map<Player, Long> lastSuicide = new HashMap<>();
	public final Map<Player, Long> lastDeath = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerCommand(PlayerCommandPreprocessEvent event) {
		String cmd = event.getMessage();
		if (cmd.startsWith("/")) {
			cmd = cmd.substring(1);
		}
		String split[] = cmd.split(" ");
		Player p = event.getPlayer();
		Long myLastTypedSuicideLong = lastTypedSuicide.get(p);
		Long myLastSuicideLong = lastSuicide.get(p);
		Long myLastDeathLong = lastDeath.get(p);
		Integer mySuicideCountInteger = suicideCount.get(p);
		long myLastTypedSuicide = myLastTypedSuicideLong == null ? 0L : myLastTypedSuicideLong;
		long myLastSuicide = myLastSuicideLong == null ? 0L : myLastSuicideLong;
		long myLastDeath = myLastDeathLong == null ? 0L : myLastDeathLong;
		int mySuicideCount = mySuicideCountInteger == null ? 0 : mySuicideCountInteger;
		long now = System.currentTimeMillis();
		long timeSinceLastTypedSuicide = now - myLastTypedSuicide;
		long timeSinceLastSuicide = now - myLastSuicide;
		long timeSinceLastDeath = now - myLastDeath;
		if (timeSinceLastSuicide > 600000L) {
			mySuicideCount = 0;
		}
		if (split[0].equalsIgnoreCase("suicide") || split[0].equalsIgnoreCase("esuicide")) {
			if (serverName.contains("factions") || serverName.contains("survival") || serverName.contains("skyblock")) {
				BankModule money = getBankModule();
				double myCurrentBalance = money.getBalance(p);
				if (timeSinceLastTypedSuicide > 15000L) {
					int costToSuicide = 50;
					if ((timeSinceLastDeath < 120000L && timeSinceLastSuicide > 300000L)
							|| (timeSinceLastDeath < 15000L && timeSinceLastSuicide > 60000L)) {
						costToSuicide = 0;
					} else if (myCurrentBalance >= 20000000.0) { // 20M
						costToSuicide = 100000; // 100K
					} else if (myCurrentBalance >= 10000000.0) { // 10M
						costToSuicide = 50000; // 50K
					} else if (myCurrentBalance >= 1000000.0) { // 1M
						costToSuicide = 10000; // 10K
					} else if (myCurrentBalance >= 500000.0) { // 500K
						costToSuicide = 5000; // 5K
					} else if (myCurrentBalance >= 250000.0) { // 250K
						costToSuicide = 3000; // 3K
					} else if (myCurrentBalance >= 100000.0) { // 100K
						costToSuicide = 2000; // 2K
					} else if (myCurrentBalance >= 25000.0) { // 25K
						costToSuicide = 1000; // 1K
					} else if (myCurrentBalance >= 15000.0) { // 15K
						costToSuicide = 800;
					} else if (myCurrentBalance >= 10000.0) { // 10K
						costToSuicide = 500;
					} else if (myCurrentBalance >= 5000.0) { // 5K
						costToSuicide = 250;
					} else if (myCurrentBalance >= 1000.0) { // 1K
						costToSuicide = 100;
					}
					costToSuicide *= Math.pow(2, mySuicideCount);
					lastTypedSuicide.put(p, now);
					suicideCost.put(p, costToSuicide);
					p.sendMessage(ChatColor.RED + "Are you sure you want to kill yourself?");
					p.sendMessage(ChatColor.RED + "Cost of killing yourself: " + ChatColor.GOLD + (costToSuicide == 0 ? "FREE" : ("$" + costToSuicide)));
					p.sendMessage(ChatColor.RED + "If you're sure, type " + ChatColor.GOLD + "/suicide" + ChatColor.RED + " again.");
					event.setCancelled(true);
				} else {
					lastTypedSuicide.remove(p);
					suicideCount.remove(p);
					int costToSuicide = suicideCost.get(p);
					if (costToSuicide == 0) {
					} else if (myCurrentBalance < costToSuicide || !money.chargeMoney(p, costToSuicide)) {
						p.sendMessage(ChatColor.RED + "You don't have enough money to kill yourself.");
						event.setCancelled(true);
					} else {
						mySuicideCount += 1;
						suicideCount.put(p, mySuicideCount);
						lastSuicide.put(p, now);
					}
				}
			} else if (serverName.contains("ffapvp")) {
			} else if (serverName.contains("hub")) {
			} else {
				p.sendMessage("This command is not available here.");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void dropItem(PlayerDropItemEvent event) {
		ItemStack stack = event.getItemDrop().getItemStack();
		if (isUndroppable(stack)) {
			event.getItemDrop().remove();
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerDied(PlayerDeathEvent event) {
		List<ItemStack> dropsList = event.getDrops();
		ItemStack[] drops = dropsList.toArray(new ItemStack[dropsList.size()]);
		for (ItemStack drop : drops) {
			if (isUndroppable(drop)) {
				dropsList.remove(drop);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerInteractInventory(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (player == null) {
			return;
		}
		resetAFKTimer(player);
		if (player.hasPermission("hk.siggi.plugcubebuildersin.spawneditems")) {
			return;
		}
		PlayerInventory inventory = player.getInventory();
		boolean removedAnItem = false;
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack == null) {
				continue;
			}
			if (isUndroppable(stack)) {
				removedAnItem = true;
				inventory.remove(stack);
			}
		}
		if (removedAnItem) {
			player.updateInventory();
		}
	}

	public boolean isUndroppable(ItemStack stack) {
		if (isSpawnedItem(stack) || isIllegalItem(stack)) {
			return true;
		}
		try {
			NBTCompound rootTag = NBTTool.getUtil().getTag(stack);
			NBTCompound compound = rootTag.getCompound("CubeBuilders");
			boolean isUndroppable = compound.getInt("undroppable") == 1;
			return isUndroppable;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isSpawnedItem(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		List<String> loreList = meta.getLore();
		if (loreList == null) {
			return false;
		}
		String[] lore = (String[]) loreList.toArray(new String[loreList.size()]);
		for (String lore1 : lore) {
			if (lore1 == null) {
				continue;
			}
			if (lore1.equalsIgnoreCase("Spawned Item") || lore1.equalsIgnoreCase("Modified Item")) {
				return true;
			}
		}
		return false;
	}

	public boolean isIllegalItem(ItemStack stack) {
		ItemMeta meta = stack.getItemMeta();
		List<String> loreList = meta.getLore();
		if (loreList == null) {
			return false;
		}
		String[] lore = (String[]) loreList.toArray(new String[loreList.size()]);
		for (String loreLine : lore) {
			if (loreLine == null) {
				continue;
			}
			if (loreLine.startsWith("CubeTokens: ")) {
				return true;
			}
		}
		return false;
	}

	private Material commandBlock = null;

	public Material getCommandBlockMaterial() {
		if (commandBlock == null) {
			try {
				commandBlock = Material.COMMAND_BLOCK;
			} catch (Throwable t) {
				commandBlock = Material.getMaterial("COMMAND");
			}
		}
		return commandBlock;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void placeBlock(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		ItemStack inHand = event.getItemInHand();
		if (inHand.getType() == getCommandBlockMaterial()) {
			ItemMeta meta = inHand.getItemMeta();
			List<String> loreList = meta.getLore();
			String[] lore = (String[]) loreList.toArray(new String[loreList.size()]);
			String command = null;
			String displayName = meta.getDisplayName();
			for (String loreLine : lore) {
				if (loreLine.startsWith("Command: ")) {
					command = loreLine.substring(loreLine.indexOf(" ") + 1);
				}
			}
			Block block = event.getBlockPlaced();
			block.setType(getCommandBlockMaterial());
			CommandBlock commandBlock = (CommandBlock) block.getState();
			if (displayName != null) {
				commandBlock.setName(displayName);
			}
			if (command != null) {
				commandBlock.setCommand(command);
			}
			commandBlock.update();
		}
	}
	private String joinMessage = null;
	private String quitMessage = null;

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerJoined_early(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		for (Player pp : Bukkit.getOnlinePlayers()) {
			if (pp == p) {
				continue;
			}
			//hide now, we'll unhide later!
			p.hidePlayer(this, pp);
			pp.hidePlayer(this, p);
		}
		boolean goToSpawn = spawnOnLogin;
		String lobbyWarpRequest = getLobbyWarpRequest(p.getUniqueId());
		if (lobbyWarpRequest != null) {
			if (teleportPlayerToWarp(p, lobbyWarpRequest)) {
				goToSpawn = false;
			}
		}
		if (goToSpawn) {
			spawn(p);
		}
	}

	private boolean spectatorsCanSeeOtherSpectators = false;

	public boolean spectatorsCanSeeOtherSpectators() {
		return spectatorsCanSeeOtherSpectators;
	}

	public void setSpectatorsCanSeeOtherSpectators(boolean spectatorsCanSeeOtherSpectators) {
		this.spectatorsCanSeeOtherSpectators = spectatorsCanSeeOtherSpectators;
	}

	private final Set<PlayerVanisher> vanishers = new HashSet<>();

	public void addPlayerVanisher(PlayerVanisher vanisher) {
		vanishers.add(vanisher);
	}

	public void removePlayerVanisher(PlayerVanisher vanisher) {
		vanishers.remove(vanisher);
	}

	private void updatePlayerVisibility() {
		if (skinServerHandler != null) {
			return;
		}
		List<Player> players = new LinkedList<>();
		players.addAll(getServer().getOnlinePlayers());
		EssentialsModule essentials = getModule(EssentialsModule.class);
		int s = players.size();
		for (int a = 0; a < s; a++) {
			for (int b = a + 1; b < s; b++) {
				Player p1 = players.get(a);
				Player p2 = players.get(b);
				boolean p1Vanish = isVanished(p1);
				boolean p2Vanish = isVanished(p2);
				boolean p1CanSeeVanished = p1.hasPermission("hk.siggi.plugcubebuildersin.seehiddenplayers");
				boolean p2CanSeeVanished = p2.hasPermission("hk.siggi.plugcubebuildersin.seehiddenplayers");
				boolean showP1toP2 = true;
				boolean showP2toP1 = true;
				PlayerSession p1Session = getSession(p1);
				PlayerSession p2Session = getSession(p2);
				if (!p1Session.canHandleVanish() || !p2Session.canHandleVanish()) {
					continue;
				}
				for (PlayerVanisher vanisher : vanishers) {
					try {
						if (!vanisher.canSee(p1, p2)) {
							showP2toP1 = false;
						}
					} catch (Exception e) {
					}
					try {
						if (!vanisher.canSee(p2, p1)) {
							showP1toP2 = false;
						}
					} catch (Exception e) {
					}
				}
				if (p1Vanish && !p2CanSeeVanished) {
					showP1toP2 = false;
				}
				if (p2Vanish && !p1CanSeeVanished) {
					showP2toP1 = false;
				}
				if (showP2toP1 || p1Session.forceShowAll) {
					p1.showPlayer(this, p2);
					if (essentials != null) {
						essentials.makePlayerSeePlayer(p1, p2);
					}
				} else {
					p1.hidePlayer(this, p2);
				}
				if (showP1toP2 || p2Session.forceShowAll) {
					p2.showPlayer(this, p1);
					if (essentials != null) {
						essentials.makePlayerSeePlayer(p2, p1);
					}
				} else {
					p2.hidePlayer(this, p1);
				}
			}
		}
	}

	public Location getSpawn(Player p) {
		EssentialsSpawnModule esm = getModule(EssentialsSpawnModule.class);
		if (esm != null) {
			Location loc = esm.getSpawn(p);
			if (loc != null) {
				return loc;
			}
		}
		return getServer().getWorlds().get(0).getSpawnLocation();
	}

	public void spawn(Player p) {
		p.teleport(getSpawn(p));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerJoined(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		p.setOp(false);
		PlayerSession session;
		playerSessions.put(p.getUniqueId(), session = new PlayerSession(this, p));
		session.loginTime = System.currentTimeMillis();
		if (!runningRunnable) {
			runningRunnable = true;
			new BukkitRunnable() {
				@Override
				public void run() {
					tick += 1;
					PlugCubeBuildersIn.this.tickMusic();
				}
			}.runTaskTimer(this, 1, 1);
		}
		boolean vanished = isVanished(p);
		event.setJoinMessage(null);
		int playerCount = getServer().getOnlinePlayers().size();
		updatePlayers(playerCount);
		updateVanishedStatus(p, vanished);
		cleanInventory(p, p.getGameMode() == GameMode.CREATIVE);
	}

	public void changeGameModeEvent(PlayerGameModeChangeEvent event) {
		if (event.getNewGameMode() == GameMode.CREATIVE) {
			cleanInventory(event.getPlayer(), true);
		} else {
			cleanInventory(event.getPlayer(), false);
		}
	}

	public void cleanInventory(Player p) {
		cleanInventory(p, p.getGameMode() == GameMode.CREATIVE);
	}

	public void cleanInventory(Player p, boolean creative) {
		PlayerInventory inventory = p.getInventory();
		if (inventory == null) {
			return;
		}
		ItemStack[] contents = inventory.getContents();
		boolean modifiedInventory = false;
		for (int i = 0; i < contents.length; i++) {
			ItemStack inventoryItem = contents[i];
			if (inventoryItem == null) {
				continue;
			}
			Material type = inventoryItem.getType();
			if (type == null) {
				continue;
			}
			boolean remove = false;
			String inventoryItemName = type.toString();
			if (creative) {
				for (String item : blockedItems) {
					int x = item.indexOf("=");
					if (x == -1) {
						continue;
					}
					String itemBlocked = item.substring(0, x).trim();
					String message = item.substring(x + 1).trim();
					if (inventoryItemName != null) {
						if (inventoryItemName.equals(itemBlocked)) {
							remove = true;
						}
					}
					if (remove) {
						break;
					}
				}
			}
			if (isHackedItem(inventoryItem)) {
				remove = true;
			}
			if (remove) {
				modifiedInventory = true;
				inventory.clear(i);
			}
		}
		Inventory enderchest = p.getEnderChest();
		ItemStack[] enderchestContents = enderchest.getContents();
		for (int i = 0; i < enderchestContents.length; i++) {
			ItemStack inventoryItem = enderchestContents[i];
			if (inventoryItem == null) {
				continue;
			}
			Material type = inventoryItem.getType();
			if (type == null) {
				continue;
			}
			boolean remove = false;
			String inventoryItemName = type.toString();
			if (creative) {
				for (String item : blockedItems) {
					int x = item.indexOf("=");
					if (x == -1) {
						continue;
					}
					String itemBlocked = item.substring(0, x).trim();
					String message = item.substring(x + 1).trim();
					if (inventoryItemName != null) {
						if (inventoryItemName.equals(itemBlocked)) {
							remove = true;
						}
					}
					if (remove) {
						break;
					}
				}
			}
			if (isHackedItem(inventoryItem)) {
				remove = true;
			}
			if (remove) {
				modifiedInventory = true;
				enderchest.clear(i);
			}
		}
		if (modifiedInventory) {
			p.sendMessage(ChatColor.RED + "Items from your inventory that are prohibited have been deleted.");
		}
	}

	private boolean pvpLogPunishment = false;

	private boolean isItem(ItemStack item) {
		return item != null && item.getType() != Material.AIR;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerQuit(PlayerQuitEvent event) {
		long now = System.currentTimeMillis();
		Player p = event.getPlayer();
		getServer().dispatchCommand(getServer().getConsoleSender(), "pex user " + p.getName() + " delete");
		PlayerSession session = getSession(p);
		session.logout();
		if (pvpLogPunishment) {
			try {
				Strike lastStrike = lastStrikes.get(p);
				if (lastStrike != null) {
					Location loc = p.getLocation();
					World w = loc.getWorld();
					if (lastStrike.time > now - 5000L) {
						PlayerInventory inventory = p.getInventory();
						int size = inventory.getSize();
						for (int i = 0; i < size; i++) {
							ItemStack item = inventory.getItem(i);
							if (isItem(item)) {
								inventory.setItem(i, new ItemStack(Material.AIR));
								w.dropItemNaturally(loc, item);
							}
						}
						ItemStack helmet = inventory.getHelmet();
						if (isItem(helmet)) {
							inventory.setHelmet(new ItemStack(Material.AIR));
							w.dropItemNaturally(loc, helmet);
						}
						ItemStack chestplate = inventory.getChestplate();
						if (isItem(chestplate)) {
							inventory.setChestplate(new ItemStack(Material.AIR));
							w.dropItemNaturally(loc, chestplate);
						}
						ItemStack leggings = inventory.getLeggings();
						if (isItem(leggings)) {
							inventory.setLeggings(new ItemStack(Material.AIR));
							w.dropItemNaturally(loc, leggings);
						}
						ItemStack boots = inventory.getBoots();
						if (isItem(boots)) {
							inventory.setBoots(new ItemStack(Material.AIR));
							w.dropItemNaturally(loc, boots);
						}
						ItemStack offhand = inventory.getItemInOffHand();
						if (isItem(offhand)) {
							inventory.setItemInOffHand(new ItemStack(Material.AIR));
							w.dropItemNaturally(loc, offhand);
						}
						BankModule bankModule = getBankModule();
						if (bankModule != null) {
							double balance = bankModule.getBalance(p);
							double charge = Math.min(balance, 2000.0);
							if (charge > 0.0) {
								bankModule.chargeMoney(p, charge);
							}
						}
						FactionsModule facs = getModule(FactionsModule.class);
						if (facs != null) {
							facs.alterPower(p, -5.0);
						}
					}
				}
			} catch (Exception e) {
				reportProblem("Could not punish player for PvP logging", e);
			}
		}
		lastAttacks.remove(p);
		lastStrikes.remove(p);
		recentAttacks.remove(p);
		lastPvPCommand.remove(p);
		lastSpawnerMined.remove(p);
		lastTypedSuicide.remove(p);
		suicideCount.remove(p);
		lastSuicide.remove(p);
		lastDeath.remove(p);
		suicideCost.remove(p);
		event.setQuitMessage(null);
		int playerCount = getServer().getOnlinePlayers().size() - 1;
		updatePlayers(playerCount);
		playerSessions.remove(p.getUniqueId());
	}

	private void tick() {
		for (Module m : modules) {
			try {
				m.tick();
			} catch (Exception | LinkageError e) {
			}
		}
	}

	public String getOwner(Skull skull) {
		return skull.getOwner();
	}

	private Class skullClass = null;
	private Field profileFieldInSkullClass = null;

	public void setOwner(Skull skull, String name) {
		setGameProfile(skull, getGameProfile(new GameProfile(null, name)));
	}

	public void setGameProfile(Skull skull, GameProfile profile) {
		try {
			if (skullClass == null || profileFieldInSkullClass == null) {
				skullClass = skull.getClass();
				profileFieldInSkullClass = skullClass.getDeclaredField("profile");
				profileFieldInSkullClass.setAccessible(true);
			}
			profileFieldInSkullClass.set(skull, profile);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			reportProblem("Could not set profile in Skull", e);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerDeath(final PlayerDeathEvent event) {
		Player p = (Player) event.getEntity();
		String message = event.getDeathMessage();
		if (message == null) {
			return;
		}
		if (message.endsWith("fell from a high place") || message.contains("fell off")) {
			event.setDeathMessage(message.substring(0, message.indexOf(" ")) + " went SPLAT!");
		} else if (message.toLowerCase().contains("was blown up by creeper")) {
			event.setDeathMessage(message.substring(0, message.indexOf(" ")) + " got creeped!");
		} else if (message.toLowerCase().contains("fell out of the world")) {
			event.setDeathMessage(message.substring(0, message.indexOf(" ")) + " went on a journey to the centre of the Earth!");
		}
		lastDeath.put((Player) event.getEntity(), System.currentTimeMillis());
	}

	public void antiCheatAutoBan(Player p, String code) {
		if (true) {
			return; // TODO: Do something else instead of just skip this code!
		}
		PlayerSession session = getSession(p);
		if (session.didAutoBan) {
			return;
		}
		session.didAutoBan = true;
		ban(p, anticheatDetector, "modded_client", "Modified cheat client detected (" + code + ")", -1L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerMoved(PlayerMoveEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		if (from.getPitch() != to.getPitch() || from.getYaw() != to.getYaw()) {
			resetAFKTimer(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerHackMoving(PlayerMoveEvent event) {
		if (event instanceof PlayerTeleportEvent) {
			return;
		}
		Player p = event.getPlayer();
		Location to = event.getTo();
		float pitch = to.getPitch();
		if (pitch > 91.0f || pitch < -91.0f) {
			event.setCancelled(true);
			antiCheatAutoBan(p, "hack move");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void chorusFruit(PlayerTeleportEvent event) {
		if (serverName.startsWith("factions")) {
			if (event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerWentAboveTheNether(PlayerMoveEvent event) {
		// srsly, stop glitching!
		Location to = event.getTo();
		Location from = event.getFrom();
		if (!isAboveTheNether(to) || isAboveTheNether(from)) {
			return;
		}
		final Player p = event.getPlayer();
		p.setHealth(1.0);
		p.damage(1000.0);
		// we have to do it again in case player is holding a totem of undying
		new BukkitRunnable() {
			@Override
			public void run() {
				p.setHealth(1.0);
				p.damage(1000.0);
			}
		}.runTaskLater(this, 1);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerTeleportedAboveTheNether(PlayerTeleportEvent event) {
		playerWentAboveTheNether(event);
	}

	public boolean isAboveTheNether(Location location) {
		if (location.getWorld().getEnvironment() == World.Environment.NETHER) {
			if (location.getBlockY() >= 128) {
				return true;
			}
		}
		return false;
	}

	public void resetAFKTimer(Player p) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("ResetAFKTimer");

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerJoiningSetUUID(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		getUUIDCache().storeToCache(p.getName(), uuid);
	}

	private Material enderPortal = null;

	public Material getEndPortalMaterial() {
		if (enderPortal == null) {
			try {
				enderPortal = Material.END_PORTAL;
			} catch (Throwable e) {
				enderPortal = Material.getMaterial("ENDER_PORTAL");
			}
		}
		return enderPortal;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void moveIntoEndPortal(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!player.getWorld().getName().equalsIgnoreCase("theend")) {
			return;
		}
		Location from = event.getFrom();
		Location to = event.getTo();
		Block fromBlock = from.getBlock();
		Block toBlock = to.getBlock();
		if (!fromBlock.equals(toBlock)) {
			if (toBlock.getType() == getEndPortalMaterial()) {
				player.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation().add(0.5, 0.5, 0.5));
			}
		}
	}

	private Material spawnerMaterial = null;

	public Material getSpawnerMaterial() {
		if (spawnerMaterial == null) {
			try {
				spawnerMaterial = Material.SPAWNER;
			} catch (Throwable e) {
				spawnerMaterial = Material.getMaterial("MOB_SPAWNER");
			}
		}
		return spawnerMaterial;
	}

	private final Map<Player, Block> lastSpawnerMined = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerSilkTouchSpawner(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player p = event.getPlayer();
		if (p == null) {
			return;
		}
		Block block = event.getBlock();
		Material blockType = block.getType();
		if (blockType == getSpawnerMaterial()) {
			ItemStack itemInHand = p.getItemInHand();
			if (itemInHand == null) {
				lastSpawnerMined.remove(p);
				return;
			}
			if (itemInHand.containsEnchantment(Enchantment.SILK_TOUCH)) {
				if (block.equals((Block) lastSpawnerMined.remove(p))) {
					return; // player confirms to not mine the spawner
				}
				CreatureSpawner spawner = (CreatureSpawner) block.getState();
				EntityType type = spawner.getSpawnedType();
				String name = null;
				if (type == EntityType.BAT) {
					name = "Bat";
				} else if (type == EntityType.BLAZE) {
					name = "Blaze";
				} else if (type == EntityType.CAVE_SPIDER) {
					name = "Cave Spider";
				} else if (type == EntityType.CHICKEN) {
					name = "Chicken";
				} else if (type == EntityType.COW) {
					name = "Cow";
				} else if (type == EntityType.CREEPER) {
					name = "Creeper";
				} else if (type == EntityType.ENDER_DRAGON) {
					name = "Ender Dragon";
				} else if (type == EntityType.ENDERMAN) {
					name = "Enderman";
				} else if (type == EntityType.GHAST) {
					name = "Ghast";
				} else if (type == EntityType.GIANT) {
					name = "Giant";
				} else if (type == EntityType.HORSE) {
					name = "Horse";
				} else if (type == EntityType.IRON_GOLEM) {
					name = "Iron Golem";
				} else if (type == EntityType.MAGMA_CUBE) {
					name = "Magma Cube";
				} else if (type == EntityType.MUSHROOM_COW) {
					name = "Mushroom Cow";
				} else if (type == EntityType.OCELOT) {
					name = "Ocelot";
				} else if (type == EntityType.PIG) {
					name = "Pig";
				} else if (type == EntityType.ZOMBIFIED_PIGLIN) {
					name = "Zombified Piglin";
				} else if (type == EntityType.SHEEP) {
					name = "Sheep";
				} else if (type == EntityType.SKELETON) {
					name = "Skeleton";
				} else if (type == EntityType.SLIME) {
					name = "Slime";
				} else if (type == EntityType.SNOWMAN) {
					name = "Snowman";
				} else if (type == EntityType.SPIDER) {
					name = "Spider";
				} else if (type == EntityType.SQUID) {
					name = "Squid";
				} else if (type == EntityType.VILLAGER) {
					name = "Villager";
				} else if (type == EntityType.WITCH) {
					name = "Witch";
				} else if (type == EntityType.WITHER) {
					name = "Wither";
				} else if (type == EntityType.WOLF) {
					name = "Wolf";
				} else if (type == EntityType.ZOMBIE) {
					name = "Zombie";
				}
				boolean canMine = true;
				String cannotMineMessage = "This spawner cannot be silk touched. Mine it again to destroy it.";
				if (name == null) {
					canMine = false;
				}
				if (type == EntityType.BLAZE) {
					canMine = false;
					cannotMineMessage = "Blaze spawners cannot be silk touched. Mine it again to destroy it.";
				}
				if (type == EntityType.GHAST) {
					canMine = false;
					cannotMineMessage = "Ghast spawners cannot be silk touched. Mine it again to destroy it.";
				}
				if (type == EntityType.GIANT) {
					canMine = false;
					cannotMineMessage = "Giant spawners cannot be silk touched. Mine it again to destroy it.";
				}
				if (type == EntityType.VILLAGER) {
					canMine = false;
					cannotMineMessage = "Villager spawners cannot be silk touched. Mine it again to destroy it.";
				}
				if (type == EntityType.WITHER) {
					canMine = false;
					cannotMineMessage = "Wither spawners cannot be silk touched. Mine it again to destroy it.";
				}
				if (canMine) {
					if (!p.hasPermission("hk.siggi.plugcubebuildersin.silktouchspawner")) {
						p.sendMessage(ChatColor.RED + "You don't have permission to silk touch spawners. Mine the spawner again to destroy it.");
						p.sendMessage(ChatColor.RED + "For information on silk touching spawners, visit the CubeBuilders Store by typing /donate");
						lastSpawnerMined.put(p, block);
						event.setCancelled(true);
					} else {
						event.setExpToDrop(0);
						ItemStack stack = new ItemStack(getSpawnerMaterial(), 1);
						ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName(ChatColor.GOLD + name + " Spawner");
						stack.setItemMeta(meta);
						block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), stack);
					}
				} else {
					lastSpawnerMined.put(p, block);
					event.setCancelled(true);
					p.sendMessage(ChatColor.RED + cannotMineMessage);
					return;
				}
			} else {
				lastSpawnerMined.remove(p);
			}
		} else {
			lastSpawnerMined.remove(p);
		}
	}
	boolean canOpenTheEnd = false;

	public void startTheEndTimer() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(getDataFolder(), "canopentheend.txt"))));
			canOpenTheEnd = reader.readLine().equalsIgnoreCase("true");
		} catch (Exception e) {
		}
		BukkitRunnable runnable = new BukkitRunnable() {
			long previousTime = timeToOpeningTheEnd();
			boolean flip = false;
			boolean barOn = false;
			long endOpen = -1L;

			@Override
			public void run() {
				flip = !flip;
				long time = timeToOpeningTheEnd();
				if (previousTime < 60000L && time > 86400000L) {
					// open the end
				} else {

				}
			}
		};
		runnable.runTaskTimer(this, 20L, 20L);
	}

	public long timeToOpeningTheEnd() {
		long now = System.currentTimeMillis();
		long time = (now - 158400000) % 604800000;
		return 604800000 - time;
	}

	private File textureCacheDir = null;

	private File getTextureCacheDir() {
		if (textureCacheDir == null) {
			textureCacheDir = new File(getDataFolder(), "texturecache");
			if (!textureCacheDir.isDirectory()) {
				if (textureCacheDir.exists()) {
					textureCacheDir.delete();
				}
				textureCacheDir.mkdirs();
			}
			migrateTextures(textureCacheDir);
		}
		return textureCacheDir;
	}

	private void migrateTextures(File textureCacheDir) {
		File[] ff = textureCacheDir.listFiles();
		if (ff == null) {
			return;
		}
		File failDir = new File(textureCacheDir, "failed");
		for (File f : ff) {
			String name = f.getName();
			File pf = f.getParentFile();
			if (name.endsWith(".txt")) {
				String username = name.substring(0, name.length() - 4);
				if (username.length() <= 16) {
					UUID uuid = getUUIDCache().getUUIDFromName(username);
					if (uuid == null) {
						File newFile = new File(failDir, name);
						if (!failDir.isDirectory()) {
							if (failDir.exists()) {
								failDir.delete();
							}
							failDir.mkdirs();
						}
						f.renameTo(newFile);
					} else {
						String newFileName = (uuid.toString().replace("-", "").toLowerCase()) + ".txt";
						File newFile = new File(pf, newFileName);
						f.renameTo(newFile);
					}
				}
			}
		}
	}

	/**
	 * Completes a GameProfile by filling in the missing information.
	 *
	 * @param profile
	 * @return
	 */
	public GameProfile getGameProfile(GameProfile profile) {
		try {
			String name = profile.getName();
			UUID uuid = profile.getId();
			if (name == null) {
				name = getUUIDCache().getNameFromUUID(uuid);
			} else if (uuid == null) {
				uuid = getUUIDCache().getUUIDFromName(name);
			}
			if (name == null || uuid == null) {
				return null;
			}
			GameProfile gameProfile = new GameProfile(uuid, name);
			String value = null;
			String signature = null;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(getTextureCacheDir(), (uuid.toString().replace("-", "").toLowerCase()) + ".txt"))))) {
				value = reader.readLine();
				signature = reader.readLine();
			} catch (Exception e) {
			}
			if (value != null && signature != null) {
				gameProfile.getProperties().put("textures", new Property("textures", value, signature));
			}
			return gameProfile;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Copy properties from the source to the destination. All properties will
	 * be deleted from the destination prior to copying.
	 *
	 * @param source
	 * @param destination
	 */
	public void copyProperties(GameProfile source, GameProfile destination) {
		PropertyMap propSource = source.getProperties();
		PropertyMap propDest = destination.getProperties();
		propDest.clear();
		for (String key : propSource.keySet()) {
			propDest.putAll(key, propSource.get(key));
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void saveToTextureCache(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		FileOutputStream fos = null;
		try {
			GameProfile profile = getGameProfile(p);
			Property property = profile.getProperties().get("textures").iterator().next();
			String value = property.getValue();
			String signature = property.getSignature();
			fos = new FileOutputStream(new File(getTextureCacheDir(), (p.getUniqueId().toString().replace("-", "").toLowerCase()) + ".txt"));
			fos.write((value + "\n").getBytes());
			fos.write((signature + "\n").getBytes());
			fos.close();
		} catch (Exception e) {
		} finally {
			tryClose(fos);
		}
	}
	private Field internalEntityField = null;
	private Field gameProfileField = null;

	public GameProfile getGameProfile(Player p) {
		return getGameProfile0(getInternalEntity(p));
	}

	private Object getInternalEntity(Player p) {
		try {
			if (internalEntityField == null) {
				Class clazz = p.getClass();
				while (clazz.getSuperclass() != Object.class) {
					clazz = clazz.getSuperclass();
				}
				internalEntityField = clazz.getDeclaredField("entity");
			}
			internalEntityField.setAccessible(true);
			Object o = internalEntityField.get(p);
			return o;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			reportProblem("Could not get internal entity for Player", e);
			return null;
		}
	}

	private GameProfile getGameProfile0(Object o) {
		try {
			if (gameProfileField == null) {
				Field[] fields = o.getClass().getSuperclass().getDeclaredFields();
				for (Field field : fields) {
					if (field.getType() == GameProfile.class) {
						gameProfileField = field;
					}
				}
			}
			boolean shouldBeAccessible = gameProfileField.isAccessible();
			if (!shouldBeAccessible) {
				gameProfileField.setAccessible(true);
			}
			GameProfile gp = (GameProfile) gameProfileField.get(o);
			if (!shouldBeAccessible) {
				gameProfileField.setAccessible(false);
			}
			return gp;
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			reportProblem("Could not get GameProfile from Player entity", e);
			return null;
		}
	}

	@EventHandler
	public void registerChannelEvent(PlayerRegisterChannelEvent event) {
		if (event.getChannel().equals("BungeeCord")) {
			if (skinServerHandler != null) {
				skinServerHandler.sendSkinRequest(event.getPlayer());
			}
			getSession(event.getPlayer()).updateGroups();
		}
	}

	public void sharedChat(final String message, final String channel) {
		setVariable("@chat." + channel, message);
	}
	private String lobbyServer = null;
	private String lobbyWarp = null;

	private QuitHandler quitHandler = null;

	public void setQuitHandler(QuitHandler handler) {
		this.quitHandler = handler;
	}

	public void quitToLobby(Player p) {
		if (p == null) {
			return;
		}
		if (quitHandler != null) {
			quitHandler.playerQuitting(p);
			return;
		}
		if (serverName.equalsIgnoreCase("hub")) {
			p.sendMessage(ChatColor.RED + "Quit? Quit what? You're in the main lobby!");
			return;
		}
		sendToServer(p, lobbyServer == null ? "hub" : lobbyServer, lobbyWarp);
	}

	public void sendToServer(Player p, String server, String warp) {
		if (p == null || server == null) {
			throw new NullPointerException();
		}
		if (warp != null) {
			setVariable("@" + server + "@warp@" + (p.getUniqueId().toString()), warp);
		}
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("Connect");
			out.writeUTF(server);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	public void allQuitToLobby() {
		Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
		for (Player p : onlinePlayers) {
			quitToLobby(p);
		}
	}

	public void reportSpeeder(Player p, boolean fly, boolean gliding, GameMode gameMode, int speedPotion, double speed3D, double speedXZ, double speedY) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("SpeedReport");
			int gamemodeVal = 0;
			switch (gameMode) {
				case SURVIVAL:
					gamemodeVal = 0;
					break;
				case CREATIVE:
					gamemodeVal = 1;
					break;
				case ADVENTURE:
					gamemodeVal = 2;
					break;
				case SPECTATOR:
					gamemodeVal = 3;
					break;
			}
			int infoByte = (fly ? 0x80 : 0) + (gliding ? 0x40 : 0) + gamemodeVal;
			out.writeByte(infoByte);
			out.writeShort(speedPotion);
			out.writeDouble(speed3D);
			out.writeDouble(speedXZ);
			out.writeDouble(speedY);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	@Override
	public void onPluginMessageReceived(String channel, final Player p, byte[] message) {
		String subchannel = null;
		try {
			if (channel.equals("BungeeCord")) {
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
				subchannel = in.readUTF();
				if (subchannel.equals("CBInfo")) {
					String groupList = in.readUTF();
					String nickname = in.readUTF();
					if (nickname.equals("")) {
						nickname = null;
					}
					try {
						EssentialsModule essentials = getModule(EssentialsModule.class);
						if (essentials != null) {
							essentials.setNickname(p, nickname);
						}
					} catch (Exception e) {
					}
					getNicknameCache().setNicknameCache(p.getUniqueId(), nickname);
					PlayerSession session = getSession(p);
					session.setGroups(groupList.replace(" ", "").split(","));
				} else if (subchannel.equals("GetSecretCode")) {
					final String secretCode = in.readUTF();
					final PlayerSession session = getSession(p);
					final Block block = session.secretCodeBlock;
					if (block == null) {
						return;
					}
					session.secretCodeBlock = null;
					BlockState state = block.getState();
					if (!(state instanceof Sign)) {
						return;
					}
					final int randomNumber = (int) Math.floor(Math.random() * 1000000);
					session.secretCodeRandomNumber = randomNumber;
					final Location loc = block.getLocation();
					if (secretCode.isEmpty()) {
						p.sendMessage(ChatColor.RED + "Something went wrong! :( Click on the sign to try again.");
						p.sendSignChange(loc, new String[]{"Something went", "wrong! :( Click", "to try again.", ""});
					} else {
						p.sendMessage(ChatColor.GOLD + "Your secret code is: " + ChatColor.AQUA + secretCode);
						p.sendMessage(ChatColor.GOLD + "This code is a SECRET! Don't tell anyone, not even staff!");
						p.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Only" + ChatColor.RESET + ChatColor.GOLD + " the following websites are allowed to ask for this code:");
						p.sendMessage(ChatColor.AQUA + "https://cubebuilders.net/");
						p.sendMessage(ChatColor.AQUA + "https://siggi.io/");
						p.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Do not" + ChatColor.RESET + ChatColor.GOLD + " enter the code on any other website!");
						//p.sendMessage(ChatColor.GOLD + "To register on Discord, send a private message to the CubeBuilders Bot on the Discord server:");
						//p.sendMessage(ChatColor.AQUA + "!register " + p.getName() + " " + secretCode);
						new BukkitRunnable() {
							int part = 0;

							@Override
							public void run() {
								if (!p.isOnline()) {
									cancel();
									return;
								}
								if (session.secretCodeRandomNumber != randomNumber) {
									cancel();
									return;
								}
								String[] pieces = new String[]{"Secret Code:", secretCode, "", ""};
								if (part < 10) {
									if (part > 5 || part % 2 == 0) {
										pieces[2] = "Staff will NEVER";
										pieces[3] = "ask for this!";
									}
								} else if (part < 20) {
									if (part > 15 || part % 2 == 0) {
										pieces[2] = "ONLY enter the";
										pieces[3] = "code on website!";
									}
								} else if (part < 30) {
									if (part > 25 || part % 2 == 0) {
										pieces[2] = "Do NOT share it";
										pieces[3] = "with others!";
									}
								} else if (part < 40) {
									if (part > 35 || part % 2 == 0) {
										pieces[2] = "This code will";
										pieces[3] = "expire in 5 mins.";
									}
								}
								p.sendSignChange(loc, pieces);
								part += 1;
								if (part == 40) {
									cancel();
								}
							}
						}.runTaskTimer(this, 10L, 10L);
					}
				} else if (subchannel.equals("GetSkin")) {
					String mojangSkin = in.readUTF();
					String mojangSkinSignature = null;
					if (!mojangSkin.equals("NO")) {
						mojangSkinSignature = in.readUTF();
					} else {
						mojangSkin = null;
					}
					String customSkin = in.readUTF();
					String customSkinSignature = null;
					if (!customSkin.equals("NO")) {
						customSkinSignature = in.readUTF();
					} else {
						customSkin = null;
					}
					skinServerHandler.mySkin(p, mojangSkin, mojangSkinSignature, customSkin, customSkinSignature);
				} else if (subchannel.equals("GetOtherSkin")) {
					String correctName = in.readUTF();
					if (correctName.isEmpty()) {
						skinServerHandler.otherSkinNotFound(p);
					} else {
						long most = in.readLong();
						long least = in.readLong();
						UUID uuid = new UUID(most, least);
						String mojangSkin = in.readUTF();
						String mojangSkinSignature = null;
						if (!mojangSkin.equals("NO")) {
							mojangSkinSignature = in.readUTF();
						} else {
							mojangSkin = null;
						}
						String customSkin = in.readUTF();
						String customSkinSignature = null;
						if (!customSkin.equals("NO")) {
							customSkinSignature = in.readUTF();
						} else {
							customSkin = null;
						}
						skinServerHandler.otherSkin(p, correctName, uuid, mojangSkin, mojangSkinSignature, customSkin, customSkinSignature);
					}
				} else if (subchannel.equalsIgnoreCase("CubeTokens")) {
					String req = in.readUTF();
					if (req.equalsIgnoreCase("UPDATE")) {
						String uuidStr = in.readUTF();
						UUID uuid = UUID.fromString(uuidStr.replace("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5"));
						updateCT(uuid);
					}
				} else if (subchannel.equalsIgnoreCase("Sound")) {
					final String soundID = in.readUTF();
					final float volume = in.readFloat();
					final float pitch = in.readFloat();
					final int tickDelay = in.readInt();
					final Sound sound = Sound.valueOf(soundID);
					BukkitRunnable runnable = new BukkitRunnable() {
						@Override
						public void run() {
							if (sound != null) {
								p.playSound(p.getLocation(), sound, volume, pitch);
							}
						}
					};
					if (tickDelay <= 0) {
						runnable.run();
					} else {
						runnable.runTaskLater(this, tickDelay);
					}
				} else if (subchannel.equalsIgnoreCase("LightningStrike")) {
					Location location = p.getLocation();
					World world = location.getWorld();
					world.strikeLightningEffect(location);
				} else if (subchannel.equalsIgnoreCase("OpenReporter")) {
					getModule(PunisherModule.class).openReporter(p);
				} else if (subchannel.equalsIgnoreCase("OpenPunisher")) {
					final String uuidStr = in.readUTF();
					final UUID targetPlayer = Util.uuidFromString(uuidStr);
					final String playerName = in.readUTF();
					final String skinPayload = in.readUTF();
					final String skinSignature = in.readUTF();
					final boolean allowTroll = in.readBoolean();
					final boolean allowMute = in.readBoolean();
					final boolean allowBan = in.readBoolean();
					getModule(PunisherModule.class).openPunisher(p, targetPlayer, playerName, skinPayload.equals("null") ? null : skinPayload, skinSignature.equals("null") ? null : skinSignature, allowTroll, allowMute, allowBan);
				} else if (subchannel.equalsIgnoreCase("OpenPunishmentSetup")) {
					final String uuidStr = in.readUTF();
					final UUID targetPlayer = Util.uuidFromString(uuidStr);
					final String playerName = in.readUTF();
					final String skinPayload = in.readUTF();
					final String skinSignature = in.readUTF();
					final String offence = in.readUTF();
					final String preselectedType = in.readUTF();
					final long preselectedLength = in.readLong();
					final boolean allowTroll = in.readBoolean();
					final boolean allowMute = in.readBoolean();
					final boolean allowBan = in.readBoolean();
					getModule(PunisherModule.class).setupPunishment(p, targetPlayer, playerName, skinPayload.equals("null") ? null : skinPayload, skinSignature.equals("null") ? null : skinSignature, offence, preselectedType, preselectedLength, allowTroll, allowMute, allowBan);
				}
				return;
			}
		} catch (IOException | IllegalArgumentException | IllegalStateException e) {
			reportProblem("An error occurred processing a PluginMessage (" + channel + (subchannel == null ? "" : ("/" + subchannel)) + ")", e);
		}
	}

	private Object cubeTokens = null;
	private Method getCubeTokens = null;
	private Method giveCubeTokens = null;
	private Method chargeCubeTokens = null;
	private Method reloadCT = null;

	private void setupCubeTokens() {
		if (cubeTokens != null) {
			return;
		}
		try {
			cubeTokens = getServer().getPluginManager().getPlugin("CubeTokens");
			Class clazz = cubeTokens.getClass();
			getCubeTokens = clazz.getMethod("getCubeTokens", UUID.class);
			giveCubeTokens = clazz.getMethod("giveCubeTokens", UUID.class, long.class);
			chargeCubeTokens = clazz.getMethod("chargeCubeTokens", UUID.class, long.class, boolean.class);
			reloadCT = clazz.getMethod("reloadCT", UUID.class);
		} catch (NoSuchMethodException | SecurityException e) {
			reportProblem("Could not set up CubeTokens", e);
		}
	}

	@Deprecated
	public long getCubeTokens(UUID player) {
		// change to private & remove deprecated after we confirm no code in other plugin uses this.
		setupCubeTokens();
		try {
			return ((Long) getCubeTokens.invoke(cubeTokens, player));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			reportProblem("Could not read CubeTokens balance", e);
		}
		return Long.MIN_VALUE;
	}

	@Deprecated
	public boolean giveCubeTokens(UUID player, long tokens) {
		// change to private & remove deprecated after we confirm no code in other plugin uses this.
		setupCubeTokens();
		try {
			return ((Boolean) giveCubeTokens.invoke(cubeTokens, player, tokens));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			reportProblem("Could not give CubeTokens", e);
		}
		return false;
	}

	@Deprecated
	public boolean chargeCubeTokens(UUID player, long tokens, boolean force) {
		// change to private & remove deprecated after we confirm no code in other plugin uses this.
		setupCubeTokens();
		try {
			return ((Boolean) chargeCubeTokens.invoke(cubeTokens, player, tokens, force));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			reportProblem("Could not take CubeTokens", e);
		}
		return false;
	}

	@Deprecated
	public void updateCT(UUID player) {
		// change to private & remove deprecated after we confirm no code in other plugin uses this.
		setupCubeTokens();
		try {
			reloadCT.invoke(cubeTokens, player);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			reportProblem("Could not reload CubeTokens", e);
		}
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		PlayerSession session = getSession(p);
		Block block = event.getClickedBlock();
		BlockState state = null;
		ItemStack itemUsed = event.getItem();
		Action action = event.getAction();
		if (itemUsed != null) {
			if (itemUsed.getType() == Material.END_CRYSTAL) {
				if (action == Action.RIGHT_CLICK_BLOCK) {
					if (p.getWorld().getEnvironment() == World.Environment.THE_END) {
						p.sendMessage(ChatColor.RED + "End Crystals are not allowed in The End.");
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		if (block != null) {
			state = block.getState();
		}
		if (session.signEdit) {
			session.signEdit = false;
			if (state instanceof Sign) {
				session.signEditBlock = block;
				session.player.sendMessage(ChatColor.GREEN + "Use /signedit [line] [newmessage]");
			}
			return;
		}
		if (session.speedUpMultiplier != 1.0) {
			if (event.getAction() == Action.RIGHT_CLICK_AIR) {
				p.setVelocity(p.getVelocity().multiply(session.speedUpMultiplier));
			}
		}
		if (state instanceof Sign) {
			Sign sign = (Sign) state;
			String[] lines = sign.getLines();
			if (lines.length == 4) {
				if (lines[0].equals("Click here for")
						&& lines[1].equals("a secret code")
						&& lines[2].equals("for the")
						&& lines[3].equals("website!")) {
					session.secretCodeBlock = block;
					try {
						ByteArrayOutputStream b = new ByteArrayOutputStream();
						DataOutputStream out = new DataOutputStream(b);

						out.writeUTF("GetSecretCode");

						p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
					} catch (Exception e) {
					}
				}
			}
		}
	}

	public static final UUID console = new UUID(0L, 0L);
	public static final UUID anticheatDetector = new UUID(0L, 1L);
	public static final UUID cubeBuildersStore = new UUID(0L, 2L);

	public void mute(Player p, UUID userIssuingMute, String offence, String reason, long length) {
		punish(p, userIssuingMute, reason, "Mute", offence, length);
	}

	public void ban(Player p, UUID userIssuingBan, String offence, String reason, long length) {
		punish(p, userIssuingBan, reason, "Ban", offence, length);
	}

	private void punish(Player p, UUID userIssuingPunishment, String reason, String punishmentType, String offence, long punishmentLength) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("Punish");
			out.writeUTF(punishmentType);
			out.writeUTF(offence);
			out.writeLong(userIssuingPunishment.getMostSignificantBits());
			out.writeLong(userIssuingPunishment.getLeastSignificantBits());
			out.writeUTF(reason);
			out.writeLong(punishmentLength);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	public void addPlus(Player p, String paymentRef, long time) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("AddPlus");
			out.writeUTF(paymentRef);
			out.writeLong(time);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	public void sendHotbarMessage(Player p, String message) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("HotbarMessage");
			out.writeUTF(message);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	public void addHotbar(Player p, String id, String message) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("AddHotbar");
			out.writeUTF(id);
			out.writeUTF(message);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	public void removeHotbar(Player p, String id) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("RemoveHotbar");
			out.writeUTF(id);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	public void updateBungeeGroups(Player p, List<String> groups) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("PlayerGroups");
			out.writeUTF(getServerName());
			out.writeInt(groups.size());
			for (String gr : groups) {
				out.writeUTF(gr);
			}
			Plugin[] plugins = getServer().getPluginManager().getPlugins();
			out.writeInt(plugins.length);
			for (Plugin plug : plugins) {
				out.writeUTF(plug.getName());
			}

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void placeItemFrame(HangingPlaceEvent event) {
		Hanging hanging = event.getEntity();
		if (!(hanging instanceof ItemFrame)) {
			return;
		}
		ItemFrame itemFrame = (ItemFrame) hanging;

		Player p = event.getPlayer();
		if (!p.hasPermission("hk.siggi.plugcubebuildersin.autoframe")) {
			return;
		}
		Block placedOn = event.getBlock();
		if (placedOn == null) {
			return;
		}
		BlockState state = placedOn.getState();
		if (!(state instanceof InventoryHolder)) {
			return;
		}
		InventoryHolder inventoryHolder = (InventoryHolder) state;
		Inventory inventory = inventoryHolder.getInventory();
		ItemStack[] contents = inventory.getContents();
		ItemStack item = null;
		int index = 0;
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == null) {
				continue;
			}
			if (item == null) {
				item = contents[i];
				index = i;
			} else if (!item.isSimilar(contents[i])) {
				return;
			}
		}
		if (item == null) {
			return;
		}
		ItemStack itemFrameItem = item.clone();
		itemFrameItem.setAmount(1);
		itemFrame.setItem(itemFrameItem);
		if (item.getAmount() <= 1) {
			item = null;
		} else {
			item.setAmount(item.getAmount() - 1);
		}
		inventory.setItem(index, item);
	}

	@EventHandler(ignoreCancelled = true)
	public void spectatorEntityDamage(EntityDamageEvent event) {
		Entity smacked = event.getEntity();
		if ((smacked instanceof Player)) {
			if (isVanished((Player) smacked)) {
				event.setCancelled(true);
			}
		}
		if ((event instanceof EntityDamageByEntityEvent)) {
			EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) event;
			Entity damager = ev.getDamager();
			Player player = null;
			if ((damager instanceof Player)) {
				player = (Player) damager;
			} else if ((damager instanceof Projectile)) {
				Projectile projectile = (Projectile) damager;
				if ((projectile.getShooter() != null) && ((projectile.getShooter() instanceof Player))) {
					player = (Player) projectile.getShooter();
				}
			}
			if (player != null) {
				if (isVanished(player)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void spectatorEntityTarget(EntityTargetEvent event) {
		if (event.getTarget() instanceof Player) {
			if (isVanished((Player) event.getTarget())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void spectatorVehicleDestroy(VehicleDestroyEvent event) {
		Entity entity = event.getAttacker();
		if (entity instanceof Player) {
			if (isVanishProtected((Player) entity)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void spectatorVehicleEntityCollision(VehicleEntityCollisionEvent event) {
		if (event.getEntity() instanceof Player) {
			if (isVanished((Player) event.getEntity())) {
				event.setCancelled(true);
			}
		}
	}

	private VariableUpdater[] variableUpdaters = null;

	@EventHandler
	public void worldLoad(WorldLoadEvent event) {
		if (doNotKeepSpawnLoaded) {
			event.getWorld().setKeepSpawnInMemory(false);
		}
	}

	private final List<String> blockedItems = new ArrayList<>();

	@EventHandler
	public void creativeItem(InventoryCreativeEvent event) {
		HumanEntity whoClicked = event.getWhoClicked();
		if (!(whoClicked instanceof Player)) {
			return;
		}
		Player p = (Player) whoClicked;
		resetAFKTimer(p);
		ItemStack currentItem = event.getCurrentItem();
		ItemStack cursor = event.getCursor();
		String cI = currentItem == null ? null : currentItem.getType().toString();
		String c = cursor == null ? null : cursor.getType().toString();
		String messageToShow = null;
		for (String item : blockedItems) {
			int x = item.indexOf("=");
			if (x == -1) {
				continue;
			}
			String itemBlocked = item.substring(0, x).trim();
			String message = item.substring(x + 1).trim();
			if (cI != null) {
				if (cI.equals(itemBlocked)) {
					messageToShow = message;
				}
			}
			if (c != null) {
				if (c.equals(itemBlocked)) {
					messageToShow = message;
				}
			}
			if (messageToShow != null) {
				break;
			}
		}
		if (messageToShow != null) {
			event.setCancelled(true);
			if (!messageToShow.equals("")) {
				p.sendMessage(ChatColor.RED + messageToShow);
			}
		}
	}

	public void clearInventoryOfHackedItems(Inventory inventory) {
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack == null) {
				continue;
			}
			if (isHackedItem(stack)) {
				inventory.clear(i);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void teleportedCleanInventory(PlayerTeleportEvent event) {
		final Player p = event.getPlayer();
		PlayerSession session = getSession(p);
		if (session != null) {
			session.autobanGracePeriod = tick + 200;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				cleanInventory(p);
			}
		}.runTaskLater(this, 198L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void joiningCleanInventory(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		PlayerSession session = getSession(p);
		session.autobanGracePeriod = tick + 200;
		new BukkitRunnable() {
			@Override
			public void run() {
				cleanInventory(p);
			}
		}.runTaskLater(this, 198L);
	}

	@EventHandler
	public void inventoryOpened(InventoryOpenEvent event) {
		clearInventoryOfHackedItems(event.getInventory());
	}

	@EventHandler
	public void pickupHackedItem(PlayerPickupItemEvent event) {
		Item groundItem = event.getItem();
		ItemStack item = groundItem.getItemStack();
		if (isHackedItem(item)) {
			event.setCancelled(true);
			groundItem.remove();
		}
	}

	@EventHandler
	public void detectHackedItem(InventoryCreativeEvent event) {
		HumanEntity whoClicked = event.getWhoClicked();
		if (!(whoClicked instanceof Player)) {
			return;
		}
		Player p = (Player) whoClicked;
		ItemStack cursor = event.getCursor();
		ItemStack currentItem = event.getCurrentItem();
		if (cursor != null) {
			if (isHackedItem(cursor)) {
				event.setCancelled(true);
				cleanInventory(p);
				return;
			}
		}
		if (currentItem != null) {
			if (isHackedItem(currentItem)) {
				event.setCancelled(true);
				cleanInventory(p);
				return;
			}
		}
	}

	public boolean isHackedItem(ItemStack stack) {
		NBTCompound tag = NBTTool.getUtil().getTag(stack);
		if (tag == null) {
			return false;
		}
		NBTCompound wurst = tag.getCompound("www.wurst-client.tk");
		if (wurst != null) {
			NBTList list = wurst.getList("www.wurst-client.tk");
			if (list != null) {
				if (list.size() >= 1) {
					return true;
				}
			}
		}
		NBTCompound wurst2 = tag.getCompound("www.wurstclient.net");
		if (wurst2 != null) {
			NBTList list = wurst2.getList("www.wurstclient.net");
			if (list != null) {
				if (list.size() >= 1) {
					return true;
				}
			}
		}
		NBTCompound skullOwner = tag.getCompound("SkullOwner");
		if (skullOwner != null) {
			try {
				NBTCompound properties = skullOwner.getCompound("Properties");
				if (properties != null) {
					NBTList list = properties.getList("textures");
					if (list != null) {
						for (int i = 0; i < list.size(); i++) {
							NBTCompound textures = list.getCompound(i);
							String value = textures.getString("Value");
							if (value.isEmpty()) { // SkillClient
								return true;
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		boolean hasLore = false;
		NBTCompound display = tag.getCompound("display");
		if (display != null) {
			String name = display.getString("Name");
			if (name != null) {
				if (name.length() > 512) {
					return true;
				}
				if (name.contains("Server Creeper")) {
					return true;
				}
			}
			String customName = display.getString("CustomName");
			if (customName != null) {
				if (customName.length() > 512) { // SkillClient
					return true;
				}
			}
			NBTList loreList = display.getList("Lore");
			if (loreList != null) {
				hasLore = loreList.size() > 0;
			}
		}
		NBTCompound blockEntity = tag.getCompound("BlockEntityTag");
		if (blockEntity != null) {
			String command = tag.getString("Command");
			if (command != null) {
				if (command.length() > 0) { // could be any client
					if (!hasLore) {
						return true;
					}
				}
			}
		}
		NBTList attributeModifiers = tag.getList("AttributeModifiers");
		try {
			if (attributeModifiers != null) {
				for (int i = 0; i < attributeModifiers.size(); i++) {
					NBTCompound compound = attributeModifiers.getCompound(i);
					int least = compound.getInt("UUIDLeast");
					int most = compound.getInt("UUIDMost");
					if (least == 24636 && most == 246216) { // unknown hacked client, found it off a player
						// this is the UUID that one hack client always creates.
						return true;
					}
					if (compound.getInt("Amount") > 200000) {
						return true;
					}
				}
			}
		} catch (Exception e) {
		}
		NBTList potionEffects = tag.getList("CustomPotionEffects");
		try {
			if (potionEffects != null) {
				for (int i = 0; i < potionEffects.size(); i++) {
					try {
						NBTCompound compound = potionEffects.getCompound(i);
						int amplifier = compound.getByte("Amplifier");
						if (amplifier > 10) {
							return true;
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
		}
		NBTList enchants = tag.getList("ench");
		try {
			if (enchants != null) {
				for (int i = 0; i < enchants.size(); i++) {
					NBTCompound compound = enchants.getCompound(i);
					try {
						int lvl = (int) compound.getShort("lvl");
						if (lvl > 10) {
							return true;
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
		}
		try {
			List<String> lore = stack.getItemMeta().getLore();
			for (String l : lore) {
				if (ChatColor.stripColor(l).equalsIgnoreCase("Instantly Kills EVERY Entity in range.")) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	private void updatePlayers(int players) {
		if (externalPlayerIndicator) {
			return;
		}
		updatingPlayerCount = true;
		setVariable("players", players + " player" + (players == 1 ? "" : "s"));
		setVariable("status", "Online");
		updatingPlayerCount = false;
	}

	public void setVariable(String variable, String value) {
		if (!pluginEnabled && !updatingPlayerCount) {
			return;
		}
		if (!updatingPlayerCount && variable.equals("players")) {
			externalPlayerIndicator = true;
		}
		setRawVariable((variable.startsWith("@") ? "" : (serverName + ".")) + variable, value);
	}

	public void setRawVariable(String variable, String value) {
		try {
			variableServer.updateVariable(variable, value);
		} catch (Exception e) {
		}
		receivedVariable(variable, value);
	}

	@Override
	public void receivedVariable(final String variable, final String value) {
		if (!Bukkit.isPrimaryThread()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					receivedVariable(variable, value);
				}
			}.runTask(this);
			return;
		}
		for (VariableListener variableListener : variableListeners) {
			try {
				variableListener.receivedVariable(variable, value);
			} catch (Exception e) {
			}
		}
		if (variable.equalsIgnoreCase("@command")) {
			getServer().dispatchCommand(getServer().getConsoleSender(), value);
			return;
		}
		if (variable.equalsIgnoreCase("@fakejoin")) {
			if (getServer().getPlayer(value) != null) {
				String msg = joinMessage.replaceAll("\\[player\\]", value);
				announce(msg);
			}
		}
		if (variable.equalsIgnoreCase("@fakequit")) {
			if (getServer().getPlayer(value) != null) {
				String msg = quitMessage.replaceAll("\\[player\\]", value);
				announce(msg);
			}
		}
		if (variable.startsWith("@")) {
			if (variable.startsWith("@" + serverName + "@")) {
				try {
					variableCommand(variable.substring(serverName.length() + 2), value);
				} catch (Exception e) {
					reportProblem("Error occurred handling a command", e);
				}
			}
			return;
		}
		if (variable.equalsIgnoreCase("vanishlist")) {
			if (value.length() == 0) {
				updateVanishList(new String[0]);
			} else {
				updateVanishList(value.split(","));
			}
		}
		if (variableUpdaters == null) {
			return;
		}
		for (VariableUpdater variableUpdater : variableUpdaters) {
			try {
				variableUpdater.update(variable, value);
			} catch (Exception e) {
			}
		}
		if (variable.startsWith("@chat.")) {
			String cChannel = variable.substring(6);
			if (cChannel.equals("StaffChat")) {
				for (Player player : getServer().getOnlinePlayers()) {
					if (player.hasPermission("hk.siggi.plugcubebuildersin.staffchat")) {
						player.sendRawMessage(value);
					}
				}
				CommandSender cs = getServer().getConsoleSender();
				cs.sendMessage(value);
			}
			if (cChannel.equals("NotStaffChat")) {
				for (Player player : getServer().getOnlinePlayers()) {
					if (!player.hasPermission("hk.siggi.plugcubebuildersin.staffchat")) {
						player.sendRawMessage(value);
					}
				}
				CommandSender cs = getServer().getConsoleSender();
				cs.sendMessage(value);
			}
		}
	}

	@Override
	public void receivedMessage(final String from, final byte[] message) {
		if (!Bukkit.isPrimaryThread()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					receivedMessage(from, message);
				}
			}.runTask(this);
			return;
		}
		for (VariableListener variableListener : variableListeners) {
			try {
				variableListener.receivedMessage(from, message);
			} catch (Exception e) {
			}
		}
	}

	private void announce(String message) {
		Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
		for (Player player : onlinePlayers) {
			player.sendRawMessage(message);
		}
	}

	private List<String> vanishedPlayers = new LinkedList<>();
	private final Object vanishLock = new Object();

	public boolean isVanished(Player p) {
		return isVanished(p.getName());
	}

	public boolean isVanishProtected(Player p) {
		if (!isVanished(p.getName())) {
			return false;
		} else {
			return getSession(p).vanishProtection;
		}
	}

	public boolean isVanished(String p) {
		synchronized (vanishLock) {
			return vanishedPlayers.contains(p);
		}
	}

	private void updateVanishList(final String vanished[]) {
		(new BukkitRunnable() {
			@Override
			public void run() {
				long now = System.currentTimeMillis();
				synchronized (vanishLock) {
					ArrayList<String> newList = new ArrayList<>();
					newList.addAll(Arrays.asList(vanished));
					Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
					for (String vanishedPlayer : vanishedPlayers) {
						if (!newList.contains(vanishedPlayer)) {
							// this player was hidden, but is no longer hidden
							try {
								Player pToUnhide = getServer().getPlayer(vanishedPlayer);
								if (pToUnhide == null) {
									continue;
								}
								PlayerSession session = getSession(pToUnhide);
								session.unvanishTime = now;
								updateVanishedStatus(pToUnhide, false);
							} catch (Exception e) {
								reportProblem("Error occurred processing vanish", e);
							}
						}
					}
					for (String pl : newList) {
						if (!vanishedPlayers.contains(pl)) {
							// this player was not hidden, but is now hidden
							try {
								Player pToHide = getServer().getPlayer(pl);
								if (pToHide == null) {
									continue;
								}
								updateVanishedStatus(pToHide, true);
							} catch (Exception e) {
								reportProblem("Error occurred processing vanish", e);
							}
						}
					}
					vanishedPlayers = newList;
				}
			}
		}).runTask(this);
	}

	private void updateVanishedStatus(Player p, boolean vanish) {
		EssentialsModule essentials = getModule(EssentialsModule.class);
		if (essentials != null) {
			essentials.setVanished(p, vanish);
		}
		OpenInvModule openInv = getModule(OpenInvModule.class);
		if (openInv != null) {
			if (vanish || openInv.getSilentChest(p)) {
				openInv.setSilentChest(p, vanish);
			}
		}
	}
	private final ArrayList<LobbyWarpRequest> lobbyWarpRequests = new ArrayList<>();

	public void variableCommand(String variable, String value) throws Exception {
		if (variable.startsWith("warp@")) {
			UUID player = UUID.fromString(variable.substring(5));
			Player p = getServer().getPlayer(player);
			if (p == null) {
				lobbyWarpRequests.add(new LobbyWarpRequest(player, value));
			} else {
				teleportPlayerToWarp(p, value);
			}
		}
	}

	public boolean teleportPlayerToWarp(Player p, String warpName) {
		EssentialsModule ess = getModule(EssentialsModule.class);
		if (ess == null) {
			return false;
		}
		Location warpLocation = ess.getWarpLocation(warpName);
		if (warpLocation != null) {
			try {
				p.teleport(warpLocation);
				return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	public String getLobbyWarpRequest(UUID player) {
		String result = null;
		for (int i = 0; i < lobbyWarpRequests.size(); i++) {
			LobbyWarpRequest req = lobbyWarpRequests.get(i);
			if (req.expired()) {
				lobbyWarpRequests.remove(i);
				i--;
				continue;
			}
			if (req.player.equals(player)) {
				result = req.warpName;
				lobbyWarpRequests.remove(i);
				i--;
			}
		}
		return result;
	}
	public final long pvpTimer = 15000L;

	public long getLastPvPActivity(Player p) {
		Strike lastAttackS = (Strike) lastAttacks.get(p);
		Strike lastDamageS = (Strike) lastStrikes.get(p);
		long lastAttack = lastAttackS == null ? 0L : lastAttackS.time;
		long lastDamage = lastDamageS == null ? 0L : lastDamageS.time;
		return Math.max(lastAttack, lastDamage);
	}

	public double getTeleportDelay(Player p) {
		long timeSincePvP = System.currentTimeMillis() - getLastPvPActivity(p);
		if (timeSincePvP > pvpTimer) {
			return 0.0;
		}
		p.sendMessage(ChatColor.RED + "You can't teleport until " + (pvpTimer / 1000) + " seconds after PvP.");
		return ((double) (pvpTimer - timeSincePvP)) / 1000.0;
	}

	public boolean isUnsafe(Player p) {
		long timeSincePvP = System.currentTimeMillis() - getLastPvPActivity(p);
		return timeSincePvP <= pvpTimer;
	}

	public boolean recentlyEnteredPvP(Player p) {
		Long i = lastPvPCommand.get(p);
		if (i == null) {
			return false;
		}
		long timeSince = System.currentTimeMillis() - i;
		return timeSince <= pvpTimer;
	}

	public void enteredPvP(Player p) {
		lastPvPCommand.put(p, System.currentTimeMillis());
	}

	public boolean allowAttack(Player attacker, Player victim) {
		if (recentlyEnteredPvP(attacker) && recentlyEnteredPvP(victim)) {
			return true;
		}
		long now = System.currentTimeMillis();
		ArrayList<Strike> recent = recentAttacks.get(attacker);
		if (recent != null) {
			for (Strike strike : recent) {
				if (strike.victim == victim && now - strike.time <= pvpTimer) {
					return true;
				}
			}
		}
		recent = recentAttacks.get(victim);
		if (recent != null) {
			for (Strike strike : recent) {
				if (strike.victim == attacker && now - strike.time <= pvpTimer) {
					return true;
				}
			}
		}
		return false;
	}

	private TeleportControlSystem teleportControlSystem = null;

	public boolean playerTPA(Player from, Player to, boolean tpaHere) {
		if (teleportControlSystem != null) {
			return teleportControlSystem.playerTPA(from, to, tpaHere);
		}
		return true;
	}

	public boolean goHome(Player player, Location home) {
		if (teleportControlSystem != null) {
			return teleportControlSystem.goHome(player, home);
		}
		return true;
	}

	public boolean setHome(Player player) {
		if (teleportControlSystem != null) {
			return teleportControlSystem.setHome(player);
		}
		return true;
	}

	public boolean loadWorld(String world) {
		{
			ByteArrayOutputStream baos;
			PrintStream ps = new PrintStream(baos = new ByteArrayOutputStream());
			new Exception().printStackTrace(ps);
			if (baos.toString().contains("com.earth2me.essentials.commands.Commandbalancetop")) {
				// don't load worlds for Essentials /baltop command -- that command
				// loads every Essentials user file which loads every user's last
				// location which loads the worlds for all those locations.
				return false;
			}
		}
		for (WorldLoaderModule m : getModules(WorldLoaderModule.class)) {
			if (m.loadWorld(world)) {
				return true;
			}
		}
		return false;
	}

	public List<MusicPlayer> musicPlayers = new LinkedList<>();
	public List<BlockPlayer> blockPlayers = new LinkedList<>();

	public void tickMusic() {
		Iterator<MusicPlayer> musicPlayerIterator = musicPlayers.iterator();
		while (musicPlayerIterator.hasNext()) {
			MusicPlayer musicPlayer = musicPlayerIterator.next();
			if (!musicPlayer.playOneTick()) {
				musicPlayerIterator.remove();
				if (musicPlayer.onFinish != null) {
					musicPlayer.onFinish.run();
				}
			}
		}
		Iterator<BlockPlayer> blockPlayerIterator = blockPlayers.iterator();
		while (blockPlayerIterator.hasNext()) {
			BlockPlayer blockPlayer = blockPlayerIterator.next();
			if (!blockPlayer.tick()) {
				blockPlayerIterator.remove();
			}
		}
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerSession session = getSession(p);
			if (session.musicPlayer != null) {
				if (!session.musicPlayer.playOneTick()) {
					MusicPlayer musicPlayer = session.musicPlayer;
					session.musicPlayer = null;
					if (musicPlayer.onFinish != null) {
						musicPlayer.onFinish.run();
					}
				}
			}
		}
	}

	public void reportProblem(String message, Throwable t) {
		ErrorReportModule errorReporter = getModule(ErrorReportModule.class);
		if (errorReporter != null) {
			errorReporter.reportProblem(message, t);
		}
	}

	public ItemStack createSkull(Player p) {
		return createSkull(getGameProfile(p));
	}

	public ItemStack createSkull(UUID player) {
		String name = getUUIDCache().getNameFromUUID(player);
		if (name == null) {
			return new ItemStack(Material.PLAYER_HEAD, 1);
		}
		return createSkull(getGameProfile(new GameProfile(player, name)));
	}

	public ItemStack createSkull(GameProfile gameProfile) {
		if (gameProfile == null) {
			return new ItemStack(Material.PLAYER_HEAD, 1);
		}
		UUID player = gameProfile.getId();
		String name = gameProfile.getName();
		String textures = null;
		String texturesSignature = null;
		Collection<Property> texturesProperty = gameProfile.getProperties().get("textures");
		if (texturesProperty != null) {
			Iterator<Property> iterator = texturesProperty.iterator();
			if (iterator.hasNext()) {
				Property prop = iterator.next();
				if (prop != null) {
					textures = prop.getValue();
					texturesSignature = prop.getSignature();
				}
			}
		}
		return createSkull(player, name, textures, texturesSignature);
	}

	public ItemStack createSkull(UUID player, String name, String textures, String texturesSignature) {
		NBTUtil util = NBTTool.getUtil();
		ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
		NBTCompound compound = util.newCompound();
		NBTCompound skullOwner = util.newCompound();
		if (player != null) {
			skullOwner.setString("Id", player.toString());
		}
		if (name != null) {
			skullOwner.setString("Name", name);
		}
		if (textures != null) {
			NBTCompound properties = util.newCompound();
			NBTList texturesList = util.newList();
			NBTCompound texturePayload = util.newCompound();
			texturePayload.setString("Value", textures);
			if (texturesSignature != null) {
				texturePayload.setString("Signature", texturesSignature);
			}
			texturesList.addCompound(texturePayload);
			properties.setList("textures", texturesList);
			skullOwner.setCompound("Properties", properties);
		}
		compound.setCompound("SkullOwner", skullOwner);
		return util.setTag(stack, compound);
	}

	public void setPublicChatGroup(String server, String group) {
		if (group == null) {
			group = "";
		}
		setVariable("@publicchatgroup." + server, group);
	}

	public void setPublicChatGroup(Player p, String group) {
		if (group == null) {
			group = "";
		}
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("PublicChatGroup");
			out.writeUTF(group);

			p.sendPluginMessage(this, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	public void postProcessRootCommandNode(Object rootObj) {
		try {
			BrigadierUtil bu = NMSUtil.get().getBrigadierUtil();
			RootCommandNode root = (RootCommandNode) rootObj;
			root.removeCommand("w");
			LiteralCommandNode whisper = new LiteralCommandNode("w", null, null, null, null, false);
			ArgumentCommandNode username = new ArgumentCommandNode("username", StringArgumentType.word(), null, null, null, null, false, bu.suggestionProviderAskServer());
			ArgumentCommandNode message = new ArgumentCommandNode("message", bu.argumentTypeChat(), null, null, null, null, false, null);
			username.addChild(message);
			whisper.addChild(username);
			root.addChild(whisper);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
