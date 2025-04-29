package se.alvarsjogren.trailTracker;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import se.alvarsjogren.trailTracker.commands.TTCommandExecutor;
import se.alvarsjogren.trailTracker.commands.TTTabCompleter;
import se.alvarsjogren.trailTracker.listeners.PlayerWalking;
import se.alvarsjogren.trailTracker.utilities.StorageManager;


/**
 * Main plugin class for TrailTracker.
 * Handles plugin initialization, shutdown, and provides access to core components.
 *
 * TrailTracker is a Minecraft plugin that allows players to:
 * - Record their movement paths in the world
 * - Save and load these paths
 * - Visualize paths using particles
 * - Share paths with other players
 *
 * This class coordinates the major components:
 * - PathRecorder for managing path creation/tracking
 * - StorageManager for persistent data storage
 * - Command handlers for user interaction
 * - Event listeners for tracking player movement
 */
public final class TrailTracker extends JavaPlugin {
    /** Core component that manages path recording and display */
    public PathRecorder pathRecorder;

    /** Handles persistence of paths to/from disk */
    private StorageManager storageManager;

    /** bStats plugin metrics */
    private Metrics metrics;

    /**
     * Called when the plugin is enabled.
     * Initializes all components and loads saved data.
     */
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting up...");

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize PathRecorder
        pathRecorder = new PathRecorder(this);

        // Load saved paths from disk
        getLogger().info("Loading data...");
        storageManager = new StorageManager(this);
        storageManager.load();
        getLogger().info("Data loaded successfully!");

        // Register commands
        TTCommandExecutor ttCommandExecutor = new TTCommandExecutor(this);
        TTTabCompleter ttTabCompleter = new TTTabCompleter(ttCommandExecutor.getSubCommands(), this);
        getCommand("tt").setExecutor(ttCommandExecutor);
        getCommand("tt").setTabCompleter(ttTabCompleter);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerWalking(this), this);

        // Initialize bStats
        setupMetrics();

        getLogger().info("Started successfully!");
    }

    /**
     * Sets up bStats metrics for tracking plugin usage.
     * Includes custom charts to track path usage statistics.
     * Respects the enable-metrics setting in config.yml.
     */
    private void setupMetrics() {
        // Check if metrics are enabled in config
        if (!getConfig().getBoolean("enable-metrics", true)) {
            getLogger().info("bStats metrics are disabled in config.yml");
            return;
        }

        // Create bStats metrics instance with plugin ID 25685
        metrics = new Metrics(this, 25685);

        // Add custom chart: Total number of paths
        metrics.addCustomChart(new SingleLineChart("total_paths", () ->
                pathRecorder.getPaths().size()
        ));

        // Add custom chart: Are paths being displayed?
        metrics.addCustomChart(new SimplePie("paths_displayed", () ->
                pathRecorder.getDisplayedPaths().isEmpty() ? "No" : "Yes"
        ));

        // Add custom chart: Particle type being used
        metrics.addCustomChart(new SimplePie("particle_type", () ->
                getConfig().getString("default-display-particle", "HAPPY_VILLAGER")
        ));

        // Add custom chart: Number of active path recorders
        metrics.addCustomChart(new SingleLineChart("active_recorders", () ->
                pathRecorder.getTrackedPaths().size()
        ));

        getLogger().info("bStats metrics initialized (Plugin ID: 25685)");
    }

    /**
     * Called when the plugin is disabled.
     * Saves all data and performs cleanup.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Shutting down...");

        // Save all paths to disk
        getLogger().info("Saving data...");
        storageManager.save();
        getLogger().info("Data saved successfully!");

        getLogger().info("Shutdown successfully!");
    }

    /**
     * Reloads the plugin configuration.
     * Can be called by admin commands or other plugins.
     */
    public void reloadPlugin() {
        // Reload config
        reloadConfig();

        // Reload PathRecorder settings
        if (pathRecorder != null) {
            pathRecorder.loadConfigValues();
        }

        getLogger().info("Configuration reloaded!");
    }
}