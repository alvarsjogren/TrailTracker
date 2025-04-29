package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

/**
 * Command that starts tracking a new path.
 * Creates a new path and begins recording player movement.
 */
public class StartCommand implements SubCommand{
    /** Reference to the PathRecorder for managing paths */
    private final PathRecorder pathRecorder;

    /**
     * Creates a new StartCommand.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public StartCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Starts tracking a new path.";
    }

    @Override
    public String getSyntax() {
        return "/tt start <path>";
    }

    /**
     * Starts tracking a new path with the given name.
     * Checks permissions and validates input before creating a path.
     *
     * @param sender The command sender (must be a player)
     * @param args The command arguments (args[1+] combine to form path name)
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

        // Check if path name is provided
        if (args.length < 2) {
            player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            return;
        }

        // Combine all remaining arguments for path name to allow spaces
        String pathName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Attempt to start tracking the path
        PathRecorder.Result result = pathRecorder.startTrackingPath(player.getUniqueId(), player.getName(), pathName);

        // Display appropriate success/error message
        if (result.flag) {
            player.sendMessage(UITextComponents.successMessage("Started tracking path", pathName));
        } else {
            player.sendMessage(UITextComponents.errorMessage(result.message));
        }
    }
}