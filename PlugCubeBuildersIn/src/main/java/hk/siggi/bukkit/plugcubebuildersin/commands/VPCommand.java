package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlayerSession;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VPCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public VPCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
			return true;
		}
		Player p = (Player) sender;
		PlayerSession session = plugin.getSession(p);
		session.vanishProtection=!session.vanishProtection;
		if (session.vanishProtection) {
			p.sendMessage(ChatColor.GOLD + "Vanish protection on.");
		} else {
			p.sendMessage(ChatColor.GOLD + "Vanish protection off.");
		}
		return true;
	}

}
