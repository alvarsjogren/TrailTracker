package se.alvarsjogren.trailTracker;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Path {
    private int version;
    private final String name;
    private String description = "";
    private int radius;
    private final ArrayList<Location> trackedPath = new ArrayList<>();

    public Path(String name, int pathRadius) {
        this.name = name;
        this.radius = pathRadius;
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

    public void putLocationToPath(Location location) {
        this.trackedPath.add(location);
    }

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
