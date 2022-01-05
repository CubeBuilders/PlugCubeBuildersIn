package hk.siggi.bukkit.plugcubebuildersin.skins;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import hk.siggi.bukkit.touchsigns.TouchsignEvent;
import hk.siggi.bukkit.touchsigns.TouchsignListener;
import hk.siggi.bukkit.touchsigns.Touchsigns;
import hk.siggi.statues.Statue;
import hk.siggi.statues.Statues;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SkinServerHandler implements Listener, TouchsignListener {

	public static int getMaxSlot(Player p) {
		if (!p.hasPermission("hk.siggi.plugcubebuildersin.skins.donatorslots")) {
			return 6;
		}
		return Integer.MAX_VALUE;
	}

	public final PlugCubeBuildersIn plugin;
	private Touchsigns touchsignsPlugin = null;
	private Statues statuesPlugin = null;
	public Location currentSkinStatue = null;
	public Block currentSkinSign = null;
	public Location mojangSkinStatue = null;
	public Block mojangSkinSign = null;

	public Location[] savedSkinStatues = null;
	public Block[] savedSkinSigns = null;

	public SkinServerHandler(PlugCubeBuildersIn plugin, File fileToLoadFrom) {
		this.plugin = plugin;
		World world;
		ArrayList<Location> skinStatues = new ArrayList<>();
		ArrayList<Block> skinSigns = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileToLoadFrom));
			String worldName = reader.readLine();
			world = plugin.getServer().getWorld(worldName);
			String line;
			currentSkinStatue = toLocation(world, reader.readLine());
			currentSkinSign = toBlock(world, reader.readLine());
			mojangSkinStatue = toLocation(world, reader.readLine());
			mojangSkinSign = toBlock(world, reader.readLine());
			while ((line = reader.readLine()) != null) {
				Location location = toLocation(world, line);
				Block block = toBlock(world, reader.readLine());
				if (location == null || block == null) {
					break;
				}
				skinStatues.add(location);
				skinSigns.add(block);
			}
		} catch (Exception e) {
		} finally {
			tryClose(reader);
		}
		savedSkinStatues = skinStatues.toArray(new Location[0]);
		savedSkinSigns = skinSigns.toArray(new Block[0]);
		touchsignsPlugin = (Touchsigns) (plugin.getServer().getPluginManager().getPlugin("Touchsigns"));
		statuesPlugin = (Statues) (plugin.getServer().getPluginManager().getPlugin("Statues"));
	}

	public void start() {
		if (currentSkinStatue == null || currentSkinSign == null) {
			return;
		}
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		touchsignsPlugin.addListener(this, plugin);
		plugin.getCommand("wardrobe").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
				return command(sender, command, label, split);
			}
		});
	}

	private boolean command(CommandSender sender, Command command, String label, String[] split) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This can only be done ingame!");
			return true;
		}
		Player p = (Player) sender;
		try {
			sendOtherSkinRequest(p, split[0]);
		} catch (Exception e) {
		}
		return true;
	}

	private final WeakHashMap<Player, PlayerSkins> skinMap = new WeakHashMap<>();
	private final Map<Player, SkinSession> sessions = new WeakHashMap<>();

	public PlayerSkins getPlayerSkins(Player p) {
		PlayerSkins skins = skinMap.get(p);
		if (skins == null) {
			skinMap.put(p, skins = new PlayerSkins(this, p));
		}
		return skins;
	}

	public SkinSession getSession(Player p) {
		SkinSession session = sessions.get(p);
		if (session == null) {
			sessions.put(p, session = new SkinSession(this, p));
		}
		return session;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		getPlayerSkins(p);
		p.setAllowFlight(true);
		for (Player pl : plugin.getServer().getOnlinePlayers()) {
			if (pl == p) {
				continue;
			}
			pl.hidePlayer(p);
			p.hidePlayer(pl);
		}
		//sendSkinRequest(p);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerChatA(final AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void playerLogout(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		PlayerSkins s = getPlayerSkins(p);
		if (s != null) {
			if (!s.quitWithoutSaving) {
				s.save();
			}
		}
	}

	@Override
	public void touchsign(TouchsignEvent event) {
		Block b = event.getBlock();
		Player p = event.getPlayer();
		PlayerSkins s = getPlayerSkins(p);
		if (!s.loaded) {
			return;
		}
		if (b.equals(mojangSkinSign)) {
			int line = event.getLine();
			PlayerSkin skin = s.showingOther ? s.otherMojangSkin : s.mojangSkin;
			if (skin == null) {
				return;
			}
			if (s.showingOther) {
				if (line == 0) { // Use Skin
					setCurrentSkin(p, skin.skin, skin.signature);
				} else if (line == 1) { // Copy Skin
					boolean saved = false;
					int maxSlot = Math.min(getMaxSlot(p), savedSkinSigns.length);
					for (int j = 0; j < maxSlot; j++) {
						if (s.get(j) == null) {
							s.set(j, skin);
							saved = true;
							break;
						}
					}
					if (!saved) {
						p.sendMessage(ChatColor.RED + "Cannot copy: You don't have enough free space in your wardrobe!");
						return;
					}
				} else if (line == 3) { // Report Skin
					p.sendMessage(ChatColor.RED + "This function is not implemented yet.");
					return;
				} else {
					return;
				}
			} else {
				if (line <= 1) {
					setCurrentSkin(p, skin.skin, skin.signature);
					return;
				} else if (line == 3) {
					sendDownloadLink(skin, p);
				}
			}
			s.showingOther = false;
			updateStatuesAndSigns(p);
			return;
		}
		if (b.equals(currentSkinSign)) {
			int line = event.getLine();
			PlayerSkin skin = s.showingOther ? s.otherCustomSkin : s.customSkin;
			if (skin == null) {
				return;
			}
			if (s.showingOther) {
				if (line == 0) { // Use Skin
					setCurrentSkin(p, skin.skin, skin.signature);
				} else if (line == 1) { // Copy Skin
					boolean saved = false;
					for (int j = 0; j < savedSkinSigns.length; j++) {
						if (s.get(j) == null) {
							s.set(j, skin);
							saved = true;
							break;
						}
					}
					if (!saved) {
						p.sendMessage(ChatColor.RED + "Cannot copy: You don't have enough free space in your wardrobe!");
						return;
					}
				} else if (line == 3) { // Report Skin
					p.sendMessage(ChatColor.RED + "This function is not implemented yet.");
					return;
				} else {
					return;
				}
			} else {
				if (line <= 1) {
					setCurrentSkin(p, skin.skin, skin.signature);
					return;
				} else if (line == 3) {
					sendDownloadLink(skin, p);
				}
			}
			s.showingOther = false;
			updateStatuesAndSigns(p);
			return;
		}
		for (int i = 0; i < savedSkinSigns.length; i++) {
			Block block = savedSkinSigns[i];
			if (b.equals(block)) {
				boolean updateStatues = false;
				PlayerSkin skin = s.showingOther ? s.getOther(i) : s.get(i);
				if (s.showingOther) {
					if (skin == null) {
						return;
					} else {
						int line = event.getLine();
						if (line == 0) { // Use Skin
							setCurrentSkin(p, skin.skin, skin.signature);
							updateStatues = true;
						} else if (line == 1) { // Copy Skin
							boolean saved = false;
							for (int j = 0; j < savedSkinSigns.length; j++) {
								if (s.get(j) == null) {
									s.set(j, skin);
									saved = true;
									break;
								}
							}
							if (!saved) {
								p.sendMessage(ChatColor.RED + "Cannot copy: You don't have enough free space in your wardrobe!");
								return;
							}
							updateStatues = true;
						} else if (line == 3) { // Report Skin
							//p.sendMessage(ChatColor.RED + "This function is not implemented yet.");
							return;
						} else {
							return;
						}
						s.showingOther = false;
					}
				} else {
					int maxSlot = getMaxSlot(p);
					int moveIdx = -1;
					PlayerSkin movingSkin = null;
					for (int j = 0; j < s.skinArraySize(); j++) {
						PlayerSkin a = s.get(j);
						if (a == null) {
							continue;
						}
						if (a.move) {
							moveIdx = j;
							movingSkin = a;
							break;
						}
					}
					if (skin == null) {
						if (movingSkin == null) {
							if (i >= getMaxSlot(p)) {
								p.sendMessage(ChatColor.RED + "This is a donator-only slot. Upgrade to use this slot.");
								return;
							} else {
								if (s.mojangSkin == null) {
									p.sendMessage(ChatColor.RED + "Can't save skin: You don't have a skin set!");
									return;
								} else {
									s.set(i, new PlayerSkin(s.mojangSkin.skin, s.mojangSkin.signature));
									updateStatues = true;
								}
							}
						} else {
							if (i >= getMaxSlot(p)) {
								p.sendMessage(ChatColor.RED + "This is a donator-only slot. Upgrade to use this slot.");
							} else {
								s.set(i, movingSkin);
								s.delete(moveIdx);
								movingSkin.move = false;
								updateStatues = true;
							}
						}
					} else {
						if (movingSkin != null && i != moveIdx) {
							if (i >= getMaxSlot(p)) {
								p.sendMessage(ChatColor.RED + "Swapping skins to/from donator-only slots requires a donator rank.");
							} else if (moveIdx >= getMaxSlot(p)) {
								p.sendMessage(ChatColor.RED + "Swapping skins to/from donator-only slots requires a donator rank.");
							} else {
								s.set(i, movingSkin);
								s.set(moveIdx, skin);
								movingSkin.move = false;
								updateStatues = true;
							}
						} else if (skin.move) {
							skin.move = false;
						} else if (skin.hitDelete) {
							int line = event.getLine();
							if (line >= 2) {
								double clickX = event.getClickX();
								if (clickX < 0.5) {
									s.delete(i);
									updateStatues = true;
								} else {
									skin.hitDelete = false;
								}
							}
						} else {
							int line = event.getLine();
							if (line == 0) {
								setCurrentSkin(p, skin.skin, skin.signature);
								updateStatues = true;
							} else if (line == 1) {
								skin.move = true;
							} else if (line == 2) {
								sendDownloadLink(skin, p);
							} else if (line == 3) {
								skin.hitDelete = true;
							}
						}
					}
				}
				updateStatuesAndSigns(p, updateStatues);
				return;
			}
		}
	}

	private void sendDownloadLink(PlayerSkin skin, Player p) {
		try {
			{
				Decoder decoder = Base64.getDecoder();
				byte[] decode = decoder.decode(skin.skin);
				String json = new String(decode);
				JsonObject rootObject = new JsonParser().parse(json).getAsJsonObject();
				String skinUrl = rootObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
				TextComponent txt = new TextComponent("");
				TextComponent clickToDownload = new TextComponent("Download Skin: ");
				clickToDownload.setColor(net.md_5.bungee.api.ChatColor.GOLD);
				TextComponent skinUrlTxt = new TextComponent(skinUrl);
				skinUrlTxt.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, skinUrl));
				skinUrlTxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to download!")}));
				skinUrlTxt.setColor(net.md_5.bungee.api.ChatColor.AQUA);
				txt.addExtra(clickToDownload);
				txt.addExtra(skinUrlTxt);
				p.spigot().sendMessage(txt);
			}
			{
				TextComponent txt = new TextComponent("");
				TextComponent msg = new TextComponent("The link will open in a web browser. Once the skin is loaded in your web browser, you can save the skin.");
				msg.setColor(net.md_5.bungee.api.ChatColor.GOLD);
				txt.addExtra(msg);
				p.spigot().sendMessage(txt);
			}
		} catch (Exception e) {
		}
	}

	public void mySkin(final Player p, String mojangSkin, String mojangSignature, String customSkin, String customSignature) {
		final PlayerSkins s = getPlayerSkins(p);
		s.mojangSkin = new PlayerSkin(mojangSkin, mojangSignature);
		if (mojangSkin == null) {
			p.sendMessage("Please set a skin on Minecraft.net before using the skin changer!");
			plugin.quitToLobby(p);
			return;
		}
		if (customSkin == null) {
			s.customSkin = s.mojangSkin;
		} else {
			s.customSkin = new PlayerSkin(customSkin, customSignature);
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				s.loaded = true;
				updateStatuesAndSigns(p);
			}
		}.runTaskLater(plugin, 60);
	}

	public void otherSkin(Player p, String username, UUID uuid, String mojangSkin, String mojangSkinSignature, String customSkin, String customSkinSignature) {
		PlayerSkins s = getPlayerSkins(p);
		PlayerSkin mojang = mojangSkin == null ? null : new PlayerSkin(mojangSkin, mojangSkinSignature);
		PlayerSkin custom = customSkin == null ? null : new PlayerSkin(customSkin, customSkinSignature);
		s.loadOthers(uuid, username, mojang, custom);
		updateStatuesAndSigns(p);
	}

	public void otherSkinNotFound(Player p) {
		p.sendMessage(ChatColor.RED + "Player not found!");
	}

	public void sendSkinRequest(Player p) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);
			out.writeUTF("GetSkin");
			out.flush();
			byte[] result = baos.toByteArray();
			p.sendPluginMessage(plugin, "BungeeCord", result);
		} catch (Exception e) {
			// this should never happen
		}
	}

	public void sendOtherSkinRequest(Player p, String username) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);
			out.writeUTF("GetOtherSkin");
			out.writeUTF(username);
			out.flush();
			byte[] result = baos.toByteArray();
			p.sendPluginMessage(plugin, "BungeeCord", result);
		} catch (Exception e) {
			// this should never happen
		}
	}

	public void setCurrentSkin(Player p, String skin, String signature) {
		try {
			PlayerSkins s = getPlayerSkins(p);
			s.customSkin = new PlayerSkin(skin, signature);
			updateStatuesAndSigns(p);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);
			out.writeUTF("SetSkin");
			out.writeUTF(skin);
			out.writeUTF(signature);
			out.flush();
			byte[] result = baos.toByteArray();
			p.sendPluginMessage(plugin, "BungeeCord", result);
		} catch (Exception e) {
			// this should never happen
		}
	}

	public void updateStatuesAndSigns(Player p) {
		updateStatuesAndSigns(p, true);
	}

	public void updateStatuesAndSigns(Player p, boolean updateStatues) {
		PlayerSkins s = getPlayerSkins(p);
		if (updateStatues) {
			for (Statue statue : s.shownStatues) {
				statue.delete();
			}
			s.shownStatues.clear();
		}
		PlayerSkin showCustom = s.showingOther ? s.otherCustomSkin : s.customSkin;
		PlayerSkin showMojang = s.showingOther ? s.otherMojangSkin : s.mojangSkin;
		if (showCustom != null) {
			if (updateStatues) {
				Statue statue = new Statue(generateProfile("Current Skin", showCustom.skin, showCustom.signature), currentSkinStatue, p);
				statue.save = false;
				s.shownStatues.add(statue);
				statuesPlugin.add(statue);
			}
			if (s.showingOther) {
				String[] sign = new String[]{"Use Skin", "Save a copy", "--------------", ""};
				p.sendSignChange(currentSkinSign.getLocation(), sign);
			} else {
				String[] sign = new String[]{"Your current", "skin.", "--------------", "Download Skin"};
				p.sendSignChange(currentSkinSign.getLocation(), sign);
			}
		}
		if (showMojang != null) {
			if (updateStatues) {
				Statue statue = new Statue(generateProfile("MC Website", showMojang.skin, showMojang.signature), mojangSkinStatue, p);
				statue.save = false;
				s.shownStatues.add(statue);
				statuesPlugin.add(statue);
			}
			if (s.showingOther) {
				String[] sign = new String[]{"Use Skin", "Save a copy", "--------------", ""};
				p.sendSignChange(mojangSkinSign.getLocation(), sign);
			} else {
				String[] sign = new String[]{"Click to use", "this skin", "--------------", "Download Skin"};
				p.sendSignChange(mojangSkinSign.getLocation(), sign);
			}
		}
		if (updateStatues) {
			for (int i = 0; i < savedSkinStatues.length; i++) {
				PlayerSkin skin = s.showingOther ? s.getOther(i) : s.get(i);
				if (skin == null) {
					continue;
				}
				Statue statue = new Statue(generateProfile("Saved Skin " + Integer.toString(i + 1), skin.skin, skin.signature), savedSkinStatues[i], p);
				statue.save = false;
				s.shownStatues.add(statue);
				statuesPlugin.add(statue);
			}
		}
		int moveIdx = -1;
		PlayerSkin movingSkin = null;
		for (int j = 0; j < s.skinArraySize(); j++) {
			PlayerSkin a = s.get(j);
			if (a == null) {
				continue;
			}
			if (a.move) {
				moveIdx = j;
				movingSkin = a;
				break;
			}
		}
		int maxSlot = getMaxSlot(p);
		for (int i = 0; i < savedSkinSigns.length; i++) {
			PlayerSkin skin = s.showingOther ? s.getOther(i) : s.get(i);
			String[] sign = new String[]{"", "", "", ""};
			if (skin == null) {
				if (i >= maxSlot) {
					sign[1] = "Upgrade to";
					sign[2] = "use this slot";
				} else {
					if (movingSkin != null) {
						sign[1] = "Move here";
					} else {
						sign[0] = "--EMPTY--";
						if (!s.showingOther) {
							sign[1] = "Click to save";
							sign[2] = "MC Website Skin";
						}
					}
				}
			} else {
				if (skin.move) {
					sign[0] = "Now click on";
					sign[1] = "another slot or";
					sign[2] = "click here to";
					sign[3] = "cancel";
				} else if (movingSkin != null) {
					if (i >= maxSlot) {
						sign[1] = "Upgrade to";
						sign[2] = "use this slot";
					} else {
						sign[1] = "Swap with this";
						sign[2] = "skin";
					}
				} else if (skin.hitDelete) {
					sign[0] = "Delete Skin?";
					sign[1] = "";
					sign[2] = "Yes    No";
					sign[3] = "";
				} else {
					sign[0] = "Use Skin";
					sign[1] = "Move";
					sign[2] = "Download Skin";
					if (s.showingOther) {
						sign = new String[]{"Use Skin", "Save a copy", "--------------", ""};
					} else {
						sign[3] = "Delete Skin";
					}
				}
			}
			p.sendSignChange(savedSkinSigns[i].getLocation(), sign);
		}
	}

	public GameProfile generateProfile(String displayName, String skin, String signature) {
		GameProfile gp = new GameProfile(UUID.randomUUID(), displayName);
		PropertyMap properties = gp.getProperties();
		properties.put("textures", new Property("textures", skin, signature));
		return gp;
	}

	public static Block toBlock(World world, String line) {
		try {
			String[] parts = line.split(",");
			return world.getBlockAt(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		} catch (Exception e) {
			return null;
		}
	}

	public static Location toLocation(World world, String line) {
		try {
			String[] parts = line.split(",");
			return new Location(
					world,
					Double.parseDouble(parts[0]),
					Double.parseDouble(parts[1]),
					Double.parseDouble(parts[2]),
					Float.parseFloat(parts[3]),
					Float.parseFloat(parts[4])
			);
		} catch (Exception e) {
			return null;
		}
	}
}
