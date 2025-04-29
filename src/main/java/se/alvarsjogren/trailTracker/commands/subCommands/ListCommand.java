package se.alvarsjogren.trailTracker.commands.subCommands;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;

public class ListCommand implements SubCommand{
    private final PathRecorder pathRecorder;

    public ListCommand(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Lists all paths.";
    }

    @Override
    public String getSyntax() {
        return "/tt list";
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        sender.sendMessage("\n");
        final TextComponent header = Component
                .text("=== ")
                .color(TextColor.color(0xE78B48))
                .append(Component.text("Trail")
                        .color(TextColor.color(0x102E50)))
                .append(Component.text("Tracker ")
                        .color(TextColor.color(0xBE3D2A)))
                .append(Component
                        .text("Paths ===")
                        .color(TextColor.color(0xE78B48)));
        sender.sendMessage(header);

        for (Path path : pathRecorder.getPaths().values()) {
            final TextComponent item = Component
                    .text(path.getName() + " - " + path.getDescription())
                    .color(TextColor.color(0xF5C45E));
            sender.sendMessage(item);
        }

        final TextComponent footer = Component
                .text("=======================")
                .color(TextColor.color(0xE78B48));
        sender.sendMessage(footer);
        sender.sendMessage("\n");
    }
}
