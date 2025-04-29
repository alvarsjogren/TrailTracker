package se.alvarsjogren.trailTracker.utilities;

import com.google.gson.*;
import org.bukkit.Location;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.TrailTracker;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the storage and retrieval of path data.
 * Handles serialization and deserialization of path objects to/from JSON files.
 */
public class StorageManager {
    private final TrailTracker plugin;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .create();
    private final File pathsFolder;

    /**
     * Creates a new StorageManager.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public StorageManager(TrailTracker plugin) {
        this.plugin = plugin;

        // Get the path folder from the config file (use default "paths" if not set)
        String folderPath = plugin.getConfig().getString("path-folder", "paths");

        // Validate folder path to prevent invalid paths
        if (folderPath.trim().isEmpty()) {
            plugin.getLogger().warning("Invalid path folder specified in config. Using default 'paths'.");
            folderPath = "paths"; // Fallback to default if config is invalid
        }

        this.pathsFolder = new File(plugin.getDataFolder(), folderPath);

        // Create the folder if it doesn't exist
        if (!pathsFolder.exists()) {
            if (pathsFolder.mkdirs()) {
                plugin.getLogger().info("Created path folder: " + pathsFolder.getAbsolutePath());
            } else {
                plugin.getLogger().warning("Failed to create path folder: " + pathsFolder.getAbsolutePath());
            }
        }
    }

    /**
     * Saves all current paths to disk.
     */
    public void save() {
        HashMap<String, Path> currentPaths = (HashMap<String, Path>) plugin.pathRecorder.getPaths();
        HashMap<String, File> existingFiles = new HashMap<>();

        // Map existing path files
        File[] files = pathsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String baseName = file.getName().substring(0, file.getName().length() - 5); // Remove ".json"
                existingFiles.put(baseName, file);
            }
        } else {
            plugin.getLogger().warning("Failed to list files in path folder: " + pathsFolder.getAbsolutePath());
            return; // Exit if files cannot be listed
        }

        int savedCount = 0;

        // Save all current paths
        for (Map.Entry<String, Path> entry : currentPaths.entrySet()) {
            try {
                savePath(entry.getValue());
                existingFiles.remove(sanitizeFileName(entry.getKey())); // Remove based on sanitized name
                savedCount++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save path: " + entry.getKey() + " - Error: " + e.getMessage());
            }
        }

        plugin.getLogger().info("Saved " + savedCount + " paths to disk.");

        // Delete or move old path files that are no longer used
        if (!existingFiles.isEmpty()) {
            deleteOldPathFiles(existingFiles);
        }
    }

    /**
     * Saves a single path to disk.
     *
     * @param path The path to save
     * @throws RuntimeException If saving fails
     */
    private void savePath(Path path) {
        String safeFileName = sanitizeFileName(path.getName());
        File pathFile = new File(pathsFolder, safeFileName + ".json");

        // Always ensure version is set before saving
        path.setVersion(1); // Current version is 1

        try (Writer writer = new FileWriter(pathFile, false)) {
            gson.toJson(path, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save path: " + path.getName() + " to file: " + pathFile.getAbsolutePath() + " - Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Moves old path files to a backup folder.
     *
     * @param leftoverFiles Map of files to move
     */
    private void deleteOldPathFiles(Map<String, File> leftoverFiles) {
        int movedCount = 0;

        // Create the "deleted" folder if it doesn't exist
        File deletedFolder = new File(pathsFolder, "deleted");
        if (!deletedFolder.exists()) {
            if (deletedFolder.mkdirs()) {
                plugin.getLogger().info("Created 'deleted' folder for backup.");
            } else {
                plugin.getLogger().warning("Failed to create 'deleted' folder for backup.");
            }
        }

        for (File leftoverFile : leftoverFiles.values()) {
            // Move the file to the "deleted" folder
            File target = new File(deletedFolder, leftoverFile.getName());
            if (leftoverFile.renameTo(target)) {
                movedCount++;
            } else {
                plugin.getLogger().warning("Failed to move old path file to backup: " + leftoverFile.getName());
            }
        }

        if (movedCount > 0) {
            plugin.getLogger().info("Moved " + movedCount + " old path file(s) to 'deleted' folder.");
        }
    }

    /**
     * Loads all paths from disk.
     */
    public void load() {
        if (!pathsFolder.exists()) {
            plugin.getLogger().warning("Path folder does not exist: " + pathsFolder.getAbsolutePath());
            return; // No folder = nothing to load
        }

        File[] files = pathsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            plugin.getLogger().warning("Failed to list files in path folder: " + pathsFolder.getAbsolutePath());
            return; // Exit if files cannot be listed
        }

        HashMap<String, Path> loadedPaths = new HashMap<>();
        int successCount = 0;
        int errorCount = 0;

        for (File file : files) {
            try (Reader reader = new FileReader(file)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                int fileVersion = json.has("version") ? json.get("version").getAsInt() : 1; // Default to version 1

                if (fileVersion > 1) {
                    plugin.getLogger().warning("Path file " + file.getName() + " uses a newer version (" + fileVersion + ") than supported!");
                    // Handle version migration here if necessary
                }

                // Deserialize the Path
                Path path = gson.fromJson(json, Path.class);

                if (path != null && path.getName() != null) {
                    loadedPaths.put(path.getName(), path);
                    successCount++;
                } else {
                    plugin.getLogger().warning("Path file " + file.getName() + " is missing a name! Skipping...");
                    errorCount++;
                }
            } catch (JsonSyntaxException e) {
                plugin.getLogger().warning("Invalid JSON syntax in " + file.getName() + ": " + e.getMessage());
                errorCount++;
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load path from " + file.getName() + " - Error: " + e.getMessage());
                errorCount++;
            }
        }

        if (loadedPaths.isEmpty()) {
            plugin.getLogger().info("No valid paths loaded.");
        } else {
            plugin.pathRecorder.setPaths(loadedPaths);
            plugin.getLogger().info("Loaded " + successCount + " paths from folder. Errors: " + errorCount);
        }
    }

    /**
     * Sanitizes a filename to ensure it's safe for file system operations.
     *
     * @param input The input string
     * @return A sanitized filename
     */
    private String sanitizeFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}