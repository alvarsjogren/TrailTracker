package se.alvarsjogren.trailTracker.api;

import se.alvarsjogren.trailTracker.Path;
import se.alvarsjogren.trailTracker.PathRecorder;
import se.alvarsjogren.trailTracker.TrailTracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the TrailTracker API.
 * Provides read-only access to path data.
 */
public class TrailTrackerAPIImpl implements TrailTrackerAPI {
    private final TrailTracker plugin;

    /**
     * Creates a new API implementation instance.
     *
     * @param plugin The main plugin instance
     */
    public TrailTrackerAPIImpl(TrailTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public Map<String, PathInfo> getCompletedPaths() {
        Map<String, PathInfo> completedPaths = new HashMap<>();
        PathRecorder pathRecorder = plugin.pathRecorder;

        if (pathRecorder == null) {
            return Collections.emptyMap();
        }

        // Get all paths and filter out those currently being tracked
        Map<String, Path> allPaths = pathRecorder.getPaths();
        Map<UUID, String> trackedPaths = pathRecorder.getTrackedPaths(); // UUID -> PathName

        for (Map.Entry<String, Path> entry : allPaths.entrySet()) {
            String pathName = entry.getKey();
            Path path = entry.getValue();

            // Check if this path is currently being tracked
            if (!trackedPaths.containsValue(pathName)) {
                // Convert Path to PathInfo for API exposure
                PathInfo pathInfo = new PathInfo(
                        path.getName(),
                        path.getDescription(),
                        path.getRadius(),
                        path.getCreationDate(),
                        path.getCreatedBy(),
                        path.getTrackedPath()
                );
                completedPaths.put(pathName, pathInfo);
            }
        }

        // Return an unmodifiable view
        return Collections.unmodifiableMap(completedPaths);
    }

    @Override
    public boolean isPathCompleted(String pathName) {
        if (pathName == null || plugin.pathRecorder == null) {
            return false;
        }

        Map<String, Path> allPaths = plugin.pathRecorder.getPaths();
        Map<UUID, String> trackedPaths = plugin.pathRecorder.getTrackedPaths();

        // Path must exist and not be currently tracked
        return allPaths.containsKey(pathName) && !trackedPaths.containsValue(pathName);
    }

    @Override
    public PathInfo getCompletedPath(String pathName) {
        if (!isPathCompleted(pathName)) {
            return null;
        }

        Path path = plugin.pathRecorder.getPaths().get(pathName);
        if (path == null) {
            return null;
        }

        return new PathInfo(
                path.getName(),
                path.getDescription(),
                path.getRadius(),
                path.getCreationDate(),
                path.getCreatedBy(),
                path.getTrackedPath()
        );
    }
}