package cn.ningmo.playerspawn.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.block.BlockBreakEvent;

import cn.ningmo.playerspawn.PlayerSpawn;
import cn.ningmo.playerspawn.utils.SpawnUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener for player events related to spawning
 */
public class PlayerListener implements Listener {
    private final PlayerSpawn plugin;
    private final Map<UUID, Location> logoutLocations = new HashMap<>();
    
    public PlayerListener(PlayerSpawn plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handle player join event
     * @param event The player join event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if this is the player's first time joining
        if (!player.hasPlayedBefore()) {
            // Get new player spawn location, passing the player to check for Floodgate status
            Location newPlayerSpawn = SpawnUtils.getNewPlayerSpawn(player);
            
            // Teleport player to new player spawn if set
            if (newPlayerSpawn != null) {
                // Use a delayed task to ensure the teleport happens after the player fully joins
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.teleport(newPlayerSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }, 5L); // 5 tick delay (0.25 seconds)
            }
        } else {
            // Handle rejoining players
            String rejoinBehavior = plugin.getConfigManager().getConfig().getString("on_rejoin.behavior", "nothing");
            
            switch (rejoinBehavior.toLowerCase()) {
                case "last_location":
                    // Teleport player to their last logout location
                    Location lastLocation = logoutLocations.get(player.getUniqueId());
                    if (lastLocation != null) {
                        // Use a delayed task to ensure the teleport happens after the player fully joins
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            player.teleport(lastLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                            logoutLocations.remove(player.getUniqueId()); // Clean up after use
                        }, 5L);
                    }
                    break;
                    
                case "spawn":
                    // Teleport player to their spawn location
                    Location playerSpawn = SpawnUtils.getPlayerSpawn(player, player.getWorld());
                    if (playerSpawn != null) {
                        // Use a delayed task to ensure the teleport happens after the player fully joins
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            player.teleport(playerSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        }, 5L);
                    }
                    break;
                    
                default:
                    // "nothing" or any other value - just let the player join normally
                    // But we still handle teleport_on_join setting for backward compatibility
                    if (plugin.getConfigManager().getConfig().getBoolean("teleport_on_join", false)) {
                        // Get player's spawn location
                        Location playerDefaultSpawn = SpawnUtils.getPlayerSpawn(player, player.getWorld());
                        
                        // Teleport player to their spawn if set
                        if (playerDefaultSpawn != null) {
                            // Use a delayed task to ensure the teleport happens after the player fully joins
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                player.teleport(playerDefaultSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                            }, 5L);
                        }
                    }
                    break;
            }
        }
    }
    
    /**
     * Handle player quit event
     * @param event The player quit event
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfigManager().getConfig();
        
        // Save logout location if enabled
        if (config.getBoolean("on_rejoin.save_logout_location", true)) {
            logoutLocations.put(player.getUniqueId(), player.getLocation());
        }
    }
    
    /**
     * Handle player respawn event
     * @param event The player respawn event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        FileConfiguration config = plugin.getConfigManager().getConfig();
        
        // Check if we should respect vanilla bed and respawn anchor mechanics
        boolean respectBedAndAnchor = config.getBoolean("vanilla_mechanics.respect_bed_and_anchor", true);
        
        // If player has a bed/respawn anchor spawn and we're respecting those
        if (respectBedAndAnchor && event.isBedSpawn()) {
            // Don't override vanilla behavior
            return;
        }
        
        // Get respawn location
        Location respawnLocation = SpawnUtils.getRespawnLocation(player, world);
        
        // Set respawn location if found
        if (respawnLocation != null) {
            event.setRespawnLocation(respawnLocation);
        }
    }
    
    /**
     * Handle bed enter event to manage bed spawn behavior if needed
     * This is a placeholder for potential future customization
     * @param event The player bed enter event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        // Currently we don't need to do anything here
        // The event is registered for future expansion
    }
}