package se.alvarsjogren.trailTracker;

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

        getLogger().info("Started successfully!");
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