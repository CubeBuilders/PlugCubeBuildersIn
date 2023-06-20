/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import static hk.siggi.bukkit.plugcubebuildersin.util.Util.tryClose;
import io.siggi.nbt.NBTCompound;
import io.siggi.nbt.NBTToolBukkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Siggi
 */
public class MinigameUtil {

	private MinigameUtil() {
	}

	public static ItemStack setInfo(ItemStack stack, String info) {
		try {
			NBTCompound tag = NBTToolBukkit.getTag(stack);
			if (tag == null) {
				tag = new NBTCompound();
			}
			tag.setString("minigameInfo", info);
			tag.setByte("HideFlags", (byte) 63);
			stack = NBTToolBukkit.setTag(stack, tag);
		} catch (Exception e) {
		}
		return stack;
	}

	public static String getInfo(ItemStack stack) {
		try {
			return NBTToolBukkit.getTag(stack).getString("minigameInfo");
		} catch (Exception e) {
		}
		return null;
	}

	public static ItemStack timeLeftItem(int timeLeft) {
		ItemStack stack = new ItemStack(getClockItem(), timeLeft < 1 ? 1 : timeLeft);
		ItemMeta meta = stack.getItemMeta();
		if (timeLeft == -1) {
			meta.setDisplayName(ChatColor.RESET + "No time limit");
		} else {
			meta.setDisplayName(ChatColor.RESET + Integer.toString(timeLeft) + " second" + (timeLeft == 1 ? "" : "s") + " left to vote");
		}
		stack.setItemMeta(meta);
		return stack;
	}

	/**
	 * 1.12/1.13 compatibility workaround.
	 *
	 * @return
	 */
	private static Material getClockItem() {
		if (timeLeftItem == null) {
			try {
				timeLeftItem = Material.CLOCK;
			} catch (Throwable t) {
				timeLeftItem = Material.getMaterial("WATCH");
			}
		}
		return timeLeftItem;
	}
	private static Material timeLeftItem;

