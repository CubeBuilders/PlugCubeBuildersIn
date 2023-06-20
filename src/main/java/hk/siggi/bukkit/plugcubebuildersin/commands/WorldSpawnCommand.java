package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldSpawnCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public WorldSpawnCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player;
		String senderName = "<CONSOLE>";
		if (sender instanceof Player) {
			player = (Player) sender;
			senderName = player.getName();
			if (!player.hasPermission("hk.siggi.plugcubebuildersin.worldspawn")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}
		} else {
			sender.sendMessage("This command can only be used in-game.");
			return true;
		}
		Location location = player.getLocation();
		player.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		return true;
	}
}
