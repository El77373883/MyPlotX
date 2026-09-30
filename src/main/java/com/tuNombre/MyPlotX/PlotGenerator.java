package com.tuNombre.MyPlotX;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class PlotGenerator extends ChunkGenerator {

    private final int plotSize;
    private final int pathWidth;
    private final int totalSize;
    private final int plotHeight;
    private final Material floorBlock;
    private final Material pathBlock;

    public PlotGenerator(MyPlotX plugin) {
        this.plotSize = plugin.getConfig().getInt("plot-world.plot-size", 32);
        this.pathWidth = plugin.getConfig().getInt("plot-world.path-width", 8);
        this.totalSize = plotSize + pathWidth;
        this.plotHeight = plugin.getConfig().getInt("plot-world.plot-height", 64);
        this.floorBlock = Material.matchMaterial(plugin.getConfig().getString("plot-world.floor-block", "GRASS_BLOCK"));
        this.pathBlock = Material.matchMaterial(plugin.getConfig().getString("plot-world.path-block", "SMOOTH_STONE"));
    }

    @Override
    public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int chunkX, int chunkZ, @NotNull BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);

        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;

                boolean isPath = isPath(worldX, worldZ);
                Material block = isPath ? pathBlock : floorBlock;

                chunk.setBlock(worldX, plotHeight, worldZ, block);

                for (int y = plotHeight - 1; y > plotHeight - 5; y--) {
                    chunk.setBlock(worldX, y, worldZ, Material.STONE);
                }
            }
        }
        return chunk;
    }

    public boolean isPath(int x, int z) {
        int modX = Math.floorMod(x, totalSize);
        int modZ = Math.floorMod(z, totalSize);
        return modX < pathWidth || modZ < pathWidth;
    }

    public boolean isPlot(int x, int z) {
        return !isPath(x, z);
    }
}