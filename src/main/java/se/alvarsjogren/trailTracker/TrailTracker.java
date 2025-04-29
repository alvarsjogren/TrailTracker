package se.alvarsjogren.trailTracker;

import org.bukkit.plugin.java.JavaPlugin;
import se.alvarsjogren.trailTracker.commands.TTCommandExecutor;
import se.alvarsjogren.trailTracker.commands.TTTabCompleter;
import se.alvarsjogren.trailTracker.listeners.PlayerWalking;
import se.alvarsjogren.trailTracker.utilities.StorageManager;


public final class TrailTracker extends JavaPlugin {
    public PathRecorder pathRecorder = new PathRecorder(this);
    private StorageManager storageManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Starting up...");

        // Path names in this plugin are identifiers. Therefore, two paths cant share a name.
        // The player is only able to track one path at a time.
        // Color scheme https://colorhunt.co/palette/102e50f5c45ee78b48be3d2a

        // TODO:
        //  Commands:
        //   Add path description
        //   Save/Load command; Admin only!
        //  Features:
        //   Player customize "Traveling <path>" text
        //   Path specific "path-radius"
        //  Ideas:
        //   Make the path displayed even when not moving??
        //  Bug:

        saveDefaultConfig();

        getLogger().info("Loading data...");
        storageManager = new StorageManager(this);
        storageManager.load();
        getLogger().info("Data loaded successfully!");

        TTCommandExecutor ttCommandExecutor = new TTCommandExecutor(this);
        TTTabCompleter ttTabCompleter = new TTTabCompleter(ttCommandExecutor.getSubCommands(), this);
        getCommand("tt").setExecutor(ttCommandExecutor);
        getCommand("tt").setTabCompleter(ttTabCompleter);
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
}
