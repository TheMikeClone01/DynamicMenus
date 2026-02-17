package com.example.dynamicmenus.listener;

import com.example.dynamicmenus.menu.MenuManager;
import com.example.dynamicmenus.menu.MenuSession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class MenuListener implements Listener {

    private final MenuManager menuManager;

    public MenuListener(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        MenuSession session = menuManager.getSession(player.getUniqueId());
        if (session == null) {
            return;
        }

        if (menuManager.shouldCancelClick(session.menuId())) {
            event.setCancelled(true);
        }

        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getInventory().getSize()) {
            return;
        }

        menuManager.handleClick(player, event.getRawSlot());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        menuManager.clearSession(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        menuManager.clearSession(event.getPlayer().getUniqueId());
    }
}
