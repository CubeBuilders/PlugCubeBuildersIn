package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import io.siggi.cubecore.nms.NMSUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RenderDistanceCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public RenderDistanceCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (!player.hasPermission("hk.siggi.plugcubebuildersin.renderdistance")) {
				sender.sendMessage(ChatColor.RED + "This command is only available to staff members.");
				return true;
			}
		}
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "This command must be used in-game.");
			return true;
		}
		NMSUtil.get().setRenderDistance(player.getWorld(), Integer.parseInt(split[0]));
		return true;
	}
}
