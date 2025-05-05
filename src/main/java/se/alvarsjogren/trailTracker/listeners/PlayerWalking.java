package se.alvarsjogren.trailTracker.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;

import java.util.Collection;
import java.util.Objects;

/**
 * Listens for player movement events to handle path tracking and display.
 *
 * This class is responsible for:
 * - Detecting when players move
 * - Recording movement for players who are tracking paths
 * - Notifying players when they are on or near a path
 */
public class PlayerWalking implements Listener {
    /** Reference to the PathRecorder for tracking and displaying paths */
    private final PathRecorder pathRecorder;

    /** Message template for when a player is traveling on a path */
    private String travelingMessage;

    /** Message template for when a player is recording a path */
    private String recordMessage;

    /**
     * Creates a new PlayerWalking listener.
     * Loads configuration values for particles and messages.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public PlayerWalking(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;

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

    /**
     * Handles player movement events.
     * Shows action bar messages for players on paths or recording paths.
     * Updates path recording for players who are tracking paths.
     *
     * @param event The PlayerMoveEvent
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only process significant movement (position changed)
        if ((event.getFrom().getX() != event.getTo().getX()) ||
                (event.getFrom().getY() != event.getTo().getY()) ||
                (event.getFrom().getZ() != event.getTo().getZ())) {

            // Check if player is near any path and show action bar message if they are
            showPathDetectionMessages(player);

            // If player is tracking a path, update the path with their new position and show record message
            if (pathRecorder.isPlayerTracking(player.getUniqueId())) {
                showRecordingMessage(player);
                pathRecorder.trackPaths(player);
            }
        }
    }

    /**
     * Shows messages for players near existing paths
     *
     * @param player The player to check and show messages to
     */
    private void showPathDetectionMessages(Player player) {
        for (Path path : pathRecorder.getPaths().values()) {
            // Only check paths that aren't currently being recorded
            if (!pathRecorder.getTrackedPaths().containsValue(path.getName())) {
                for (Location location : path.getTrackedPath()) {
                    Collection<Player> closePlayers = location.getNearbyPlayers(path.getRadius());
                    if (closePlayers.contains(player)) {
                        // Show action bar message to player near the path
                        final TextComponent text = Component
                                .text(travelingMessage)
                                .color(TextColor.color(0xF5C45E))
                                .append(Component
                                        .text(path.getName())
                                        .color(TextColor.color(0xE78B48))
                                        .decoration(TextDecoration.BOLD, true));
                        player.sendActionBar(text);
                        return; // Only show one message at a time
                    }
                }
            }
        }
    }

    /**
     * Shows recording message for a player who is tracking a path
     *
     * @param player The player recording a path
     */
    private void showRecordingMessage(Player player) {
        String pathName = pathRecorder.getTrackedPaths().get(player.getUniqueId());
        Path path = pathRecorder.getPaths().get(pathName);

        if (path != null) {
            final TextComponent text = Component
                    .text(recordMessage)
                    .color(TextColor.color(0xF5C45E))
                    .append(Component
                            .text(path.getName())
                            .color(TextColor.color(0xE78B48))
                            .decoration(TextDecoration.BOLD, true));
            player.sendActionBar(text);
        }
    }
}