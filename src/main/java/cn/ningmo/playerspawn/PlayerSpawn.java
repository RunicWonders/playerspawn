package cn.ningmo.playerspawn;

import org.bukkit.plugin.java.JavaPlugin;
import cn.ningmo.playerspawn.commands.PlayerSpawnCommand;
import cn.ningmo.playerspawn.config.ConfigManager;
import cn.ningmo.playerspawn.listeners.PlayerListener;

public class PlayerSpawn extends JavaPlugin {
    private static PlayerSpawn instance;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        // Set instance
        instance = this;
        
        // Initialize config manager
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Register commands
        getCommand("ps").setExecutor(new PlayerSpawnCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("PlayerSpawn plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("PlayerSpawn plugin has been disabled!");
    }
    
    /**
     * Get the plugin instance
     * @return The plugin instance
     */
    public static PlayerSpawn getInstance() {
        return instance;
    }
    
    /**
     * Get the config manager
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}