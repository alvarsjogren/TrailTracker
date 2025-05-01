package se.alvarsjogren.trailTracker.commands.subCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Command that shows help information for all available commands.
 * Displays a formatted list of all registered subcommands with descriptions.
 */
public class HelpCommand implements SubCommand {
    /** List of all registered subcommands to display help for */
    private final List<SubCommand> subCommands;

    /**
     * Creates a new HelpCommand.
     *
     * @param subCommands The list of all registered subcommands
     */
    public HelpCommand(List<SubCommand> subCommands) {
        this.subCommands = subCommands;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays a list of available commands.";
    }

    @Override
    public String getSyntax() {
        return "/tt help";
    }

    /**
     * Displays a formatted list of all available commands with descriptions.
     * Uses Adventure API for rich text formatting.
     *
     * @param sender The command sender (player or console)
     * @param args The command arguments (not used)
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        // No permission check needed as help is visible to everyone

        // Start with a blank line for cleaner output
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
                        .text("Commands ===")
                        .color(TextColor.color(0xE78B48)));
        sender.sendMessage(header);

        // List all commands with their syntax and description
        for (SubCommand subCommand : subCommands) {
            final TextComponent item = Component
                    .text(subCommand.getSyntax() + " - " + subCommand.getDescription())
                    .color(TextColor.color(0xF5C45E));
            sender.sendMessage(item);
        }

        // Create styled footer
        final TextComponent footer = Component
                .text("==========================")
                .color(TextColor.color(0xE78B48));
        sender.sendMessage(footer);

        // End with a blank line for cleaner output
        sender.sendMessage("\n");
    }
}