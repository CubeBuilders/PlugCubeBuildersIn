package hk.siggi.bukkit.plugcubebuildersin.music;

import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class MusicNote {
	
	public static MusicNote[] readNotes(InputStream in) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			return readNotes(reader);
		} finally {
			tryClose(reader);
		}
	}
	
	public static MusicNote[] readNotes(BufferedReader reader) throws IOException {
		LinkedList<MusicNote> notes = new LinkedList<>();
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					String parts[] = line.split("/");
					int tick = Integer.parseInt(parts[0]);
					String soundName = parts[1];
					float volume = Float.parseFloat(parts[2]);
					float pitch = Float.parseFloat(parts[3]);
					if (soundName.startsWith("minecraft:")) {
						soundName = soundName.substring(10);
					}
					soundName = soundName.replace(".", "_");
					soundName = soundName.toUpperCase();
					Sound sound = Sound.valueOf(soundName);
					notes.add(new MusicNote(tick, sound, volume, pitch));
				} catch (Exception e) {
				}
			}
		} finally {
			tryClose(reader);
		}
		return notes.toArray(new MusicNote[notes.size()]);
	}
	
	public MusicNote(int tick, Sound sound, float volume, float pitch) {
		this.tick = tick;
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}
	public final int tick;
	public final Sound sound;
	public final float volume;
	public final float pitch;
	
	public void play(Player p) {
		p.playSound(p.getLocation(), sound, volume, pitch);
	}
	public void play(Location l) {
		l.getWorld().playSound(l, sound, volume, pitch);
	}
}
