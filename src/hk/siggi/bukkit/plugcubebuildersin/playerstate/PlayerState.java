package hk.siggi.bukkit.plugcubebuildersin.playerstate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hk.siggi.bukkit.nbt.NBTTool;
import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.Collection;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class PlayerState {

	private String world = null;
	private double x = 0.0;
	private double y = 0.0;
	private double z = 0.0;
	private float yaw = 0.0f;
	private float pitch = 0.0f;
	private PotionEffect[] potionEffects = null;
	private String gamemode = "SURVIVAL";
	private double health = 0.0f;
	private int level = 0;
	private float exp = 0.0f;
	private float exhaustion = 0.0f;
	private int fireTicks = 0;
	private int foodLevel = 0;
	private float fallDistance = 0;
	private ItemStack[] inventory = null;
	private ItemStack[] enderchest = null;
	private ItemStack helmet;
	private ItemStack chest;
	private ItemStack legs;
	private ItemStack boots;
	private ItemStack offhand;

	public void blankState() {
		potionEffects = null;
		gamemode = "SURVIVAL";
		health = 20.0f;
		level = 0;
		exp = 0.0f;
		exhaustion = 0.0f;
		fireTicks = 0;
		foodLevel = 20;
		fallDistance = 0;
		inventory = null;
		enderchest = null;
		helmet = null;
		chest = null;
		legs = null;
		boots = null;
		offhand = null;
	}

	public PlayerState() {
	}

	public PlayerState(Player p, Location location) {
		if (location != null) {
			world = location.getWorld().getName();
			x = location.getX();
			y = location.getY();
			z = location.getZ();
			yaw = location.getYaw();
			pitch = location.getPitch();
		}
		Collection<PotionEffect> pe = p.getActivePotionEffects();
		potionEffects = pe.toArray(new PotionEffect[pe.size()]);
		gamemode = p.getGameMode().toString();
		health = p.getHealth();
		level = p.getLevel();
		exp = p.getExp();
		exhaustion = p.getExhaustion();
		fireTicks = p.getFireTicks();
		foodLevel = p.getFoodLevel();
		fallDistance = p.getFallDistance();
		PlayerInventory pInventory = p.getInventory();
		inventory = new ItemStack[36];
		for (int i = 0; i < inventory.length; i++) {
			inventory[i] = pInventory.getItem(i);
			if (inventory[i] != null) {
				if (inventory[i].getType() == Material.AIR) {
					inventory[i] = null;
				}
			}
		}
		helmet = pInventory.getHelmet();
		chest = pInventory.getChestplate();
		legs = pInventory.getLeggings();
		boots = pInventory.getBoots();
		offhand = pInventory.getItemInOffHand();
		if (offhand != null) {
			if (offhand.getType() == Material.AIR) {
				offhand = null;
			}
		}
		Inventory pEnderchest = p.getEnderChest();
		enderchest = new ItemStack[27];
		for (int i = 0; i < enderchest.length; i++) {
			enderchest[i] = pEnderchest.getItem(i);
			if (enderchest[i] != null) {
				if (enderchest[i].getType() == Material.AIR) {
					enderchest[i] = null;
				}
			}
		}
	}

	public void apply(Player p, GameMode defaultGamemode, boolean forceGameMode) {
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
		if (potionEffects != null) {
			for (PotionEffect pe : potionEffects) {
				p.addPotionEffect(pe);
			}
		}
		if (forceGameMode) {
			p.setGameMode(defaultGamemode);
		} else {
			try {
				p.setGameMode(GameMode.valueOf(gamemode));
			} catch (Exception e) {
				p.setGameMode(defaultGamemode);
			}
		}
		p.setHealth(health);
		p.setLevel(level);
		p.setExp(exp);
		p.setExhaustion(exhaustion);
		p.setFireTicks(fireTicks);
		p.setFoodLevel(foodLevel);
		PlayerInventory pInventory = p.getInventory();
		pInventory.clear();
		PlugCubeBuildersIn pcbi = PlugCubeBuildersIn.getInstance();
		if (inventory != null) {
			int inventorySize = Math.min(36, inventory.length);
			for (int i = 0; i < inventorySize; i++) {
				if (inventory[i] != null) {
					if (!pcbi.isHackedItem(inventory[i])) {
						pInventory.setItem(i, inventory[i]);
					}
				}
			}
		}
		if (helmet != null) {
			if (!pcbi.isHackedItem(helmet)) {
				pInventory.setHelmet(helmet);
			}
		}
		if (chest != null) {
			if (!pcbi.isHackedItem(chest)) {
				pInventory.setChestplate(chest);
			}
		}
		if (legs != null) {
			if (!pcbi.isHackedItem(legs)) {
				pInventory.setLeggings(legs);
			}
		}
		if (boots != null) {
			if (!pcbi.isHackedItem(boots)) {
				pInventory.setBoots(boots);
			}
		}
		if (offhand != null) {
			if (!pcbi.isHackedItem(offhand)) {
				pInventory.setItemInOffHand(offhand);
			}
		}
		Inventory pEnderchest = p.getEnderChest();
		pEnderchest.clear();
		if (enderchest != null) {
			int enderchestSize = Math.min(27, enderchest.length);
			for (int i = 0; i < enderchestSize; i++) {
				if (enderchest[i] != null) {
					if (!pcbi.isHackedItem(enderchest[i])) {
						pEnderchest.setItem(i, enderchest[i]);
					}
				}
			}
		}
	}

	public void teleport(Player p) {
		Location loc = null;
		try {
			PlugCubeBuildersIn.getInstance().loadWorld(world);
			loc = new Location(p.getServer().getWorld(world), x, y, z, yaw, pitch);
			p.setFallDistance(fallDistance);
		} catch (Exception e) {
		}
		if (loc != null) {
			p.teleport(loc);
		}
	}

	private static final Gson gson;

	static {
		gson = NBTTool.getSerializer().registerTo(new GsonBuilder()).registerTypeAdapter(PotionEffect.class, new PotionEffectAdapter()).setPrettyPrinting().create();
	}

	public String toJson() {
		return gson.toJson(this);
	}

	public static PlayerState fromJson(String json) {
		return gson.fromJson(json, PlayerState.class);
	}
}
