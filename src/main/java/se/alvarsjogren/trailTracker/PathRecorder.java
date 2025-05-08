package se.alvarsjogren.trailTracker;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import se.alvarsjogren.trailTracker.utilities.ParticleUtilities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records and manages player movement paths in the Minecraft world.
 * Thread-safe implementation for handling concurrent access in a multithreaded server environment.
 *
 * The PathRecorder acts as the central coordinator for:
 * - Creating, updating, and removing paths
 * - Tracking player movement to record paths
 * - Managing which paths are being displayed to which players
 * - Handling all path-related operations with proper thread safety
 *
 * It uses ConcurrentHashMap and synchronized collections to ensure thread safety
 * in the multithreaded Bukkit server environment.
 */
public class PathRecorder {
    /** Master collection of all paths by name */
    private final Map<String, Path> paths = new ConcurrentHashMap<>();

    /** Maps player UUIDs to the path names they are currently tracking */
    private final Map<UUID, String> trackedPaths = new ConcurrentHashMap<>();

    /** Maps player UUIDs to the set of path names they are displaying */
    private final Map<UUID, Set<String>> displayedPaths = new ConcurrentHashMap<>();

    /** Tracks the last time each player's position was recorded to prevent excessive updates */
    private final Map<UUID, Long> lastTrackedTime = new ConcurrentHashMap<>();

    /** Reference to the main plugin instance */
    private final TrailTracker plugin;

    /** The particle type used for displaying paths */
    private Particle defaultDisplayParticle;

    /** Maximum allowed length for path names */
    private int maxPathNameLength;

    /** Maximum number of points allowed in a path (0 = unlimited) */
    private int maxPathPoints;

    /** Reference to the scheduled task that displays particles */
    private BukkitTask displayTask;

    /** How often to display particles (in ticks) */
    private int particleFrequency;

    /** Default radius around path points where players are detected */
    private int defaultPathRadius;

