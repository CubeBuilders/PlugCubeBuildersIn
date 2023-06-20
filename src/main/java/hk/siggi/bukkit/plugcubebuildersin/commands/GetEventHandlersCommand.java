package hk.siggi.bukkit.plugcubebuildersin.commands;

import java.lang.reflect.Field;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

public class GetEventHandlersCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		try {
			String eventName = args[0];
			Class<Event> eventClass = (Class<Event>) Class.forName(eventName);
			Field declaredField = eventClass.getDeclaredField("handlers");
			declaredField.setAccessible(true);
			HandlerList list =  (HandlerList) declaredField.get(null);
			RegisteredListener[] registeredListeners = list.getRegisteredListeners();
			for (RegisteredListener listener : registeredListeners){
				sender.sendMessage(listener.getPlugin().getName());
			}
		} catch (Exception ex) {ex.printStackTrace();
		}
		return true;
	}

}
