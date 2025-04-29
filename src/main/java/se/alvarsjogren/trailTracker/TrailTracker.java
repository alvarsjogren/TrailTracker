package se.alvarsjogren.trailTracker;

import org.bukkit.plugin.java.JavaPlugin;
import se.alvarsjogren.trailTracker.commands.TTCommandExecutor;
import se.alvarsjogren.trailTracker.commands.TTTabCompleter;
import se.alvarsjogren.trailTracker.listeners.PlayerWalking;
import se.alvarsjogren.trailTracker.utilities.StorageManager;


/**
 * Main plugin class for TrailTracker.
 * Handles plugin initialization, shutdown, and provides access to core components.
 */
public final class TrailTracker extends JavaPlugin {
    public PathRecorder pathRecorder;
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting up...");

        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize PathRecorder
        pathRecorder = new PathRecorder(this);

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

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Shutting down...");

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