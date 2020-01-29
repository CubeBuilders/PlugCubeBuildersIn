package hk.siggi.bukkit.plugcubebuildersin.replay.action;

import java.util.Objects;
import java.util.UUID;

public final class ChatAction extends Action {

	public final String message;

	public ChatAction(long time, UUID player, String message) {
		super(time, player);
		this.message = message;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ChatAction) {
			return equals((ChatAction) other);
		} else {
			return false;
		}
	}

	public boolean equals(ChatAction other) {
		if (other == null) {
			return false;
		}
		return (message == null ? other.message == null : message.equals(other.message))
				&& time == other.time
				&& (player == null ? other.player == null : player.equals(other.player));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + (int) (this.time ^ (this.time >>> 32));
		hash = 47 * hash + Objects.hashCode(this.player);
		hash = 47 * hash + Objects.hashCode(this.message);
		return hash;
	}
}
