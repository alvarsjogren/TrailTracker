package se.alvarsjogren.trailTracker.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.commands.subCommands.*;

import java.util.ArrayList;
import java.util.List;

public class TTCommandExecutor implements CommandExecutor {

    private final List<SubCommand> subCommands = new ArrayList<>();

    // Add new commands here!
    public TTCommandExecutor(TrailTracker plugin) {
        subCommands.add(new HelpCommand(subCommands));
        subCommands.add(new StartCommand(plugin));
        subCommands.add(new StopCommand(plugin));
        subCommands.add(new ListCommand(plugin));
        subCommands.add(new RemoveCommand(plugin));
        subCommands.add(new DisplayCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            final TextComponent text = Component
                    .text("[tt] ")
                    .color(TextColor.color(0x102E50))
                    .append(Component
                            .text("Use /tt help for commands.")
                            .color(TextColor.color(0xF5C45E)));

            sender.sendMessage(text);
            return true;
        }

        for (SubCommand subCommand : subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName())) {
                subCommand.perform(sender, args);
                return true;
            }
        }

        final TextComponent text = Component
                .text("[tt] ")
                .color(TextColor.color(0x102E50))
                .append(Component
                        .text("Unknown command. Use /tt help.")
                        .color(TextColor.color(0xF5C45E)));
        sender.sendMessage(text);
        return true;
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}
