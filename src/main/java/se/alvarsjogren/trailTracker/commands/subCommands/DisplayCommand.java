package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;
import java.util.Set;

/**
 * Command that controls path visibility for players.
 * Allows players to toggle the visibility of specific paths with particles.
 */
public class DisplayCommand implements SubCommand {
    /** Reference to the PathRecorder for managing path display */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new DisplayCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public DisplayCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "display";
    }

    @Override
    public String getDescription() {
        return "Toggles display of the specified path.";
    }

    @Override
    public String getSyntax() {
        return "/tt display <path>";
    }

    /**
     * Toggles path visibility for the player.
     * If the path is not currently being displayed, it will start displaying.
     * If the path is already being displayed, it will stop displaying.
     *
     * @param sender The command sender (must be a player)
     * @param args Command arguments: path name
     */
    @Override
    public void perform(CommandSender sender, String[] args) {
        // Command can only be executed by players
        if (!(sender instanceof Player player)) {
            sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
            return;
        }

        // Check permissions
        if (!player.hasPermission("TrailTracker.display")) {
            player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
            return;
        }

        // Validate command format
        if (args.length < 2) {
            player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt display <path>"));
            return;
        }

        // Combine all remaining arguments for path name to allow spaces
        String pathName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Check if the path exists
        if (!pathRecorder.getPaths().containsKey(pathName)) {
            player.sendMessage(UITextComponents.errorMessage("Path not found: " + pathName));
            return;
        }

        // Check if player is already displaying this path
        Set<String> playerDisplayedPaths = pathRecorder.getDisplayedPaths().get(player.getUniqueId());
        boolean isDisplaying = playerDisplayedPaths != null && playerDisplayedPaths.contains(pathName);

        PathRecorder.Result result;

        // Toggle the display status
        if (isDisplaying) {
            // Path is currently displayed - stop displaying it
            result = pathRecorder.stopDisplayingPath(player.getUniqueId(), pathName);
            if (result.flag) {
                player.sendMessage(UITextComponents.successMessage("Stopped displaying path", pathName));
            } else {
                player.sendMessage(UITextComponents.errorMessage(result.message));
            }
        } else {
            // Path is not displayed - start displaying it
            result = pathRecorder.startDisplayingPath(player.getUniqueId(), pathName);
            if (result.flag) {
                player.sendMessage(UITextComponents.successMessage("Started displaying path", pathName));
            } else {
                player.sendMessage(UITextComponents.errorMessage(result.message));
            }
        }
    }
}