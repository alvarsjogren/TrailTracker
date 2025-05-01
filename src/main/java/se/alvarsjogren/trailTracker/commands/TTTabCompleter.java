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
                case "info":
                    // Complete with path names for commands that operate on existing paths
                    completions = suggestPartialPathNames(args, 1);
                    break;
                case "describe":
                    // For describe, offer path name suggestions
                    completions = suggestPartialPathNames(args, 1);
                    break;
                case "display":
                    // Complete with "on" or "off" for display command
                    if ("on".startsWith(args[1].toLowerCase())) completions.add("on");
                    if ("off".startsWith(args[1].toLowerCase())) completions.add("off");
                    break;
            }
        } else if (args.length >= 3) {
            // Third+ argument depends on the first and second arguments
            if (args[0].equalsIgnoreCase("display")) {
                if (args[1].equalsIgnoreCase("on")) {
                    // Complete with available paths when turning display on
                    completions = suggestPartialPathNames(args, 2);
                } else if (args[1].equalsIgnoreCase("off") && commandSender instanceof Player player) {
                    // Complete with paths the player is currently displaying when turning display off
                    var playerPaths = pathRecorder.getDisplayedPaths().get(player.getUniqueId());
                    if (playerPaths != null) {
                        completions = suggestPartialDisplayedPaths(args, 2, playerPaths);
                    }
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                // For info command with 3+ arguments, check if we need path completion
                // This helps with multi-word path names
                String partialPath = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                boolean foundExactMatch = false;

                // Check if we have an exact match for any path
                for (String pathName : pathRecorder.getPaths().keySet()) {
                    if (pathName.equals(partialPath)) {
                        foundExactMatch = true;
                        break;
                    }
                }

                // If no exact match yet, suggest paths that start with the current partial path
                if (!foundExactMatch) {
                    completions = pathRecorder.getPaths().keySet().stream()
                            .filter(name -> name.startsWith(partialPath))
                            .collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("describe")) {
                // For describe command, we need to determine if we're still typing the path name
                // or if we've moved on to the description

                // First check if we have an exact match for any path in the arguments so far
                for (int i = 1; i < args.length; i++) {
                    String possiblePath = String.join(" ", java.util.Arrays.copyOfRange(args, 1, i + 1));

                    // If this is a complete path name, then we're no longer suggesting path completions
                    if (pathRecorder.getPaths().containsKey(possiblePath)) {
                        // We found a complete path match, so we're now typing the description
                        // No tab completion for description text
                        return completions; // Return empty list
                    }
                }

                // If we get here, we're still looking for a path name
                String partialPath = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

                // Suggest paths that start with the current partial path
                completions = suggestPartialPathNames(args, 1);
            }
        }

        return completions;
    }

    /**
     * Suggests path names for tab completion, considering both full and partial path names.
     *
     * @param args Command arguments
     * @param startIndex Index where the path name begins
     * @return List of matching path suggestions
     */
    private List<String> suggestPartialPathNames(String[] args, int startIndex) {
        // If we're just starting to type a path name
        if (args.length == startIndex + 1) {
            // Simple case - suggest paths that start with the current argument
            return pathRecorder.getPaths().values().stream()
                    .map(Path::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[startIndex].toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // We might be typing a path name with spaces
            String partialPath = String.join(" ", java.util.Arrays.copyOfRange(args, startIndex, args.length));

            // Get all paths that start with our partial path
            List<String> matchingPaths = pathRecorder.getPaths().values().stream()
                    .map(Path::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialPath.toLowerCase()))
                    .collect(Collectors.toList());

            // For tab completion with spaces, we only want to suggest the next word,
            // not the entire remaining path
            List<String> nextWordSuggestions = new ArrayList<>();
            for (String fullPath : matchingPaths) {
                // Only process if this path has more content than what we've already typed
                if (fullPath.length() > partialPath.length()) {
                    // Extract just the next part after what we've already typed
                    String remainingPart = fullPath.substring(partialPath.length());

                    // If there's a space in the remaining part, only get the text up to that space
                    int nextSpacePos = remainingPart.indexOf(' ');
                    if (nextSpacePos > 0) {
                        remainingPart = remainingPart.substring(0, nextSpacePos);
                    }

                    // Add this as the suggested completion
                    nextWordSuggestions.add(args[args.length - 1] + remainingPart);
                }
            }

            return nextWordSuggestions.isEmpty() ? matchingPaths : nextWordSuggestions;
        }
    }

    /**
     * Suggests displayed path names for tab completion.
     *
     * @param args Command arguments
     * @param startIndex Index where the path name begins
     * @param playerPaths Set of path names the player is displaying
     * @return List of matching path suggestions
     */
    private List<String> suggestPartialDisplayedPaths(String[] args, int startIndex, java.util.Set<String> playerPaths) {
        // If we're just starting to type a path name
        if (args.length == startIndex + 1) {
            // Simple case - suggest paths that start with the current argument
            return playerPaths.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[startIndex].toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // We might be typing a path name with spaces
            String partialPath = String.join(" ", java.util.Arrays.copyOfRange(args, startIndex, args.length));

            // Get all paths that start with our partial path
            List<String> matchingPaths = playerPaths.stream()
                    .filter(name -> name.toLowerCase().startsWith(partialPath.toLowerCase()))
                    .collect(Collectors.toList());

            // For tab completion with spaces, we only want to suggest the next word,
            // not the entire remaining path
            List<String> nextWordSuggestions = new ArrayList<>();
            for (String fullPath : matchingPaths) {
                // Only process if this path has more content than what we've already typed
                if (fullPath.length() > partialPath.length()) {
                    // Extract just the next part after what we've already typed
                    String remainingPart = fullPath.substring(partialPath.length());

                    // If there's a space in the remaining part, only get the text up to that space
                    int nextSpacePos = remainingPart.indexOf(' ');
                    if (nextSpacePos > 0) {
                        remainingPart = remainingPart.substring(0, nextSpacePos);
                    }

                    // Add this as the suggested completion
                    nextWordSuggestions.add(args[args.length - 1] + remainingPart);
                }
            }

            return nextWordSuggestions.isEmpty() ? matchingPaths : nextWordSuggestions;
        }
    }
}