package com.comorichico.searchcontainer;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Collections;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
/**
 *
 * @author comorichico
 */
public class SearchContainer extends JavaPlugin implements Listener,TabExecutor{
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.setCommandExecutor(this, "sc");
    }
    private void setCommandExecutor(@NotNull CommandExecutor executor, String @NotNull ... commands) {
        for (String commandName : commands) {
            PluginCommand command = this.getCommand(commandName);
            if (command != null) {
                command.setExecutor(executor);
            }
        }
    }
    
    @Override
    public void onDisable() {
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            // Must supply material
            return false;
        }

        Material material = Material.getMaterial(args[0].toUpperCase());

        if (material == null) {
            sender.sendMessage(args[0] + "というアイテムは存在しません。");
            return false;
        }

        int radius = 5;

        if (args.length > 1) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                // Invalid radius supplied
                return false;
            }
        }

        radius = Math.max(0, Math.min(radius, 10));
        Player player = (Player)sender;
        World world = player.getWorld();
        Chunk centerChunk = player.getLocation().getChunk();
        StringBuilder locations = new StringBuilder();

        for (int dX = -radius; dX <= radius; ++dX) {
            for (int dZ = -radius; dZ <= radius; ++dZ) {
                if (!world.loadChunk(centerChunk.getX() + dX, centerChunk.getZ() + dZ, false)) {
                    continue;
                }
                Chunk chunk = world.getChunkAt(centerChunk.getX() + dX, centerChunk.getZ() + dZ);
                for (BlockState tileEntity : chunk.getTileEntities()) {
                    if (!(tileEntity instanceof InventoryHolder holder)) {
                        continue;
                    }
                    if (!holder.getInventory().contains(material)) {
                        continue;
                    }
                    locations.append(holder.getInventory().getType().name().toLowerCase()).append(" (")
                            .append(tileEntity.getX()).append(',').append(tileEntity.getY()).append(',')
                            .append(tileEntity.getZ()).append("), ");
                }
            }
        }

        // Matches found, delete trailing comma and space
        if (locations.length() > 0) {
            locations.delete(locations.length() - 2, locations.length());
        } else {
            sender.sendMessage(material.name() + "は見つかりませんでした。");
            return true;
        }

        sender.sendMessage(material.name() + "が見つかりました。" + locations.toString());
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 1 || args.length > 2 || !command.testPermissionSilent(sender)) {
            return Collections.emptyList();
        }

        String argument = args[args.length - 1];
        if (args.length == 1) {
            return TabCompleter.completeEnum(argument, Material.class);
        } else {
            return TabCompleter.completeInteger(argument);
        }
    }
}
