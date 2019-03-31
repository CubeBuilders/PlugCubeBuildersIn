package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import java.util.UUID;

public final class ArmswingAction extends Action {

	public ArmswingAction(long time, UUID player) {
		super(time, player);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ArmswingAction) {
			return equals((ArmswingAction) other);
		} else {
			return false;
		}
	}

	public boolean equals(ArmswingAction other) {
		if (other == null) {
			return false;
		}
		return time == other.time
				&& (player == null ? other.player == null : player.equals(other.player));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.player);
		hash = 53 * hash + (int) (this.time ^ (this.time >>> 32));
		return hash;
	}
}
