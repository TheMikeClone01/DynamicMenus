package com.example.dynamicmenus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public final class Text {

    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();

    private Text() {
    }

    public static String color(String text) {
        return text == null ? "" : text.replace('&', '§');
    }

    public static Component component(String text) {
        return SECTION.deserialize(color(text));
    }

    public static List<Component> componentList(List<String> lines) {
        return lines.stream().map(Text::component).collect(Collectors.toList());
    }
}
