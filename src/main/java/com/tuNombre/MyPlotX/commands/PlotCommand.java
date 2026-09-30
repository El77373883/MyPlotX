package com.tuNombre.MyPlotX.commands;

import com.tuNombre.MyPlotX.MyPlotX;
import com.tuNombre.MyPlotX.Plot;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotCommand implements CommandExecutor {

    private final MyPlotX plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlotCommand(MyPlotX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(mm.deserialize("<yellow>/plot <claim|auto|home|list|sell|buy|add|remove>"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "claim" -> handleClaim(player);
            case "home" -> handleHome(player);
            case "list" -> handleList(player);
            case "sell" -> handleSell(player, args);
            case "buy" -> handleBuy(player);
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            default -> player.sendMessage(mm.deserialize("<red>Subcomando desconocido."));
        }
        return true;
    }

    private void handleClaim(Player player) {
        Plot existing = plugin.getPlotManager().getPlotAt(player.getLocation());
        if (existing != null) {
            player.sendMessage(mm.deserialize("<red>Esta parcela ya está reclamada."));
            return;
        }
        double price = plugin.getConfig().getDouble("economy.claim-price", 100.0);
        if (plugin.getEconomyService().isEnabled() && !plugin.getEconomyService().has(player, price)) {
            player.sendMessage(mm.deserialize("<red>No tienes suficiente dinero. Necesitas " + price));
            return;
        }
        if (plugin.getEconomyService().isEnabled()) {
            plugin.getEconomyService().withdraw(player, price);
        }
        Plot plot = plugin.getPlotManager().claimPlot(player.getLocation(), player.getUniqueId());
        if (plot == null) {
            player.sendMessage(mm.deserialize("<red>No puedes reclamar aquí."));
            return;
        }
        player.sendMessage(mm.deserialize("<green>¡Has reclamado la parcela!"));
    }

    private void handleHome(Player player) {
        for (Plot plot : plugin.getPlotManager().getPlots().values()) {
            if (plot.isOwner(player.getUniqueId())) {
                String[] parts = plot.getId().split(";");
                int cellX = Integer.parseInt(parts[0]);
                int cellZ = Integer.parseInt(parts[1]);
                int plotSize = plugin.getConfig().getInt("plot-world.plot-size", 32);
                int pathWidth = plugin.getConfig().getInt("plot-world.path-width", 8);
                int totalSize = plotSize + pathWidth;

                int x = cellX * totalSize + pathWidth + plotSize / 2;
                int z = cellZ * totalSize + pathWidth + plotSize / 2;
                int y = plugin.getConfig().getInt("plot-world.plot-height", 64) + 1;

                player.teleport(new org.bukkit.Location(player.getWorld(), x + 0.5, y, z + 0.5));
                player.sendMessage(mm.deserialize("<green>Teletransportado a tu parcela."));
                return;
            }
        }
        player.sendMessage(mm.deserialize("<red>No tienes parcelas."));
    }

    private void handleList(Player player) {
        player.sendMessage(mm.deserialize("<yellow>Tus parcelas:"));
        for (Plot plot : plugin.getPlotManager().getPlots().values()) {
            if (plot.isOwner(player.getUniqueId())) {
                player.sendMessage(mm.deserialize("<gray>- " + plot.getId() + " " + (plot.isForSale() ? "<gold>[En venta]" : "")));
            }
        }
    }

    private void handleSell(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<red>Uso: /plot sell <precio>"));
            return;
        }
        double price;
        try {
            price = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize("<red>Precio inválido."));
            return;
        }
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(mm.deserialize("<red>No eres el dueño de esta parcela."));
            return;
        }
        plot.setForSale(true);
        plot.setPrice(price);
        plugin.getPlotManager().save();
        player.sendMessage(mm.deserialize("<green>Parcela en venta por " + price));
    }

    private void handleBuy(Player player) {
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        if (plot == null || !plot.isForSale()) {
            player.sendMessage(mm.deserialize("<red>Esta parcela no está en venta."));
            return;
        }
        if (!plugin.getEconomyService().has(player, plot.getPrice())) {
            player.sendMessage(mm.deserialize("<red>No tienes suficiente dinero."));
            return;
        }
        plugin.getEconomyService().withdraw(player, plot.getPrice());
        if (plot.getOwner() != null) {
            plugin.getEconomyService().deposit(plugin.getServer().getPlayer(plot.getOwner()), plot.getPrice());
        }
        plot.setOwner(player.getUniqueId());
        plot.setForSale(false);
        plugin.getPlotManager().save();
        player.sendMessage(mm.deserialize("<green>¡Has comprado la parcela!"));
    }

    private void handleAdd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<red>Uso: /plot add <jugador>"));
            return;
        }
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(mm.deserialize("<red>No eres el dueño."));
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(mm.deserialize("<red>Jugador no encontrado."));
            return;
        }
        plot.getMembers().add(target.getUniqueId());
        plugin.getPlotManager().save();
        player.sendMessage(mm.deserialize("<green>Añadido " + target.getName()));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(mm.deserialize("<red>Uso: /plot remove <jugador>"));
            return;
        }
        Plot plot = plugin.getPlotManager().getPlotAt(player.getLocation());
        if (plot == null || !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(mm.deserialize("<red>No eres el dueño."));
            return;
        }
        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(mm.deserialize("<red>Jugador no encontrado."));
            return;
        }
        plot.getMembers().remove(target.getUniqueId());
        plugin.getPlotManager().save();
        player.sendMessage(mm.deserialize("<green>Eliminado " + target.getName()));
    }
}
