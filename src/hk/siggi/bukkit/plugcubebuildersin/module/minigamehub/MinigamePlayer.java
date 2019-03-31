package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.lang.ref.WeakReference;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class MinigamePlayer {

	private final UUID uuid;
	private WeakReference<MinigameSession> session = null;

	public MinigamePlayer(UUID uuid) {
		if (uuid == null) {
			throw new NullPointerException();
		}
		this.uuid = uuid;
	}

	public Player getBukkitPlayer() {
		try {
			return Bukkit.getPlayer(uuid);
		} catch (Exception e) {
		}
		return null;
	}

	public void joinSession(MinigameSession session) {
		if (session.isOpen()) {
			this.session = new WeakReference<>(session);
			session.addPlayer(this);
		}
	}

	public void quitSession() {
		MinigameSession sess = getSession();
		if (sess != null) {
			sess.removePlayer(this);
			session = null;
		}
	}

	public MinigameSession getSession() {
		if (session == null) {
			return null;
		}
		MinigameSession sess = session.get();
		if (sess == null) {
			return null;
		}
		if (sess.isOpen()) {
			if (!valid) {
				return null;
			}
			if (sess.players.contains(this)) {
				return sess;
			}
			session = null;
			return null;
		} else {
			session = null;
			return null;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MinigamePlayer) {
			return equals((MinigamePlayer) other);
		}
		return false;
	}

	public boolean equals(MinigamePlayer other) {
		return uuid.equals(other.uuid);
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	private long lastSeen = 0L;

	void updateLastSeen() {
		lastSeen = System.currentTimeMillis();
	}

	private boolean valid = true;

	public boolean hasExpired() {
		if (!valid) {
			return true;
		}
		if (getBukkitPlayer() != null) {
			return false;
		}
		MinigameSession sess = getSession();
		if (sess == null) {
			valid = false;
			return true;
		}
		long now = System.currentTimeMillis();
		if (!sess.isInGame() && now - lastSeen > 15000L) {
			valid = false;
			return true;
		} else {
			return false;
		}
	}

	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public String toString() {
		return "MinigamePlayer(" + PlugCubeBuildersIn.getInstance().getUUIDCache().getNameFromUUID(uuid) + "/" + uuid.toString() + ")";
	}
}
