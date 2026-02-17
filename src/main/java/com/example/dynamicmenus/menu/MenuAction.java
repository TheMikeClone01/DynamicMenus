package com.example.dynamicmenus.menu;

public record MenuAction(MenuActionType type, String value) {

    public static MenuAction parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String upper = raw.toUpperCase();
        if (upper.equals("CLOSE")) {
            return new MenuAction(MenuActionType.CLOSE, "");
        }

        int separator = raw.indexOf(':');
        if (separator <= 0) {
            return new MenuAction(MenuActionType.MESSAGE, raw);
        }

        String typeString = raw.substring(0, separator).trim().toUpperCase();
        String value = raw.substring(separator + 1).trim();

        return switch (typeString) {
            case "MESSAGE" -> new MenuAction(MenuActionType.MESSAGE, value);
            case "CONSOLE" -> new MenuAction(MenuActionType.CONSOLE, value);
            case "PLAYER" -> new MenuAction(MenuActionType.PLAYER, value);
            case "OPEN" -> new MenuAction(MenuActionType.OPEN, value);
            default -> new MenuAction(MenuActionType.MESSAGE, raw);
        };
    }
}
