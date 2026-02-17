package com.example.dynamicmenus.config;

import com.example.dynamicmenus.DynamicMenusPlugin;
import com.example.dynamicmenus.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class MessageService {

    private final DynamicMenusPlugin plugin;
    private final Map<String, String> messages;
    private String prefix;

    public MessageService(DynamicMenusPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        reload();
    }

    public void reload() {
        messages.clear();
        FileConfiguration config = plugin.getConfig();
        prefix = config.getString("messages.prefix", "&8[&bDynamicMenus&8] &r");

        if (config.getConfigurationSection("messages") != null) {
            config.getConfigurationSection("messages")
                    .getKeys(false)
                    .forEach(key -> messages.put(key, config.getString("messages." + key, "")));
        }
    }

    public String get(String key) {
        return messages.getOrDefault(key, "");
    }

    public String format(String key, String... replacements) {
        String raw = prefix + get(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return Text.color(raw);
    }

    public void send(CommandSender sender, String key, String... replacements) {
        sender.sendMessage(format(key, replacements));
    }
}
