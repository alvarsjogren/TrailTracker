package se.alvarsjogren.trailTracker.api;

/**
 * Provider class for accessing the TrailTracker API.
 * This is the main entry point for other plugins to access the API.
 */
public class TrailTrackerProvider {
    private static TrailTrackerAPI api;

    /**
     * Gets the TrailTracker API instance.
     *
     * @return The TrailTrackerAPI instance
     * @throws IllegalStateException if the API has not been registered yet
     */
    public static TrailTrackerAPI getAPI() {
        if (api == null) {
            throw new IllegalStateException("TrailTracker API has not been registered yet!");
        }
        return api;
    }

    /**
     * Internal method to register the API implementation.
     * Called by the TrailTracker plugin during startup.
     *
     * @param apiImplementation The API implementation to register
     * @throws IllegalStateException if an API is already registered
     */
    public static void registerAPI(TrailTrackerAPI apiImplementation) {
        if (api != null) {
            throw new IllegalStateException("TrailTracker API has already been registered!");
        }
        api = apiImplementation;
    }

    /**
     * Internal method to unregister the API.
     * Called by the TrailTracker plugin during shutdown.
     */
    public static void unregisterAPI() {
        api = null;
    }

    /**
     * Checks if the API is currently available.
     *
     * @return true if the API is registered and available, false otherwise
     */
    public static boolean isAPIAvailable() {
        return api != null;
    }
}