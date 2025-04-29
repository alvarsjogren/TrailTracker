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
import java.util.stream.Collectors;

/**
 * Tab completer for TrailTracker commands.
 * Provides command completion suggestions based on context.
 */
public class TTTabCompleter implements TabCompleter {
    private final List<SubCommand> subCommands;
    private final PathRecorder pathRecorder;

    /**
     * Creates a new TTTabCompleter.
     *
     * @param subCommands The list of subcommands to provide completion for
     * @param plugin The TrailTracker plugin instance
     */
    public TTTabCompleter(List<SubCommand> subCommands, TrailTracker plugin) {
        this.subCommands = subCommands;
        pathRecorder = plugin.pathRecorder;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument is always the subcommand name
            completions = subCommands.stream()
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Second argument depends on the first argument (subcommand)
            switch (args[0].toLowerCase()) {
                case "remove":
                case "describe":
                case "info":
                    // Complete with path names
                    completions = pathRecorder.getPaths().values().stream()
                            .map(Path::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                    break;
                case "display":
                    // Complete with "on" or "off"
                    if ("on".startsWith(args[1].toLowerCase())) completions.add("on");
                    if ("off".startsWith(args[1].toLowerCase())) completions.add("off");
                    break;
            }
        } else if (args.length == 3) {
            // Third argument depends on the first and second arguments
            if (args[0].equalsIgnoreCase("display")) {
                if (args[1].equalsIgnoreCase("on")) {
                    // Complete with available paths
                    completions = pathRecorder.getPaths().values().stream()
                            .map(Path::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                } else if (args[1].equalsIgnoreCase("off") && commandSender instanceof Player player) {
                    // Complete with paths the player is displaying
                    var playerPaths = pathRecorder.getDisplayedPaths().get(player.getUniqueId());
                    if (playerPaths != null) {
                        completions = playerPaths.stream()
                                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                                .collect(Collectors.toList());
                    }
                }
            }
        }

        return completions;
    }
}