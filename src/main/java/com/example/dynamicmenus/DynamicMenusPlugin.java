package com.example.dynamicmenus;

import com.example.dynamicmenus.command.DynamicMenusCommand;
import com.example.dynamicmenus.config.MessageService;
import com.example.dynamicmenus.listener.MenuListener;
import com.example.dynamicmenus.menu.MenuManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicMenusPlugin extends JavaPlugin {

    private MessageService messageService;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("menus/example.yml", false);

        this.messageService = new MessageService(this);
        this.menuManager = new MenuManager(this, messageService);
        menuManager.reload();

        registerCommands();
        getServer().getPluginManager().registerEvents(new MenuListener(menuManager), this);

        getLogger().info("DynamicMenus habilitado. Menús cargados: " + menuManager.getMenuCount());
    }

    @Override
    public void onDisable() {
        menuManager.clearSessions();
    }

    public void reloadPlugin() {
        reloadConfig();
        messageService.reload();
        menuManager.reload();
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    private void registerCommands() {
        PluginCommand command = getCommand("dynamicmenus");
        if (command == null) {
            getLogger().severe("No se pudo registrar el comando /dynamicmenus.");
            return;
        }

        DynamicMenusCommand executor = new DynamicMenusCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
