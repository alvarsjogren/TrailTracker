package se.alvarsjogren.trailTracker.commands;

import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.commands.subCommands.*;
import se.alvarsjogren.trailTracker.utilities.ParticleUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tab completer for TrailTracker commands.
 * Provides command completion suggestions based on context.
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
                case "display":
                    // Complete with path names for commands that operate on existing paths
                    completions = suggestPartialPathNames(args, 1);
                    break;
                case "modify":
                    // Get all path names from recorder
                    Map<String, Path> availablePaths = pathRecorder.getPaths();

                    // Check if we have a complete path name
                    for (String path : availablePaths.keySet()) {
                        String[] pathWords = path.split(" ");

                        // If path has multiple words, check if it's fully typed
                        if (pathWords.length > 1) {
                            // Check if we have enough args to potentially contain this path
                            if (args.length >= 1 + pathWords.length) {
                                boolean matches = true;

                                // Check if all words of the path are present in args
                                for (int i = 0; i < pathWords.length; i++) {
                                    if (!args[i + 1].equalsIgnoreCase(pathWords[i])) {
                                        matches = false;
                                        break;
                                    }
                                }

                                // If the path is fully typed, suggest actions
                                if (matches) {
                                    // We're right after a complete path name - suggest actions
                                    if (args.length == 1 + pathWords.length) {
                                        // User has typed the full path, now suggest actions
                                        SubCommand modifyCmd = subCommands.stream()
                                                .filter(cmd -> cmd.getName().equals("modify"))
                                                .findFirst()
                                                .orElse(null);

                                        if (modifyCmd instanceof ModifyCommand) {
                                            return ((ModifyCommand) modifyCmd).getAvailableActions();
                                        }
                                    }
                                    // User is typing an action
                                    else if (args.length == 2 + pathWords.length) {
                                        String typed = args[1 + pathWords.length].toLowerCase();
                                        SubCommand modifyCmd = subCommands.stream()
                                                .filter(cmd -> cmd.getName().equals("modify"))
                                                .findFirst()
                                                .orElse(null);

                                        if (modifyCmd instanceof ModifyCommand) {
                                            return ((ModifyCommand) modifyCmd).getAvailableActions().stream()
                                                    .filter(action -> action.toLowerCase().startsWith(typed))
                                                    .collect(Collectors.toList());
                                        }
                                    }
                                    // User is typing a value for "particle" action
                                    else if (args.length == 3 + pathWords.length) {
                                        String action = args[1 + pathWords.length].toLowerCase();
                                        String typed = args[2 + pathWords.length].toLowerCase();

                                        if (action.equals("particle")) {
                                            completions = Arrays.stream(Particle.values())
                                                    .filter(particle -> !ParticleUtilities.isProblematicParticle(particle))
                                                    .map(Particle::name)
                                                    .filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                                                    .collect(Collectors.toList());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // If we reach here, either:
                    // 1. The path is single-word
                    // 2. The path isn't completely typed yet
                    // Handle single-word paths specifically
                    if (args.length == 2) {
                        // Only try tab completion for a path name
                        completions = suggestPartialPathNames(args, 1);
                    } else if (args.length == 3) {
                        // Check if arg[1] is a complete path (no spaces)
                        String potentialPath = args[1];
                        if (availablePaths.containsKey(potentialPath)) {
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
                        } else {
                            // Could still be typing a multi-word path
                            completions = suggestPartialPathNames(args, 1);
                        }
                    } else if (args.length == 4) {
                        // First check if args[1] + args[2] is a path (has a space)
                        String potentialPath = args[1] + " " + args[2];
                        if (availablePaths.containsKey(potentialPath)) {
                            // args[3] should be an action
                            SubCommand modifyCmd = subCommands.stream()
                                    .filter(cmd -> cmd.getName().equals("modify"))
                                    .findFirst()
                                    .orElse(null);

                            if (modifyCmd instanceof ModifyCommand) {
                                // Suggest available actions
                                completions = ((ModifyCommand) modifyCmd).getAvailableActions().stream()
                                        .filter(action -> action.toLowerCase().startsWith(args[3].toLowerCase()))
                                        .collect(Collectors.toList());
                            }
                        } else if (availablePaths.containsKey(args[1])) {
                            // args[1] is the path, args[2] is the action
                            String action = args[2].toLowerCase();

                            // If the action is "particle", suggest particle types
                            if (action.equals("particle")) {
                                completions = Arrays.stream(Particle.values())
                                        .map(Particle::name)
                                        .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                                        .collect(Collectors.toList());
                            }
                        } else {
                            // Could still be typing a multi-word path
                            completions = suggestPartialPathNames(args, 1);
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
            String partialPath = String.join(" ", Arrays.copyOfRange(args, startIndex, args.length - 1));

            if (!partialPath.isEmpty()) {
                partialPath += " ";
            }

            // Add the last argument if it exists
            String lastArg = args[args.length - 1];
            String finalPartialPath = partialPath + lastArg;

            // Get all paths that start with our partial path
            List<String> matchingPaths = pathRecorder.getPaths().values().stream()
                    .map(Path::getName)
                    .filter(name -> name.toLowerCase().startsWith(finalPartialPath.toLowerCase()))
                    .collect(Collectors.toList());

            // For tab completion with spaces, we want to suggest the next word or completion
            List<String> nextWordSuggestions = new ArrayList<>();
            for (String fullPath : matchingPaths) {
                // Only process if this path has more content than what we've already typed
                if (fullPath.length() > finalPartialPath.length()) {
                    // Extract just the next part after what we've already typed
                    String remainingPart = fullPath.substring(finalPartialPath.length());

                    // If there's a space in the remaining part, only get the text up to that space
                    int nextSpacePos = remainingPart.indexOf(' ');
                    if (nextSpacePos > 0) {
                        remainingPart = remainingPart.substring(0, nextSpacePos);
                    }

                    // Add this as the suggested completion
                    nextWordSuggestions.add(lastArg + remainingPart);
                } else if (fullPath.length() == finalPartialPath.length() && fullPath.equalsIgnoreCase(finalPartialPath)) {
                    // Exact match, add to suggestions to support continuing to the action
                    nextWordSuggestions.add(lastArg);
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
        String longestMatch = null;

        for (int i = 1; i < args.length - 1; i++) {
            // Try using i arguments for the path name
            String pathName = String.join(" ", Arrays.copyOfRange(args, 1, i + 1));

            if (pathRecorder.getPaths().containsKey(pathName)) {
                // Save the longest path match
                if (longestMatch == null || pathName.length() > longestMatch.length()) {
                    longestMatch = pathName;
                }
            }
        }

        return longestMatch;
    }
}