package hk.siggi.bukkit.plugcubebuildersin.module.minigamehub;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface MinigameInventoryHandler {
	public void inventoryClick(InventoryClickEvent event);
	public void inventoryClose(InventoryCloseEvent event);
}
