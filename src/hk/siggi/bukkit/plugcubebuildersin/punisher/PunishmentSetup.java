package hk.siggi.bukkit.plugcubebuildersin.punisher;

import hk.siggi.bukkit.plugcubebuildersin.util.TimeUtil;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PunishmentSetup implements PunisherMenu {

	private final PunisherModuleImpl module;
	private final Player p;
	private final UUID targetPlayer;
	private final String playerName;
	private final String skinPayload;
	private final String skinSignature;
	private final String offence;
	private String type;
	private long length;
	private final boolean allowTroll;
	private final boolean allowMute;
	private final boolean allowBan;

	private Inventory openedInventory = null;

	private Inventory getInventory() {
		return openedInventory;
	}

	PunishmentSetup(
			PunisherModuleImpl module,
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
		this.module = module;
		this.p = p;
		this.targetPlayer = targetPlayer;
		this.playerName = playerName;
		this.skinPayload = skinPayload;
		this.skinSignature = skinSignature;
		this.offence = offence;
		this.type = preselectedType;
		this.length = preselectedLength;
		this.allowTroll = allowTroll;
		this.allowMute = allowMute;
		this.allowBan = allowBan;
	}

	@Override
	public void openInventory() {
		Inventory inv = module.plugin.getServer().createInventory(p, 54, "Setup Punishment");
		openedInventory = inv;
		updateInventory();
		InventoryView view = p.openInventory(inv);
		Inventory topInventory = view.getTopInventory();
		module.setInventory(topInventory, this);
	}

	private void updateInventory() {
		int[] row = new int[]{0, 9, 18, 27, 36, 45};
		Inventory inv = getInventory();
		if (inv == null) {
			return;
		}
		String selectedType = type;
		String selectedLength = length == -1L ? "Permanent" : TimeUtil.timeToString(length, 3);
		{
			ItemStack playerHead = module.plugin.createSkull(targetPlayer, playerName, skinPayload, skinSignature);
			ItemMeta meta = playerHead.getItemMeta();
			meta.setDisplayName(ChatColor.WHITE + "Punishing " + ChatColor.GOLD + playerName);
			meta.setLore(Arrays.asList(new String[]{ChatColor.WHITE + "Offence: " + ChatColor.GOLD + offence}));
			playerHead.setItemMeta(meta);
			inv.setItem(row[0] + 0, playerHead);
		}
		{
			ItemStack warning, mute, ban;
			try {
				warning = setName(new ItemStack(Material.YELLOW_TERRACOTTA), ChatColor.RESET + "Warning");
				mute = setName(new ItemStack(Material.ORANGE_TERRACOTTA), ChatColor.RESET + "Mute");
				ban = setName(new ItemStack(Material.RED_TERRACOTTA), ChatColor.RESET + "Ban");
			} catch (Throwable t) {
				Material stainedClay = Material.getMaterial("STAINED_CLAY");
				warning = setName(new ItemStack(stainedClay, 1, (short) 4), ChatColor.RESET + "Warning");
				mute = setName(new ItemStack(stainedClay, 1, (short) 1), ChatColor.RESET + "Mute");
				ban = setName(new ItemStack(stainedClay, 1, (short) 14), ChatColor.RESET + "Ban");
			}
			inv.setItem(row[1] + 0, warning);
			inv.setItem(row[1] + 1, mute);
			inv.setItem(row[1] + 2, ban);
		}
		{
			clearRow(row[2]);
			int pos = -1;
			String selected = null;
			switch (type) {
				case "warning":
					pos = 0;
					selected = "Warning";
					break;
				case "mute":
					pos = 1;
					selected = "Mute";
					break;
				case "ban":
					pos = 2;
					selected = "Ban";
					break;
			}
			if (pos != -1) {
				inv.setItem(row[2] + pos, setName(new ItemStack(Material.GHAST_TEAR), ChatColor.RESET + "Selected: " + selected));
			}
		}
		if (type.equals("warning")) {
			clearRow(row[3]);
			clearRow(row[4]);
		} else {
			{
				try {
					inv.setItem(row[3] + 0, setName(new ItemStack(Material.BLUE_STAINED_GLASS_PANE), ChatColor.RESET + "15 minutes"));
					inv.setItem(row[3] + 1, setName(new ItemStack(Material.GREEN_STAINED_GLASS_PANE), ChatColor.RESET + "30 minutes"));
					inv.setItem(row[3] + 2, setName(new ItemStack(Material.LIME_STAINED_GLASS_PANE), ChatColor.RESET + "1 hour"));
					inv.setItem(row[3] + 3, setName(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE), ChatColor.RESET + "2 hours"));
					inv.setItem(row[3] + 4, setName(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE), ChatColor.RESET + "24 hours"));
					inv.setItem(row[3] + 5, setName(new ItemStack(Material.RED_STAINED_GLASS_PANE), ChatColor.RESET + "2 days"));
					inv.setItem(row[3] + 6, setName(new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE), ChatColor.RESET + "7 days"));
					inv.setItem(row[3] + 7, setName(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE), ChatColor.RESET + "14 days"));
				} catch (Throwable e) {
					Material stainedGlassPane = Material.getMaterial("STAINED_GLASS_PANE");
					inv.setItem(row[3] + 0, setName(new ItemStack(stainedGlassPane, 1, (short) 11), ChatColor.RESET + "15 minutes"));
					inv.setItem(row[3] + 1, setName(new ItemStack(stainedGlassPane, 1, (short) 13), ChatColor.RESET + "30 minutes"));
					inv.setItem(row[3] + 2, setName(new ItemStack(stainedGlassPane, 1, (short) 5), ChatColor.RESET + "1 hour"));
					inv.setItem(row[3] + 3, setName(new ItemStack(stainedGlassPane, 1, (short) 4), ChatColor.RESET + "2 hours"));
					inv.setItem(row[3] + 4, setName(new ItemStack(stainedGlassPane, 1, (short) 1), ChatColor.RESET + "24 hours"));
					inv.setItem(row[3] + 5, setName(new ItemStack(stainedGlassPane, 1, (short) 14), ChatColor.RESET + "2 days"));
					inv.setItem(row[3] + 6, setName(new ItemStack(stainedGlassPane, 1, (short) 2), ChatColor.RESET + "7 days"));
					inv.setItem(row[3] + 7, setName(new ItemStack(stainedGlassPane, 1, (short) 10), ChatColor.RESET + "14 days"));
				}
				inv.setItem(row[3] + 8, setName(new ItemStack(Material.END_CRYSTAL), ChatColor.RESET + "Permanent"));
			}
			{
				clearRow(row[4]);
				int pos = -1;
				String selected = null;
				if (length == 60000L * 15L) {
					pos = 0;
					selected = "15 minutes";
				} else if (length == 60000L * 30L) {
					pos = 1;
					selected = "30 minutes";
				} else if (length == 3600000L) {
					pos = 2;
					selected = "1 hour";
				} else if (length == 3600000L * 2L) {
					pos = 3;
					selected = "2 hours";
				} else if (length == 86400000L) {
					pos = 4;
					selected = "24 hours";
				} else if (length == 86400000L * 2L) {
					pos = 5;
					selected = "2 days";
				} else if (length == 86400000L * 7L) {
					pos = 6;
					selected = "7 days";
				} else if (length == 86400000L * 14L) {
					pos = 7;
					selected = "14 days";
				} else if (length == -1L) {
					pos = 8;
					selected = "Permanent";
				}
				if (pos != -1) {
					inv.setItem(row[4] + pos, setName(new ItemStack(Material.GHAST_TEAR), ChatColor.RESET + "Selected: " + selected));
				}
			}
		}
		{
			inv.setItem(row[5] + 0, setName(new ItemStack(Material.BARRIER), ChatColor.RESET + "Cancel"));
			ItemStack confirm = new ItemStack(Material.EMERALD);
			ItemMeta meta = confirm.getItemMeta();
			meta.setDisplayName(ChatColor.RESET + "Confirm");
			List<String> lore = new LinkedList<>();
			lore.addAll(Arrays.asList(new String[]{
				ChatColor.RESET + "Punishing " + playerName,
				ChatColor.RESET + "Offence: " + offence,
				ChatColor.RESET + "Punishment: " + type
			}));
			if (length != 0L) {
				lore.add(ChatColor.RESET + "Length: " + selectedLength);
			}
			meta.setLore(lore);
			confirm.setItemMeta(meta);
			inv.setItem(row[5] + 8, confirm);
		}
	}

	private void clearRow(int startPos) {
		Inventory inv = getInventory();
		if (inv == null) {
			return;
		}
		for (int i = 0; i < 9; i++) {
			inv.clear(startPos + i);
		}
	}

	@Override
	public void handleClickEvent(InventoryClickEvent event) {
		event.setCancelled(true);
		int[] row = new int[]{0, 9, 18, 27, 36, 45};
		int slot = event.getSlot();
		if (slot == row[1] + 0) {
			type = "warning";
			length = 0L;
		} else if (slot == row[1] + 1) {
			if (allowMute) {
				type = "mute";
			}
		} else if (slot == row[1] + 2) {
			if (allowBan) {
				type = "ban";
			}
		} else if (slot == row[3] + 0) {
			length = 60000L * 15L;
		} else if (slot == row[3] + 1) {
			length = 60000L * 30L;
		} else if (slot == row[3] + 2) {
			length = 3600000L;
		} else if (slot == row[3] + 3) {
			length = 3600000L * 2L;
		} else if (slot == row[3] + 4) {
			length = 86400000L;
		} else if (slot == row[3] + 5) {
			length = 86400000L * 2L;
		} else if (slot == row[3] + 6) {
			length = 86400000L * 7L;
		} else if (slot == row[3] + 7) {
			length = 86400000L * 14L;
		} else if (slot == row[3] + 8) {
			length = -1L;
		} else if (slot == row[5] + 0) {
			module.openPunisher(p, targetPlayer, playerName, skinPayload, skinSignature, allowTroll, allowMute, allowBan);
		} else if (slot == row[5] + 8) {
			p.closeInventory();
			module.confirmOffence(p, targetPlayer, offence, type, length);
		}
		if (!type.equals("warning") && length == 0L) {
			length = 60000L * 15L;
		}
		updateInventory();
	}

	private ItemStack setName(ItemStack stack, String name) {
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);
		return stack;
	}

}
