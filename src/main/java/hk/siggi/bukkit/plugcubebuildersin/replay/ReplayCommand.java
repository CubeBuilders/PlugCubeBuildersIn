package hk.siggi.bukkit.plugcubebuildersin.replay;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.Action;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ArmswingAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.BlockChangeAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.ChatAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.LogAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.LoginAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.LogoutAction;
import hk.siggi.bukkit.plugcubebuildersin.replay.action.MoveAction;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ReplayCommand implements CommandExecutor, TabCompleter {

	private final PlugCubeBuildersIn plugin;
	private final ActionReplayImpl replay;

	public ReplayCommand(PlugCubeBuildersIn plugin, ActionReplayImpl replay) {
		this.plugin = plugin;
		this.replay = replay;
		setupCommands();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
			return true;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			args = new String[]{"help"};
		}
		String c = args[0];
		String[] subArgs = new String[args.length - 1];
		if (subArgs.length > 0) {
			System.arraycopy(args, 1, subArgs, 0, subArgs.length);
		}
		ARCommand cm = commands.get(c.toLowerCase());
		if (cm == null) {
			p.sendMessage(ChatColor.RED + "Unknown command " + ChatColor.GOLD + c + ChatColor.RED + ".");
			return true;
		}
		cm.execute(p, subArgs);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			List<String> result = new ArrayList<>();
			return result;
		}
		Player p = (Player) sender;
		if (args.length == 0) {

		} else if (args.length == 1) {
			List<String> result = new ArrayList<>();
			for (String cc : commands.keySet()) {
				if (cc.startsWith(args[0].toLowerCase())) {
					result.add(cc);
				}
			}
			return result;
		} else if (args.length >= 2) {
			String c = args[0];
			String[] subArgs = new String[args.length - 1];
			System.arraycopy(args, 1, subArgs, 0, subArgs.length);
			ARCommand cm = commands.get(c.toLowerCase());
			if (cm == null) {
				List<String> result = new ArrayList<>();
				return result;
			}
			return cm.autoComplete(p, subArgs);
		}
		List<String> result = new ArrayList<>();
		return result;
	}

	private ReplaySession getSession(Player p) {
		return replay.getSession(p);
	}

	private final Map<String, ARCommand> commands = new HashMap<>();

	private void setupCommands() {
		commands.put("load", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				if (!checkPermission(p)) {
					p.sendMessage(ChatColor.RED + "You don't have permission to do this.");
					return;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
				int currentYear = Integer.parseInt(sdf.format(new Date(System.currentTimeMillis())));
				if (args.length == 0) {
					p.sendMessage(ChatColor.GOLD + "Usage:" + ChatColor.YELLOW + "/replay load YYYY/MM/DD-HH:MM:SS-TimeZone");
					p.sendMessage(ChatColor.GOLD + "Omitting year will assume " + ChatColor.YELLOW + currentYear + ChatColor.GOLD + ", omitting timezone will assume " + ChatColor.YELLOW + "GMT" + ChatColor.GOLD + ".");
					p.sendMessage(ChatColor.YELLOW + "Important: GMT does not adjust with daylight saving time. American Eastern Time is GMT-5 in Winter, and GMT-4 in Summer, it's NOT always GMT-5.");
					p.sendMessage(ChatColor.GOLD + "Usage:" + ChatColor.YELLOW + "/replay load -DD:HH:MM:SS");
					p.sendMessage(ChatColor.GOLD + "Backtrack from the current time a certain number of hours, minutes, seconds. You may omit " + ChatColor.YELLOW + "<days>" + ChatColor.GOLD + ", " + ChatColor.YELLOW + "<days and hours>" + ChatColor.GOLD + ", or " + ChatColor.YELLOW + "<days, hours and minutes>" + ChatColor.GOLD + ".");
					return;
				} else {
					long requestedTime = Util.parseTime(args[0], System.currentTimeMillis());
					if (requestedTime == -1L) {
						p.sendMessage(ChatColor.RED + "Could not parse timezone.");
						return;
					}
					ReplaySession session = getSession(p);
					if (session != null) {
						session.removeVisibleTo(p);
					}
					session = new ReplaySession(replay.getRecorder(), requestedTime);
					session.starter = p;
					session.addVisibleTo(p);
					session.seek(requestedTime);
					replay.addSession(session);
					session.sendMessage(ChatColor.GOLD + "Initializing Replay at: " + ChatColor.YELLOW + Util.unparseTime(requestedTime));
					session.sendMessage(ChatColor.GOLD + "Play with " + ChatColor.YELLOW + "/replay play");
					session.sendMessage(ChatColor.GOLD + "Reverse with " + ChatColor.YELLOW + "/replay reverse");
					session.sendMessage(ChatColor.GOLD + "Stop with " + ChatColor.YELLOW + "/replay stop");
					session.sendMessage(ChatColor.GOLD + "Quit with " + ChatColor.YELLOW + "/replay quit");
					session.sendMessage(ChatColor.GOLD + "Teleport to a player with " + ChatColor.YELLOW + "/replay tp [name]");
					session.sendMessage(ChatColor.GOLD + "Add another staff member to watch the same session with you, with " + ChatColor.YELLOW + "/replay add [name]");
				}
			}
		});
		commands.put("play", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					if (args.length > 0) {
						double speed = Double.parseDouble(args[0]);
						if (speed <= 0.0) {
							p.sendMessage(ChatColor.RED + "Speed " + speed + " is too slow.");
						} else if (speed > 100.0) {
							p.sendMessage(ChatColor.RED + "Speed " + speed + " is too fast.");
						} else {
							session.play(speed);
							session.sendMessage(ChatColor.GOLD + p.getName() + ": Playing replay at " + speed + "x speed.");
						}
					} else {
						session.play();
						session.sendMessage(ChatColor.GOLD + p.getName() + ": Playing replay.");
					}
				}
			}
		});
		commands.put("stop", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					session.stop();
					session.sendMessage(ChatColor.GOLD + p.getName() + ": Stopping replay.");
				}
			}
		});
		commands.put("reverse", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					if (args.length > 0) {
						double speed = Double.parseDouble(args[0]);
						if (speed <= 0.0) {
							p.sendMessage(ChatColor.RED + "Speed " + speed + " is too slow.");
						} else if (speed > 100.0) {
							p.sendMessage(ChatColor.RED + "Speed " + speed + " is too fast.");
						} else {
							session.reverse(speed);
							session.sendMessage(ChatColor.GOLD + p.getName() + ": Reversing replay at " + speed + "x speed.");
						}
					} else {
						session.reverse();
						session.sendMessage(ChatColor.GOLD + p.getName() + ": Reversing replay.");
					}
				}
			}
		});
		commands.put("quit", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					session.removeVisibleTo(p);
					plugin.removeHotbar(p, "replay");
					plugin.sendHotbarMessage(p, ChatColor.GREEN + "Quit Replay");
				}
			}
		});
		commands.put("seek", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					long time = Util.parseTime(args[0], session.getCurrentTimeOffset());
					if (time != -1L) {
						session.sendMessage(ChatColor.GOLD + p.getName() + ": Seeking replay to {time:" + time + "}.");
						session.seek(time);
						return;
					}
					String name = args[0];
					boolean backwards = false;
					if (name.startsWith("-")) {
						backwards = true;
						name = name.substring(1);
					}
					UUID uuid = null;
					if (!name.equals("?")) {
						uuid = plugin.getUUIDCache().getUUIDFromName(name);
						if (uuid == null) {
							p.sendMessage(ChatColor.RED + "Could not find player " + name + ".");
							return;
						}
					}
					Class<? extends Action> matchClass = null;
					if (args.length >= 2) {
						String actionStr = args[1];
						if (actionStr.equalsIgnoreCase("armswing")) {
							matchClass = ArmswingAction.class;
						} else if (actionStr.equalsIgnoreCase("blockchange")) {
							matchClass = BlockChangeAction.class;
						} else if (actionStr.equalsIgnoreCase("chat")) {
							matchClass = ChatAction.class;
						} else if (actionStr.equalsIgnoreCase("log")) {
							matchClass = LogAction.class;
						} else if (actionStr.equalsIgnoreCase("login")) {
							matchClass = LoginAction.class;
						} else if (actionStr.equalsIgnoreCase("logout")) {
							matchClass = LogoutAction.class;
						} else if (actionStr.equalsIgnoreCase("move")) {
							matchClass = MoveAction.class;
						}
					}
					session.seek(p, uuid, backwards, matchClass);
				}
			}
		});
		commands.put("list", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append(ChatColor.GOLD).append("Players currently visible: ");
					sb.append(ChatColor.YELLOW);
					boolean comma = false;
					PlugCubeBuildersIn pcbi = PlugCubeBuildersIn.getInstance();
					for (UUID uuid : session.statues.keySet()) {
						if (comma) {
							sb.append(ChatColor.GOLD).append(", ").append(ChatColor.YELLOW);
						}
						comma = true;
						sb.append(pcbi.getUUIDCache().getNameFromUUID(uuid));
					}
					p.sendMessage(sb.toString());
				}
			}
		});
		commands.put("tp", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					String player = args[0];
					UUID uuid = PlugCubeBuildersIn.getInstance().getUUIDCache().getUUIDFromName(player);
					session.teleport(p, uuid);
				}
			}

			@Override
			public List<String> autoComplete(Player p, String[] args) {
				if (args.length != 1) {
					return Arrays.asList(new String[0]);
				}
				String pre = args[0].toLowerCase();
				List<String> result = new ArrayList<>();
				ReplaySession session = getSession(p);
				if (session != null) {
					for (UUID uuid : session.statues.keySet()) {
						String name = PlugCubeBuildersIn.getInstance().getUUIDCache().getNameFromUUID(uuid);
						if (name != null) {
							if (name.toLowerCase().startsWith(pre)) {
								result.add(name);
							}
						}
					}
				}
				return result;
			}
		});
		commands.put("add", new ARCommand() {

			@Override
			public void execute(Player p, String[] args) {
				ReplaySession session = getSession(p);
				if (session == null) {
					p.sendMessage(ChatColor.RED + "You're not currently watching a session.");
				} else {
					if (args.length == 0) {
					} else {
						Player pp = Bukkit.getPlayer(args[0]);
						if (pp == null) {
							p.sendMessage(ChatColor.RED + "Could not find player " + ChatColor.GOLD + args[0] + ChatColor.RED + ".");
							return;
						}
						if (!checkPermission(pp)) {
							p.sendMessage(ChatColor.RED + "Can't add " + ChatColor.GOLD + pp.getName() + ChatColor.RED + ": no permission to view replays.");
							return;
						}
						if (getSession(pp) != null) {
							p.sendMessage(ChatColor.RED + "Can't add " + ChatColor.GOLD + pp.getName() + ChatColor.RED + ": they're already watching a replay.");
							return;
						}
						session.addVisibleTo(pp);
					}
				}
			}

			@Override
			public List<String> autoComplete(Player p, String[] args) {
				if (args.length != 1) {
					return Arrays.asList(new String[0]);
				}
				String pre = args[0].toLowerCase();
				List<String> result = new ArrayList<>();
				ReplaySession session = getSession(p);
				if (session != null) {
					for (Player pp : Bukkit.getOnlinePlayers()) {
						String n = pp.getName();
						if (n.toLowerCase().startsWith(pre) && checkPermission(pp) && getSession(pp) == null) {
							result.add(n);
						}
					}
				}
				return result;
			}
		});
		commands.put("help", new ARCommand() {
			@Override
			public void execute(Player p, String[] args) {
				p.sendMessage(ChatColor.GOLD + "ActionReplay - created by Siggi88 for CubeBuilders.");
				p.sendMessage(ChatColor.YELLOW + "/replay load [absolute or relative time]" + ChatColor.GOLD + " - Initialize a replay session.");
				p.sendMessage(ChatColor.GOLD + "Type " + ChatColor.YELLOW + "/replay load" + ChatColor.GOLD + " for time format info.");
				p.sendMessage(ChatColor.YELLOW + "/replay play" + ChatColor.GOLD + " - Play the replay.");
				p.sendMessage(ChatColor.YELLOW + "/replay play [speed]" + ChatColor.GOLD + " - Play the replay faster or slower.");
				p.sendMessage(ChatColor.YELLOW + "/replay reverse" + ChatColor.GOLD + " - Reverse the replay.");
				p.sendMessage(ChatColor.YELLOW + "/replay reverse [speed]" + ChatColor.GOLD + " - Reverse the replay faster or slower.");
				p.sendMessage(ChatColor.YELLOW + "/replay stop" + ChatColor.GOLD + " - Stop the replay.");
				p.sendMessage(ChatColor.YELLOW + "/replay quit" + ChatColor.GOLD + " - Quit the replay.");
				//p.sendMessage(ChatColor.YELLOW + "/replay seek [relative time]" + ChatColor.GOLD + " - Seek an amount of time.");
				p.sendMessage(ChatColor.YELLOW + "/replay list" + ChatColor.GOLD + " - Get a list of players.");
				p.sendMessage(ChatColor.YELLOW + "/replay tp [name]" + ChatColor.GOLD + " - Teleport to a player.");
				p.sendMessage(ChatColor.YELLOW + "/replay add [name]" + ChatColor.GOLD + " - Add another staff member to the replay to watch.");
			}
		});
	}

	private boolean checkPermission(Player p) {
		return p.hasPermission("hk.siggi.plugcubebuildersin.replay");
	}

	private abstract class ARCommand {

		public abstract void execute(Player p, String args[]);

		public List<String> autoComplete(Player p, String[] args) {
			return Arrays.asList(new String[0]);
		}
	}
}
