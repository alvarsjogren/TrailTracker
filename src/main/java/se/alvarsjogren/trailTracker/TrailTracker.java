package se.alvarsjogren.trailTracker;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import se.alvarsjogren.trailTracker.api.TrailTrackerAPI;
import se.alvarsjogren.trailTracker.api.TrailTrackerAPIImpl;
import se.alvarsjogren.trailTracker.api.TrailTrackerProvider;
import se.alvarsjogren.trailTracker.commands.TTCommandExecutor;
import se.alvarsjogren.trailTracker.commands.TTTabCompleter;
import se.alvarsjogren.trailTracker.listeners.PlayerHandler;
import se.alvarsjogren.trailTracker.utilities.StorageManager;
import se.alvarsjogren.trailTracker.utilities.VersionCompatibility;


/**
 * Main plugin class for TrailTracker.
 * Handles plugin initialization, shutdown, and provides access to core components.
 */
public final class TrailTracker extends JavaPlugin {
    /** Core component that manages path recording and display */
    public PathRecorder pathRecorder;

    /** Handles persistence of paths to/from disk */
    private StorageManager storageManager;

    /** bStats plugin metrics */
    private Metrics metrics;

    /** API implementation for external plugin access */
    private TrailTrackerAPI api;

    /**
     * Called when the plugin is enabled.
     * Initializes all components and loads saved data.
     */
    @Override
    public void onEnable() {
        getLogger().info("Starting up...");

        // Check if the server version is compatible - we require 1.21+
        if (!isServerVersionCompatible()) {
            getLogger().severe("TrailTracker requires Minecraft 1.21 or newer!");
            getLogger().severe("This server is running: " + VersionCompatibility.getVersionString());
            getLogger().severe("The plugin will now disable itself.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Log the detected server version
        getLogger().info("Detected Minecraft version: " + VersionCompatibility.getVersionString());

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
        getServer().getPluginManager().registerEvents(new PlayerHandler(this), this);

        // Initialize bStats
        setupMetrics();

        // Initialize and register API
        initializeAPI();

        getLogger().info("Started successfully!");
    }

    /**
     * Checks if the server version is supported by this plugin.
     * We require Minecraft 1.21+.
     *
     * @return true if the server version is compatible, false otherwise
     */
    private boolean isServerVersionCompatible() {
        // Require Minecraft 1.21+
        return VersionCompatibility.getMajorVersion() >= 21;
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

        // Add server version info to metrics
        metrics.addCustomChart(new SimplePie("minecraft_version", VersionCompatibility::getVersionString));

        getLogger().info("bStats metrics initialized (Plugin ID: 25685)");
    }

    /**
     * Initializes and registers the API for external plugin access.
     */
    private void initializeAPI() {
        // Create API implementation
        api = new TrailTrackerAPIImpl(this);

        // Register API with provider
        TrailTrackerProvider.registerAPI(api);

        getLogger().info("TrailTracker API initialized and registered");
    }

    /**
     * Called when the plugin is disabled.
     * Saves all data and performs cleanup.
     */
    @Override
    public void onDisable() {
        getLogger().info("Shutting down...");

        // Save all paths to disk
        getLogger().info("Saving data...");
        storageManager.save();
        getLogger().info("Data saved successfully!");

        // Unregister API
        TrailTrackerProvider.unregisterAPI();
        getLogger().info("API unregistered");

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