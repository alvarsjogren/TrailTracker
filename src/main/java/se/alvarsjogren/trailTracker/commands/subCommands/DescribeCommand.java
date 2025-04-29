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
     * @param args The command arguments (args[1] = path name, args[2+] = description)
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        // Command can only be executed by players
        if (sender instanceof Player player) {
            // Check permissions
            if (player.hasPermission("TrailTracker.startstop")) {
                // Validate command format
                if (args.length >= 3) {
                    // Get path name from arguments
                    String pathName = args[1];

                    // Combine remaining arguments as the description
                    String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                    // Try to get the path
                    Path path = pathRecorder.getPaths().get(pathName);
                    if (path != null) {
                        // Update the description
                        path.setDescription(description);
                        player.sendMessage(UITextComponents.successMessage("Updated description for", pathName));
                    } else player.sendMessage(UITextComponents.errorMessage("Path not found: " + pathName));

                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt describe <path> <description>"));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}