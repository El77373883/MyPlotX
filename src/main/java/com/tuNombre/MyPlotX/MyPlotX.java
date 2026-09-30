package com.tuNombre.MyPlotX;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MyPlotX extends JavaPlugin {

    private PlotManager plotManager;
    private EconomyService economyService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.plotManager = new PlotManager(this);
        this.economyService = new EconomyService(this);

        getServer().getPluginManager().registerEvents(new PlotListener(this), this);

        getCommand("plotadmin").setExecutor(new commands.PlotAdminCommand(this));
        getCommand("plot").setExecutor(new commands.PlotCommand(this));

        getLogger().info("MyPlotX habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MyPlotX deshabilitado.");
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        if (worldName.equalsIgnoreCase("plotworld")) {
            return new PlotGenerator(this);
        }
        return null;
    }

    public PlotManager getPlotManager() { return plotManager; }
    public EconomyService getEconomyService() { return economyService; }
}