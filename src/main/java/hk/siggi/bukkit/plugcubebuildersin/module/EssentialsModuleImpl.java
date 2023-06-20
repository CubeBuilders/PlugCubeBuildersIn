package hk.siggi.bukkit.plugcubebuildersin.module;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.IWarps;
import com.earth2me.essentials.commands.WarpNotFoundException;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;
import net.ess3.api.InvalidWorldException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class EssentialsModuleImpl implements EssentialsModule {

	private PlugCubeBuildersIn plugin = null;
	private Essentials essentials = null;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		Plugin ess = plugin.getServer().getPluginManager().getPlugin("Essentials");
		if (ess instanceof Essentials) {
			essentials = (Essentials) ess;
		}
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}

	@Override
	public Location getWarpLocation(String warpName) {
		if (essentials == null) {
			return null;
		}
		try {
			IWarps warps = essentials.getWarps();
			Location loc = warps.getWarp(warpName);
			return loc;
		} catch (WarpNotFoundException | InvalidWorldException e) {
			// don't insert a reportProblem(String,Throwable) here.
		}
		return null;
	}

	@Override
	public double getBalance(Player p) {
		return getBalance(p.getUniqueId());
	}

	@Override
	public double getBalance(UUID uuid) {
		try {
			User user = essentials.getUser(uuid);
			return user.getMoney().doubleValue();
		} catch (Exception e) {
		}
		return 0.0;
	}

	@Override
	public boolean chargeMoney(Player p, double amount) {
		return chargeMoney(p.getUniqueId(), amount);
	}

	@Override
	public boolean chargeMoney(UUID uuid, double amount) {
		try {
			User user = essentials.getUser(uuid);
			user.takeMoney(new BigDecimal(amount));
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public boolean giveMoney(Player p, double amount) {
		return giveMoney(p.getUniqueId(), amount);
	}

	@Override
	public boolean giveMoney(UUID uuid, double amount) {
		try {
			User user = essentials.getUser(uuid);
			user.giveMoney(new BigDecimal(amount));
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public void setVanished(Player p, boolean vanish) {
		try {
			User u = essentials.getUser(p);
			Class clazz = u.getClass();
			Field hidden = clazz.getDeclaredField("hidden");
			Field vanished = clazz.getDeclaredField("vanished");
			boolean oA1 = hidden.isAccessible();
			boolean oA2 = vanished.isAccessible();
			hidden.setAccessible(true);
			vanished.setAccessible(true);
			hidden.setBoolean(u, vanish);
			vanished.setBoolean(u, vanish);
			hidden.setAccessible(oA1);
			vanished.setAccessible(oA2);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			plugin.reportProblem("Couldn't set player vanish status in Essentials", e);
		}
		if (!vanish) {
			PlugCubeBuildersIn.getInstance().removeEssentialsVanishInvisibility(p);
		}
	}

	@Override
	public void setNickname(Player p, String nickname) {
		essentials.getUser(p).setNickname(nickname);
	}

	@Override
	public void setNickname(UUID p, String nickname) {
		essentials.getUser(p).setNickname(nickname);
	}

	@Override
	public void setGodmode(Player p, boolean godmode) {
		essentials.getUser(p).setGodModeEnabled(godmode);
	}

	@Override
	public boolean isGodmode(Player p) {
		return essentials.getUser(p).isGodModeEnabled();
	}

	@Override
	public void makePlayerSeePlayer(Player p1, Player p2) {
		p1.showPlayer(essentials, p2);
		p1.showPlayer(p2);
	}
}
