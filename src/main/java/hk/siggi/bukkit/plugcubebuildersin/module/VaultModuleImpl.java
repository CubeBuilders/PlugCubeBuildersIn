package hk.siggi.bukkit.plugcubebuildersin.module;

import hk.siggi.bukkit.plugcubebuildersin.PlugCubeBuildersIn;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultModuleImpl implements VaultModule {
	private PlugCubeBuildersIn plugin = null;
	private Economy economy = null;
	
	@Override
	public void load(PlugCubeBuildersIn plugin){
		this.plugin = plugin;
	}
	@Override
	public void init() {
		if (!setupEconomy()) {
			throw new RuntimeException("Can't setup Vault");
		}
	}
	@Override
	public void kill() {
	}

	@Override
	public void tick() {
	}
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
		
        return (economy != null);
    }
	@Override
	public double getBalance(Player p) {
		return getBalance(p.getUniqueId());
	}
	@Override
	public double getBalance(UUID uuid) {
		return economy.getBalance(Bukkit.getOfflinePlayer(uuid));
	}
	@Override
	public boolean chargeMoney(Player p, double amount) {
		return chargeMoney(p.getUniqueId(), amount);
	}
	@Override
	public boolean chargeMoney(UUID uuid, double amount) {
		EconomyResponse response = economy.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), amount);
		return response.transactionSuccess();
	}
	@Override
	public boolean giveMoney(Player p, double amount) {
		return giveMoney(p.getUniqueId(), amount);
	}
	@Override
	public boolean giveMoney(UUID uuid, double amount) {
		EconomyResponse response = economy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
		return response.transactionSuccess();
	}
}
