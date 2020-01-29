package hk.siggi.bukkit.plugcubebuildersin.punisher;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface PunisherMenu {
	public void openInventory();
	public void handleClickEvent(InventoryClickEvent event);
}
