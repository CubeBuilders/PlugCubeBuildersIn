package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import static hk.siggi.bukkit.plugcubebuildersin.util.ChatUtil.link;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DonateCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public DonateCommand(PlugCubeBuildersIn plugin) {
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
		player.sendMessage(ChatColor.GOLD + "Click on the following link to donate or buy items:");
		link(player, "https://cubebuilders.net/store ", "https://cubebuilders.net/store");
		return true;
	}
}
