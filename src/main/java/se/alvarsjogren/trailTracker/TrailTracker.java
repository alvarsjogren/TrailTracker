package se.alvarsjogren.trailTracker;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import se.alvarsjogren.trailTracker.commands.TTCommandExecutor;
import se.alvarsjogren.trailTracker.commands.TTTabCompleter;
import se.alvarsjogren.trailTracker.listeners.PlayerWalking;
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

    /**
     * Called when the plugin is enabled.
     * Initializes all components and loads saved data.
     */
    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting up...");

        // Log the detected server version
        getLogger().info("Detected Minecraft version: " + VersionCompatibility.getVersionString());

        // Check if the server version is compatible - we support 1.19+
        if (!isServerVersionCompatible()) {
            getLogger().warning("Running on an older version: " + VersionCompatibility.getVersionString());
            getLogger().warning("TrailTracker is optimized for 1.19 or newer.");
            getLogger().warning("The plugin should still work, but please report any issues.");
            // We'll continue loading the plugin and just warn instead of throwing an error
        }

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
     * Checks if the server version is supported by this plugin.
     * We support Minecraft 1.19 and newer, with best support for 1.21+
     *
     * @return true if the server version is compatible, false otherwise
     */
    private boolean isServerVersionCompatible() {
        // Support Minecraft 1.19+
        return VersionCompatibility.getMajorVersion() >= 19;
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