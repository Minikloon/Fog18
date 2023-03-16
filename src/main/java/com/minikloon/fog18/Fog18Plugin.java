package com.minikloon.fog18;

import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutGameStateChange;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.BiConsumer;

public class Fog18Plugin extends JavaPlugin {
    @Override
    public void onEnable() {
        super.onEnable();

        registerCommand("fog", (player, args) -> {
            float value = Float.parseFloat(args[0]);
            EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(2, 0));
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, 1000f));
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, value));
            player.sendMessage(ChatColor.YELLOW + "Sent rain game state to " + ChatColor.AQUA + value);
        });

        int[] around = {-2, -1, 0, 1, 2};
        registerCommand("biome", (player, args) -> {
            Biome biome = Biome.valueOf(args[0].toUpperCase());
            Block base = player.getLocation().getBlock();

            for (int x : around) {
                for (int z : around) {
                    base.getWorld().setBiome(base.getX() + x, base.getZ() + z, biome);
                }
            }
            updateChunkForPlayer(player, base.getChunk());

            player.sendMessage(ChatColor.YELLOW + "Set the biome around you to " + ChatColor.GREEN + biome.name() + ChatColor.YELLOW + "!");
        });

        registerCommand("blindness", (player, args) -> {
            int durationTicks = Integer.parseInt(args[0]);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 9));
            player.sendMessage(ChatColor.YELLOW + "Added blindness!");
        });

        registerCommand("nightvision", (player, args) -> {
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                player.sendMessage(ChatColor.YELLOW + "Night Vision turned " + ChatColor.RED + "OFF" + ChatColor.YELLOW + "!");
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE / 2, 9));
                player.sendMessage(ChatColor.YELLOW + "Night Vision turned " + ChatColor.GREEN + "ON" + ChatColor.YELLOW + "!");
            }
        });

        registerCommand("env", (player, args) -> {
            int index = Integer.parseInt(args[0]);
            World.Environment environment = World.Environment.values()[index];
            World world = Bukkit.getWorlds().stream()
                    .filter(w -> w.getEnvironment() == environment)
                    .findFirst().orElse(null);
            if (world == null) {
                player.sendMessage(ChatColor.RED + "There is no matching world!");
                return;
            }

            Location spawnLoc = world.getSpawnLocation();
            if (environment == World.Environment.THE_END) {
                spawnLoc.setY(100);
            }

            player.teleport(spawnLoc);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "Sent you to " + ChatColor.WHITE + environment.name() + ChatColor.YELLOW + "!");
        });
    }

    private void updateChunkForPlayer(Player player, Chunk chunk) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.chunkCoordIntPairQueue.add(new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
    }

    private void registerCommand(String command, BiConsumer<Player, String[]> executor) {
        getCommand(command).setExecutor((sender, command1, label, args) -> {
            executor.accept((Player) sender, args);
            return true;
        });
    }
}
