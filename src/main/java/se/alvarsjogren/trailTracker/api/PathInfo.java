package se.alvarsjogren.trailTracker.api;

import org.bukkit.Location;
import java.util.Date;
import java.util.List;

/**
 * Read-only information about a path.
 * This class is immutable to maintain API integrity.
 */
public class PathInfo {
    private final String name;
    private final String description;
    private final int radius;
    private final Date creationDate;
    private final String createdBy;
    private final List<Location> locations;

    /**
     * Creates a new PathInfo with all necessary data.
     *
     * @param name The name of the path
     * @param description The description of the path
     * @param radius The detection radius for the path
     * @param creationDate When the path was created
     * @param createdBy Who created the path
     * @param locations The list of locations that make up the path
     */
    public PathInfo(String name, String description, int radius, Date creationDate,
                    String createdBy, List<Location> locations) {
        this.name = name;
        this.description = description;
        this.radius = radius;
        this.creationDate = new Date(creationDate.getTime()); // Defensive copy
        this.createdBy = createdBy;
        // Create an immutable copy of locations
        this.locations = List.copyOf(locations);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getRadius() {
        return radius;
    }

    /**
     * Gets a copy of the creation date to prevent modification.
     *
     * @return A copy of the path's creation date
     */
    public Date getCreationDate() {
        return new Date(creationDate.getTime()); // Return defensive copy
    }

    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Gets the locations that make up this path.
     * The returned list is immutable.
     *
     * @return An immutable list of locations
     */
    public List<Location> getLocations() {
        return locations; // Already immutable
    }

    /**
     * Gets the number of points in this path.
     *
     * @return The number of location points in the path
     */
    public int getPointCount() {
        return locations.size();
    }
}