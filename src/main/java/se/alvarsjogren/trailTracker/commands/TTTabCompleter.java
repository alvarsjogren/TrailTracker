package se.alvarsjogren.trailTracker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
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
        } else if (args.length >= 2) {
            switch (args[0].toLowerCase()) {
                case "remove":
                case "info":
                case "describe":
                case "display":
                    // Complete with path names for commands that operate on existing paths
                    completions = suggestPartialPathNames(args, 1);
                    break;
                case "modify":
                    if (args.length == 2) {
                        // Suggest path names for the first argument of modify
                        completions = suggestPartialPathNames(args, 1);
                    } else if (args.length == 3) {
                        // Find if we've completed a path name
                        String pathName = findCompletedPathName(args);
                        if (pathName != null) {
                            // Get the modify command to access available actions
                            SubCommand modifyCmd = subCommands.stream()
                                    .filter(cmd -> cmd.getName().equals("modify"))
                                    .findFirst()
                                    .orElse(null);

                            if (modifyCmd instanceof ModifyCommand) {
                                // Suggest available actions for the second argument
                                completions = ((ModifyCommand) modifyCmd).getAvailableActions().stream()
                                        .filter(action -> action.toLowerCase().startsWith(args[2].toLowerCase()))
                                        .collect(Collectors.toList());
                            }
                        }
                    }
                    break;
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
     * Helper method to find a completed path name from command arguments.
     * Used for tab completion of subsequent arguments.
     *
     * @param args The command arguments
     * @return The matching path name, or null if no path was found
     */
    private String findCompletedPathName(String[] args) {
        // Try different combinations for path name
        for (int i = 1; i < args.length - 1; i++) {
            // Try using i arguments for the path name
            String pathName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, i + 1));

            if (pathRecorder.getPaths().containsKey(pathName)) {
                return pathName;
            }
        }

        return null;
    }
}