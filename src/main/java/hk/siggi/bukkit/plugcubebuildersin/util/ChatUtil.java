package hk.siggi.bukkit.plugcubebuildersin.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ChatUtil {

	private ChatUtil() {
	}

	public static void link(Player player, String text, String url) {
		TextComponent component = new TextComponent("");
		
		TextComponent prefixPart = new TextComponent("-> ");
		component.addExtra(prefixPart);
		
		TextComponent textPart = new TextComponent(text);
		textPart.setColor(ChatColor.AQUA);
		textPart.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
		component.addExtra(textPart);
		
		player.spigot().sendMessage(component);
	}

	public static void runCommand(Player player, String text, String command) {
		TextComponent component = new TextComponent("");
		
		TextComponent prefixPart = new TextComponent("-> ");
		component.addExtra(prefixPart);
		
		TextComponent textPart = new TextComponent(text);
		textPart.setColor(ChatColor.AQUA);
		textPart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
		component.addExtra(textPart);
		
		player.spigot().sendMessage(component);
	}

	public static void suggestCommand(Player player, String text, String command) {
		TextComponent component = new TextComponent("");
		
		TextComponent prefixPart = new TextComponent("-> ");
		component.addExtra(prefixPart);
		
		TextComponent textPart = new TextComponent(text);
		textPart.setColor(ChatColor.AQUA);
		textPart.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
		component.addExtra(textPart);
		
		player.spigot().sendMessage(component);
	}
}
