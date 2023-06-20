package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvPCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;
	private final String serverKind;

	public PvPCommand(PlugCubeBuildersIn plugin, String serverKind) {
		this.plugin = plugin;
		this.serverKind = serverKind;
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
		if (!serverKind.startsWith("factions")) {
			player.sendMessage(ChatColor.RED + "This command only works on a Factions server.");
			return true;
		}
		int time = (int) (plugin.pvpTimer / 1000);
		player.sendMessage(ChatColor.GREEN + "You are now vulnerable to PvP for " + time + " second" + (time == 1 ? "" : "s") + ".");
		player.sendMessage(ChatColor.GREEN + "Both players must type /pvp in order to start a fight in a protected area.");
		plugin.enteredPvP(player);
		return true;
	}
}
