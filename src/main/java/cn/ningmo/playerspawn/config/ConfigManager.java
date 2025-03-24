package cn.ningmo.playerspawn.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import cn.ningmo.playerspawn.PlayerSpawn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private final PlayerSpawn plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private File configFile;
    private File messagesFile;
    
    public ConfigManager(PlayerSpawn plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load all configuration files
     */
    public void loadConfigs() {
        loadConfig();
        loadMessages();
    }
    
    /**
     * Load the main config file
     */
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Set default values if they don't exist
        setConfigDefaults();
    }
    
    /**
     * Load the messages file
     */
    public void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Set default values if they don't exist
        setMessagesDefaults();
    }
    
    /**
     * Set default values for config.yml
     */
    private void setConfigDefaults() {
        // Load default config from resources
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
    }
    
    /**
     * Set default values for messages.yml
     */
    private void setMessagesDefaults() {
        // Load default messages from resources
        InputStream defaultMessagesStream = plugin.getResource("messages.yml");
        if (defaultMessagesStream != null) {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultMessagesStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultMessages);
        }
    }
    
    /**
     * Save the config file
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config to " + configFile);
            e.printStackTrace();
        }
    }
    
    /**
     * Save the messages file
     */
    public void saveMessages() {
        try {
            messages.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save messages to " + messagesFile);
            e.printStackTrace();
        }
    }
    
    /**
     * Get the config file
     * @return The config file
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }
    
    /**
     * Get the messages file
     * @return The messages file
     */
    public FileConfiguration getMessages() {
        if (messages == null) {
            loadMessages();
        }
        return messages;
    }
}