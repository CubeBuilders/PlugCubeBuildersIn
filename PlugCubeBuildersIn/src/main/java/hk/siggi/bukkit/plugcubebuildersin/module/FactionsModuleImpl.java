package hk.siggi.bukkit.plugcubebuildersin.module;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.P;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRenameEvent;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.nms.NMSUtil;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class FactionsModuleImpl implements FactionsModule {

	public Properties lastCreatedFaction = new Properties();
	public Properties lastRenamedFaction = new Properties();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void createdFaction(FactionCreateEvent event) {
		long lastCreated = 0L;
		try {
			lastCreated = Long.parseLong(lastCreatedFaction.getProperty(event.getFPlayer().getPlayer().getName()));
		} catch (Exception e) {
		}
		long now = System.currentTimeMillis();
		if (!event.getFPlayer().getPlayer().hasPermission("hk.siggi.plugcubebuildersin.bypassfactionthrottle") && now - lastCreated < 1200000L) {
			event.setCancelled(true);
			event.getFPlayer().getPlayer().sendMessage(ChatColor.RED + "You only recently created a new faction.");
			event.getFPlayer().getPlayer().sendMessage(ChatColor.RED + "Please wait at least 20 minutes before creating another one.");
		} else {
			lastCreatedFaction.setProperty(event.getFPlayer().getPlayer().getName(), Long.toString(now));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void disbandedFaction(FactionDisbandEvent event) {
		try {
			lastRenamedFaction.remove(event.getFaction().getId());
		} catch (Exception e) {
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void renameFaction(FactionRenameEvent event) {
		long lastRenamed = 0L;
		try {
			lastRenamed = Long.parseLong(lastRenamedFaction.getProperty(event.getFaction().getId()));
		} catch (Exception e) {
		}
		long now = System.currentTimeMillis();
		if (!event.getfPlayer().getPlayer().hasPermission("hk.siggi.plugcubebuildersin.bypassfactionthrottle") && now - lastRenamed < 1200000L) {
			event.setCancelled(true);
			event.getfPlayer().getPlayer().sendMessage(ChatColor.RED + "Your faction was only recently renamed.");
			event.getfPlayer().getPlayer().sendMessage(ChatColor.RED + "Please wait at least 20 minutes before renaming it again.");
		} else {
			lastRenamedFaction.setProperty(event.getFaction().getId(), Long.toString(now));
		}
	}

	private PlugCubeBuildersIn plugin;
	private P factions;

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		factions = (P) plugin.getServer().getPluginManager().getPlugin("Factions");
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
		try {
			for (World world : plugin.getServer().getWorlds()) {
				List<Entity> entities = world.getEntities();
				for (Entity entity : entities) {
					if (!(entity instanceof LivingEntity)) {
						continue;
					}
					LivingEntity living = (LivingEntity) entity;
					try {
						if (NMSUtil.get().isHostile(living)) {
							Location location = living.getLocation();
							FLocation floc = new FLocation(location);
							Faction faction = Board.getInstance().getFactionAt(floc);
							if (faction.isSafeZone()) {
								living.setFireTicks(100);
							}
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
		}
	}

	private FPlayer getFPlayer(Player p) {
		return FPlayers.getInstance().getByPlayer(p);
	}

	private FPlayer getFPlayer(UUID p) {
		return FPlayers.getInstance().getById(p.toString());
	}

	@Override
	public double getPower(Player player) {
		return getFPlayer(player).getPower();
	}

	@Override
	public double getPower(UUID player) {
		return getFPlayer(player).getPower();
	}

	@Override
	public void alterPower(Player player, double power) {
		FPlayer fPlayer = getFPlayer(player);
		fPlayer.alterPower(power);
	}

	@Override
	public void alterPower(UUID player, double power) {
		FPlayer fPlayer = getFPlayer(player);
		fPlayer.alterPower(power);
	}

	@Override
	public double getMinimumPower() {
		return Conf.powerPlayerMin;
	}

	@Override
	public double getMaximumPower() {
		return Conf.powerPlayerMax;
	}

	@Override
	public double getDefaultPower() {
		return Conf.powerPlayerStarting;
	}

	@Override
	public void setBypassing(Player player, boolean bypassing) {
		FPlayers.getInstance().getByPlayer(player).setIsAdminBypassing(bypassing);
	}

	@Override
	public boolean isBypassing(Player player) {
		return FPlayers.getInstance().getByPlayer(player).isAdminBypassing();
	}
}
