package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

/**
 * Command that removes an existing path from the system.
 * Deletes a path permanently from memory (will be removed from storage on next save).
 */
public class RemoveCommand implements SubCommand{
    /** Reference to the PathRecorder for managing paths */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new RemoveCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public RemoveCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes the path.";
    }

    @Override
    public String getSyntax() {
        return "/tt remove <path>";
    }

    /**
     * Removes a specified path from the system.
     * Checks permissions and validates that the path exists and isn't currently being tracked.
     *
     * @param sender The command sender (must be a player)
     * @param args The command arguments (args[1+] combine to form path name)
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        // Command can only be executed by players
        if (sender instanceof Player player) {
            // Check permissions
            if (player.hasPermission("TrailTracker.startstop")) {
                // Validate command format
                if (args.length >= 2) {
                    // Combine all remaining arguments for path name to allow spaces
                    String pathName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                    // Attempt to remove the path
                    PathRecorder.Result result = pathRecorder.removePath(pathName);

                    // Display appropriate success/error message
                    if (result.flag) {
                        player.sendMessage(UITextComponents.successMessage("Removed path", pathName));
                    } else player.sendMessage(UITextComponents.errorMessage(result.message));

                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}