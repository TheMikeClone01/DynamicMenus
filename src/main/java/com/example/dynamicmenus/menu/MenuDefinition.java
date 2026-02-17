package com.example.dynamicmenus.menu;

import org.bukkit.Material;

import java.util.Map;

public record MenuDefinition(
        String id,
        String title,
        int size,
        boolean cancelClick,
        boolean fillEnabled,
        Material fillMaterial,
        String fillName,
        Map<Integer, MenuItemDefinition> itemsBySlot
) {
}
