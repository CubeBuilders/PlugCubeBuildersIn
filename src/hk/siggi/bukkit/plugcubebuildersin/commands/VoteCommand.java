package hk.siggi.bukkit.plugcubebuildersin.commands;

import com.bobacadodl.JSONChatLib.JSONChatClickEventType;
import com.bobacadodl.JSONChatLib.JSONChatColor;
import com.bobacadodl.JSONChatLib.JSONChatExtra;
import com.bobacadodl.JSONChatLib.JSONChatFormat;
import com.bobacadodl.JSONChatLib.JSONChatMessage;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public VoteCommand(PlugCubeBuildersIn plugin) {
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
		player.sendMessage(ChatColor.GOLD + "Voting has been removed, as it's a waste of everyone's time. We've decided to give free CubeTokens for playing on CubeBuilders and inviting friends instead.");
		return true;
	}

	public void link(Player player, String text, String url) {
		JSONChatMessage message = new JSONChatMessage("-> ", JSONChatColor.WHITE, new ArrayList<JSONChatFormat>());
		JSONChatExtra extra = new JSONChatExtra(text, JSONChatColor.AQUA, new ArrayList<JSONChatFormat>());
		extra.setClickEvent(JSONChatClickEventType.OPEN_URL, url);
		message.addExtra(extra);
		message.sendToPlayer(player);
	}
}
