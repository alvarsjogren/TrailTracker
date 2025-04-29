package se.alvarsjogren.trailTracker.commands.subCommands;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand implements SubCommand{
    private final List<SubCommand> subCommands;

    public HelpCommand(List<SubCommand> subCommands) {
        this.subCommands = subCommands;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays a list of available commands.";
    }

    @Override
    public String getSyntax() {
        return "/tt help";
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
                        .text("Commands ===")
                        .color(TextColor.color(0xE78B48)));
        sender.sendMessage(header);

        for (SubCommand subCommand : subCommands) {
            final TextComponent item = Component
                    .text(subCommand.getSyntax() + " - " + subCommand.getDescription())
                    .color(TextColor.color(0xF5C45E));
            sender.sendMessage(item);
        }

        final TextComponent footer = Component
                .text("==========================")
                .color(TextColor.color(0xE78B48));
        sender.sendMessage(footer);
        sender.sendMessage("\n");
    }
}
