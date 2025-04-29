package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

public class DisplayCommand implements SubCommand{
    private final PathRecorder pathRecorder;

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

    @Override
    public void perform(CommandSender sender, String[] args) {

        if (sender instanceof Player player) {
            if (player.hasPermission("TrailTracker.display")) {
                if (args.length >= 3) {
                    String pathName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (args[1].equals("on")) {
                        PathRecorder.Result result = pathRecorder.startDisplayingPath(player.getUniqueId(), pathName);
                        if (result.flag) {
                            player.sendMessage(UITextComponents.successMessage("Started displaying path", pathName));
                        } else player.sendMessage(UITextComponents.errorMessage(result.message));

                    } else if (args[1].equals("off")) {
                        PathRecorder.Result result = pathRecorder.stopDisplayingPath(player.getUniqueId(), pathName);
                        if (result.flag) {
                            player.sendMessage(UITextComponents.successMessage("Stopped displaying path", pathName));
                        } else player.sendMessage(UITextComponents.errorMessage(result.message));
                    }
                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}
