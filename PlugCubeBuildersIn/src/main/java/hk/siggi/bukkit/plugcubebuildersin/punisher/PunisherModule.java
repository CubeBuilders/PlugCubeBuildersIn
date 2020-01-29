package hk.siggi.bukkit.plugcubebuildersin.punisher;

import hk.siggi.bukkit.plugcubebuildersin.module.Module;
import java.util.UUID;
import org.bukkit.entity.Player;

public interface PunisherModule extends Module {

	public void openReporter(Player p);

	public void openPunisher(
			Player p,
			UUID targetPlayer,
			String playerName,
			String skinPayload,
			String skinSignature,
			boolean allowTroll,
			boolean allowMute,
			boolean allowBan
	);

	public void setupPunishment(
			Player p,
			UUID targetPlayer,
			String playerName,
			String skinPayload,
			String skinSignature,
			String offence,
			String preselectedType,
			long preselectedLength,
			boolean allowTroll,
			boolean allowMute,
			boolean allowBan
	);
}
