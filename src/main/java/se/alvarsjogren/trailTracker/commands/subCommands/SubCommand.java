package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;

/**
 * Interface for all TrailTracker subcommands.
 *
 * This interface establishes a consistent structure for all subcommands,
 * making it easy to add new commands and maintain existing ones.
 *
 * It follows the Command pattern, where each command is encapsulated in its own class,
 * allowing for modular and extensible command handling.
 */
public interface SubCommand {
    /**
     * Gets the name of the subcommand.
     * Used for command routing and help display.
     *
     * @return The subcommand name (without the /tt prefix)
     */
    String getName();

    /**
     * Gets a short description of what the subcommand does.
     * Used in help listings to explain the command purpose.
     *
     * @return The command description
     */
    String getDescription();

    /**
     * Gets the command syntax/usage information.
     * Shows users how to properly use the command.
     *
     * @return The command syntax with arguments
     */
    String getSyntax();

    /**
     * Executes the subcommand logic.
     * Called when a player or console runs the command.
     *
     * @param sender The command sender (player or console)
     * @param args The command arguments (including the subcommand name at index 0)
     */
    void perform(CommandSender sender, String[] args);
}