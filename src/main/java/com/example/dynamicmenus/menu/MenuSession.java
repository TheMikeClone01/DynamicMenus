package com.example.dynamicmenus.menu;

import java.util.Map;

public record MenuSession(String menuId, Map<Integer, MenuItemDefinition> itemsBySlot) {
}
