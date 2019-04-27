package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.module.BankModule;
import hk.siggi.bukkit.plugcubebuildersin.module.HighscoresModule;
import hk.siggi.bukkit.plugcubebuildersin.module.HighscoresModule.Scoreboard;
import static hk.siggi.bukkit.plugcubebuildersin.util.ChatUtil.runCommand;
import static hk.siggi.bukkit.plugcubebuildersin.util.ChatUtil.suggestCommand;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public BountyCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage("This command can only be run by in-game players.");
			return true;
		}
		if (!plugin.bountyIsEnabled()) {
			sender.sendMessage(ChatColor.RED + "Bounty is disabled on this server.");
			return true;
		}
		Scoreboard bountyScoreboard = plugin.getModule(HighscoresModule.class).getScoreboard("bounty");
		int currentPage = 1;
		if (split.length >= 1) {
			if (split[0].equalsIgnoreCase("add")) {
				try {
					String name = split[1];
					String amountStr = split[2];
					if (amountStr.contains(".")) {
						player.sendMessage(ChatColor.RED + "The amount must be an integer amount! You cannot include cents!");
						return true;
					}
					if (amountStr.startsWith("$")) {
						amountStr = amountStr.substring(1);
					}
					long amount = Long.parseLong(amountStr);
					if (amount < 0) {
						player.sendMessage(ChatColor.RED + "The amount must be positive!");
						return true;
					}
					if (amount < 100) {
						player.sendMessage(ChatColor.RED + "The minimum bounty you can place on someone's head is $100.");
						return true;
					}
					BankModule money = plugin.getBankModule();
					if (money.getBalance(player) >= amount) {
						name = plugin.autoCompleteName(name);
						if (name.equalsIgnoreCase("CubeBuildersGirl")) {
							String cbgStrings[] = new String[]{
								"Why you wanna do that? CubeBuildersGirl is sad! :(",
								"That's not nice! CubeBuildersGirl is sad! :(",
								"You make CubeBuildersGirl wanna cry! :'(",
								"You're not nice to CubeBuildersGirl! </3 :(",
								"What did CubeBuildersGirl ever do to you? :o"
							};
							player.sendMessage(ChatColor.RED + cbgStrings[(int) Math.floor(Math.random() * cbgStrings.length)]);
							return true;
						}
						UUID targetPlayer = plugin.getUUIDCache().getUUIDFromName(name);
						if (targetPlayer == null) {
							player.sendMessage(ChatColor.RED + "Cannot place bounty: Player not found!");
						} else if (money.chargeMoney(player, amount)) {
							name = plugin.getUUIDCache().getNameFromUUID(targetPlayer);
							long newBounty = bountyScoreboard.incrementScore(targetPlayer, amount);
							player.sendMessage(ChatColor.GREEN + "You've placed $" + amount + " bounty on " + name + ".");
							player.sendMessage(ChatColor.GREEN + name + "'s new bounty is $" + newBounty + "!");
						} else {
							player.sendMessage(ChatColor.RED + "You don't have enough money to place that much bounty!");
						}
					} else {
						player.sendMessage(ChatColor.RED + "You don't have enough money to place that much bounty!");
					}
				} catch (Exception e) {
					plugin.reportProblem("Could not add bounty", e);
					player.sendMessage(ChatColor.RED + "Usage: /bounty add [name] [amount]");
				}
				return true;
			} else if (split[0].equalsIgnoreCase("clean")) {
				if (!player.hasPermission("hk.siggi.plugcubebuildersin.bounty.clean")) {
					player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					return true;
				}
				try {
					String name = split[1];
					name = plugin.autoCompleteName(name);
					UUID targetPlayer = plugin.getUUIDCache().getUUIDFromName(name);
					if (targetPlayer == null) {
						player.sendMessage(ChatColor.RED + "Cannot clear bounty: Player not found!");
						return true;
					}
					name = plugin.getUUIDCache().getNameFromUUID(targetPlayer);
					bountyScoreboard.setScore(targetPlayer, 0L);
					player.sendMessage(ChatColor.GREEN + name + "'s bounty has been reset to $0.");
				} catch (Exception e) {
					plugin.reportProblem("Could not clean bounty", e);
					player.sendMessage(ChatColor.RED + "Usage: /bounty clean [name]");
				}
				return true;
			}
			try {
				currentPage = Integer.parseInt(split[0]);
			} catch (NumberFormatException e) {
				String name = plugin.autoCompleteName(split[0]);
				UUID targetPlayer = plugin.getUUIDCache().getUUIDFromName(name);
				if (targetPlayer != null) {
					name = plugin.getUUIDCache().getNameFromUUID(targetPlayer);
					long bounty = bountyScoreboard.getScore(targetPlayer);
					player.sendMessage(ChatColor.GREEN + name + "'s Bounty: $" + bounty);
					suggestCommand(player, "Add more bounty: /bounty add " + name + " [amount]", "/bounty add " + name + " ");
					return true;
				}
			}
		}
		int perPage = 8;
		int pageCount;
		List<? extends Scoreboard.Score> scores = bountyScoreboard.getScores();
		int mostWantedCount = scores.size();
		pageCount = (mostWantedCount + perPage) / perPage;
		player.sendMessage(ChatColor.GOLD + "--- CubeBuilders Most Wanted (" + currentPage + "/" + pageCount + ") ---");
		if (currentPage == 1) {
			long myBounty = bountyScoreboard.getScore(player.getUniqueId());
			player.sendMessage(ChatColor.GREEN + "Your Bounty: $" + myBounty);
		}
		int offset = (currentPage - 1) * perPage;
		for (int i = 0; i < perPage; i++) {
			int index = i + offset;
			int index_humanReadable = index + 1;
			if (scores.size() <= index) {
				break;
			}
			Scoreboard.Score score = scores.get(index);
			player.sendMessage(ChatColor.GREEN + "" + index_humanReadable + ". " + plugin.getUUIDCache().getNameFromUUID(score.getPlayer()) + ": $" + score.getScore());
		}
		if (currentPage == 1) {
			suggestCommand(player, "Place Bounty on someone - /bounty add [name] [amount]", "/bounty add ");
		}
		if (currentPage < pageCount) {
			runCommand(player, "Next page - /bounty " + (currentPage + 1), "/bounty " + (currentPage + 1));
		}
		return true;
	}
}
