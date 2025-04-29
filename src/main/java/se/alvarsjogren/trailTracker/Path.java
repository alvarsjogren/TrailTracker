package se.alvarsjogren.trailTracker;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a recorded path in the world.
 * A path consists of a series of locations that can be displayed with particles.
 *
 * The Path class stores all the information about a path, including:
 * - The list of locations that make up the path
 * - Metadata such as name, description, and creator
 * - Configuration for how the path should be displayed
 *
 * Paths are serialized to/from JSON for persistent storage.
 */
public class Path {
    /** Current version of the path data format, used for backward compatibility */
    private int version;

    /** Unique name identifier for this path */
    private final String name;

    /** User-friendly description of the path */
    private String description = "No description for this path yet";

    /** Detection radius around path points (in blocks) */
    private int radius;

    /** When this path was first created */
    private Date creationDate = new Date();

    /** Username of player who created the path */
    private String createdBy = "Unknown";

    /** Maximum number of points allowed (0 = unlimited) */
    private int maxPoints = 0; // 0 means unlimited

    /** Ordered list of locations that make up the path */
    private final ArrayList<Location> trackedPath = new ArrayList<>();

    /**
     * Creates a new path with the specified name and detection radius.
     *
     * @param name The name of the path
     * @param pathRadius The radius around path points where players are detected as "on path"
     */
    public Path(String name, int pathRadius) {
        this.name = name;
        this.radius = pathRadius;
    }

    /**
     * Gets the name of the path.
     *
     * @return The path name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the path.
     *
     * @return The path description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the path.
     *
     * @param pathDescription The new description
     */
    public void setDescription(String pathDescription) {
        this.description = pathDescription;
    }

    /**
     * Gets the list of locations that make up this path.
     *
     * @return An ArrayList of locations
     */
    public ArrayList<Location> getTrackedPath() {
        return trackedPath;
    }

    /**
     * Gets the version of this path's data format.
     *
     * @return The path version
     */
    public int getVersion() {
        return this.version;
    }

    /**
     * Sets the version of this path's data format.
     *
     * @param pathVersion The version number
     */
    public void setVersion(int pathVersion) {
        this.version = pathVersion;
    }

    /**
     * Gets the detection radius around path points.
     *
     * @return The radius in blocks
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Sets the detection radius around path points.
     *
     * @param radius The radius in blocks
     */
    public void setRadius(int radius) {
        this.radius = radius;
    }

    /**
     * Gets the creator of this path.
     *
     * @return The name of the creator
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the creator of this path.
     *
     * @param createdBy The name of the creator
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Gets the creation date of this path.
     *
     * @return The creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date of this path.
     *
     * @param creationDate The creation date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the maximum number of points allowed for this path.
     *
     * @return The maximum points (0 = unlimited)
     */
    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * Sets the maximum number of points allowed for this path.
     *
     * @param maxPoints The maximum points (0 = unlimited)
     */
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    /**
     * Adds a new location to the path, with checks for max points limit.
     * Will only skip identical locations (completely still player).
     *
     * @param location The location to add
     * @return true if the location was added, false if not added
     */
    public boolean putLocationToPath(Location location) {
        // Check if we've reached the maximum number of points
        if (maxPoints > 0 && trackedPath.size() >= maxPoints) {
            return false;
        }

        // If the path is empty, just add the location
        if (trackedPath.isEmpty()) {
            this.trackedPath.add(location);
            return true;
        }

        // Get the last location in the path
        Location lastLocation = trackedPath.getLast();

        // Only skip if the location is exactly the same (player is completely still)
        // This compares x, y, z and world - we want to capture even tiny movements
        if (lastLocation.equals(location)) {
            return false;
        }

        // Add the location to the path
        this.trackedPath.add(location);
        return true;
    }

    /**
     * Displays the path to a player using particles.
     * Spawns a single particle at each location along the path.
     *
     * @param player The player to display the path to
     * @param displayParticle The particle type to use
     */
    public void displayPath(Player player, Particle displayParticle) {
        for (Location location : trackedPath) {
            player.spawnParticle(
                    displayParticle,
                    location,
                    1
            );
        }
    }
}