package se.alvarsjogren.trailTracker.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Provides standardized text components for consistent UI messaging.
 *
 * This utility class centralizes the creation of formatted text messages
 * used throughout the plugin to ensure consistent styling and branding.
 * Uses Paper's Adventure API for rich text formatting.
 */
public class UITextComponents {
    /**
     * Creates the standard TrailTracker prefix used at the beginning of messages.
     * Format: [TT] with custom colors and styling.
     *
     * @return A formatted text component with the plugin prefix
     */
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

    /**
     * Creates a standardized error message with the plugin prefix.
     *
     * @param message The error message text
     * @return A formatted text component with the error message
     */
    public static TextComponent errorMessage(String message) {
        return TTPrefix().append(Component.text(message).color(TextColor.color(0xBE3D2A)));
    }

    /**
     * Creates a standardized success message with the plugin prefix.
     * Formats the message as "[TT] {action} {pathName}" with consistent styling.
     *
     * @param action The action that was performed (e.g., "Started tracking path")
     * @param pathName The name of the path the action was performed on
     * @return A formatted text component with the success message
     */
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