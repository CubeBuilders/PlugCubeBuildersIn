package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlayerSession;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignEditCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public SignEditCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player) sender;
		if (!player.hasPermission("hk.siggi.plugcubebuildersin.signedit")) {
			player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
			return true;
		}
		PlayerSession session = plugin.getSession(player);
		if (split.length > 0) {
			if (session.signEditBlock != null) {
				int lineNo = Integer.parseInt(split[0]) - 1;
				String msg = "";
				for (int i = 1; i < split.length; i++) {
					msg += (i == 1 ? "" : " ") + split[i];
				}
				BlockState state = session.signEditBlock.getState();
				if (state instanceof Sign) {
					Sign sign = (Sign) state;
					sign.setLine(lineNo, msg);
					sign.update();
				}
			}
		} else {
			session.signEdit = true;
			player.sendMessage(ChatColor.GREEN + "Now click on a sign.");
		}
		return true;
	}
}
