package cn.ningmo.playerspawn.commands;

import cn.ningmo.playerspawn.utils.SpawnUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import cn.ningmo.playerspawn.PlayerSpawn;
import cn.ningmo.playerspawn.utils.MessageUtils;

public class PlayerSpawnCommand implements CommandExecutor {
    private final PlayerSpawn plugin;
    
    public PlayerSpawnCommand(PlayerSpawn plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "set":
                handleSetCommand(sender, args);
                break;
            case "group":
                handleGroupCommand(sender, args);
                break;
            case "floodgate":
                handleFloodgateCommand(sender, args);
                break;
            case "reload":
                handleReloadCommand(sender);
                break;
            default:
                MessageUtils.sendMessage(sender, "commands.invalid-syntax");
                break;
        }
        
        return true;
    }
    
    /**
     * Send help message to the sender
     * @param sender The command sender
     */
    private void sendHelp(CommandSender sender) {
        MessageUtils.sendMessage(sender, "commands.help.header");
        MessageUtils.sendMessage(sender, "commands.help.set");
        MessageUtils.sendMessage(sender, "commands.help.group");
        
        // Only show floodgate command if sender has permission
        if (sender.hasPermission("playerspawn.floodgate")) {
            MessageUtils.sendMessage(sender, "commands.help.floodgate");
        }
        
        MessageUtils.sendMessage(sender, "commands.help.reload");
        MessageUtils.sendMessage(sender, "commands.help.help");
        MessageUtils.sendMessage(sender, "commands.help.footer");
    }
    
    /**
     * Handle the set command
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleSetCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("playerspawn.set")) {
            MessageUtils.sendMessage(sender, "commands.no-permission");
            return;
        }
        
        // Check if sender is a player
        if (!(sender instanceof Player) && args.length < 3) {
            MessageUtils.sendMessage(sender, "commands.player-only");
            return;
        }
        
        Player player = (sender instanceof Player) ? (Player) sender : null;
        Location location = null;
        Player targetPlayer = null;
        String worldName = null;
        boolean isServerSpawn = true;
        
        // Parse command arguments
        int argIndex = 1;
        
        // Parse location
        if (args.length > argIndex) {
            if (args[argIndex].equals("~")) {
                // Use current location
                if (player == null) {
                    MessageUtils.sendMessage(sender, "commands.player-only");
                    return;
                }
                location = player.getLocation();
                argIndex++;
            } else if (args.length > argIndex + 2) {
                // Parse x, y, z coordinates
                try {
                    double x = Double.parseDouble(args[argIndex++]);
                    double y = Double.parseDouble(args[argIndex++]);
                    double z = Double.parseDouble(args[argIndex++]);
                    
                    // Use current world if sender is a player
                    World world = player != null ? player.getWorld() : null;
                    location = new Location(world, x, y, z);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(sender, "commands.invalid-coordinates");
                    return;
                }
            }
        }
        
        // Parse player name
        if (args.length > argIndex) {
            targetPlayer = Bukkit.getPlayer(args[argIndex]);
            if (targetPlayer == null) {
                MessageUtils.sendMessage(sender, "commands.player-not-found", "{player}", args[argIndex]);
                return;
            }
            argIndex++;
        } else {
            // Use sender as target player if no player name is specified
            if (player == null) {
                MessageUtils.sendMessage(sender, "commands.player-only");
                return;
            }
            targetPlayer = player;
        }
        
        // Parse world or server
        if (args.length > argIndex) {
            if (args[argIndex].equalsIgnoreCase("server")) {
                isServerSpawn = true;
            } else {
                isServerSpawn = false;
                worldName = args[argIndex];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    MessageUtils.sendMessage(sender, "commands.world-not-found", "{world}", worldName);
                    return;
                }
                
                // Update location world if it was not set
                if (location != null && location.getWorld() == null) {
                    location = new Location(world, location.getX(), location.getY(), location.getZ(), 
                            location.getYaw(), location.getPitch());
                }
            }
        }
        
        // Use current location if no location is specified
        if (location == null) {
            if (player == null) {
                MessageUtils.sendMessage(sender, "commands.player-only");
                return;
            }
            location = player.getLocation();
        }
        
        // Set spawn point
        setPlayerSpawn(targetPlayer, location, worldName, isServerSpawn);
        
        // Send success message
        if (isServerSpawn) {
            MessageUtils.sendMessage(sender, "commands.set.success-server", 
                    "{player}", targetPlayer.getName(),
                    "{world}", location.getWorld().getName(),
                    "{x}", String.format("%.2f", location.getX()),
                    "{y}", String.format("%.2f", location.getY()),
                    "{z}", String.format("%.2f", location.getZ()));
        } else {
            MessageUtils.sendMessage(sender, "commands.set.success-world", 
                    "{player}", targetPlayer.getName(),
                    "{world}", location.getWorld().getName(),
                    "{x}", String.format("%.2f", location.getX()),
                    "{y}", String.format("%.2f", location.getY()),
                    "{z}", String.format("%.2f", location.getZ()));
        }
    }
    
    /**
     * Handle the group command
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleGroupCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("playerspawn.group")) {
            MessageUtils.sendMessage(sender, "commands.no-permission");
            return;
        }
        
        // Check if sender is a player
        if (!(sender instanceof Player) && args.length < 5) {
            MessageUtils.sendMessage(sender, "commands.player-only");
            return;
        }
        
        Player player = (sender instanceof Player) ? (Player) sender : null;
        
        // Check if enough arguments are provided
        if (args.length < 3) {
            MessageUtils.sendMessage(sender, "commands.invalid-syntax");
            return;
        }
        
        // Get group name
        String groupName = args[1];
        
        // Check if the command is 'set'
        if (!args[2].equalsIgnoreCase("set")) {
            MessageUtils.sendMessage(sender, "commands.invalid-syntax");
            return;
        }
        
        Location location = null;
        String worldName = null;
        boolean isServerSpawn = true;
        
        // Parse command arguments
        int argIndex = 3;
        
        // Parse location
        if (args.length > argIndex) {
            if (args[argIndex].equals("~")) {
                // Use current location
                if (player == null) {
                    MessageUtils.sendMessage(sender, "commands.player-only");
                    return;
                }
                location = player.getLocation();
                argIndex++;
            } else if (args.length > argIndex + 2) {
                // Parse x, y, z coordinates
                try {
                    double x = Double.parseDouble(args[argIndex++]);
                    double y = Double.parseDouble(args[argIndex++]);
                    double z = Double.parseDouble(args[argIndex++]);
                    
                    // Use current world if sender is a player
                    World world = player != null ? player.getWorld() : null;
                    location = new Location(world, x, y, z);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(sender, "commands.invalid-coordinates");
                    return;
                }
            }
        }
        
        // Skip player name parameter if provided
        if (args.length > argIndex) {
            Player targetPlayer = Bukkit.getPlayer(args[argIndex]);
            if (targetPlayer != null) {
                argIndex++;
            }
        }
        
        // Parse world or server
        if (args.length > argIndex) {
            if (args[argIndex].equalsIgnoreCase("server")) {
                isServerSpawn = true;
            } else {
                isServerSpawn = false;
                worldName = args[argIndex];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    MessageUtils.sendMessage(sender, "commands.world-not-found", "{world}", worldName);
                    return;
                }
                
                // Update location world if it was not set
                if (location != null && location.getWorld() == null) {
                    location = new Location(world, location.getX(), location.getY(), location.getZ(), 
                            location.getYaw(), location.getPitch());
                }
            }
        }
        
        // Use current location if no location is specified
        if (location == null) {
            if (player == null) {
                MessageUtils.sendMessage(sender, "commands.player-only");
                return;
            }
            location = player.getLocation();
        }
        
        // Set group spawn point
        setGroupSpawn(groupName, location, worldName, isServerSpawn);
        
        // Send success message
        if (isServerSpawn) {
            MessageUtils.sendMessage(sender, "commands.group.success-server", 
                    "{group}", groupName,
                    "{world}", location.getWorld().getName(),
                    "{x}", String.format("%.2f", location.getX()),
                    "{y}", String.format("%.2f", location.getY()),
                    "{z}", String.format("%.2f", location.getZ()));
        } else {
            MessageUtils.sendMessage(sender, "commands.group.success-world", 
                    "{group}", groupName,
                    "{world}", location.getWorld().getName(),
                    "{x}", String.format("%.2f", location.getX()),
                    "{y}", String.format("%.2f", location.getY()),
                    "{z}", String.format("%.2f", location.getZ()));
        }
    }
    
    /**
     * Handle the reload command
     * @param sender The command sender
     */
    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("playerspawn.reload")) {
            MessageUtils.sendMessage(sender, "commands.no-permission");
            return;
        }
        
        plugin.getConfigManager().loadConfigs();
        MessageUtils.sendMessage(sender, "commands.reload.success");
    }
    
    /**
     * Handle the floodgate command
     * @param sender The command sender
     * @param args The command arguments
     */
    private void handleFloodgateCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("playerspawn.floodgate")) {
            MessageUtils.sendMessage(sender, "commands.no-permission");
            return;
        }
        
        // Check if sender is a player
        if (!(sender instanceof Player) && args.length < 5) {
            MessageUtils.sendMessage(sender, "commands.player-only");
            return;
        }
        
        Player player = (sender instanceof Player) ? (Player) sender : null;
        
        // Check if enough arguments are provided
        if (args.length < 3) {
            MessageUtils.sendMessage(sender, "commands.invalid-syntax");
            return;
        }
        
        // Check if the command is 'set'
        if (!args[2].equalsIgnoreCase("set")) {
            MessageUtils.sendMessage(sender, "commands.invalid-syntax");
            return;
        }
        
        // Check if it's for new player or respawn
        String spawnType = args[1].toLowerCase();
        if (!spawnType.equals("newplayer") && !spawnType.equals("respawn")) {
            MessageUtils.sendMessage(sender, "commands.invalid-syntax");
            return;
        }
        
        Location location = null;
        String worldName = null;
        boolean isServerSpawn = true;
        
        // Parse command arguments
        int argIndex = 3;
        
        // Parse location
        if (args.length > argIndex) {
            if (args[argIndex].equals("~")) {
                // Use current location
                if (player == null) {
                    MessageUtils.sendMessage(sender, "commands.player-only");
                    return;
                }
                location = player.getLocation();
                argIndex++;
            } else if (args.length > argIndex + 2) {
                // Parse x, y, z coordinates
                try {
                    double x = Double.parseDouble(args[argIndex++]);
                    double y = Double.parseDouble(args[argIndex++]);
                    double z = Double.parseDouble(args[argIndex++]);
                    
                    // Use current world if sender is a player
                    World world = player != null ? player.getWorld() : null;
                    location = new Location(world, x, y, z);
                } catch (NumberFormatException e) {
                    MessageUtils.sendMessage(sender, "commands.invalid-coordinates");
                    return;
                }
            }
        }
        
        // Parse world or server
        if (args.length > argIndex) {
            if (args[argIndex].equalsIgnoreCase("server")) {
                isServerSpawn = true;
            } else {
                isServerSpawn = false;
                worldName = args[argIndex];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    MessageUtils.sendMessage(sender, "commands.world-not-found", "{world}", worldName);
                    return;
                }
                
                // Update location world if it was not set
                if (location != null && location.getWorld() == null) {
                    location = new Location(world, location.getX(), location.getY(), location.getZ(), 
                            location.getYaw(), location.getPitch());
                }
            }
        }
        
        // Use current location if no location is specified
        if (location == null) {
            if (player == null) {
                MessageUtils.sendMessage(sender, "commands.player-only");
                return;
            }
            location = player.getLocation();
        }
        
        // Set Floodgate spawn point
        if (spawnType.equals("newplayer")) {
            // Set new player spawn for Floodgate players
            SpawnUtils.setFloodgateNewPlayerSpawn(location);
            
            // Send success message
            MessageUtils.sendMessage(sender, "commands.floodgate.success-newplayer", 
                    "{world}", location.getWorld().getName(),
                    "{x}", String.format("%.2f", location.getX()),
                    "{y}", String.format("%.2f", location.getY()),
                    "{z}", String.format("%.2f", location.getZ()));
        } else {
            // Set respawn point for Floodgate players
            SpawnUtils.setFloodgateRespawn(location, worldName, isServerSpawn);
            
            // Send success message
            if (isServerSpawn) {
                MessageUtils.sendMessage(sender, "commands.floodgate.success-respawn-server", 
                        "{world}", location.getWorld().getName(),
                        "{x}", String.format("%.2f", location.getX()),
                        "{y}", String.format("%.2f", location.getY()),
                        "{z}", String.format("%.2f", location.getZ()));
            } else {
                MessageUtils.sendMessage(sender, "commands.floodgate.success-respawn-world", 
                        "{world}", location.getWorld().getName(),
                        "{x}", String.format("%.2f", location.getX()),
                        "{y}", String.format("%.2f", location.getY()),
                        "{z}", String.format("%.2f", location.getZ()));
            }
        }
    }
    
    /**
     * Set a player's spawn point
     * @param player The player
     * @param location The location
     * @param worldName The world name
     * @param isServerSpawn Whether it's a server spawn or world spawn
     */
    private void setPlayerSpawn(Player player, Location location, String worldName, boolean isServerSpawn) {
        // Use SpawnUtils to set player spawn
        SpawnUtils.setPlayerSpawn(player, location, worldName, isServerSpawn);
    }
    
    /**
     * Set a group's spawn point
     * @param groupName The group name
     * @param location The location
     * @param worldName The world name
     * @param isServerSpawn Whether it's a server spawn or world spawn
     */
    private void setGroupSpawn(String groupName, Location location, String worldName, boolean isServerSpawn) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String path;
        
        if (isServerSpawn) {
            path = "groups." + groupName + ".server";
        } else {
            path = "groups." + groupName + ".worlds." + worldName;
        }
        
        // Save location
        config.set(path + ".enabled", true);
        config.set(path + ".location.world", location.getWorld().getName());
        config.set(path + ".location.x", location.getX());
        config.set(path + ".location.y", location.getY());
        config.set(path + ".location.z", location.getZ());
        config.set(path + ".location.yaw", location.getYaw());
        config.set(path + ".location.pitch", location.getPitch());
        
        plugin.getConfigManager().saveConfig();
    }
}