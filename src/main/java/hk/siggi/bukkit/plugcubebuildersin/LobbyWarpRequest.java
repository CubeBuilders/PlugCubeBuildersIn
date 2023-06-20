package hk.siggi.bukkit.plugcubebuildersin;

import java.util.UUID;

public class LobbyWarpRequest {

	public final UUID player;
	public final String warpName;
	public final long time;

	public LobbyWarpRequest(UUID player, String warpName) {
		this.player = player;
		this.warpName = warpName;
		this.time = System.currentTimeMillis();
	}

	public boolean expired() {
		return (System.currentTimeMillis() - time) > 30000L;
	}
}
