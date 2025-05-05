package se.alvarsjogren.trailTracker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.commands.subCommands.*;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * Main command executor for the TrailTracker plugin.
 * Handles the routing of commands to the appropriate subcommand handlers.
 *
 * This class follows the Command pattern combined with a Chain of Responsibility pattern,
 * where multiple handlers (subcommands) are tried in sequence until one accepts
 * responsibility for handling the command.
 */
public class TTCommandExecutor implements CommandExecutor {

    /** List of all registered subcommands */
    private final List<SubCommand> subCommands = new ArrayList<>();

    /**
     * Creates a new TTCommandExecutor.
     * Initializes and registers all available subcommands.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public TTCommandExecutor(TrailTracker plugin) {

        // Register all subcommands
        subCommands.add(new HelpCommand(subCommands));
        subCommands.add(new StartCommand(plugin));
        subCommands.add(new StopCommand(plugin));
        subCommands.add(new ListCommand(plugin));
        subCommands.add(new RemoveCommand(plugin));
        subCommands.add(new InfoCommand(plugin));
        subCommands.add(new ModifyCommand(plugin));
    }

    /**
     * Handles all commands for the plugin.
     * Routes commands to the appropriate subcommand handler based on the first argument.
     *
     * @param sender The sender of the command
     * @param command The command being executed
     * @param label The alias used for the command
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Show default message if no arguments provided
        if (args.length == 0) {
            sender.sendMessage(UITextComponents.errorMessage("Use /tt help for commands."));
            return true;
        }

        // Try to find matching subcommand by comparing first argument with command names
        for (SubCommand subCommand : subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName())) {
                subCommand.perform(sender, args);
                return true;
            }
        }

        // No matching subcommand found, show error message
        sender.sendMessage(UITextComponents.errorMessage("Unknown command. Use /tt help."));
        return true;
    }

    /**
     * Gets the list of registered subcommands.
     * Used by the tab completer and help command.
     *
     * @return The list of subcommands
     */
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}