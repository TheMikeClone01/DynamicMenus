package com.example.dynamicmenus.menu;

import com.example.dynamicmenus.DynamicMenusPlugin;
import com.example.dynamicmenus.config.MessageService;
import com.example.dynamicmenus.util.Placeholders;
import com.example.dynamicmenus.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuManager {

    private final DynamicMenusPlugin plugin;
    private final MessageService messageService;
    private final MenuLoader loader;

    private final Map<String, MenuDefinition> menus;
    private final Map<UUID, MenuSession> sessions;

    public MenuManager(DynamicMenusPlugin plugin, MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.loader = new MenuLoader(plugin);
        this.menus = new LinkedHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
    }

    public void reload() {
        menus.clear();
        menus.putAll(loader.loadMenus());
        sessions.clear();
    }

    public int getMenuCount() {
        return menus.size();
    }

    public List<String> getMenuIds() {
        return menus.keySet().stream().sorted(Comparator.naturalOrder()).toList();
    }

    public boolean openMenu(Player player, String menuId) {
        MenuDefinition menu = menus.get(menuId.toLowerCase());
        if (menu == null) {
            messageService.send(player, "menu-not-found", "%menu%", menuId);
            return false;
        }

        String title = Text.color(Placeholders.apply(menu.title(), player));
        Inventory inventory = Bukkit.createInventory(player, menu.size(), Text.component(title));

        if (menu.fillEnabled()) {
            ItemStack filler = createSimpleItem(menu.fillMaterial(), 1, Placeholders.apply(menu.fillName(), player), List.of());
            for (int i = 0; i < menu.size(); i++) {
                inventory.setItem(i, filler);
            }
        }

        Map<Integer, MenuItemDefinition> activeItems = new LinkedHashMap<>();
        for (Map.Entry<Integer, MenuItemDefinition> entry : menu.itemsBySlot().entrySet()) {
            MenuItemDefinition def = entry.getValue();
            ItemStack stack = buildItem(def, player);
            inventory.setItem(entry.getKey(), stack);
            activeItems.put(entry.getKey(), def);
        }

        sessions.put(player.getUniqueId(), new MenuSession(menu.id(), activeItems));
        player.openInventory(inventory);
        return true;
    }

    public void clearSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public void clearSessions() {
        sessions.clear();
    }

    public MenuSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public boolean shouldCancelClick(String menuId) {
        MenuDefinition definition = menus.get(menuId.toLowerCase());
        boolean defaultCancel = plugin.getConfig().getBoolean("settings.default-cancel-click", true);
        return definition == null ? defaultCancel : definition.cancelClick();
    }

    public void handleClick(Player player, int rawSlot) {
        MenuSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return;
        }

        MenuItemDefinition item = session.itemsBySlot().get(rawSlot);
        if (item == null) {
            return;
        }

        for (MenuAction action : new ArrayList<>(item.actions())) {
            executeAction(player, action);
        }
    }

    private void executeAction(Player player, MenuAction action) {
        String value = Text.color(Placeholders.apply(action.value(), player));
        switch (action.type()) {
            case MESSAGE -> player.sendMessage(Text.component(value));
            case PLAYER -> player.performCommand(stripSlash(value));
            case CONSOLE -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), stripSlash(value));
            case CLOSE -> player.closeInventory();
            case OPEN -> openMenu(player, value);
        }
    }

    private String stripSlash(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }

    private ItemStack buildItem(MenuItemDefinition def, Player player) {
        ItemStack stack = new ItemStack(def.material(), def.amount());
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        String displayName = Text.color(Placeholders.apply(def.name(), player));
        meta.displayName(Text.component(displayName));

        List<String> lore = def.lore().stream()
                .map(line -> Text.color(Placeholders.apply(line, player)))
                .toList();
        if (!lore.isEmpty()) {
            meta.lore(Text.componentList(lore));
        }

        if (meta instanceof SkullMeta skullMeta
                && def.material() == Material.PLAYER_HEAD
                && def.skullOwner() != null
                && !def.skullOwner().isBlank()) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(Placeholders.apply(def.skullOwner(), player)));
            meta = skullMeta;
        }

        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack createSimpleItem(Material material, int amount, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        meta.displayName(Text.component(Text.color(name)));
        if (!lore.isEmpty()) {
            meta.lore(Text.componentList(lore));
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
