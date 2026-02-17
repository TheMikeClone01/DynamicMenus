package com.example.dynamicmenus.command;

import com.example.dynamicmenus.DynamicMenusPlugin;
import com.example.dynamicmenus.config.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DynamicMenusCommand implements CommandExecutor, TabCompleter {

    private final DynamicMenusPlugin plugin;
    private final MessageService messages;

    public DynamicMenusCommand(DynamicMenusPlugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            messages.send(sender, "usage");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("dynamicmenus.admin")) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                plugin.reloadPlugin();
                messages.send(sender, "reloaded");
                return true;
            }
            case "list" -> {
                if (!sender.hasPermission("dynamicmenus.admin")) {
                    messages.send(sender, "no-permission");
                    return true;
                }
                List<String> menuIds = plugin.getMenuManager().getMenuIds();
                String joined = menuIds.isEmpty() ? "ninguno" : String.join("&7, &b", menuIds);
                messages.send(sender, "menu-list-header", "%count%", String.valueOf(menuIds.size()), "%menus%", joined);
                return true;
            }
            case "open" -> {
                return handleOpen(sender, args);
            }
            default -> {
                messages.send(sender, "usage");
                return true;
            }
        }
    }

    private boolean handleOpen(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dynamicmenus.open")) {
            messages.send(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            messages.send(sender, "usage");
            return true;
        }

        String menuId = args[1];
        Player target;

        if (args.length >= 3) {
            if (!sender.hasPermission("dynamicmenus.open.others")) {
                messages.send(sender, "no-permission");
                return true;
            }
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage("§cJugador no encontrado.");
                return true;
            }
        } else {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "player-only");
                return true;
            }
            target = player;
        }

        boolean opened = plugin.getMenuManager().openMenu(target, menuId);
        if (opened) {
            messages.send(sender, "menu-opened", "%menu%", menuId, "%player%", target.getName());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("open", "reload", "list"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
            return filter(plugin.getMenuManager().getMenuIds(), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("open")) {
            List<String> players = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
            return filter(players, args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String input) {
        String lower = input.toLowerCase();
        return values.stream().filter(value -> value.toLowerCase().startsWith(lower)).toList();
    }
}
