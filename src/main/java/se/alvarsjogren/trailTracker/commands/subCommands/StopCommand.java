package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

public class StopCommand implements SubCommand{
    private final PathRecorder pathRecorder;

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

    @Override
    public void perform(CommandSender sender, String[] args) {

        if (sender instanceof Player player) {
            if (player.hasPermission("TrailTracker.startstop")) {
                if (args.length == 1) {
                    String pathName = "";
                    if (pathRecorder.isPlayerTracking(player.getUniqueId())) {
                        pathName = pathRecorder.getTrackedPaths().get(player.getUniqueId());
                    }
                    PathRecorder.Result result = pathRecorder.stopTrackingPath(player.getUniqueId());
                    if (result.flag) {

                        player.sendMessage(UITextComponents.successMessage("Stopped tracking path", pathName));
                    } else player.sendMessage(UITextComponents.errorMessage(result.message));
                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}
