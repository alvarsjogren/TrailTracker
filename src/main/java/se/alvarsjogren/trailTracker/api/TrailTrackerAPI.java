package se.alvarsjogren.trailTracker.api;

import java.util.Map;

/**
 * Public API interface for TrailTracker plugin.
 * Provides read-only access to paths data.
 */
public interface TrailTrackerAPI {

    /**
     * Gets a read-only view of all completed paths in the system.
     * Completed paths are those that are not currently being recorded.
     *
     * @return An unmodifiable map of path names to PathInfo objects
     */
    Map<String, PathInfo> getCompletedPaths();

    /**
     * Checks if a specific path exists and is completed.
     *
     * @param pathName The name of the path to check
     * @return true if the path exists and is completed, false otherwise
     */
    boolean isPathCompleted(String pathName);

    /**
     * Gets information about a specific completed path.
     *
     * @param pathName The name of the path
     * @return PathInfo object if the path exists and is completed, null otherwise
     */
    PathInfo getCompletedPath(String pathName);
}