package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class DebugCommand implements CommandExecutor, TabExecutor {

	private final PlugCubeBuildersIn plugin;

	public DebugCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player p = (Player) sender;
		switch (args[0]) {
			case "canisee": {
			Player otherPlayer = Bukkit.getPlayer(args[1]);
			p.sendMessage("Can see: " + (p.canSee(otherPlayer) ? "yes":"no"));
			}
			break;
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return new ArrayList<>();
	}

}
