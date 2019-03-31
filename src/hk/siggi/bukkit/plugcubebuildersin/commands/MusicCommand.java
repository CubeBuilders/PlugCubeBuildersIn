package hk.siggi.bukkit.plugcubebuildersin.commands;

import hk.siggi.bukkit.plugcubebuildersin.PlayerSession;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.music.MusicNote;
import hk.siggi.bukkit.plugcubebuildersin.music.MusicPlayer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MusicCommand implements CommandExecutor {

	private final PlugCubeBuildersIn plugin;

	public MusicCommand(PlugCubeBuildersIn plugin) {
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
		PlayerSession session = plugin.getSession(player);
		String track = null;
		if (split.length > 0) {
			track = split[0];
			for (int i = 1; i < split.length; i++) {
				track += " " + split[i];
			}
		}
		if (track.equalsIgnoreCase("stop")) {
			session.musicPlayer = null;
			return true;
		}
		File music = new File(plugin.getDataFolder(), "music");
		File trackFile = new File(music, track + ".txt");
		if (!trackFile.exists()) {
			sender.sendMessage(ChatColor.RED + "Could not find track: " + track);
			return true;
		}
		sender.sendMessage(ChatColor.GREEN + "Now playing: " + track);
		try {
			InputStream in = new FileInputStream(trackFile);
			MusicNote[] notes = MusicNote.readNotes(in);
			MusicPlayer musicPlayer = new MusicPlayer(notes, player);
			session.musicPlayer = musicPlayer;
		} catch (Exception e) {
			plugin.reportProblem("Could not play music for music command", e);
		}
		return true;
	}
}
