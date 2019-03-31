package hk.siggi.bukkit.plugcubebuildersin.highscores;

import org.bukkit.entity.Player;

public class Strike {

	public final Player attacker;
	public final Player victim;
	public final long time;

	public Strike(Player attacker, Player victim, long time) {
		this.attacker = attacker;
		this.victim = victim;
		this.time = time;
	}
}
