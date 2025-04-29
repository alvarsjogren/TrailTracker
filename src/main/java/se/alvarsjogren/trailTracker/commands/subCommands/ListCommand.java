package se.alvarsjogren.trailTracker.commands.subCommands;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;

/**
 * Command that lists all available paths.
 * Displays a formatted list of all paths with their descriptions.
 */
public class ListCommand implements SubCommand{
    /** Reference to the PathRecorder for accessing path information */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new ListCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public ListCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists all paths.";
    }

    @Override
    public String getSyntax() {
        return "/tt list";
    }

    /**
     * Lists all available paths in a formatted display.
     * Shows path names and descriptions using Adventure API for rich text formatting.
     * This command can be used by any sender (player or console).
     *
     * @param sender The command sender
     * @param args The command arguments (not used)
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        sender.sendMessage("\n");
        // Create styled header with plugin colors
        final TextComponent header = Component
                .text("=== ")
                .color(TextColor.color(0xE78B48))
                .append(Component.text("Trail")
                        .color(TextColor.color(0x102E50)))
                .append(Component.text("Tracker ")
                        .color(TextColor.color(0xBE3D2A)))
                .append(Component
                        .text("Paths ===")
                        .color(TextColor.color(0xE78B48)));
        sender.sendMessage(header);

        // List all paths with their descriptions
        for (Path path : pathRecorder.getPaths().values()) {
            final TextComponent item = Component
                    .text(path.getName() + " - " + path.getDescription())
                    .color(TextColor.color(0xF5C45E));
            sender.sendMessage(item);
        }

        // Create styled footer
        final TextComponent footer = Component
                .text("=======================")
                .color(TextColor.color(0xE78B48));
        sender.sendMessage(footer);
        sender.sendMessage("\n");
    }
}