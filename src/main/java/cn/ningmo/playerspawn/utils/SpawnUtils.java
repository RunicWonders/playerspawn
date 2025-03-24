package cn.ningmo.playerspawn.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import cn.ningmo.playerspawn.PlayerSpawn;

import java.util.UUID;

/**
 * Utility class for managing player spawn points
 */
public class SpawnUtils {
    
    /**
     * Set a player's spawn point
     * @param player The player
     * @param location The location
     * @param worldName The world name (null for server spawn)
     * @param isServerSpawn Whether it's a server spawn or world spawn
     */
    public static void setPlayerSpawn(Player player, Location location, String worldName, boolean isServerSpawn) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
        String path;
        
        if (isServerSpawn) {
            path = "players." + player.getUniqueId() + ".server";
        } else {
            path = "players." + player.getUniqueId() + ".worlds." + worldName;
        }
        
        // Save location
        config.set(path + ".enabled", true);
        config.set(path + ".location.world", location.getWorld().getName());
        config.set(path + ".location.x", location.getX());
        config.set(path + ".location.y", location.getY());
        config.set(path + ".location.z", location.getZ());
        config.set(path + ".location.yaw", location.getYaw());
        config.set(path + ".location.pitch", location.getPitch());
        
        PlayerSpawn.getInstance().getConfigManager().saveConfig();
    }
    
    /**
     * Set a group's spawn point
     * @param groupName The group name
     * @param location The location
     * @param worldName The world name (null for server spawn)
     * @param isServerSpawn Whether it's a server spawn or world spawn
     */
    public static void setGroupSpawn(String groupName, Location location, String worldName, boolean isServerSpawn) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
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
        
        PlayerSpawn.getInstance().getConfigManager().saveConfig();
    }
    
    /**
     * Get a player's spawn location
     * @param player The player
     * @param world The world (null for server spawn)
     * @return The spawn location, or null if not set
     */
    public static Location getPlayerSpawn(Player player, World world) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
        UUID playerUUID = player.getUniqueId();
        String path;
        
        // Check if player has a specific spawn point for this world
        if (world != null) {
            path = "players." + playerUUID + ".worlds." + world.getName();
            if (config.getBoolean(path + ".enabled", false)) {
                return getLocationFromConfig(config, path + ".location");
            }
        }
        
        // Check if player has a server-wide spawn point
        path = "players." + playerUUID + ".server";
        if (config.getBoolean(path + ".enabled", false)) {
            return getLocationFromConfig(config, path + ".location");
        }
        
        // Check if player belongs to any groups with spawn points
        ConfigurationSection groupsSection = config.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                // Check if player has permission for this group
                if (player.hasPermission("playerspawn.group." + groupName)) {
                    // Check for world-specific group spawn
                    if (world != null) {
                        path = "groups." + groupName + ".worlds." + world.getName();
                        if (config.getBoolean(path + ".enabled", false)) {
                            return getLocationFromConfig(config, path + ".location");
                        }
                    }
                    
                    // Check for server-wide group spawn
                    path = "groups." + groupName + ".server";
                    if (config.getBoolean(path + ".enabled", false)) {
                        return getLocationFromConfig(config, path + ".location");
                    }
                }
            }
        }
        
        // Check if player is a Floodgate player (Bedrock player)
        if (isFloodgatePlayer(player)) {
            // Check for Floodgate-specific spawn points
            if (world != null) {
                path = "floodgate.worlds." + world.getName();
                if (config.getBoolean(path + ".enabled", false)) {
                    return getLocationFromConfig(config, path + ".location");
                }
            }
            
            path = "floodgate.server";
            if (config.getBoolean(path + ".enabled", false)) {
                return getLocationFromConfig(config, path + ".location");
            }
        }
        
        // Check for default world spawn
        if (world != null) {
            path = "default.worlds." + world.getName();
            if (config.getBoolean(path + ".enabled", false)) {
                return getLocationFromConfig(config, path + ".location");
            }
        }
        
        // Check for default server spawn
        path = "default.server";
        if (config.getBoolean(path + ".enabled", false)) {
            return getLocationFromConfig(config, path + ".location");
        }
        
        // Return null if no spawn point is set
        return null;
    }
    
    /**
     * Get a location from a configuration section
     * @param config The configuration
     * @param path The path to the location section
     * @return The location, or null if invalid
     */
    public static Location getLocationFromConfig(FileConfiguration config, String path) {
        if (!config.contains(path + ".world")) {
            return null;
        }
        
        String worldName = config.getString(path + ".world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        
        double x = config.getDouble(path + ".x", 0);
        double y = config.getDouble(path + ".y", 64);
        double z = config.getDouble(path + ".z", 0);
        float yaw = (float) config.getDouble(path + ".yaw", 0);
        float pitch = (float) config.getDouble(path + ".pitch", 0);
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * Save a location to a configuration section
     * @param config The configuration
     * @param path The path to the location section
     * @param location The location to save
     */
    public static void saveLocationToConfig(FileConfiguration config, String path, Location location) {
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
    }
    
    /**
     * Check if a player is a Floodgate player (Bedrock player)
     * @param player The player to check
     * @return True if the player is a Floodgate player, false otherwise
     */
    public static boolean isFloodgatePlayer(Player player) {
        // Check if player is null
        if (player == null) {
            return false;
        }
        
        // Check if Floodgate is installed
        if (!Bukkit.getPluginManager().isPluginEnabled("floodgate")) {
            return false;
        }
        
        // Check if player has the floodgate prefix in their UUID
        // Floodgate UUIDs start with 00000000-0000-0000-
        String uuid = player.getUniqueId().toString();
        return uuid.startsWith("00000000-0000-0000-");
    }
    
    /**
     * Set a Floodgate player's spawn point
     * @param location The location
     * @param worldName The world name (null for server spawn)
     * @param isServerSpawn Whether it's a server spawn or world spawn
     */
    public static void setFloodgateSpawn(Location location, String worldName, boolean isServerSpawn) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
        String path;
        
        if (isServerSpawn) {
            path = "floodgate.server";
        } else {
            path = "floodgate.worlds." + worldName;
        }
        
        // Save location
        config.set(path + ".enabled", true);
        config.set(path + ".location.world", location.getWorld().getName());
        config.set(path + ".location.x", location.getX());
        config.set(path + ".location.y", location.getY());
        config.set(path + ".location.z", location.getZ());
        config.set(path + ".location.yaw", location.getYaw());
        config.set(path + ".location.pitch", location.getPitch());
        
        PlayerSpawn.getInstance().getConfigManager().saveConfig();
    }
    
    /**
     * Set a Floodgate new player spawn point
     * @param location The location
     */
    public static void setFloodgateNewPlayerSpawn(Location location) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
        String path = "floodgate.new_player";
        
        // Save location
        config.set(path + ".enabled", true);
        saveLocationToConfig(config, path + ".location", location);
        
        PlayerSpawn.getInstance().getConfigManager().saveConfig();
    }
    
    /**
     * Set a Floodgate respawn point
     * @param location The location
     * @param worldName The world name (null for server spawn)
     * @param isServerSpawn Whether it's a server spawn or world spawn
     */
    public static void setFloodgateRespawn(Location location, String worldName, boolean isServerSpawn) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
        String path;
        
        if (isServerSpawn) {
            path = "floodgate.respawn.server";
        } else {
            path = "floodgate.respawn.worlds." + worldName;
        }
        
        // Save location
        config.set(path + ".enabled", true);
        saveLocationToConfig(config, path + ".location", location);
        
        PlayerSpawn.getInstance().getConfigManager().saveConfig();
    }
    
    /**
     * Get the new player spawn location
     * @param player The player to get spawn for (can be null for general new player spawn)
     * @return The new player spawn location, or null if not set
     */
    public static Location getNewPlayerSpawn(Player player) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
        
        // Check if player is a Floodgate player (Bedrock player) and if Floodgate spawn is enabled
        if (player != null && isFloodgatePlayer(player) && config.getBoolean("floodgate.new_player.enabled", false)) {
            // Return the Floodgate new player spawn location
            Location floodgateSpawn = getLocationFromConfig(config, "floodgate.new_player.location");
            if (floodgateSpawn != null) {
                return floodgateSpawn;
            }
        }
        
        // Check if new player spawn is enabled
        if (!config.getBoolean("new_player.enabled", false)) {
            // Use default spawn if not specified
            if (config.getBoolean("new_player.use_default_if_not_specified", true)) {
                // Check for default server spawn
                if (config.getBoolean("default.server.enabled", false)) {
                    return getLocationFromConfig(config, "default.server.location");
                }
            }
            return null;
        }
        
        // Return the new player spawn location
        return getLocationFromConfig(config, "new_player.location");
    }
    
    /**
     * Get the new player spawn location (overloaded method for backward compatibility)
     * @return The new player spawn location, or null if not set
     */
    public static Location getNewPlayerSpawn() {
        return getNewPlayerSpawn(null);
    }
    
    /**
     * Get the respawn location for a player
     * @param player The player
     * @param world The world the player died in
     * @return The respawn location, or null to use the default respawn location
     */
    public static Location getRespawnLocation(Player player, World world) {
        FileConfiguration config = PlayerSpawn.getInstance().getConfigManager().getConfig();
        
        // Check if respawn is enabled
        if (!config.getBoolean("respawn.enabled", false)) {
            return null;
        }
        
        // Check if we should use player spawn
        if (config.getBoolean("respawn.use_player_spawn", true)) {
            Location playerSpawn = getPlayerSpawn(player, world);
            if (playerSpawn != null) {
                return playerSpawn;
            }
        }
        
        // Check if player is a Floodgate player (Bedrock player)
        if (isFloodgatePlayer(player)) {
            // Check for Floodgate-specific respawn points
            if (world != null) {
                String worldPath = "floodgate.respawn.worlds." + world.getName();
                if (config.getBoolean(worldPath + ".enabled", false)) {
                    return getLocationFromConfig(config, worldPath + ".location");
                }
            }
            
            // Check for server-wide Floodgate respawn
            if (config.getBoolean("floodgate.respawn.server.enabled", false)) {
                return getLocationFromConfig(config, "floodgate.respawn.server.location");
            }
        }
        
        // Check for world-specific respawn
        if (world != null) {
            String worldPath = "respawn.worlds." + world.getName();
            if (config.getBoolean(worldPath + ".enabled", false)) {
                return getLocationFromConfig(config, worldPath + ".location");
            }
        }
        
        // Check for server-wide respawn
        if (config.getBoolean("respawn.server.enabled", false)) {
            return getLocationFromConfig(config, "respawn.server.location");
        }
        
        // Use default spawn if not specified
        if (config.getBoolean("respawn.use_default_if_not_specified", true)) {
            // Check for default server spawn
            if (config.getBoolean("default.server.enabled", false)) {
                return getLocationFromConfig(config, "default.server.location");
            }
        }
        
        return null;
    }
}