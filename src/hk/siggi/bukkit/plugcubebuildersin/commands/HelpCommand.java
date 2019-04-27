package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import static hk.siggi.bukkit.plugcubebuildersin.util.ChatUtil.link;
import static hk.siggi.bukkit.plugcubebuildersin.util.ChatUtil.runCommand;
import static hk.siggi.bukkit.plugcubebuildersin.util.ChatUtil.suggestCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;
	private final String serverKind;
	private final org.bukkit.command.defaults.HelpCommand helpCommand;

	public HelpCommand(PlugCubeBuildersIn plugin, String serverKind) {
		this.plugin = plugin;
		this.serverKind = serverKind;
		helpCommand = new org.bukkit.command.defaults.HelpCommand();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			helpCommand.execute(sender, label, split);
			return true;
		}
		if (split.length >= 1) {
			if (split[0].equalsIgnoreCase("--")) {
				if (split.length == 1) {
					Bukkit.dispatchCommand(sender, "help -- pg 1");
					return true;
				}
				if (split[1].equalsIgnoreCase("factionsmenu")) {
					player.sendMessage("");
					int currentPage = 1;
					try {
						currentPage = Integer.parseInt(split[2]);
					} catch (Exception e) {
					}
					if (currentPage == 1) {
						player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "-----How to use Factions (Survival Mode) [1/4]-----");
						player.sendMessage(ChatColor.GREEN + "To create a Faction, use this command:");
						suggestCommand(player, "/f create [factionname] " + ChatColor.GREEN + "(Or click and type faction name)", "/f create ");
						player.sendMessage(ChatColor.GREEN + "To set a description for your Faction, use this command:");
						suggestCommand(player, "/f desc [description] " + ChatColor.GREEN + "(Or click and type description)", "/f desc ");
						player.sendMessage(ChatColor.GREEN + "To claim land to protect it, use this command:");
						runCommand(player, "/f claim " + ChatColor.GREEN + "(Or click here the land you're standing on now)", "/f claim");
						player.sendMessage(ChatColor.GREEN + "To invite someone, use this command:");
						suggestCommand(player, "/f invite [playername] " + ChatColor.GREEN + "(Or click and type player's name)", "/f invite ");
						player.sendMessage(ChatColor.GREEN + "To join a faction you were invited to:");
						suggestCommand(player, "/f join [factionname] " + ChatColor.GREEN + "(Or click and type faction's name)", "/f join ");
						runCommand(player, "Next page", "/help -- factionsmenu 2");
					} else if (currentPage == 2) {
						player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "-----How to use Factions (Survival Mode) [2/4]-----");
						player.sendMessage("To see how much power you have, use this command:");
						runCommand(player, "/f power " + ChatColor.GREEN + "(Or click here)", "/f power");
						player.sendMessage("To see the details about your faction, use this command:");
						runCommand(player, "/f show " + ChatColor.GREEN + "(Or click here)", "/f show");
						player.sendMessage("To see the details about another faction, use this command:");
						suggestCommand(player, "/f show [factionname] " + ChatColor.GREEN + "(Or click and type faction's name)", "/f show ");
						player.sendMessage("To see the territory map around you, use this command:");
						runCommand(player, "/f map " + ChatColor.GREEN + "(Or click here)", "/f map");
						runCommand(player, "Previous page", "/help -- factionsmenu 1");
						runCommand(player, "Next page", "/help -- factionsmenu 3");
					} else if (currentPage == 3) {
						player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "-----How to use Factions (Survival Mode) [3/4]-----");
						player.sendMessage(ChatColor.GREEN + "The more power you have, the more land you can hold.");
						player.sendMessage(ChatColor.GREEN + "The power of all faction members adds up in your faction.");
						player.sendMessage(ChatColor.GREEN + "You slowly gain power throughout time by playing on CubeBuilders.");
						player.sendMessage(ChatColor.GREEN + "You lose power when you die.");
						player.sendMessage(ChatColor.AQUA + "If you trust someone, give them Moderator status in your faction.");
						player.sendMessage(ChatColor.GREEN + "It will allow them to claim land as well, use this command:");
						suggestCommand(player, "/f mod [playername] " + ChatColor.GREEN + "(Or click and type player's name)", "/f mod ");
						runCommand(player, "Previous page", "/help -- factionsmenu 2");
						runCommand(player, "Next page", "/help -- factionsmenu 4");
					} else if (currentPage == 4) {
						player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "-----How to use Factions (Survival Mode) [4/4]-----");
						player.sendMessage(ChatColor.GREEN + "If someone is being a nuisance, kick them out of your faction.");
						player.sendMessage(ChatColor.GREEN + "Use this command:");
						suggestCommand(player, "/f kick [playername] " + ChatColor.GREEN + "(Or click and type player's name)", "/f kick ");
						runCommand(player, "Previous page", "/help -- factionsmenu 3");
					}
					runCommand(player, ChatColor.YELLOW + "Open Advanced Help Menu", "/help -- factions 1");
					runCommand(player, "Return to Previous Menu", "/help");
				} else if (split[1].equalsIgnoreCase("factions")) {
					player.sendMessage("");
					int currentPage = 1;
					try {
						currentPage = Integer.parseInt(split[2]);
					} catch (Exception e) {
					}
					Bukkit.dispatchCommand(sender, "f help " + currentPage);
					if (currentPage > 1) {
						runCommand(player, "Previous Page", "/help -- factions " + (currentPage - 1));
					}
					runCommand(player, "Next Page", "/help -- factions " + (currentPage + 1));
					runCommand(player, "Return to Previous Menu", "/help");
				} else if (split[1].equalsIgnoreCase("rules")) {
					player.sendMessage("");
					int currentPage = 1;
					try {
						currentPage = Integer.parseInt(split[2]);
					} catch (Exception e) {
					}
					Bukkit.dispatchCommand(sender, "rules " + currentPage);
					if (currentPage > 1) {
						runCommand(player, "Previous Page", "/help -- rules " + (currentPage - 1));
					}
					runCommand(player, "Next Page", "/help -- rules " + (currentPage + 1));
					runCommand(player, "Return to Previous Menu", "/help");
				} else if (split[1].equalsIgnoreCase("pg")) {
					player.sendMessage("");
					int currentPage = 1;
					try {
						currentPage = Integer.parseInt(split[2]);
					} catch (Exception e) {
					}
					helpCommand.execute(sender, label, new String[]{Integer.toString(currentPage)});
					if (currentPage > 1) {
						runCommand(player, "Previous Page", "/help -- pg " + (currentPage - 1));
					}
					runCommand(player, "Next Page", "/help -- pg " + (currentPage + 1));
					suggestCommand(player, "Jump To Page (click then type page number)", "/help -- pg ");
					runCommand(player, "Return to Previous Menu", "/help");
				} else if (split[1].equalsIgnoreCase("prpg")) {
					player.sendMessage("");
					int currentPage = 1;
					try {
						currentPage = Integer.parseInt(split[2]);
					} catch (Exception e) {
					}
					Bukkit.dispatchCommand(sender, "pr pg " + currentPage);
					if (currentPage > 1) {
						runCommand(player, "Previous Page", "/help -- prpg " + (currentPage - 1));
					}
					runCommand(player, "Next Page", "/help -- prpg " + (currentPage + 1));
					runCommand(player, "Return to Previous Menu", "/help");
				} else if (split[1].equalsIgnoreCase("signingup")) {
					player.sendMessage("");
					player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "-----How To Become A Member-----");
					player.sendMessage(ChatColor.GOLD + "To become a member, simply visit the CubeBuilders Website and register!");
					player.sendMessage(ChatColor.GOLD + "Click below to open the forums!");
					link(player, ChatColor.YELLOW + "Visit the CubeBuilders Website", "https://cubebuilders.net/");
					runCommand(player, "Return to Previous Menu", "/help");
				} else if (split[1].equalsIgnoreCase("applystaff")) {
					player.sendMessage("");
					player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "-----Apply for a Staff Position-----");
					player.sendMessage(ChatColor.GOLD + "CubeBuilders does need staff members!");
					player.sendMessage(ChatColor.GOLD + "Click the apply button below to read the staff application requirements and apply!");
					link(player, ChatColor.YELLOW + "Click here to apply now!", "https://cubebuilders.net/community/forums/show/8.html");
					runCommand(player, "Return to Previous Menu", "/help");
				} else if (split[1].equalsIgnoreCase("reloaduuid")) {
					if (player.hasPermission("hk.siggi.plugcubebuildersin.reloaduuid")) {
						player.sendMessage("Reloading UUIDs");
						plugin.getUUIDCache().reloadData();
					}
				}
			} else {
				helpCommand.execute(sender, label, split);
			}
		} else {
			player.sendMessage("");
			player.sendMessage("" + ChatColor.AQUA + ChatColor.BOLD + "-----CubeBuilders Menu-----");
			player.sendMessage(ChatColor.GOLD + "Click on a link below: (Yes! You can click on them!)");
			link(player, ChatColor.GREEN + "Visit our Website! https://cubebuilders.net/", "https://cubebuilders.net/");
			link(player, "Like us on Facebook! :) FB/CubeBuilders", "https://facebook.com/CubeBuilders");
			link(player, "Follow us on Instagram! :) IG/cube.builders", "https://instagram.com/cube.builders");
			link(player, "Join our Discord Server!", "https://discord.gg/xPKHpyQ");
			link(player, ChatColor.GREEN + "Visit the CubeBuilders Store for items and upgrades", "https://cubebuilders.net/store");
			runCommand(player, ChatColor.GREEN + "CubeTokens Store (/cubetokens)", "/cubetokens");
			runCommand(player, ChatColor.YELLOW + "Read the Rules of CubeBuilders!", "/help -- rules 1");
			player.sendMessage(ChatColor.GOLD + "Click on a link above! (Yes! You can click on them!)");
			if (serverKind != null && !serverKind.startsWith("hub")) {
				runCommand(player, ChatColor.YELLOW + "Return to Lobby (/lobby)", "/lobby");
			}
			if (serverKind != null && serverKind.startsWith("hub")) {
				player.sendMessage("" + ChatColor.AQUA + "-------------------------------------");
				player.sendMessage("" + ChatColor.GOLD + "Welcome to CubeBuilders!");
				player.sendMessage("" + ChatColor.GOLD + "Enter one of the portals to get started!");
			}
			if (serverKind != null && serverKind.startsWith("minigames")) {
				player.sendMessage("" + ChatColor.AQUA + "-------------------------------------");
				player.sendMessage("" + ChatColor.GOLD + "Enter a portal to join a minigame!");
			}
		}
		return true;
	}
}
