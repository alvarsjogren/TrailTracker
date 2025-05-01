package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

/**
 * Command that controls path visibility for players.
 * Allows players to show or hide specific paths with particles.
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
        return "Starts displaying the path.";
    }

    @Override
    public String getSyntax() {
        return "/tt display on|off <path>";
    }

    /**
     * Toggles path visibility for the player.
     * Allows turning on/off visibility of specific paths with particles.
     *
     * @param sender The command sender (must be a player)
     * @param args Command arguments: "on"/"off" followed by path name
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
        if (args.length < 3) {
            player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            return;
        }

        // Validate on/off parameter
        String option = args[1].toLowerCase();
        if (!option.equals("on") && !option.equals("off")) {
            player.sendMessage(UITextComponents.errorMessage("Use either 'on' or 'off'. Example: /tt display on <path>"));
            return;
        }

        // Combine all remaining arguments for path name to allow spaces
        String pathName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        // Handle display on/off variants
        if (option.equals("on")) {
            // Turn on path display
            PathRecorder.Result result = pathRecorder.startDisplayingPath(player.getUniqueId(), pathName);
            if (result.flag) {
                player.sendMessage(UITextComponents.successMessage("Started displaying path", pathName));
            } else {
                player.sendMessage(UITextComponents.errorMessage(result.message));
            }
        } else {
            // Turn off path display
            PathRecorder.Result result = pathRecorder.stopDisplayingPath(player.getUniqueId(), pathName);
            if (result.flag) {
                player.sendMessage(UITextComponents.successMessage("Stopped displaying path", pathName));
            } else {
                player.sendMessage(UITextComponents.errorMessage(result.message));
            }
        }
    }
}