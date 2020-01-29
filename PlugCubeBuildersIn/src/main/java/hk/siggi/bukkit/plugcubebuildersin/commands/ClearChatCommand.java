package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.Collection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearChatCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public ClearChatCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (!player.hasPermission("hk.siggi.plugcubebuildersin.clearchat")) {
				sender.sendMessage(ChatColor.RED + "This command is only available to staff members.");
				return true;
			}
		}
		Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
		for (Player pp : onlinePlayers) {
			if (pp.hasPermission("hk.siggi.plugcubebuildersin.staffchat")) {
				pp.sendMessage(ChatColor.GREEN + (player == null ? "<CONSOLE>" : player.getName()) + " has cleared the chat.");
			} else {
				for (int j = 0; j < 100; j++) {
					pp.sendMessage("");
				}
			}
		}
		for (int i = 0; i < 100; i++) {
			plugin.sharedChat("", "NotStaffChat");
		}
		plugin.sharedChat(ChatColor.GREEN + (player == null ? "<CONSOLE>" : player.getName()) + " has cleared the chat.", "StaffChat");
		return true;
	}
}
