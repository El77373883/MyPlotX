package com.tuNombre.MyPlotX;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlotListener implements Listener {

    private final MyPlotX plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, String> lastPlot = new HashMap<>();

    public PlotListener(MyPlotX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (isPath(loc)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        if (isPath(loc)) {
            event.setCancelled(true);
        }
    }

    private boolean isPath(Location loc) {
        if (!loc.getWorld().getName().equalsIgnoreCase("plotworld")) return false;
        if (!(loc.getWorld().getGenerator() instanceof PlotGenerator gen)) return false;
        return gen.isPath(loc.getBlockX(), loc.getBlockZ());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;

        Player player = event.getPlayer();
        Location to = event.getTo();

        if (!to.getWorld().getName().equalsIgnoreCase("plotworld")) return;

        String currentPlotId = getPlotId(to);
        String previousPlotId = lastPlot.get(player.getUniqueId());

        if (currentPlotId != null && !currentPlotId.equals(previousPlotId)) {
            lastPlot.put(player.getUniqueId(), currentPlotId);

            String owner = "Desconocido";
            String price = "100";

            Component title = mm.deserialize("<yellow>Parcela de " + owner);
            Component subtitle = mm.deserialize("<gold>Compra por " + price);

            player.showTitle(Title.title(title, subtitle));
        } else if (currentPlotId == null && previousPlotId != null) {
            lastPlot.remove(player.getUniqueId());
        }
    }

    private String getPlotId(Location loc) {
        if (!(loc.getWorld().getGenerator() instanceof PlotGenerator gen)) return null;
        if (!gen.isPlot(loc.getBlockX(), loc.getBlockZ())) return null;

        int plotSize = plugin.getConfig().getInt("plot-world.plot-size", 32);
        int pathWidth = plugin.getConfig().getInt("plot-world.path-width", 8);
        int totalSize = plotSize + pathWidth;

        int cellX = Math.floorDiv(loc.getBlockX(), totalSize);
        int cellZ = Math.floorDiv(loc.getBlockZ(), totalSize);

        return cellX + ";" + cellZ;
    }
}