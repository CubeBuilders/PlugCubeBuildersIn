package hk.siggi.bukkit.plugcubebuildersin.punisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MainMenu implements PunisherMenu {

	private final PunisherModuleImpl module;
	private final Player p;
	private final UUID targetPlayer;
	private final String playerName;
	private final String skinPayload;
	private final String skinSignature;
	private final boolean allowTroll;
	private final boolean allowMute;
	private final boolean allowBan;

	public MainMenu(
			PunisherModuleImpl module,
			Player p,
			UUID targetPlayer,
			String playerName,
			String skinPayload,
			String skinSignature,
			boolean allowTroll,
			boolean allowMute,
			boolean allowBan
	) {
		this.module = module;
		this.p = p;
		this.targetPlayer = targetPlayer;
		this.playerName = playerName;
		this.skinPayload = skinPayload;
		this.skinSignature = skinSignature;
		this.allowTroll = allowTroll;
		this.allowMute = allowMute;
		this.allowBan = allowBan;
	}

	private Material endPortalFrame = null;

	public Material getEndPortalFrame() {
		if (endPortalFrame == null) {
			try {
				endPortalFrame = Material.END_PORTAL_FRAME;
			} catch (Throwable e) {
				endPortalFrame = Material.getMaterial("ENDER_PORTAL_FRAME");
			}
		}
		return endPortalFrame;
	}

	private Material rawFishObject = null;

	public Material getRawFishObject() {
		if (rawFishObject == null) {
			try {
				rawFishObject = Material.COD;
			} catch (Throwable e) {
				rawFishObject = Material.getMaterial("RAW_FISH");
			}
		}
		return rawFishObject;
	}

	private Material totemOfUndying = null;

	public Material getTotemOfUndying() {
		if (totemOfUndying == null) {
			try {
				totemOfUndying = Material.TOTEM_OF_UNDYING;
			} catch (Throwable e) {
				totemOfUndying = Material.getMaterial("TOTEM");
			}
		}
		return totemOfUndying;
	}

	private Material zombieHeadItem = null; // SKULL_ITEM
	private short zombieHeadDataValue = (short) 0;

	public Material getZombieHeadItem() {
		if (zombieHeadItem == null) {
			try {
				zombieHeadItem = Material.ZOMBIE_HEAD;
				zombieHeadDataValue = (short) 0;
			} catch (Throwable e) {
				zombieHeadItem = Material.getMaterial("SKULL_ITEM");
				zombieHeadDataValue = (short) 2;
			}
		}
		return zombieHeadItem;
	}

	private Material clockItem = null;

	public Material getClock() {
		if (clockItem == null) {
			try {
				clockItem = Material.CLOCK;
			} catch (Throwable e) {
				clockItem = Material.getMaterial("WATCH");
			}
		}
		return clockItem;
	}

	private Material bookAndQuill = null;

	public Material getBookAndQuill() {
		if (bookAndQuill == null) {
			try {
				bookAndQuill = Material.WRITABLE_BOOK;
			} catch (Throwable e) {
				bookAndQuill = Material.getMaterial("BOOK_AND_QUILL");
			}
		}
		return bookAndQuill;
	}

	@Override
	public void openInventory() {
		List<ItemStack> trolls = new ArrayList<>(54);
		List<ItemStack> offences = new ArrayList<>(54);

		if (allowTroll) {
			trolls.add(module.item(Material.LAVA_BUCKET, "Lava", new String[]{"Next block they touch", "will become lava"}, "lavablock", targetPlayer));
			trolls.add(module.item(getEndPortalFrame(), "Drop in hole", new String[]{"Make a hole in the ground at their feet"}, "hole", targetPlayer));
			trolls.add(module.item(Material.DIAMOND_HELMET, "Zombies (God mode)", new String[]{"What the title says"}, "godzombies", targetPlayer));
			trolls.add(module.item(getRawFishObject(), (short) 2, "Silverfish", new String[]{"Next ore mined will spawn", "4 silverfish"}, "silverfish", targetPlayer));
			//trolls.add(module.item(Material.TNT, "Nuke", new String[]{"NUKEM!!"}, "nuke", targetPlayer));
			trolls.add(module.item(Material.APPLE, "Starve", new String[]{"Drain their hungerbar"}, "starve", targetPlayer));
			trolls.add(module.item(Material.GHAST_TEAR, "Levitate", new String[]{"Up, up, and away!"}, "levitate", targetPlayer));
		}

		if (allowMute) {
			offences.add(module.item(Material.GOLD_INGOT, "Advertising", "offence_advertising", targetPlayer));
			offences.add(module.item(Material.NAME_TAG, "Offensive Language", "offence_language", targetPlayer));
			offences.add(module.item(getTotemOfUndying(), "Sexual Harassment", "offence_sexual_harassment", targetPlayer));
			offences.add(module.item(Material.ENCHANTED_BOOK, "Prematurely Requesting/Giving Personal Info", "offence_personal_info", targetPlayer));
			if (allowBan) {
				offences.add(module.item(Material.LAVA_BUCKET, "Griefing", "offence_grief", targetPlayer));
			}
			offences.add(module.item(Material.BOW, "Spamming", "offence_spam", targetPlayer));
			if (allowBan) {
				offences.add(module.item(Material.DIAMOND, "Xray", "offence_xray", targetPlayer));
				offences.add(module.item(Material.DIAMOND_SWORD, "Kill Aura", "offence_kill_aura", targetPlayer));
				offences.add(module.item(Material.ELYTRA, "Fly hacking", "offence_fly", targetPlayer));
				offences.add(module.item(Material.GOLDEN_APPLE, "Other modded client", "offence_modded_client", targetPlayer));
				offences.add(module.item(Material.FERMENTED_SPIDER_EYE, "Bug abuse", "offence_bug_abuse", targetPlayer));
			}
			offences.add(module.item(getClock(), "Wasting staff time", "offence_waste_staff_time", targetPlayer));
			offences.add(module.item(Material.COAL, "Misleading", "offence_misleading", targetPlayer));
			offences.add(module.item(getZombieHeadItem(), zombieHeadDataValue, "Staff Impersonation", "offence_impersonation", targetPlayer));
			if (allowBan) {
				offences.add(module.item(Material.GLOWSTONE_DUST, "Real World Trading", "offence_real_world_trading", targetPlayer));
			}
			offences.add(module.item(Material.BARRIER, "Encouraging Rulebreaking", "offence_encouraging_rulebreaking", targetPlayer));
			if (allowBan) {
				offences.add(module.item(Material.PUMPKIN, "Alt Account", "offence_alt_account", targetPlayer));
			}
			offences.add(module.item(getBookAndQuill(), "Speaking Foreign Language", "offence_foreign_language", targetPlayer));
			if (allowBan) {
				offences.add(module.item(Material.BEDROCK, "Real World illegal activities", "offence_illegal_activities", targetPlayer));
			}
		}

		Inventory createInventory = module.plugin.getServer().createInventory(p, 54, "Punish " + playerName);

		int addPos = 0;
		for (ItemStack troll : trolls) {
			createInventory.setItem(addPos, troll);
			addPos += 1;
		}
		if (addPos != 0) {
			int y = addPos % 9;
			if (y != 0) {
				addPos += (9 - y);
			}
			addPos += 9;
		}

		for (ItemStack offence : offences) {
			createInventory.setItem(addPos, offence);
			addPos += 1;
		}

		InventoryView opened = p.openInventory(createInventory);
		Inventory topInventory = opened.getTopInventory();
		module.setInventory(topInventory, this);
	}

	@Override
	public void handleClickEvent(InventoryClickEvent event) {
		event.setCancelled(true);
		HumanEntity whoClicked = event.getWhoClicked();
		if (!(whoClicked instanceof Player)) {
			return;
		}
		Player clicker = (Player) whoClicked;
		ItemStack currentItem = event.getCurrentItem();
		if (currentItem == null) {
			return;
		}
		String action = module.getAction(currentItem);
		if (action == null) {
			return;
		}
		UUID target = module.getTarget(currentItem);
		if (target == null) {
			return;
		}
		if (action.startsWith("offence_")) {
			module.issueOffence(clicker, target, action.substring(8));
			return;
		}
		clicker.closeInventory();
		Player targetPlayerObj = module.plugin.getServer().getPlayer(target);
		if (targetPlayerObj == null) {
			clicker.sendMessage(ChatColor.RED + "Error: That player has gone offline.");
			return;
		}
		switch (action) {
			case "lavablock": {
				clicker.sendMessage(ChatColor.GOLD + "Next block " + targetPlayerObj.getName() + " touches will become lava!");
				module.addLavaBlock(targetPlayerObj);
			}
			break;
			case "hole": {
				clicker.sendMessage(ChatColor.GOLD + "Making a hole for " + targetPlayerObj.getName() + " to fall in!");
				Location location = targetPlayerObj.getLocation();
				Block block = location.getBlock();
				World world = block.getWorld();
				int x = block.getX();
				int y = block.getY();
				int z = block.getZ();
				int min = Math.max(0, y - 16);
				for (int i = y; i >= min; i--) {
					Block bb = world.getBlockAt(x, i, z);
					bb.setType(Material.AIR);
				}
				Location newLoc = new Location(world, x + 0.5, y, z + 0.5, location.getYaw(), location.getPitch());
				targetPlayerObj.teleport(newLoc);
			}
			break;
			case "godzombies": {
				clicker.sendMessage(ChatColor.GOLD + "Spawning god mode zombies on your location and making them target " + targetPlayerObj.getName() + "!");
				Location location = clicker.getLocation();
				World world = location.getWorld();
				for (int i = 0; i < 4; i++) {
					Entity spawnEntity = world.spawnEntity(location, EntityType.ZOMBIE);
					module.addGodMode(spawnEntity);
					((Creature) spawnEntity).setTarget(targetPlayerObj);
				}
			}
			break;
			case "silverfish": {
				clicker.sendMessage(ChatColor.GOLD + "Next ore " + targetPlayerObj.getName() + " mines will spawn 4 silverfish!");
				module.addSilverfish(targetPlayerObj);
			}
			break;
			case "nuke": {
				clicker.sendMessage(ChatColor.GOLD + "Nuking " + targetPlayerObj.getName() + "!");
				Location location = targetPlayerObj.getLocation();
				World world = location.getWorld();
				world.createExplosion(location, 7F, true);
			}
			break;
			case "starve": {
				clicker.sendMessage(ChatColor.GOLD + "Draining " + targetPlayerObj.getName() + "'s fuel!");
				targetPlayerObj.setFoodLevel(0);
			}
			break;
			case "levitate": {
				clicker.sendMessage(ChatColor.GOLD + "Giving " + targetPlayerObj.getName() + " levitation for 30 seconds!");
				targetPlayerObj.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 600, 1), true);
			}
			break;
		}
	}
}
