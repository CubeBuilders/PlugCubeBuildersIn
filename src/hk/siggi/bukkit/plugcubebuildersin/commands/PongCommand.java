package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.nms.NMSUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PongCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public PongCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.GOLD + "You're not a player.");
			return true;
		}
		Player player = (Player) sender;
		player.sendMessage(ChatColor.GOLD + "Ping!  " + NMSUtil.get().getPing(player) + " ms");
		return true;
	}
}