    /**
     * Creates a new PathRecorder with the specified plugin instance.
     * Loads configuration values and starts the display task.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public PathRecorder(TrailTracker plugin) {
        this.plugin = plugin;
        loadConfigValues();
    }

    /**
     * Loads configuration values from the plugin configuration.
     * Called during initialization and when the plugin is reloaded.
     */
    public void loadConfigValues() {
        try {
            // Load particle type from config or use default
            String particleName = plugin.getConfig().getString("default-display-particle", "HAPPY_VILLAGER");
            defaultDisplayParticle = getParticleFromConfig(particleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid default-display-particle in config. Using HAPPY_VILLAGER.");
            defaultDisplayParticle = Particle.HAPPY_VILLAGER;
        }

        // Load other configuration values
        maxPathNameLength = plugin.getConfig().getInt("max-path-name-length", 32);
        maxPathPoints = plugin.getConfig().getInt("max-path-points", 0); // 0 means unlimited
        particleFrequency = plugin.getConfig().getInt("particle-frequency", 5);
        defaultPathRadius = plugin.getConfig().getInt("default-path-radius", 3);

        startDisplayTask();
    }

    /**
     * Safely get a particle type from config, falling back to a default if the
     * specified particle doesn't exist in this Minecraft version.
     *
     * @param configName The name of the particle from config
     * @return A valid Particle for this server version
     */
    private Particle getParticleFromConfig(String configName) {
        try {
            Particle configParticle = Particle.valueOf(configName);

            // Check if the configured particle is problematic
            if (ParticleUtilities.isProblematicParticle(configParticle)) {
                plugin.getLogger().warning("Particle type '" + configName + "' requires additional data and cannot be used. Using a fallback particle.");
                return ParticleUtilities.getDefaultParticle();
            }

            return configParticle;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Particle type '" + configName + "' not found in this Minecraft version.");

            // Try multiple fallbacks
            for (String fallback : new String[]{"HAPPY_VILLAGER", "VILLAGER_HAPPY", "HEART", "CRIT"}) {
                try {
                    Particle fallbackParticle = Particle.valueOf(fallback);

                    // Ensure the fallback is not problematic
                    if (!ParticleUtilities.isProblematicParticle(fallbackParticle)) {
                        return fallbackParticle;
                    }
                } catch (IllegalArgumentException ignored) {
                    // Try the next fallback
                }
            }

            // Find the first available non-problematic particle
            for (Particle particle : Particle.values()) {
                if (!ParticleUtilities.isProblematicParticle(particle)) {
                    return particle;
                }
            }

            // Last resort fallback
            return Particle.values()[0];
        }
    }

    /**
     * Starts the task that displays paths to players at the configured frequency.
     * Cancels any existing task first to prevent duplicates.
     */
    private void startDisplayTask() {
        // Cancel any existing display task to prevent duplicates
        if (displayTask != null && !displayTask.isCancelled()) {
            displayTask.cancel();
        }

        // Start new task to display paths for all online players
        displayTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        // Display paths that the player has chosen to display
                        displayVisiblePaths(player);

                        // If the player is recording a path, also display that path
                        displayActivelyRecordedPath(player);
                    }
                },
                20L, // Initial delay (1 second)
                Math.max(1, particleFrequency) // Make sure frequency is at least 1 tick
        );

        plugin.getLogger().info("Started path display task with frequency: " + particleFrequency + " ticks");
    }

    /**
     * Result class for returning operation status and messages.
     * Used to provide consistent feedback from operations to commands.
     */
    public static class Result {
        /** Whether the operation was successful */
        public final boolean flag;

        /** Message describing the result of the operation */
        public final String message;

        /**
         * Creates a new Result with the specified status and message.
         *
         * @param flag True if the operation succeeded, false otherwise
         * @param message Description of the result
         */
        public Result(boolean flag, String message) {
            this.flag = flag;
            this.message = message;
        }
    }

    /**
     * Gets a copy of the paths map to prevent concurrent modification issues.
     * @return A copy of the paths map
     */
    public Map<String, Path> getPaths() {
        return new HashMap<>(paths);
    }

    /**
     * Sets the paths map with values loaded from storage.
     * Clears the existing paths and adds all the loaded ones.
     *
     * @param loadedPaths The paths to set
     */
    public synchronized void setPaths(Map<String, Path> loadedPaths) {
        paths.clear();
        if (loadedPaths != null) {
            paths.putAll(loadedPaths);
        }
    }

    /**
     * Gets a copy of the tracked paths map.
     * @return A copy of the tracked paths map
     */
    public Map<UUID, String> getTrackedPaths() {
        return new HashMap<>(trackedPaths);
    }

    /**
     * Gets a copy of the displayed paths map.
     * Creates a deep copy to prevent concurrent modification issues.
     *
     * @return A copy of the displayed paths map
     */
    public Map<UUID, Set<String>> getDisplayedPaths() {
        Map<UUID, Set<String>> copy = new HashMap<>();
        for (Map.Entry<UUID, Set<String>> entry : displayedPaths.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Checks if a player is currently tracking a path.
     *
     * @param playerUUID The UUID of the player to check
     * @return True if the player is tracking a path, false otherwise
     */
    public boolean isPlayerTracking(UUID playerUUID) {
        return trackedPaths.containsKey(playerUUID);
    }

    /**
     * Starts tracking a new path for a player.
     * Creates a new path with the specified name and adds the player as its tracker.
     *
     * @param playerUUID The UUID of the player
     * @param playerName The name of the player (for attribution)
     * @param pathName The name of the path to track
     * @return Result of the operation
     */
    public synchronized Result startTrackingPath(UUID playerUUID, String playerName, String pathName) {
        if (trackedPaths.containsKey(playerUUID)) {
            return new Result(false, "You are already tracking a path.");
        }

        if (paths.containsKey(pathName)) {
            return new Result(false, "A path with that name already exists.");
        }

        if (maxPathNameLength > 0 && pathName.length() > maxPathNameLength) {
            return new Result(false, "Path name is too long. Maximum length is " + maxPathNameLength + " characters.");
        }

        // Check for invalid characters in path name
        if (!pathName.matches("[a-zA-Z0-9_\\-\\s]+")) {
            return new Result(false, "Path name contains invalid characters. Use only letters, numbers, spaces, underscores, and hyphens.");
        }

        Path path = new Path(pathName, defaultPathRadius, defaultDisplayParticle);
        path.setCreatedBy(playerName);
        path.setCreationDate(new Date());
        path.setMaxPoints(maxPathPoints);

        trackedPaths.put(playerUUID, pathName);
        paths.put(pathName, path);
        lastTrackedTime.put(playerUUID, System.currentTimeMillis());

        return new Result(true, "Success");
    }

    /**
     * Stops tracking a path for a player.
     * Removes the player from the tracking map but keeps the path.
     *
     * @param playerUUID The UUID of the player
     * @return Result of the operation
     */
    public synchronized Result stopTrackingPath(UUID playerUUID) {
        if (!trackedPaths.containsKey(playerUUID)) {
            return new Result(false, "You are not tracking any paths.");
        }

        trackedPaths.remove(playerUUID);
        lastTrackedTime.remove(playerUUID);

        return new Result(true, "Success");
    }

    /**
     * Tracks the player's movement and adds it to their current path.
     * Records all movement, only skipping when player is completely still.
     * Uses a small throttle to prevent excessive updates for performance.
     *
     * @param player The player to track
     */
    public void trackPaths(Player player) {
        UUID playerUUID = player.getUniqueId();
        String pathName = trackedPaths.get(playerUUID);

        if (pathName == null) {
            return;
        }

        Path path = paths.get(pathName);
        if (path == null) {
            return;
        }

        // Add a small throttle to prevent excessive updates
        // This is just to avoid server performance issues, not to filter points
        long now = System.currentTimeMillis();
        Long lastTime = lastTrackedTime.get(playerUUID);

        if (lastTime != null && now - lastTime < 50) { // Reduced from 100ms to 50ms for 1.1.0.beta.2.1
            return;
        }

        lastTrackedTime.put(playerUUID, now);
        Location checkLocation = player.getLocation().clone().add(0, 0.3, 0);
        checkLocation = checkLocation.toCenterLocation();

        // Thread-safe check and add
        synchronized (path) {
            path.putLocationToPath(checkLocation);
        }
    }

    /**
     * Removes a path from the system.
     * Also removes the path from all players' displayed paths.
     *
     * @param pathName The name of the path to remove
     * @return Result of the operation
     */
    public synchronized Result removePath(String pathName) {
        if (!paths.containsKey(pathName)) {
            return new Result(false, "There is no path with that name. Use /tt list to see all paths.");
        }

        if (trackedPaths.containsValue(pathName)) {
            return new Result(false, "The path is being tracked. Stop tracking before deleting path.");
        }

        for (UUID displayingPlayerUUID : displayedPaths.keySet()) {
            Set<String> playerPaths = displayedPaths.get(displayingPlayerUUID);
            if (playerPaths != null) {
                synchronized (playerPaths) {
                    playerPaths.remove(pathName);
                }
            }
        }

        paths.remove(pathName);
        return new Result(true, "Success");
    }

    /**
     * Starts displaying a path for a player.
     * Adds the path to the player's set of displayed paths.
     *
     * @param playerUUID The UUID of the player
     * @param pathName The name of the path to display
     * @return Result of the operation
     */
    public synchronized Result startDisplayingPath(UUID playerUUID, String pathName) {
        if (!paths.containsKey(pathName)) {
            return new Result(false, "There is no path with that name.");
        }

        Set<String> playerPaths = displayedPaths.computeIfAbsent(playerUUID, k ->
                Collections.synchronizedSet(new HashSet<>()));

        if (playerPaths.contains(pathName)) {
            return new Result(false, "You are already displaying that path.");
        }

        playerPaths.add(pathName);
        return new Result(true, "Success");
    }

    /**
     * Stops displaying a path for a player.
     * Removes the path from the player's set of displayed paths.
     *
     * @param playerUUID The UUID of the player
     * @param pathName The name of the path to stop displaying
     * @return Result of the operation
     */
    public synchronized Result stopDisplayingPath(UUID playerUUID, String pathName) {
        Set<String> playerPaths = displayedPaths.get(playerUUID);

        if (playerPaths == null) {
            return new Result(false, "You are not displaying any paths.");
        }
        if (!playerPaths.contains(pathName)) {
            return new Result(false, "You are not displaying any path with that name.");
        }

        playerPaths.remove(pathName);

        if (playerPaths.isEmpty()) {
            displayedPaths.remove(playerUUID);
        }

        return new Result(true, "Success");
    }

    /**
     * Displays all paths that a player has selected to view.
     * Called periodically by the display task.
     *
     * @param player The player to display paths for
     */
    private void displayVisiblePaths(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<String> playerPaths = displayedPaths.get(playerUUID);

        if (playerPaths == null || playerPaths.isEmpty()) {
            return;
        }

        for (String pathName : new HashSet<>(playerPaths)) {
            Path path = paths.get(pathName);
            if (path != null) {
                path.displayPath(player, path.getDisplayParticle());
            } else {
                playerPaths.remove(pathName);
            }
        }
    }

    /**
     * Displays the path that a player is actively recording.
     * Called periodically by the display task.
     *
     * @param player The player to check and display for
     */
    private void displayActivelyRecordedPath(Player player) {
        UUID playerUUID = player.getUniqueId();
        String recordingPathName = trackedPaths.get(playerUUID);

        if (recordingPathName != null) {
            Path recordingPath = paths.get(recordingPathName);
            if (recordingPath != null) {
                recordingPath.displayPath(player, recordingPath.getDisplayParticle());
            }
        }
    }

    /**
     * Displays all paths that a player has selected to view.
     * This method is kept for backward compatibility but delegates to the new implementation.
     *
     * @param player The player to display paths for
     */
    public void displayPaths(Player player) {
        displayVisiblePaths(player);
    }
}