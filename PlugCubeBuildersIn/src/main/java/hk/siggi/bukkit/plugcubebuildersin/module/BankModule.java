package hk.siggi.bukkit.plugcubebuildersin.module;

import java.util.UUID;
import org.bukkit.entity.Player;

public interface BankModule {
	public double getBalance(Player p);
	public double getBalance(UUID uuid);
	public boolean giveMoney(Player p, double amount);
	public boolean giveMoney(UUID uuid, double amount);
	public boolean chargeMoney(Player p, double amount);
	public boolean chargeMoney(UUID uuid, double amount);
}
