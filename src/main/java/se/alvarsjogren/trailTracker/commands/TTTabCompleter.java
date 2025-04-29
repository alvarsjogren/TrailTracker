package se.alvarsjogren.trailTracker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.commands.subCommands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer for TrailTracker commands.
 * Provides command completion suggestions based on context.
 *
 * This class enhances user experience by offering contextual tab completion options
 * that change based on:
 * - Which subcommand is being used
 * - Which argument position the user is currently typing
 * - The current state of paths in the system
 */
public class TTTabCompleter implements TabCompleter {
    /** List of all registered subcommands */
    private final List<SubCommand> subCommands;

    /** Reference to the PathRecorder for path-related completions */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new TTTabCompleter.
     *
     * @param subCommands The list of subcommands to provide completion for
     * @param plugin The TrailTracker plugin instance
     */
    public TTTabCompleter(List<SubCommand> subCommands, TrailTracker plugin) {
        this.subCommands = subCommands;
        pathRecorder = plugin.pathRecorder;
    }

    /**
     * Provides tab completion options based on the command context.
     * Offers different suggestions depending on:
     * - Which argument position is being completed
     * - Which subcommand is being used
     * - Current player state
     *
     * @param commandSender The sender of the command
     * @param command The command being tab-completed
     * @param label The alias used for the command
     * @param args The current command arguments
     * @return A list of possible tab completions
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument is always the subcommand name
            // Suggest all available subcommands that match the current typing
            completions = subCommands.stream()
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument depends on the first argument (subcommand)
            switch (args[0].toLowerCase()) {
                case "remove":
                case "describe":
                case "info":
                    // Complete with path names for commands that operate on existing paths
                    completions = pathRecorder.getPaths().values().stream()
                            .map(Path::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                case "display":
                    // Complete with "on" or "off" for display command
                    if ("on".startsWith(args[1].toLowerCase())) completions.add("on");
                    if ("off".startsWith(args[1].toLowerCase())) completions.add("off");
                    break;
            }
        } else if (args.length == 3) {
            // Third argument depends on the first and second arguments
            if (args[0].equalsIgnoreCase("display")) {
                if (args[1].equalsIgnoreCase("on")) {
                    // Complete with available paths when turning display on
                    completions = pathRecorder.getPaths().values().stream()
                            .map(Path::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                } else if (args[1].equalsIgnoreCase("off") && commandSender instanceof Player player) {
                    // Complete with paths the player is currently displaying when turning display off
                    var playerPaths = pathRecorder.getDisplayedPaths().get(player.getUniqueId());
                    if (playerPaths != null) {
                        completions = playerPaths.stream()
                                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                }
            }
        }

        return completions;
    }
}