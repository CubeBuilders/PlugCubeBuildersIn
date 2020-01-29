package hk.siggi.bukkit.plugcubebuildersin.music;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockPlayer {

	private final PlugCubeBuildersIn plugin;

	public BlockPlayer(PlugCubeBuildersIn plugin, String world, double x, double y, double z, String folder) {
		this.plugin = plugin;
		if (world == null) {
			world = plugin.getServer().getWorlds().get(0).getName();
		}
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		if (folder == null) {
			folder = "music";
		}
		this.folder = folder;
	}
	public List<String> recentlyPlayed = new LinkedList<>();
	public String world;
	public double x;
	public double y;
	public double z;
	public String folder;
	public MusicPlayer currentlyPlaying = null;

	public boolean tick() {
		if (currentlyPlaying == null) {
			nextSong();
			if (currentlyPlaying == null) {
				return true;
			}
		}
		if (!currentlyPlaying.playOneTick()) {
			currentlyPlaying = null;
		}
		return true;
	}

	public void nextSong() {
		try {
			currentlyPlaying = null;
			World w = plugin.getServer().getWorld(world);
			if (w == null) {
				return;
			}
			Location l = new Location(w, x, y, z);
			File[] f = (folder.startsWith("/") ? new File(folder) : new File(plugin.getDataFolder(), folder)).listFiles();
			for (int tries = 0; tries < 8; tries++) {
				File random = f[(int) Math.floor(Math.random() * f.length)];
				String name = random.getName();
				if (!name.endsWith(".txt")) {
					continue;
				}
				name = name.substring(0, name.length() - 4);
				if (recentlyPlayed.contains(name)) {
					continue;
				}
				recentlyPlayed.add(name);
				while (recentlyPlayed.size() > Math.min(5, f.length - 1)) {
					recentlyPlayed.remove(0);
				}
				currentlyPlaying = new MusicPlayer(MusicNote.readNotes(new FileInputStream(random)), l);
				// wait 3 seconds before playing!
				currentlyPlaying.tick = -60;
				break;
			}
		} catch (Exception e) {
			plugin.reportProblem("Couldn't load next song", e);
		}
	}

	public void skipSong() {
		
	}
}
