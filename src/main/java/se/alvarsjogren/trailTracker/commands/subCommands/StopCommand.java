package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

/**
 * Command that stops tracking the current path.
 * Ends recording of player movement for path creation.
 */
public class StopCommand implements SubCommand{
    /** Reference to the PathRecorder for managing paths */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new StopCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public StopCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stops tracking the path.";
    }

    @Override
    public String getSyntax() {
        return "/tt stop";
    }

    /**
     * Stops tracking the current path for the player.
     * Checks permissions and validates that player is tracking a path.
     *
     * @param sender The command sender (must be a player)
     * @param args The command arguments (not used)
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        // Command can only be executed by players
        if (sender instanceof Player player) {
            // Check permissions
            if (player.hasPermission("TrailTracker.startstop")) {
                // Validate command format
                if (args.length == 1) {
                    // Get the path name being tracked (if any)
                    String pathName = "";
                    if (pathRecorder.isPlayerTracking(player.getUniqueId())) {
                        pathName = pathRecorder.getTrackedPaths().get(player.getUniqueId());
                    }

                    // Attempt to stop tracking
                    PathRecorder.Result result = pathRecorder.stopTrackingPath(player.getUniqueId());

                    // Display appropriate success/error message
                    if (result.flag) {
                        player.sendMessage(UITextComponents.successMessage("Stopped tracking path", pathName));
                    } else player.sendMessage(UITextComponents.errorMessage(result.message));
                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}