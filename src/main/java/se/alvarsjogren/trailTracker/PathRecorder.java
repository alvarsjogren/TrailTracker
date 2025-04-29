package se.alvarsjogren.trailTracker;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import se.alvarsjogren.trailTracker.utilities.ParticleManager;
import se.alvarsjogren.trailTracker.utilities.VersionCompatibility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records and manages player movement paths in the Minecraft world.
 * Thread-safe implementation for handling concurrent access in a multi-threaded server environment.
 *
 * The PathRecorder acts as the central coordinator for:
 * - Creating, updating, and removing paths
 * - Tracking player movement to record paths
 * - Managing which paths are being displayed to which players
 * - Handling all path-related operations with proper thread safety
 *
 * It uses ConcurrentHashMap and synchronized collections to ensure thread safety
 * in the multi-threaded Bukkit server environment.
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
    private Particle displayParticle;

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

    /** Manager for handling particles across different versions */
    private final ParticleManager particleManager;

    /**
     * Creates a new PathRecorder with the specified plugin instance.
     * Loads configuration values and starts the display task.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public PathRecorder(TrailTracker plugin) {
        this.plugin = plugin;
        this.particleManager = new ParticleManager(plugin);
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
            displayParticle = particleManager.getParticleFromConfig(particleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid default-display-particle in config. Using HAPPY_VILLAGER.");
            displayParticle = Particle.HAPPY_VILLAGER;
        }

        // Load other configuration values
        maxPathNameLength = plugin.getConfig().getInt("max-path-name-length", 32);
        maxPathPoints = plugin.getConfig().getInt("max-path-points", 0); // 0 means unlimited
        particleFrequency = plugin.getConfig().getInt("particle-frequency", 5);
        defaultPathRadius = plugin.getConfig().getInt("default-path-radius", 3);

        // Start task to display paths if not already running
        startDisplayTask();
    }

    /**
     * Starts the task that displays paths to players at the configured frequency.
     * Cancels any existing task first to prevent duplicates.
     */
    private void startDisplayTask() {
        // Cancel existing task if it exists
        if (displayTask != null && !displayTask.isCancelled()) {
            displayTask.cancel();
        }

        // Start new task to display paths for all online players
        displayTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        displayPaths(player);
                    }
                },
                20L, // Initial delay (1 second)
                Math.max(1, particleFrequency) // Make sure frequency is at least 1 tick
        );
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

    // ========== Getters & Setters ==========
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
     * Also injects the ParticleManager into each path for version compatibility.
     *
     * @param loadedPaths The paths to set
     */
    public synchronized void setPaths(Map<String, Path> loadedPaths) {
        paths.clear();
        if (loadedPaths != null) {
            // Add all paths and inject ParticleManager
            loadedPaths.values().forEach(path -> {
                path.setParticleManager(particleManager);
                paths.put(path.getName(), path);
            });
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
    // =======================================

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
        // Check if player is already tracking a path
        if (trackedPaths.containsKey(playerUUID)) {
            return new Result(false, "You are already tracking a path.");
        }

        // Check if path name is already in use
        if (paths.containsKey(pathName)) {
            return new Result(false, "A path with that name already exists.");
        }

        // Check if path name exceeds maximum length
        if (maxPathNameLength > 0 && pathName.length() > maxPathNameLength) {
            return new Result(false, "Path name is too long. Maximum length is " + maxPathNameLength + " characters.");
        }

        // Check for invalid characters in path name
        if (!pathName.matches("[a-zA-Z0-9_\\-\\s]+")) {
            return new Result(false, "Path name contains invalid characters. Use only letters, numbers, spaces, underscores, and hyphens.");
        }

        // Create new path with configuration values
        Path path = new Path(pathName, defaultPathRadius);
        path.setCreatedBy(playerName);
        path.setCreationDate(new Date());
        path.setMaxPoints(maxPathPoints);
        path.setParticleManager(particleManager);

        // Add to tracking maps
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
            return; // Early return if player isn't tracking
        }

        Path path = paths.get(pathName);
        if (path == null) {
            return; // Safety check in case path was removed
        }

        // Add a small throttle to prevent excessive updates
        // This is just to avoid server performance issues, not to filter points
        long now = System.currentTimeMillis();
        Long lastTime = lastTrackedTime.get(playerUUID);

        if (lastTime != null && now - lastTime < 100) { // Reduced from 200ms to 100ms for more granular tracking
            return;
        }

        lastTrackedTime.put(playerUUID, now);

        // Add a small vertical offset to avoid ground-level issues
        Location checkLocation = player.getLocation().clone().add(0, 0.3, 0);

        // Always use toCenterLocation for consistent block centering
        // This ensures particles are always at the center of blocks as in the original design
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
        // Check if path exists
        if (!paths.containsKey(pathName)) {
            return new Result(false, "There is no path with that name. Use /tt list to see all paths.");
        }

        // Check if path is being tracked
        if (trackedPaths.containsValue(pathName)) {
            return new Result(false, "The path is being tracked. Stop tracking before deleting path.");
        }

        // Remove path from all players' displayed paths
        for (UUID displayingPlayerUUID : displayedPaths.keySet()) {
            Set<String> playerPaths = displayedPaths.get(displayingPlayerUUID);
            if (playerPaths != null) {
                synchronized (playerPaths) {
                    playerPaths.remove(pathName);
                }
            }
        }

        // Remove path from main map
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
        // Check if path exists
        if (!paths.containsKey(pathName)) {
            return new Result(false, "There is no path with that name.");
        }

        // Get or create the player's displayed paths set
        Set<String> playerPaths = displayedPaths.computeIfAbsent(playerUUID, k ->
                Collections.synchronizedSet(new HashSet<>()));

        // Check if already displaying
        if (playerPaths.contains(pathName)) {
            return new Result(false, "You are already displaying that path.");
        }

        // Add to displayed paths
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

        // Check if player is displaying any paths
        if (playerPaths == null) {
            return new Result(false, "You are not displaying any paths.");
        }

        // Check if player is displaying this specific path
        if (!playerPaths.contains(pathName)) {
            return new Result(false, "You are not displaying any path with that name.");
        }

        // Remove from displayed paths
        playerPaths.remove(pathName);

        // Clean up if player is not displaying any paths anymore
        if (playerPaths.isEmpty()) {
            displayedPaths.remove(playerUUID);
        }

        return new Result(true, "Success");
    }

    /**
     * Displays all paths that a player has selected to view.
     * Called periodically by the display task and by events.
     * Uses version-compatible particle display methods.
     *
     * @param player The player to display paths for
     */
    public void displayPaths(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<String> playerPaths = displayedPaths.get(playerUUID);

        // Check if player is displaying any paths
        if (playerPaths == null || playerPaths.isEmpty()) {
            return;
        }

        // Copy to avoid concurrent modification while iterating
        for (String pathName : new HashSet<>(playerPaths)) {
            Path path = paths.get(pathName);
            if (path != null) {
                // Display the path to the player
                path.displayPath(player, displayParticle);
            } else {
                // Path has been removed but is still in player's display list
                playerPaths.remove(pathName);
            }
        }
    }

    /**
     * Gets the particle manager for this path recorder.
     * Used by paths to access the particle manager.
     *
     * @return The particle manager instance
     */
    public ParticleManager getParticleManager() {
        return particleManager;
    }
}