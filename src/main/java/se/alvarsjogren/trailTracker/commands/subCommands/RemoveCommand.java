package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

public class RemoveCommand implements SubCommand{
    private final PathRecorder pathRecorder;

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

    @Override
    public void perform(CommandSender sender, String[] args) {

        if (sender instanceof Player player) {
            if (player.hasPermission("TrailTracker.startstop")) {
                if (args.length >= 2) {
                    String pathName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    PathRecorder.Result result = pathRecorder.removePath(pathName);
                    if (result.flag) {
                        player.sendMessage(UITextComponents.successMessage("Removed path", pathName));
                    } else player.sendMessage(UITextComponents.errorMessage(result.message));

                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}
