package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TellAllRawCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public TellAllRawCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player;
		String playerName = "<CONSOLE>";

		String message = "";
		if (split.length >= 1) {
			message = split[0];
			for (int i = 1; i < split.length; i++) {
				message += " " + split[i];
			}
		}
		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("hk.siggi.plugcubebuildersin.tellallraw")) {
				player.sendMessage(ChatColor.RED + "You don't have permission to do this!");
				return true;
			}
		} else {
		}
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "tellraw " + p.getName() + " " + message);
		}
		return true;
	}
}
