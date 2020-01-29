package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlayerSession;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SpeedUpCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public SpeedUpCommand(PlugCubeBuildersIn plugin) {
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
		if (!player.hasPermission("hk.siggi.plugcubebuildersin.speedup")) {
			sender.sendMessage("You can't do this!");
			return true;
		}
		
		double multiplier = Double.parseDouble(split[0]);
		PlayerSession session = plugin.getSession(player);
		session.speedUpMultiplier = multiplier;
		
		Entity vehicle = player.getVehicle();
		if (vehicle != null) {
			vehicle.setVelocity(vehicle.getVelocity().multiply(multiplier));
		} else {
			player.setVelocity(player.getVelocity().multiply(multiplier));
		}
		return true;
	}
}
