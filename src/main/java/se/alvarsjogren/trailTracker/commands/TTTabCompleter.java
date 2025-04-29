package se.alvarsjogren.trailTracker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;
import se.alvarsjogren.trailTracker.commands.subCommands.*;

import java.util.ArrayList;
import java.util.List;

public class TTTabCompleter implements TabCompleter {
    private final List<SubCommand> subCommands;
    private final PathRecorder pathRecorder;

    public TTTabCompleter(List<SubCommand> subCommands, TrailTracker plugin) {
        this.subCommands = subCommands;
        pathRecorder = plugin.pathRecorder;

    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (SubCommand subCommand : subCommands) {
                if (subCommand.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand.getName());
                }
            }
        } else {
            if (args.length == 2) {
                if (args[0].equals("remove")) {
                    for (Path path : pathRecorder.getPaths().values()) {
                        completions.add(path.getName());
                    }
                }
                if (args[0].equals("display")) {
                    completions.add("on");
                    completions.add("off");
                }

            } else {
                if (args.length == 3) {
                    if (args[0].equals("display")) {
                        if (args[1].equals("on")) {
                            for (Path path : pathRecorder.getPaths().values()) {
                                completions.add(path.getName());
                            }
                        }
                        if (args[1].equals("off")) {
                            if (commandSender instanceof Player player) {
                                if (pathRecorder.getDisplayedPaths().containsKey(player.getUniqueId())) {
                                    completions.addAll(pathRecorder.getDisplayedPaths().get(player.getUniqueId()));
                                }
                            }
                        }

                    }
                }
            }
            // Option for more stuff!
        }

        return completions;
    }
}
