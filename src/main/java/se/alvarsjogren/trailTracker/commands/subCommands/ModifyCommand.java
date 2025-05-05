package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;
import java.util.List;

/**
 * Command that allows modification of existing path properties.
 * Currently supports modifying the detection radius of a path.
 */
public class ModifyCommand implements SubCommand {
    /** Reference to the PathRecorder for accessing path information */
    private final PathRecorder pathRecorder;

    /** List of available modification actions */
    private final List<String> availableActions = List.of("radius", "particle");

    /**
     * Creates a new ModifyCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public ModifyCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "modify";
    }

    @Override
    public String getDescription() {
        return "Modifies properties of an existing path.";
    }

    @Override
    public String getSyntax() {
        return "/tt modify <path> <action> <value>";
    }

    /**
     * Modifies a property of an existing path.
     * Currently supports changing the detection radius.
     *
     * @param sender The command sender (must be a player)
     * @param args The command arguments (args[1] = path name, args[2] = action, args[3] = value)
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        // Command can only be executed by players
        if (!(sender instanceof Player player)) {
            sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
            return;
        }

        // Check permissions
        if (!player.hasPermission("TrailTracker.startstop")) {
            player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
            return;
        }

        // Validate command format
        if (args.length < 4) {
            player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt modify <path> <action> <value>"));
            return;
        }

        // Find path name (which might contain spaces)
        String pathName = findPathName(args);
        if (pathName == null) {
            player.sendMessage(UITextComponents.errorMessage("Path not found. Use /tt list to see available paths."));
            return;
        }

        // Get the path object
        Path path = pathRecorder.getPaths().get(pathName);

        // Check if path is being tracked
        if (pathRecorder.getTrackedPaths().containsValue(pathName)) {
            player.sendMessage(UITextComponents.errorMessage("Cannot modify a path while it's being recorded. Stop tracking first."));
            return;
        }

        // Extract action and value
        int pathEndIndex = -1;
        for (int i = 1; i <= args.length - 2; i++) { // Need at least 2 more args after path
            String testPath = String.join(" ", Arrays.copyOfRange(args, 1, i + 1));
            if (testPath.equals(pathName)) {
                pathEndIndex = i;
                break;
            }
        }

        if (pathEndIndex == -1 || pathEndIndex >= args.length - 2) {
            player.sendMessage(UITextComponents.errorMessage("Please provide an action and value after the path name."));
            return;
        }

        String action = args[pathEndIndex + 1].toLowerCase();
        String valueStr = args[pathEndIndex + 2];

        // Process the action
        handleModification(player, path, action, valueStr);
    }

    /**
     * Handles the actual modification based on the action and value.
     *
     * @param player The player executing the command
     * @param path The path to modify
     * @param action The modification action to perform
     * @param valueStr The string value to apply
     */
    private void handleModification(Player player, Path path, String action, String valueStr) {
        if (!availableActions.contains(action)) {
            player.sendMessage(UITextComponents.errorMessage("Unknown action. Available actions: " +
                    String.join(", ", availableActions)));
            return;
        }

        if (action.equals("radius")) {
            try {
                int newRadius = Integer.parseInt(valueStr);
                if (newRadius <= 0) {
                    player.sendMessage(UITextComponents.errorMessage("Radius must be a positive number."));
                    return;
                }

                path.setRadius(newRadius);
                player.sendMessage(UITextComponents.successMessage("Updated path radius to " + newRadius + " for", path.getName()));
            } catch (NumberFormatException e) {
                player.sendMessage(UITextComponents.errorMessage("Invalid radius value. Please enter a number."));
            }
        }

        if (action.equals("particle")) {

        }
        // Future actions can be added here
    }

    /**
     * Helper method to find a valid path name from command arguments.
     * Tries different combinations of arguments to match an existing path.
     *
     * @param args The command arguments
     * @return The matching path name, or null if no path was found
     */
    private String findPathName(String[] args) {
        // Try different combinations for path name
        for (int i = 1; i < args.length - 1; i++) {
            // Try using i arguments for the path name
            String pathName = String.join(" ", Arrays.copyOfRange(args, 1, i + 1));

            if (pathRecorder.getPaths().containsKey(pathName)) {
                return pathName;
            }
        }

        return null;
    }

    /**
     * Gets the list of available modification actions.
     * Used for tab completion.
     *
     * @return List of available actions
     */
    public List<String> getAvailableActions() {
        return availableActions;
    }
}