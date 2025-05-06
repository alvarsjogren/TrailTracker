package se.alvarsjogren.trailTracker.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for player movement events to handle path tracking and display.
 *
 * This class is responsible for:
 * - Detecting when players move
 * - Recording movement for players who are tracking paths
 * - Notifying players when they are on or near a path
 * - Managing notification timing for improved user experience
 */
public class PlayerHandler implements Listener {
    /** Reference to the PathRecorder for tracking and displaying paths */
    private final PathRecorder pathRecorder;

    /** Message template for when a player is traveling on a path */
    private String travelingMessage;

    /** Message template for when a player is recording a path */
    private String recordMessage;

    /** Maps player UUIDs to the path they are currently on */
    private final Map<UUID, String> currentPlayerPaths = new ConcurrentHashMap<>();

    /** Maps player UUIDs to when they last received a path notification */
    private final Map<UUID, Long> lastNotificationTime = new ConcurrentHashMap<>();

    /** How long to wait before showing another notification for the same path (in milliseconds) */
    private long reminderInterval;

    /**
     * Creates a new PlayerHandler listener.
     * Loads configuration values for particles and messages.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public PlayerHandler(TrailTracker plugin) {
        this.pathRecorder = plugin.pathRecorder;

        // Load message templates
        try {
            travelingMessage = Objects.requireNonNull(plugin.getConfig().getString("travel-message")).replace("{path-name}", "");
        } catch (NullPointerException e) {
            plugin.getLogger().warning("Cannot load travel message. Will use plugin default");
            travelingMessage = "Traveling ";
        }

        try {
            recordMessage = Objects.requireNonNull(plugin.getConfig().getString("recording-message")).replace("{path-name}", "");
        } catch (NullPointerException e) {
            plugin.getLogger().warning("Cannot load recording message. Will use plugin default");
            recordMessage = "Recording ";
        }

        // Load reminder interval (default to 30 seconds if not specified)
        reminderInterval = plugin.getConfig().getLong("path-notification-reminder", 30000);
        plugin.getLogger().info("Path notification reminder interval set to " + reminderInterval + "ms");
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

            // First priority: If player is tracking a path, always show recording message
            // and update the path with their new position
            if (pathRecorder.isPlayerTracking(player.getUniqueId())) {
                showRecordingMessage(player);
                pathRecorder.trackPaths(player);
                // Skip path detection while recording to ensure recording message always shows
                return;
            }

            // Only check for paths if not recording
            checkPathsAndNotify(player);
        }
    }

    /**
     * Handles player quit events to clean up tracking data.
     * Removes the player from notification tracking maps when they log out.
     *
     * @param event The PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clearPlayerData(player.getUniqueId());
    }

    /**
     * Checks if a player is on a path and handles notifications appropriately.
     * Only shows notifications when a player enters a path or has been on it for a while.
     *
     * @param player The player to check
     */
    private void checkPathsAndNotify(Player player) {
        UUID playerUUID = player.getUniqueId();
        String currentPathName = currentPlayerPaths.get(playerUUID);
        boolean foundPath = false;
        String foundPathName = null;

        // Check all paths to see if player is near any of them
        for (Path path : pathRecorder.getPaths().values()) {
            // Skip paths that are currently being recorded
            if (!pathRecorder.getTrackedPaths().containsValue(path.getName())) {
                // Check if player is near any point on this path
                for (Location location : path.getTrackedPath()) {
                    if (player.getLocation().distance(location) <= path.getRadius()) {
                        foundPath = true;
                        foundPathName = path.getName();
                        break;
                    }
                }

                if (foundPath) {
                    break; // Stop checking once we find a path the player is on
                }
            }
        }

        // Handle path entry/exit and notifications
        if (foundPath) {
            // Player is on a path
            if (currentPathName == null || !currentPathName.equals(foundPathName)) {
                // Player entered a new path - show notification immediately
                showPathNotification(player, foundPathName);
                currentPlayerPaths.put(playerUUID, foundPathName);
                lastNotificationTime.put(playerUUID, System.currentTimeMillis());
            } else {
                // Player is still on the same path - check if we should show a reminder
                long lastTime = lastNotificationTime.getOrDefault(playerUUID, 0L);
                long now = System.currentTimeMillis();

                if (now - lastTime >= reminderInterval) {
                    // It's been long enough since the last notification, show a reminder
                    showPathNotification(player, foundPathName);
                    lastNotificationTime.put(playerUUID, now);
                }
            }
        } else {
            // Player is not on any path
            if (currentPathName != null) {
                // Player has left a path they were on
                currentPlayerPaths.remove(playerUUID);
            }
        }
    }

    /**
     * Shows a notification to a player about the path they are on.
     *
     * @param player The player to notify
     * @param pathName The name of the path they are on
     */
    private void showPathNotification(Player player, String pathName) {
        final TextComponent text = Component
                .text(travelingMessage)
                .color(TextColor.color(0xF5C45E))
                .append(Component
                        .text(pathName)
                        .color(TextColor.color(0xE78B48))
                        .decoration(TextDecoration.BOLD, true));
        player.sendActionBar(text);
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

    /**
     * Clears path tracking data for a player.
     * Called when a player logs out or for other cleanup.
     *
     * @param playerUUID The UUID of the player
     */
    public void clearPlayerData(UUID playerUUID) {
        currentPlayerPaths.remove(playerUUID);
        lastNotificationTime.remove(playerUUID);
    }
}