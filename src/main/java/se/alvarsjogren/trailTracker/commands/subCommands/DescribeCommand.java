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

        // Validate command format - need at least the command name
        if (args.length < 2) {
            player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt describe <path> <description>"));
            return;
        }

        // Special case: Check if just path name was provided without description
        String fullEnteredText = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Path exactPathMatch = pathRecorder.getPaths().get(fullEnteredText);
        if (exactPathMatch != null) {
            // We found a complete path name but no description
            player.sendMessage(UITextComponents.errorMessage("Please provide a description after the path name."));
            return;
        }

        // Try to identify path name and description from the arguments
        boolean foundPath = false;
        Path path = null;
        String pathName = "";
        String description = "";

        // Try different combinations for path name and description
        for (int i = 1; i < args.length - 1; i++) {  // Ensure at least one argument remains for description
            // Try using i arguments for the path name
            pathName = String.join(" ", Arrays.copyOfRange(args, 1, i + 1));
            description = String.join(" ", Arrays.copyOfRange(args, i + 1, args.length));

            path = pathRecorder.getPaths().get(pathName);
            if (path != null) {
                // Found a valid path with description
                foundPath = true;
                break;
            }
        }

        // Check if we found a path with description
        if (foundPath && path != null) {
            // Update the description
            path.setDescription(description);
            player.sendMessage(UITextComponents.successMessage("Updated description for", pathName));
        } else {
            player.sendMessage(UITextComponents.errorMessage("Path not found. Use /tt list to see available paths."));
        }
    }
}