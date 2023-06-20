package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SnapToGrid implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public SnapToGrid(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player;
		String senderName = "<CONSOLE>";
		if (sender instanceof Player) {
			player = (Player) sender;
			senderName = player.getName();
			if (!player.hasPermission("hk.siggi.plugcubebuildersin.snaptogrid")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}
		} else {
			sender.sendMessage("This command can only be used in-game.");
			return true;
		}
		Location location = player.getLocation();
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float yaw = location.getYaw();
		x = Math.round(x * 2.0) / 2.0;
		y = Math.round(y * 2.0) / 2.0;
		z = Math.round(z * 2.0) / 2.0;
		yaw = (Math.round(yaw / 45.0f) * 45.0f) % 360.0f;
		Location newLocation = new Location(location.getWorld(), x, y, z, yaw, 0.0f);
		player.teleport(newLocation);
		return true;
	}
}
