package se.alvarsjogren.trailTracker.commands.subCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

/**
 * Command that displays detailed information about a specific path.
 * Shows comprehensive metadata and statistics about a path.
 */
public class InfoCommand implements SubCommand {
    /** Reference to the PathRecorder for accessing path information */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new InfoCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public InfoCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Shows detailed information about a specific path.";
    }

    @Override
    public String getSyntax() {
        return "/tt info <path>";
    }

    /**
     * Displays detailed information about a specific path.
     * Shows path metadata, statistics, and status in a formatted display.
     *
     * @param sender The command sender (must be a player)
     * @param args The command arguments (args[1+] = path name)
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        // Command can only be executed by players
        if (!(sender instanceof Player player)) {
            sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
            return;
        }

        // Check permissions
        if (!player.hasPermission("TrailTracker.info")) {
            player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
            return;
        }

        // Validate command format
        if (args.length < 2) {
            player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt info <path>"));
            return;
        }

        // Combine all remaining arguments for path name to allow spaces
        String pathName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Path path = pathRecorder.getPaths().get(pathName);

        // Check if path exists
        if (path == null) {
            player.sendMessage(UITextComponents.errorMessage("Path not found: " + pathName));
            return;
        }

        // Display path information in a formatted message
        player.sendMessage("\n");

        // Create styled header with path name
        final TextComponent header = Component
                .text("=== Path Info: " + path.getName() + " ===")
                .color(TextColor.color(0xE78B48));
        player.sendMessage(header);

        // Description
        final TextComponent description = Component
                .text("Description: ")
                .color(TextColor.color(0xE78B48))
                .append(Component
                        .text(path.getDescription())
                        .color(TextColor.color(0xF5C45E)));
        player.sendMessage(description);

        // Points count
        final TextComponent points = Component
                .text("Number of Points: ")
                .color(TextColor.color(0xE78B48))
                .append(Component
                        .text(String.valueOf(path.getTrackedPath().size()))
                        .color(TextColor.color(0xF5C45E)));
        player.sendMessage(points);

        // Detection radius
        final TextComponent radius = Component
                .text("Detection Radius: ")
                .color(TextColor.color(0xE78B48))
                .append(Component
                        .text(path.getRadius() + " blocks")
                        .color(TextColor.color(0xF5C45E)));
        player.sendMessage(radius);

        // Status - is the path currently being tracked?
        boolean isBeingTracked = pathRecorder.getTrackedPaths().containsValue(path.getName());
        final TextComponent status = Component
                .text("Status: ")
                .color(TextColor.color(0xE78B48))
                .append(Component
                        .text(isBeingTracked ? "Currently being recorded" : "Completed")
                        .color(isBeingTracked ? TextColor.color(0xBE3D2A) : TextColor.color(0xF5C45E)));
        player.sendMessage(status);

        // Count how many players are currently displaying this path
        int displayCount = 0;
        for (var entry : pathRecorder.getDisplayedPaths().entrySet()) {
            if (entry.getValue().contains(path.getName())) {
                displayCount++;
            }
        }

        final TextComponent displayed = Component
                .text("Displayed by: ")
                .color(TextColor.color(0xE78B48))
                .append(Component
                        .text(displayCount + " players")
                        .color(TextColor.color(0xF5C45E)));
        player.sendMessage(displayed);

        // Create styled footer
        final TextComponent footer = Component
                .text("=======================")
                .color(TextColor.color(0xE78B48));
        player.sendMessage(footer);
        player.sendMessage("\n");
    }
}