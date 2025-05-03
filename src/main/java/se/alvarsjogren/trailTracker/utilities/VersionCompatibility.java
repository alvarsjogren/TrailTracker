package se.alvarsjogren.trailTracker.utilities;

import org.bukkit.Bukkit;

/**
 * Utility class to handle version detection for TrailTracker.
 *
 * This class provides methods to detect the server version, ensuring
 * compatibility with Minecraft 1.21+ which is required for this plugin.
 */
public class VersionCompatibility {

    /**
     * The detected major version number (e.g., 17, 18, 19, etc.)
     */
    private static final int MAJOR_VERSION;

    /**
     * The detected minor version number (e.g., 1, 2, etc. in 1.19.1)
     */
    private static final int MINOR_VERSION;

    /**
     * Whether the server is running on version 1.21 or newer
     */
    private static final boolean IS_1_21_OR_NEWER;

    /**
     * Static initializer to parse server version once when class is loaded
     */
    static {
        // Get the server version directly from Bukkit
        String versionString = Bukkit.getBukkitVersion();

        // Example: "1.21.1-R0.1-SNAPSHOT"
        String[] parts = versionString.split("-")[0].split("\\.");

        try {
            // Parse the major and minor versions
            // In "1.21.1", major is 21 and minor is 1
            MAJOR_VERSION = Integer.parseInt(parts[1]);
            MINOR_VERSION = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        } catch (Exception e) {
            // Fallback handling for unusual version strings
            Bukkit.getLogger().warning("Failed to parse server version from: " + versionString);
            throw new RuntimeException("Error parsing Minecraft version", e);
        }

        // Set version flags
        IS_1_21_OR_NEWER = MAJOR_VERSION >= 21;

        // Log the detected version for debugging
        Bukkit.getLogger().info("TrailTracker detected Minecraft version: 1." + MAJOR_VERSION + "." + MINOR_VERSION);
    }

    /**
     * Checks if the server is running on Minecraft 1.21 or newer.
     *
     * @return true if the server is 1.21 or newer
     */
    public static boolean isVersion1_21OrNewer() {
        return IS_1_21_OR_NEWER;
    }

    /**
     * Gets the server's major version number.
     *
     * @return the major version number (e.g., 17, 18, 19)
     */
    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * Gets the server's minor version number.
     *
     * @return the minor version number
     */
    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

    /**
     * Returns a human-readable version string.
     *
     * @return A string representation of the detected version
     */
    public static String getVersionString() {
        return "1." + MAJOR_VERSION + "." + MINOR_VERSION;
    }
}