	public static ItemStack changeMinigameItem() {
		ItemStack stack = new ItemStack(Material.BARRIER);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "Choose New Minigame");
		stack.setItemMeta(meta);
		return setInfo(stack, "choosenewminigame");
	}

	/**
	 * 1.12/1.13 compatibility workaround.
	 *
	 * @return
	 */
	public static ItemStack quitItem() {
		ItemStack stack = new ItemStack(getDoorItem());
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + "Leave This Group");
		stack.setItemMeta(meta);
		return stack;
	}

	private static Material doorItem = null;

	private static Material getDoorItem() {
		if (doorItem == null) {
			try {
				doorItem = Material.OAK_DOOR;
			} catch (Throwable t) {
				doorItem = Material.getMaterial("WOOD_DOOR");
			}
		}
		return doorItem;
	}

	public static void giveItem(Player p, int slot, MinigameItem item) {
		PlayerInventory inv = p.getInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack != null && item.test(stack)) {
				if (i < 9) {
					inv.setHeldItemSlot(i);
				}
				return;
			}
		}
		ItemStack stack = inv.getItem(slot);
		if (stack != null && stack.getType() != Material.AIR) {
			int emptySlot = inv.firstEmpty();
			if (emptySlot == -1) {
				return;
			}
			inv.setItem(emptySlot, stack);
		}
		inv.setItem(slot, item.create());
		if (slot < 9) {
			inv.setHeldItemSlot(slot);
		}
	}

	public static void removeItem(Player p, MinigameItem item) {
		Inventory inv = p.getInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack != null && item.test(stack)) {
				inv.clear(i);
			}
		}
	}

	public static boolean isSpecialItem(ItemStack stack) {
		try {
			NBTCompound tag = NBTToolBukkit.getTag(stack);
			String x = tag.getString("minigameitem");
			return x != null && !x.isEmpty();
		} catch (Exception e) {
			return false;
		}
	}

	public static ItemStack setVotes(ItemStack stack, int votes) {
		NBTCompound tag = NBTToolBukkit.getTag(stack);
		if (tag == null) {
			tag = new NBTCompound();
		}
		tag.setByte("HideFlags", (byte) 63);
		stack = NBTToolBukkit.setTag(stack, tag);
		ItemMeta itemMeta = stack.getItemMeta();
		for (Enchantment enc : itemMeta.getEnchants().keySet()) {
			itemMeta.removeEnchant(enc);
		}
		List<String> lore = itemMeta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}
		for (Iterator<String> it = lore.iterator(); it.hasNext();) {
			String line = it.next();
			if (line.endsWith(" vote") || line.endsWith(" votes")) {
				it.remove();
			}
		}
		lore.add(0, votes + " vote" + (votes == 1 ? "" : "s"));
		itemMeta.setLore(lore);
		stack.setItemMeta(itemMeta);
		stack.setAmount(votes < 1 ? 1 : votes);
		if (votes > 0) {
			stack.addUnsafeEnchantment(Enchantment.LUCK, 1);
		}
		return stack;
	}

	public static ItemStack setNameAndLore(ItemStack stack, String name, String... lore) {
		List<String> loreList = null;
		if (lore != null) {
			loreList = new ArrayList<>();
			loreList.addAll(Arrays.asList(lore));
		}
		ItemMeta itemMeta = stack.getItemMeta();
		itemMeta.setDisplayName(name);
		itemMeta.setLore(loreList);
		stack.setItemMeta(itemMeta);
		return stack;
	}

	public static final MinigameItem menuItem = new MinigameItem("menu") {

		@Override
		public ItemStack create() {
			ItemStack stack = new ItemStack(Material.BOOK);
			ItemMeta itemMeta = stack.getItemMeta();
			itemMeta.setDisplayName("Menu");
			stack.setItemMeta(itemMeta);
			return finish(stack);
		}
	};

	public static final MinigameItem voteItem = new MinigameItem("vote") {

		@Override
		public ItemStack create() {
			ItemStack stack = new ItemStack(getWoodenAxeMaterial());
			ItemMeta itemMeta = stack.getItemMeta();
			itemMeta.setDisplayName("Vote");
			stack.setItemMeta(itemMeta);
			return finish(stack);
		}
	};

	/**
	 * 1.12/1.13 compatibility workaround.
	 *
	 * @return
	 */
	private static Material getWoodenAxeMaterial() {
		if (woodAxe == null) {
			try {
				woodAxe = Material.WOODEN_AXE;
			} catch (Throwable t) {
				woodAxe = Material.getMaterial("WOOD_AXE");
			}
		}
		return woodAxe;
	}
	private static Material woodAxe = null;

	public static abstract class MinigameItem implements Predicate<ItemStack> {

		private final String minigameItemType;

		private MinigameItem(String minigameItemType) {
			this.minigameItemType = minigameItemType;
		}

		public abstract ItemStack create();

		protected final ItemStack finish(ItemStack stack) {
			NBTCompound tag = NBTToolBukkit.getTag(stack);
			if (tag == null) {
				tag = new NBTCompound();
			}
			tag.setByte("HideFlags", (byte) 63);
			tag.setString("minigameitem", minigameItemType);
			stack = NBTToolBukkit.setTag(stack, tag);
			return stack;
		}

		@Override
		public final boolean test(ItemStack t) {
			if (t == null) {
				return false;
			}
			try {
				NBTCompound tag = NBTToolBukkit.getTag(t);
				return tag.getString("minigameitem").equals(minigameItemType);
			} catch (Exception e) {
			}
			return false;
		}
	}

	public static String read(File file) {
		FileInputStream fis = null;
		try {
			return read(fis = new FileInputStream(file));
		} catch (Exception e) {
		} finally {
			tryClose(fis);
		}
		return null;
	}

	public static String read(InputStream in) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copy(in, out);
			return out.toString();
		} catch (Exception e) {
		}
		return null;
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[4096];
		int c;
		while ((c = in.read(b, 0, b.length)) != -1) {
			out.write(b, 0, c);
		}
	}
}
