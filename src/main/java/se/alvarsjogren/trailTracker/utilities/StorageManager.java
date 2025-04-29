package se.alvarsjogren.trailTracker.utilities;

import com.google.gson.*;
import org.bukkit.Location;
import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.TrailTracker;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StorageManager {
    private final TrailTracker plugin;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .create();
    private final File pathsFolder;

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

        // Save all current paths
        for (Map.Entry<String, Path> entry : currentPaths.entrySet()) {
            try {
                savePath(entry.getValue());
                existingFiles.remove(sanitizeFileName(entry.getKey())); // Remove based on sanitized name
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save path: " + entry.getKey() + " - Error: " + e.getMessage());
            }
        }

        // Delete or move old path files that are no longer used
        deleteOldPathFiles(existingFiles);
    }

    private void savePath(Path path) {
        String safeFileName = sanitizeFileName(path.getName());
        File pathFile = new File(pathsFolder, safeFileName + ".json");

        // Always ensure version is set before saving
        path.setVersion(1); // Current version is 1

        try (Writer writer = new FileWriter(pathFile, false)) {
            gson.toJson(path, writer);
            plugin.getLogger().info("Saved path: " + path.getName());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save path: " + path.getName() + " to file: " + pathFile.getAbsolutePath() + " - Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

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
                plugin.getLogger().info("Moved old path file to backup: " + target.getPath());
            } else {
                plugin.getLogger().warning("Failed to move old path file to backup: " + leftoverFile.getName());
            }
        }

        if (movedCount > 0) {
            plugin.getLogger().info("Moved " + movedCount + " old path file(s) to 'deleted' folder.");
        } else {
            plugin.getLogger().info("No old path files to move.");
        }
    }


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
                } else {
                    plugin.getLogger().warning("Path file " + file.getName() + " is missing a name! Skipping...");
                }
            } catch (JsonSyntaxException e) {
                plugin.getLogger().warning("Invalid JSON syntax in " + file.getName() + ": " + e.getMessage());
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load path from " + file.getName() + " - Error: " + e.getMessage());
            }
        }

        if (loadedPaths.isEmpty()) {
            plugin.getLogger().info("No valid paths loaded.");
        } else {
            plugin.pathRecorder.setPaths(loadedPaths);
            plugin.getLogger().info("Loaded " + loadedPaths.size() + " paths from folder.");
        }
    }

    private String sanitizeFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}