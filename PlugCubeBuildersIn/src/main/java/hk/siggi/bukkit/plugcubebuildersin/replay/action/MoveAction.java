package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import java.util.UUID;

public final class MoveAction extends Action {

	public final ActionLocation from, to;
	public final boolean teleport;

	public MoveAction(long time, UUID player, ActionLocation from, ActionLocation to, boolean teleport) {
		super(time, player);
		this.from = from;
		this.to = to;
		this.teleport = teleport;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MoveAction) {
			return equals((MoveAction) other);
		} else {
			return false;
		}
	}

	public boolean equals(MoveAction other) {
		if (other == null) {
			return false;
		}
		return (from == null ? (other.from == null) : from.equals(other.from))
				&& (to == null ? (other.to == null) : to.equals(other.to))
				&& teleport == other.teleport
				&& time == other.time
				&& (player == null ? other.player == null : player.equals(other.player));
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.player);
		hash = 97 * hash + Objects.hashCode(this.from);
		hash = 97 * hash + Objects.hashCode(this.to);
		hash = 97 * hash + (this.teleport ? 1 : 0);
		hash = 97 * hash + (int) (this.time ^ (this.time >>> 32));
		return hash;
	}
}
