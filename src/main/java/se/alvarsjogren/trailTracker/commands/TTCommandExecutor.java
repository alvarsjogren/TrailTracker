package se.alvarsjogren.trailTracker.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.commands.subCommands.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Main command executor for the TrailTracker plugin.
 * Handles the routing of commands to the appropriate subcommand handlers.
 */
public class TTCommandExecutor implements CommandExecutor {

    private final List<SubCommand> subCommands = new ArrayList<>();

    /**
     * Creates a new TTCommandExecutor.
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
        subCommands.add(new DisplayCommand(plugin));
        subCommands.add(new DescribeCommand(plugin));
        subCommands.add(new InfoCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            final TextComponent text = Component
                    .text("[tt] ")
                    .color(TextColor.color(0x102E50))
                    .append(Component
                            .text("Use /tt help for commands.")
                            .color(TextColor.color(0xF5C45E)));

            sender.sendMessage(text);
            return true;
        }

        // Try to find matching subcommand
        for (SubCommand subCommand : subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName())) {
                subCommand.perform(sender, args);
                return true;
            }
        }

        // No matching subcommand found
        final TextComponent text = Component
                .text("[tt] ")
                .color(TextColor.color(0x102E50))
                .append(Component
                        .text("Unknown command. Use /tt help.")
                        .color(TextColor.color(0xF5C45E)));
        sender.sendMessage(text);
        return true;
    }

    /**
     * Gets the list of registered subcommands.
     *
     * @return The list of subcommands
     */
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}