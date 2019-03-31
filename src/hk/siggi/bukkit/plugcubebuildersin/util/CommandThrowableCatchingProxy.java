package hk.siggi.bukkit.plugcubebuildersin.util;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public class CommandThrowableCatchingProxy {

	private final PlugCubeBuildersIn plugin;
	private final PluginCommand command;
	private final CommandExecutor executor;
	private final TabCompleter tabCompleter;

	public static void setup(PlugCubeBuildersIn plugin, PluginCommand command) {
		CommandThrowableCatchingProxy c = new CommandThrowableCatchingProxy(plugin, command);
		c.setup();
	}

	public CommandThrowableCatchingProxy(PlugCubeBuildersIn plugin, PluginCommand command) {
		this.plugin = plugin;
		this.command = command;
		String test = command.getName();
		executor = command.getExecutor();
		tabCompleter = command.getTabCompleter();
	}

	private void setup() {
		if (executor != null && !(executor instanceof CommandThrowableCatchingProxy)) {
			command.setExecutor(new ExecutorProxy());
		}
		if (tabCompleter != null && !(tabCompleter instanceof CommandThrowableCatchingProxy)) {
			command.setTabCompleter(new TabCompleterProxy());
		}
	}

	public class ExecutorProxy implements CommandExecutor {
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			try {
				return executor.onCommand(sender, command, label, args);
			} catch (RuntimeException | Error e) {
				plugin.reportProblem("Error handling command " + command.getName(), e);
				throw e;
			}
		}
	}

	public class TabCompleterProxy implements TabCompleter {
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
			try {
				return tabCompleter.onTabComplete(sender, command, alias, args);
			} catch (RuntimeException | Error e) {
				plugin.reportProblem("Error handling tab completer for command " + command.getName(), e);
				throw e;
			}
		}
	}
}
