package cn.ningmo.playerspawn.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import cn.ningmo.playerspawn.PlayerSpawn;
import cn.ningmo.playerspawn.utils.SpawnUtils;

/**
 * Listener for player events related to spawning
 */
public class PlayerListener implements Listener {
    private final PlayerSpawn plugin;
    
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
            // Check if we should teleport returning players to their spawn
            if (plugin.getConfigManager().getConfig().getBoolean("teleport_on_join", false)) {
                // Get player's spawn location
                Location playerSpawn = SpawnUtils.getPlayerSpawn(player, player.getWorld());
                
                // Teleport player to their spawn if set
                if (playerSpawn != null) {
                    // Use a delayed task to ensure the teleport happens after the player fully joins
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        player.teleport(playerSpawn, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    }, 5L); // 5 tick delay (0.25 seconds)
                }
            }
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
        
        // Get respawn location
        Location respawnLocation = SpawnUtils.getRespawnLocation(player, world);
        
        // Set respawn location if found
        if (respawnLocation != null) {
            event.setRespawnLocation(respawnLocation);
        }
    }
}