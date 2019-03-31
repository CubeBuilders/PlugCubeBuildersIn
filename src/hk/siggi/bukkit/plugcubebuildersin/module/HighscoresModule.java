package hk.siggi.bukkit.plugcubebuildersin.module;

import java.util.List;
import java.util.UUID;

public interface HighscoresModule extends Module {

	public Scoreboard getScoreboard(String name);

	public interface Scoreboard {

		public void clear();

		public void setScore(UUID player, long score);

		public long incrementScore(UUID player, long increment);

		public long getScore(UUID player);
		
		public int getPositionForPlayer(UUID player);

		public Score getScoreAtPosition(int position);

		public void recalculateTopList();

		public List<? extends Score> getScores();

		public interface Score {

			public UUID getPlayer();

			public long getScore();
		}
	}
}
