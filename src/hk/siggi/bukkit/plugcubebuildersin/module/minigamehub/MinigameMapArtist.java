package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.UUID;

public final class MinigameMapArtist {

	private final UUID uuid;
	private final String name;
	private final String credit;

	public MinigameMapArtist(UUID uuid, String credit) {
		if (uuid == null) {
			throw new NullPointerException();
		}
		this.uuid = uuid;
		this.name = uuid.toString();
		this.credit = credit;
	}

	public MinigameMapArtist(String name, String credit) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.uuid = null;
		this.name = name;
		this.credit = credit;
	}

	public MinigameMapArtist(String name, UUID uuid, String credit) {
		if (name == null || uuid == null) {
			throw new NullPointerException();
		}
		this.uuid = uuid;
		this.name = name;
		this.credit = credit;
	}

	public String getName() {
		if (uuid != null) {
			try {
				String n = PlugCubeBuildersIn.getInstance().getUUIDCache().getNameFromUUID(uuid);
				if (n != null) {
					return n;
				}
			} catch (Exception e) {
			}
		}
		return name;
	}

	public String getCredit() {
		return credit;
	}

	@Override
	public String toString() {
		String n = getName();
		String c = getCredit();
		if (c != null) {
			return c + " - " + c;
		} else {
			return n;
		}
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof MinigameMapArtist ? equals((MinigameMapArtist) other) : false;
	}

	public boolean equals(MinigameMapArtist other) {
		if (uuid == null) {
			if (other.uuid == null) {
				return name.equals(other.name);
			} else {
				return false;
			}
		} else {
			return uuid.equals(other.uuid);
		}
	}

	@Override
	public int hashCode() {
		if (uuid == null) {
			return name.hashCode();
		} else {
			return uuid.hashCode();
		}
	}
}
