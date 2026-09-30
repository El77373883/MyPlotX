package com.tuNombre.MyPlotX.commands;

import com.tuNombre.MyPlotX.MyPlotX;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlotAdminCommand implements CommandExecutor {

    private final MyPlotX plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlotAdminCommand(MyPlotX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("myplotx.admin")) {
            sender.sendMessage(mm.deserialize("<red>No tienes permiso."));
            return true;
        }

        if (args.length < 3 || !args[0].equalsIgnoreCase("create") || !args[1].equalsIgnoreCase("plotworld")) {
            sender.sendMessage(mm.deserialize("<yellow>Uso: /plotadmin create plotworld <nombre>"));
            return true;
        }

        String worldName = args[2];
        if (plugin.getServer().getWorld(worldName) != null) {
            sender.sendMessage(mm.deserialize("<red>Ese mundo ya existe."));
            return true;
        }

        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new com.tuNombre.MyPlotX.PlotGenerator(plugin));
        World world = creator.createWorld();

        if (world == null) {
            sender.sendMessage(mm.deserialize("<red>No se pudo crear el mundo."));
            return true;
        }

        world.setSpawnLocation(0, plugin.getConfig().getInt("plot-world.plot-height", 64) + 1, 0);

        sender.sendMessage(mm.deserialize("<green>Mundo de parcelas '" + worldName + "' creado."));

        if (sender instanceof Player player) {
            player.teleport(world.getSpawnLocation());
        }
        return true;
    }
}
