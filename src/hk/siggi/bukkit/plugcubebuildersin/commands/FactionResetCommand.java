package hk.siggi.bukkit.plugcubebuildersin.commands;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.zcore.persist.MemoryBoard;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FactionResetCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public FactionResetCommand(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player player;
		String senderName = "<CONSOLE>";
		if (sender instanceof Player) {
			player = (Player) sender;
			senderName = player.getName();
			if (!player.hasPermission("hk.siggi.plugcubebuildersin.factionreset")) {
				player.sendMessage(ChatColor.RED + "You are not allowed to use this command.");
				return true;
			}
		}
		if (split.length != 1) {
			return false;
		}
		HashMap<FLocation, String> flocationIds = null;
		try {
			Field field = MemoryBoard.class.getDeclaredField("flocationIds");
			boolean old = field.isAccessible();
			field.setAccessible(true);
			flocationIds = (HashMap<FLocation, String>) field.get(Board.getInstance());
			field.setAccessible(old);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			plugin.reportProblem("Could not get flocationIds from Factions plugin", e);
		}
		if (flocationIds == null) {
			sender.sendMessage(ChatColor.RED + "Error: Could not tell Factions plugin to declaim all land in World " + split[0] + ".");
			return true;
		}
		ArrayList<FLocation> toDeclaim = new ArrayList<>();
		Iterator<Map.Entry<FLocation, String>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<FLocation, String> entry = iter.next();
			FLocation fLocation = entry.getKey();
			if (fLocation.getWorldName().equalsIgnoreCase(split[0])) {
				toDeclaim.add(fLocation);
			}
		}
		int declaimCount = toDeclaim.size();
		FLocation[] declaim = toDeclaim.toArray(new FLocation[declaimCount]);
		for (int i = 0; i < declaimCount; i++) {
			Board.getInstance().removeAt(declaim[i]);
		}
		sender.sendMessage(ChatColor.GREEN + "Unclaimed " + declaimCount + " land at " + split[0] + ".");
		return true;
	}
}
