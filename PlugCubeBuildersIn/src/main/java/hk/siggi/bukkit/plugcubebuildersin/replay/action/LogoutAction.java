package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import java.util.UUID;

public final class LogoutAction extends LogAction {

	public final ActionLocation location;

	public LogoutAction(long time, UUID player, ActionLocation location) {
		super(time, player);
		this.location = location;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LogoutAction) {
			return equals((LogoutAction) other);
		} else {
			return false;
		}
	}

	public boolean equals(LogoutAction other) {
		if (other == null) {
			return false;
		}
		return (location == null ? other.location == null : location.equals(other.location))
				&& time == other.time
				&& (player == null ? other.player == null : player.equals(other.player));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + Objects.hashCode(this.location);
		hash = 23 * hash + (int) (this.time ^ (this.time >>> 32));
		hash = 23 * hash + Objects.hashCode(this.player);
		return hash;
	}
}
