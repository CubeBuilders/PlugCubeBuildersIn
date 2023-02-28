package hk.siggi.bukkit.plugcubebuildersin.punisher;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import hk.siggi.bukkit.plugcubebuildersin.util.Util;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTToolBukkit;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PunisherModuleImpl implements PunisherModule, Listener {

	PlugCubeBuildersIn plugin;

	private final Map<Inventory, PunisherMenu> inventories = new WeakHashMap<>();

	void setInventory(Inventory topInventory, PunisherMenu menu) {
		inventories.put(topInventory, menu);
	}

	private PunisherMenu getMenu(Inventory inv) {
		for (Iterator<Inventory> it = inventories.keySet().iterator(); it.hasNext();) {
			Inventory i = it.next();
			if (i == null) {
				it.remove();
				continue;
			}
			if (i.getViewers().isEmpty()) {
				it.remove();
				continue;
			}
		}
		return inventories.get(inv);
	}

	private final List<WeakReference<Player>> lavaBlocks = new LinkedList<>();

	void addLavaBlock(Player p) {
		lavaBlocks.add(new WeakReference<>(p));
	}

	private boolean doLavaBlock(Player p) {
		for (Iterator<WeakReference<Player>> it = lavaBlocks.iterator(); it.hasNext();) {
			WeakReference<Player> wr = it.next();
			if (wr == null) {
				it.remove();
				continue;
			}
			Player pp = wr.get();
			if (pp == null || !pp.isOnline()) {
				it.remove();
				continue;
			}
			if (p == pp) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	private final List<WeakReference<Entity>> godmode = new LinkedList<>();

	void addGodMode(Entity entity) {
		godmode.add(new WeakReference<>(entity));
	}

	private boolean isGodMode(Entity entity) {
		for (Iterator<WeakReference<Entity>> it = godmode.iterator(); it.hasNext();) {
			WeakReference<Entity> wr = it.next();
			if (wr == null) {
				it.remove();
				continue;
			}
			Entity ent = wr.get();
			if (ent == null || !ent.isValid()) {
				it.remove();
				continue;
			}
			if (entity == ent) {
				return true;
			}
		}
		return false;
	}

	private final List<WeakReference<Player>> silverfish = new LinkedList<>();

	void addSilverfish(Player p) {
		silverfish.add(new WeakReference<>(p));
	}

	private boolean doSilverfish(Player p) {
		for (Iterator<WeakReference<Player>> it = silverfish.iterator(); it.hasNext();) {
			WeakReference<Player> wr = it.next();
			if (wr == null) {
				it.remove();
				continue;
			}
			Player pp = wr.get();
			if (pp == null || !p.isOnline()) {
				it.remove();
				continue;
			}
			if (p == pp) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	@Override
	public void load(PlugCubeBuildersIn plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}

	@Override
	public void openReporter(Player p) {

	}

	@Override
	public void openPunisher(
			Player p,
			UUID targetPlayer,
			String playerName,
			String skinPayload,
			String skinSignature,
			boolean allowTroll,
			boolean allowMute,
			boolean allowBan
	) {
		MainMenu mainMenu = new MainMenu(this, p, targetPlayer, playerName, skinPayload, skinSignature, allowTroll, allowMute, allowBan);
		mainMenu.openInventory();
	}

	@Override
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
	) {
		PunishmentSetup setup = new PunishmentSetup(this, p, targetPlayer, playerName, skinPayload, skinSignature, offence, preselectedType, preselectedLength, allowTroll, allowMute, allowBan);
		setup.openInventory();
	}

	@EventHandler
	public void clicked(InventoryClickEvent event) {
		Inventory inventory = event.getInventory();
		PunisherMenu menu = getMenu(inventory);
		if (menu != null) {
			menu.handleClickEvent(event);
			return;
		}
	}

	@EventHandler
	public void blockBroke(BlockBreakEvent event) {
		Player p = event.getPlayer();
		Block b = event.getBlock();
		Material type = b.getType();
		if (doLavaBlock(p)) {
			event.setCancelled(true);
			b.setType(Material.LAVA);
		} else if ((type == Material.IRON_ORE
				|| type == Material.COAL_ORE
				|| type == Material.DIAMOND_ORE
				|| type == Material.GOLD_ORE
				/*|| type == Material.GLOWING_REDSTONE_ORE*/
				|| type == Material.EMERALD_ORE
				|| type == Material.LAPIS_ORE
				/*|| type == Material.QUARTZ_ORE*/)
				&& doSilverfish(p)) {
			Block block = event.getBlock();
			World world = block.getWorld();
			int x = block.getX();
			int y = block.getY();
			int z = block.getZ();
			Location loc = new Location(world, x + 0.5, y, z + 0.5);
			for (int i = 0; i < 4; i++) {
				Entity silv = world.spawnEntity(loc, EntityType.SILVERFISH);
				((Creature) silv).setTarget(p);
			}
		}
	}

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();
			if (b != null && doLavaBlock(p)) {
				event.setCancelled(true);
				b.setType(Material.LAVA);
			}
		}
	}

	@EventHandler
	public void attackingEntity(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (isGodMode(entity)) {
			event.setCancelled(true);
		}
	}

	ItemStack item(Material material, String name, String action, UUID target) {
		return item(material, (short) 0, name, null, action, target);
	}

	ItemStack item(Material material, String name, String[] lore, String action, UUID target) {
		return item(material, (short) 0, name, lore, action, target);
	}

	ItemStack item(Material material, short datavalue, String name, String action, UUID target) {
		return item(material, datavalue, name, null, action, target);
	}

	ItemStack item(Material material, short datavalue, String name, String[] lore, String action, UUID target) {
		ItemStack stack = new ItemStack(material, 1, datavalue);
		NBTCompound tag = new NBTCompound();
		tag.setString("PunisherAction", action);
		tag.setString("PunisherTarget", target.toString().replace("-", ""));
		stack = NBTToolBukkit.setTag(stack, tag);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		if (lore != null) {
			meta.setLore(Arrays.asList(lore));
		}
		stack.setItemMeta(meta);
		return stack;
	}

	String getAction(ItemStack item) {
		NBTCompound tag = NBTToolBukkit.getTag(item);
		if (tag == null) {
			return null;
		}
		return tag.getString("PunisherAction");
	}

	UUID getTarget(ItemStack item) {
		NBTCompound tag = NBTToolBukkit.getTag(item);
		if (tag == null) {
			return null;
		}
		String target = tag.getString("PunisherTarget");
		if (target == null) {
			return null;
		}
		try {
			return Util.uuidFromString(target);
		} catch (Exception e) {
			return null;
		}
	}

	void issueOffence(Player issuer, UUID target, String offence) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("IssueOffence");
			out.writeUTF(target.toString());
			out.writeUTF(offence);

			issuer.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

	void confirmOffence(Player issuer, UUID target, String offence, String punishmentType, long punishmentLength) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);

			out.writeUTF("ConfirmOffence");
			out.writeUTF(target.toString());
			out.writeUTF(offence);
			out.writeUTF(punishmentType);
			out.writeLong(punishmentLength);

			issuer.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
		} catch (Exception e) {
		}
	}

}
