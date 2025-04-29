package se.alvarsjogren.trailTracker.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;

import java.util.Collection;
import java.util.Objects;

public class PlayerWalking implements Listener {
    private final PathRecorder pathRecorder;
    private final FileConfiguration config;

    private Particle displayParticle;
    private String travelingMessage;
    private String recordMessage;


    public PlayerWalking(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;
        this.config = plugin.getConfig();

        try {
            displayParticle = Particle.valueOf(plugin.getConfig().getString("default-display-particle"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Cant load default display particle. Will use plugin default");
            displayParticle= Particle.HAPPY_VILLAGER;
        }
        try {
            travelingMessage = Objects.requireNonNull(plugin.getConfig().getString("travel-message")).replace("{path-name}", "");
        } catch (NullPointerException e) {
            plugin.getLogger().warning("Cant load travel message. Will use plugin default");
            travelingMessage = "Traveling ";
        }
        try {
            recordMessage = Objects.requireNonNull(plugin.getConfig().getString("recording-message")).replace("{path-name}", "");
        } catch (NullPointerException e) {
            plugin.getLogger().warning("Cant load recording message. Will use plugin default");
            recordMessage = "Recording ";
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if ((event.getFrom().getX() != event.getTo().getX()) || (event.getFrom().getY() != event.getTo().getY()) || (event.getFrom().getZ() != event.getTo().getZ())) {
            for (Path path : pathRecorder.getPaths().values()) {
                if (!pathRecorder.getTrackedPaths().containsValue(path.getName())) {
                    for (Location location : path.getTrackedPath()) {
                        Collection<Player> closePlayers = location.getNearbyPlayers(config.getInt("path-radius"));
                        for (Player closePlayer : closePlayers) {
                            final TextComponent text = Component
                                    .text(travelingMessage)
                                    .color(TextColor.color(0xF5C45E))
                                    .append(Component
                                            .text(path.getName())
                                            .color(TextColor.color(0xE78B48))
                                            .decoration(TextDecoration.BOLD, true));
                            closePlayer.sendActionBar(text);
                        }
                    }
                }
            }

            if (pathRecorder.isPlayerTracking(player.getUniqueId())) {
                String pathName = pathRecorder.getTrackedPaths().get(player.getUniqueId());
                Path path = pathRecorder.getPaths().get(pathName);

                final TextComponent text = Component
                        .text(recordMessage)
                        .color(TextColor.color(0xF5C45E))
                        .append(Component
                                .text(path.getName())
                                .color(TextColor.color(0xE78B48))
                                .decoration(TextDecoration.BOLD, true));
                player.sendActionBar(text);

                path.displayPath(player, displayParticle);
                pathRecorder.trackPaths(player);
            }

            pathRecorder.displayPaths(player);
        }


    }



}
