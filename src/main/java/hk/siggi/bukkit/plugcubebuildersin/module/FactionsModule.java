package hk.siggi.bukkit.plugcubebuildersin.module;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface FactionsModule extends Listener, Module {
	public double getPower(Player player);
	public double getPower(UUID player);
	public void alterPower(Player player, double power);
	public void alterPower(UUID player, double power);
	public double getMinimumPower();
	public double getMaximumPower();
	public double getDefaultPower();
	public void setBypassing(Player player, boolean bypass);
	public boolean isBypassing(Player player);
}
