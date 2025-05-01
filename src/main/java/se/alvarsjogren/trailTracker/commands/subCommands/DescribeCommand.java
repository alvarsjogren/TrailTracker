package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

/**
 * Command that sets or updates a path's description.
 * Allows players to add descriptive information to paths.
 */
public class DescribeCommand implements SubCommand {
    /** Reference to the PathRecorder for accessing path information */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new DescribeCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public DescribeCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "describe";
    }

    @Override
    public String getDescription() {
        return "Sets a description for a path.";
    }

    @Override
    public String getSyntax() {
        return "/tt describe <path> <description>";
    }

    /**
     * Sets or updates a description for a specified path.
     * Allows adding user-friendly descriptions to make paths more meaningful.
     *
     * @param sender The command sender (must be a player)
     * @param args The command arguments (args[1+] = path name and description)
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

        // Validate command format - need at least the command name and one more argument
        if (args.length < 3) {
            player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt describe <path> <description>"));
            return;
        }

        // Try to identify path name and description from the arguments
        String pathName = findPathName(args);

        // Check if a valid path was found
        if (pathName == null) {
            player.sendMessage(UITextComponents.errorMessage("Path not found. Use /tt list to see available paths."));
            return;
        }

        // Get the path object
        Path path = pathRecorder.getPaths().get(pathName);

        // Extract the description - everything after the path name
        int pathEndIndex = -1;
        for (int i = 1; i <= args.length - 1; i++) {
            String testPath = String.join(" ", Arrays.copyOfRange(args, 1, i + 1));
            if (testPath.equals(pathName)) {
                pathEndIndex = i;
                break;
            }
        }

        if (pathEndIndex == -1 || pathEndIndex >= args.length - 1) {
            player.sendMessage(UITextComponents.errorMessage("Please provide a description after the path name."));
            return;
        }

        String description = String.join(" ", Arrays.copyOfRange(args, pathEndIndex + 1, args.length));

        // Update the description
        path.setDescription(description);
        player.sendMessage(UITextComponents.successMessage("Updated description for", pathName));
    }

    /**
     * Helper method to find a valid path name from command arguments.
     * Tries different combinations of arguments to match an existing path.
     *
     * @param args The command arguments
     * @return The matching path name, or null if no path was found
     */
    private String findPathName(String[] args) {
        // Special case: Check if the entire argument string is a path
        String fullEnteredText = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Path exactPathMatch = pathRecorder.getPaths().get(fullEnteredText);
        if (exactPathMatch != null) {
            return fullEnteredText;
        }

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
}