package se.alvarsjogren.trailTracker.commands.subCommands;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
                        final TextComponent text = Component
                                        .text("Stopped tracking path ")
                                        .color(TextColor.color(0xF5C45E))
                                .append(Component
                                        .text(pathName)
                                        .color(TextColor.color(0xE78B48))
                                        .decoration(TextDecoration.BOLD, true));
                        player.sendMessage(UITextComponents.TTPrefix().append(text));

                    } else player.sendMessage(UITextComponents.errorMessage(result.message));
                } else player.sendMessage(UITextComponents.errorMessage("Wrong usage. Use /tt help for usage."));
            } else player.sendMessage(UITextComponents.errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(UITextComponents.errorMessage("Only a player can use this command."));
    }
}
