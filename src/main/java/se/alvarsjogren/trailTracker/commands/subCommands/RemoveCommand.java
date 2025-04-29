package se.alvarsjogren.trailTracker.commands.subCommands;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;

import java.util.Arrays;

public class RemoveCommand implements SubCommand{
    private final PathRecorder pathRecorder;

    private final TextComponent ttPrefix = Component
            .text("[")
            .color(TextColor.color(0xE78B48))
            .append(Component
                    .text("T")
                    .color(TextColor.color(0x102E50))
                    .decoration(TextDecoration.BOLD, true))
            .append(Component
                    .text("T")
                    .color(TextColor.color(0xBE3D2A))
                    .decoration(TextDecoration.BOLD, true))
            .append(Component
                    .text("] ")
                    .color(TextColor.color(0xE78B48)));

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
                        final TextComponent text = Component
                                        .text("Removed path ")
                                        .color(TextColor.color(0xF5C45E))
                                .append(Component
                                        .text(pathName)
                                        .color(TextColor.color(0xE78B48))
                                        .decoration(TextDecoration.BOLD, true));
                        player.sendMessage(ttPrefix.append(text));

                    } else player.sendMessage(errorMessage(result.message));
                } else player.sendMessage(errorMessage("Wrong usage. Use /tt help for usage."));
            } else player.sendMessage(errorMessage("You are not allowed to use that command."));
        } else sender.sendMessage(errorMessage("Only a player can use this command."));
    }

    private TextComponent errorMessage(String message) {
        return ttPrefix.append(Component.text(message).color(TextColor.color(0xBE3D2A)));
    }
}
