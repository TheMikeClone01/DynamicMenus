package com.example.dynamicmenus.menu;

import com.example.dynamicmenus.DynamicMenusPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class MenuLoader {

    private final DynamicMenusPlugin plugin;

    public MenuLoader(DynamicMenusPlugin plugin) {
        this.plugin = plugin;
    }

    public Map<String, MenuDefinition> loadMenus() {
        File menusFolder = new File(plugin.getDataFolder(), "menus");
        if (!menusFolder.exists() && !menusFolder.mkdirs()) {
            plugin.getLogger().warning("No se pudo crear la carpeta menus/");
            return Map.of();
        }

        File[] files = menusFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return Map.of();
        }

        Map<String, MenuDefinition> menus = new LinkedHashMap<>();
        for (File file : files) {
            MenuDefinition definition = loadMenu(file);
            if (definition != null) {
                menus.put(definition.id().toLowerCase(), definition);
            }
        }
        return menus;
    }

    private MenuDefinition loadMenu(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id", file.getName().replace(".yml", "")).toLowerCase();
            String title = config.getString("title", "&8Menu");
            int size = normalizeSize(config.getInt("size", 27));
            boolean cancelClick = config.getBoolean("cancel-click", true);
            boolean fillEnabled = config.getBoolean("fill-empty.enabled", false);
            Material fillMaterial = parseMaterial(config.getString("fill-empty.material", "GRAY_STAINED_GLASS_PANE"));
            String fillName = config.getString("fill-empty.name", "&7");

            Map<Integer, MenuItemDefinition> itemsBySlot = new HashMap<>();
            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    ConfigurationSection section = itemsSection.getConfigurationSection(key);
                    if (section == null) {
                        continue;
                    }

                    int slot = section.getInt("slot", -1);
                    if (slot < 0 || slot >= size) {
                        plugin.getLogger().warning("Slot inválido en " + file.getName() + " item " + key);
                        continue;
                    }

                    Material material = parseMaterial(section.getString("material", "STONE"));
                    int amount = Math.clamp(section.getInt("amount", 1), 1, 64);
                    String name = section.getString("name", "&fItem");
                    List<String> lore = section.getStringList("lore");
                    String skullOwner = section.getString("skull-owner", "");

                    List<MenuAction> actions = new ArrayList<>();
                    for (String rawAction : section.getStringList("actions")) {
                        MenuAction action = MenuAction.parse(rawAction);
                        if (action != null) {
                            actions.add(action);
                        }
                    }

                    MenuItemDefinition itemDefinition = new MenuItemDefinition(
                            slot, material, amount, name, lore, skullOwner, actions
                    );
                    itemsBySlot.put(slot, itemDefinition);
                }
            }

            return new MenuDefinition(id, title, size, cancelClick, fillEnabled, fillMaterial, fillName, itemsBySlot);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Error cargando menú " + file.getName(), ex);
            return null;
        }
    }

    private int normalizeSize(int size) {
        int normalized = Math.max(9, Math.min(size, 54));
        return normalized - normalized % 9;
    }

    private Material parseMaterial(String materialName) {
        Material material = Material.matchMaterial(materialName);
        return material == null ? Material.STONE : material;
    }
}
