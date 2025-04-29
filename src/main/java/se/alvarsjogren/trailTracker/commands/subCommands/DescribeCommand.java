package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.utilities.UITextComponents;

import java.util.Arrays;

public class DescribeCommand implements SubCommand {
    private final PathRecorder pathRecorder;

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

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            if (player.hasPermission("TrailTracker.startstop")) {
                if (args.length >= 3) {
                    String pathName = args[1];
                    String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                    Path path = pathRecorder.getPaths().get(pathName);
                    if (path != null) {
                        path.setDescription(description);
                        player.sendMessage(UITextComponents.successMessage("Updated description for", pathName));
                    } else player.sendMessage(UITextComponents.errorMessage("Path not found: " + pathName));

                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt describe <path> <description>"));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}