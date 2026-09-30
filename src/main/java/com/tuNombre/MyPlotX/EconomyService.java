package com.tuNombre.MyPlotX;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

public class EconomyService {

    private final MyPlotX plugin;
    private Economy economy;

    public EconomyService(MyPlotX plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault no encontrado. Economía deshabilitada.");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp == null) {
                    plugin.getLogger().warning("No se pudo obtener el proveedor de economía de Vault.");
                    return;
                }
                economy = rsp.getProvider();
                plugin.getLogger().info("Economía conectada: " + economy.getName());
            }
        }.runTaskLater(plugin, 1);
    }

    public boolean has(Player player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        if (economy == null) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean isEnabled() {
        return economy != null;
    }
}