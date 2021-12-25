package hk.siggi.bukkit.plugcubebuildersin.teleportcontrol;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.perms.Relation;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FactionsTeleportControlSystem implements TeleportControlSystem {

	public final PlugCubeBuildersIn plugin;
	public final FactionsPlugin factions;

	public FactionsTeleportControlSystem(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
		factions = (FactionsPlugin) plugin.getServer().getPluginManager().getPlugin("Factions");
	}

	@Override
	public boolean playerTPA(Player from, Player to, boolean tpaHere) {
		Location destination = to.getLocation();
		FPlayer fromPlayer = FPlayers.getInstance().getByPlayer(from);
		FPlayer toPlayer = FPlayers.getInstance().getByPlayer(to);
		Faction destinationFaction = Board.getInstance().getFactionAt(new FLocation(destination));
		if (destinationFaction == null) {
			destinationFaction = Factions.getInstance().getNone();
		}
		if (destinationFaction.isNone() || destinationFaction.isSafeZone() || destinationFaction.isWarZone()) {
			return true;
		}
		Faction fromFaction = fromPlayer.getFaction();
		Faction toFaction = toPlayer.getFaction();
		if (fromFaction == null) {
			fromFaction = Factions.getInstance().getNone();
		}
		if (toFaction == null) {
			toFaction = Factions.getInstance().getNone();
		}
		if (fromFaction == destinationFaction || toFaction == destinationFaction) {
			return true;
		}
		Relation relation = destinationFaction.getRelationTo(fromFaction);
		if (relation == Relation.MEMBER || relation == Relation.ALLY) {
			return true;
		}
		from.sendMessage(ChatColor.RED + "You cannot teleport to " + to.getName() + " as you do not have access to the territory they are standing in.");
		to.sendMessage(ChatColor.RED + from.getName() + " cannot teleport to you because they do not have access to this territory, and you are not a member of this territory.");
		return false;
	}

	@Override
	public boolean goHome(Player player, Location home) {
		Faction destinationFaction = Board.getInstance().getFactionAt(new FLocation(home));
		if (destinationFaction == null) {
			destinationFaction = Factions.getInstance().getNone();
		}
		if (destinationFaction.isNone() || destinationFaction.isSafeZone() || destinationFaction.isWarZone()) {
			return true;
		}
		Faction myFaction = FPlayers.getInstance().getByPlayer(player).getFaction();
		if (myFaction == null) {
			myFaction = Factions.getInstance().getNone();
		}
		if (myFaction == destinationFaction) {
			return true;
		}
		Relation relation = destinationFaction.getRelationTo(myFaction);
		if (relation == Relation.MEMBER || relation == Relation.ALLY) {
			return true;
		}
		player.sendMessage(ChatColor.RED + "Your home is located in territory that you have no access to.");
		return false;
	}

	@Override
	public boolean setHome(Player player) {
		Faction destinationFaction = Board.getInstance().getFactionAt(new FLocation(player.getLocation()));
		if (destinationFaction == null) {
			destinationFaction = Factions.getInstance().getNone();
		}
		if (destinationFaction.isNone() || destinationFaction.isSafeZone() || destinationFaction.isWarZone()) {
			return true;
		}
		Faction myFaction = FPlayers.getInstance().getByPlayer(player).getFaction();
		if (myFaction == null) {
			myFaction = Factions.getInstance().getNone();
		}
		if (myFaction == destinationFaction) {
			return true;
		}
		Relation relation = destinationFaction.getRelationTo(myFaction);
		if (relation == Relation.MEMBER || relation == Relation.ALLY) {
			return true;
		}
		player.sendMessage(ChatColor.RED + "You cannot set your home here.");
		return false;
	}
}
