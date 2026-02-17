package com.example.dynamicmenus.menu;

import org.bukkit.Material;

import java.util.List;

public record MenuItemDefinition(
        int slot,
        Material material,
        int amount,
        String name,
        List<String> lore,
        String skullOwner,
        List<MenuAction> actions
) {
}
