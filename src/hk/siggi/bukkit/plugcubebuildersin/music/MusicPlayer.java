package hk.siggi.bukkit.plugcubebuildersin.music;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MusicPlayer {

	public final MusicNote[] music;
	public final Player p;
	public final Location l;
	private int idx = 0;
	public int tick = -1;
	public Runnable onFinish = null;

	public MusicPlayer(MusicNote[] music, Player p) {
		this.music = music;
		this.p = p;
		this.l = null;
	}

	public MusicPlayer(MusicNote[] music, Location l) {
		this.music = music;
		this.p = null;
		this.l = l;
	}

	public boolean playOneTick() {
		try {
			tick += 1;
			for (; idx < music.length; idx++) {
				if (music[idx].tick > tick) {
					return true;
				}
				if (p != null) {
					music[idx].play(p);
				}
				if (l != null) {
					music[idx].play(l);
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
