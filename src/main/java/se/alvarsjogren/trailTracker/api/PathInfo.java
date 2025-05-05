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

    public Date getCreationDate() {
        return new Date(creationDate.getTime()); // Return defensive copy
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<Location> getLocations() {
        return locations; // Already immutable
    }

    public int getPointCount() {
        return locations.size();
    }
}