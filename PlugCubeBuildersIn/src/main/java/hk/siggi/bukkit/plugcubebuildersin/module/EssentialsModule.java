package hk.siggi.bukkit.plugcubebuildersin.module;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface EssentialsModule extends Module, BankModule {
	public Location getWarpLocation(String warpName);
	public void setVanished(Player p, boolean vanish);
	public void setNickname(Player p, String nick);
	public void setNickname(UUID p, String nick);
	public void setGodmode(Player p, boolean godmode);
	public boolean isGodmode(Player p);
	public void makePlayerSeePlayer(Player p1, Player p2);
}
