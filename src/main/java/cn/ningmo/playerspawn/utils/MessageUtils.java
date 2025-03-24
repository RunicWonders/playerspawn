package cn.ningmo.playerspawn.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import cn.ningmo.playerspawn.PlayerSpawn;

public class MessageUtils {
    
    /**
     * Send a message to a command sender
     * @param sender The command sender
     * @param path The message path in messages.yml
     * @param placeholders The placeholders to replace (format: placeholder1, value1, placeholder2, value2, ...)
     */
    public static void sendMessage(CommandSender sender, String path, String... placeholders) {
        String message = getMessage(path);
        
        // Replace placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        // Send message
        sender.sendMessage(message);
    }
    
    /**
     * Get a message from messages.yml
     * @param path The message path
     * @return The message with color codes translated
     */
    public static String getMessage(String path) {
        String prefix = PlayerSpawn.getInstance().getConfigManager().getMessages().getString("prefix", "&8[&aPlayerSpawn&8] &r");
        String message = PlayerSpawn.getInstance().getConfigManager().getMessages().getString(path, "&cMessage not found: " + path);
        
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
    
    /**
     * Get a message from messages.yml without prefix
     * @param path The message path
     * @return The message with color codes translated
     */
    public static String getMessageWithoutPrefix(String path) {
        String message = PlayerSpawn.getInstance().getConfigManager().getMessages().getString(path, "&cMessage not found: " + path);
        
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}