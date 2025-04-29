package se.alvarsjogren.trailTracker.commands.subCommands;

import org.bukkit.command.CommandSender;

public interface SubCommand {
    String getName();
    String getDescription();
    String getSyntax();
    void perform(CommandSender sender, String[] args);
}
