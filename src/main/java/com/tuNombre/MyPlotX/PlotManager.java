package com.tuNombre.MyPlotX;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotManager {

    private final MyPlotX plugin;
    private final Map<String, Plot> plots = new HashMap<>();
    private final File file;
    private FileConfiguration data;

    public PlotManager(MyPlotX plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "plots.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear plots.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);

        for (String key : data.getKeys(false)) {
            String ownerStr = data.getString(key + ".owner");
            UUID owner = ownerStr == null ? null : UUID.fromString(ownerStr);
            Plot plot = new Plot(key, owner);
            plot.setForSale(data.getBoolean(key + ".for-sale", false));
            plot.setPrice(data.getDouble(key + ".price", 0.0));
            plot.setName(data.getString(key + ".name", ""));

            for (String m : data.getStringList(key + ".members")) {
                plot.getMembers().add(UUID.fromString(m));
            }
            for (String d : data.getStringList(key + ".denied")) {
                plot.getDenied().add(UUID.fromString(d));
            }
            plots.put(key, plot);
        }
        plugin.getLogger().info("Cargadas " + plots.size() + " parcelas.");
    }

    public void save() {
        for (String key : plots.keySet()) {
            data.set(key, null);
        }
        for (Plot plot : plots.values()) {
            String key = plot.getId();
            data.set(key + ".owner", plot.getOwner() == null ? null : plot.getOwner().toString());
            data.set(key + ".for-sale", plot.isForSale());
            data.set(key + ".price", plot.getPrice());
            data.set(key + ".name", plot.getName());
            data.set(key + ".members", plot.getMembers().stream().map(UUID::toString).toList());
            data.set(key + ".denied", plot.getDenied().stream().map(UUID::toString).toList());
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar plots.yml: " + e.getMessage());
        }
    }

    public Plot getPlot(String id) {
        return plots.get(id);
    }

    public Plot getPlotAt(Location loc) {
        if (!(loc.getWorld().getGenerator() instanceof PlotGenerator gen)) return null;
        if (!gen.isPlot(loc.getBlockX(), loc.getBlockZ())) return null;

        int plotSize = plugin.getConfig().getInt("plot-world.plot-size", 32);
        int pathWidth = plugin.getConfig().getInt("plot-world.path-width", 8);
        int totalSize = plotSize + pathWidth;

        int cellX = Math.floorDiv(loc.getBlockX(), totalSize);
        int cellZ = Math.floorDiv(loc.getBlockZ(), totalSize);

        return plots.get(cellX + ";" + cellZ);
    }

    public Plot claimPlot(Location loc, UUID owner) {
        if (!(loc.getWorld().getGenerator() instanceof PlotGenerator gen)) return null;
        if (!gen.isPlot(loc.getBlockX(), loc.getBlockZ())) return null;

        int plotSize = plugin.getConfig().getInt("plot-world.plot-size", 32);
        int pathWidth = plugin.getConfig().getInt("plot-world.path-width", 8);
        int totalSize = plotSize + pathWidth;

        int cellX = Math.floorDiv(loc.getBlockX(), totalSize);
        int cellZ = Math.floorDiv(loc.getBlockZ(), totalSize);
        String id = cellX + ";" + cellZ;

        if (plots.containsKey(id)) return null;

        Plot plot = new Plot(id, owner);
        plots.put(id, plot);
        save();
        return plot;
    }

    public boolean deletePlot(String id) {
        if (plots.remove(id) != null) {
            save();
            return true;
        }
        return false;
    }

    public Map<String, Plot> getPlots() {
        return plots;
    }
}
