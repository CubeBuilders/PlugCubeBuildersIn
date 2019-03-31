package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CloseTheEnd implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public CloseTheEnd(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player;
		String senderName = "<CONSOLE>";
		if (sender instanceof Player) {
			player = (Player) sender;
			senderName = player.getName();
			if (!player.hasPermission("hk.siggi.plugcubebuildersin.closetheend")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}
		}
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mv delete theend");
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mvconfirm");
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "factionreset theend");
		return true;
	}
}
