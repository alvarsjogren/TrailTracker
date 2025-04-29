package se.alvarsjogren.trailTracker.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class UITextComponents {
    public static TextComponent TTPrefix() {
        return Component
                .text("[")
                .color(TextColor.color(0xE78B48))
                .append(Component
                        .text("T")
                        .color(TextColor.color(0x102E50))
                        .decoration(TextDecoration.BOLD, true))
                .append(Component
                        .text("T")
                        .color(TextColor.color(0xBE3D2A))
                        .decoration(TextDecoration.BOLD, true))
                .append(Component
                        .text("] ")
                        .color(TextColor.color(0xE78B48)));
    }

    public static TextComponent errorMessage(String message) {
        return TTPrefix().append(Component.text(message).color(TextColor.color(0xBE3D2A)));
    }

    public static TextComponent successMessage(String action, String pathName) {
        return TTPrefix().append(Component
                .text(action + " ")
                .color(TextColor.color(0xF5C45E))
                .append(Component
                        .text(pathName)
                        .color(TextColor.color(0xE78B48))
                        .decoration(TextDecoration.BOLD, true)));
    }
}
