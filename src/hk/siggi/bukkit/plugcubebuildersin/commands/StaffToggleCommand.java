package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlayerSession;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffToggleCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public StaffToggleCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used by in-game players.");
			return true;
		}
		Player p = (Player) sender;
		PlayerSession session = plugin.getSession(p);
		if (!session.canUseStaffToggle()) {
			p.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
			return true;
		}
		if (!plugin.staffPrivilegeToggle) {
			p.sendMessage(ChatColor.RED + "Staff Toggle is not available here.");
			return true;
		}
		if (plugin.isVanished(p) && session.getStaffPerms()) {
			p.sendMessage(ChatColor.RED + "You are forced to use Staff Mode while vanished!");
			return true;
		}
		boolean staffPerms = !session.getStaffPerms();
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("on")
					|| args[0].equalsIgnoreCase("1")
					|| args[0].equalsIgnoreCase("yes")
					|| args[0].equalsIgnoreCase("y")) {
				staffPerms = true;
			} else if (args[0].equalsIgnoreCase("off")
					|| args[0].equalsIgnoreCase("0")
					|| args[0].equalsIgnoreCase("no")
					|| args[0].equalsIgnoreCase("n")) {
				staffPerms = false;
			}
		}
		session.setStaffPerms(staffPerms);
		if (staffPerms) {
			p.sendMessage(ChatColor.GREEN + "Switched to Staff Mode.");
		} else {
			p.sendMessage(ChatColor.GREEN + "Switched to Player Mode.");
		}
		return true;
	}

}
