package se.alvarsjogren.trailTracker;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import se.alvarsjogren.trailTracker.utilities.ParticleUtilities;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a recorded path in the world.
 * A path consists of a series of locations that can be displayed with particles.
 */
public class Path {
    /**
     * Current version of the path data format, used for backward compatibility
     */
    private int version;

    /**
     * Unique name identifier for this path
     */
    private final String name;

    /**
     * User-friendly description of the path
     */
    private String description = "No description for this path yet";

    /**
     * Detection radius around path points (in blocks)
     */
    private int radius;

    /**
     * When this path was first created
     */
    private Date creationDate = new Date();

    /**
     * Username of player who created the path
     */
    private String createdBy = "Unknown";

    /**
     * Maximum number of points allowed (0 = unlimited)
     */
    private int maxPoints = 0; // 0 means unlimited

    /**
     * Particle that is used when path is displayed
     */
    private Particle displayParticle;

    /**
     * Ordered list of locations that make up the path
     */
    private final ArrayList<Location> trackedPath = new ArrayList<>();

    /**
     * Creates a new path with the specified name and detection radius.
     *
     * @param name The name of the path
     * @param pathRadius The radius around path points where players are detected as "on path"
     * @param particle The particle type used for displaying the path
     */
    public Path(String name, int pathRadius, Particle particle) {
        this.name = name;
        this.radius = pathRadius;
        this.displayParticle = particle;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pathDescription) {
        this.description = pathDescription;
    }

    public ArrayList<Location> getTrackedPath() {
        return trackedPath;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int pathVersion) {
        this.version = pathVersion;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public Particle getDisplayParticle() {
        return displayParticle;
    }

    /**
     * Sets the particle that is used to display a path.
     * Validates the particle type to ensure it's compatible.
     *
     * @param displayParticle The particle that is displayed
     */
    public void setDisplayParticle(Particle displayParticle) {
        if (ParticleUtilities.isProblematicParticle(displayParticle)) {
            this.displayParticle = ParticleUtilities.getDefaultParticle();
        } else {
            this.displayParticle = displayParticle;
        }
    }

    /**
     * Adds a new location to the path, with checks for max points limit.
     * Will only skip identical locations (completely still player).
     *
     * @param location The location to add
     */
    public void putLocationToPath(Location location) {
        if (maxPoints > 0 && trackedPath.size() >= maxPoints) {
            return;
        }
        if (trackedPath.isEmpty()) {
            this.trackedPath.add(location);
            return;
        }

        Location lastLocation = trackedPath.getLast();
        if (lastLocation.equals(location)) {
            return;
        }

        this.trackedPath.add(location);
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