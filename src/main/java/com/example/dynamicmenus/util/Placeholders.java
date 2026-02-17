package com.example.dynamicmenus.util;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Placeholders {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private Placeholders() {
    }

    public static String apply(String input, Player player) {
        if (input == null) {
            return "";
        }

        return input
                .replace("%player%", player.getName())
                .replace("%displayname%", PLAIN.serialize(player.displayName()))
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%world%", player.getWorld().getName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }
}
