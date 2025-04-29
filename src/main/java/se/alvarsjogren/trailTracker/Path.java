package se.alvarsjogren.trailTracker;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Path {
    private int pathVersion;
    private final String name;
    private String description = "";
    private final ArrayList<Location> trackedPath = new ArrayList<>();


    public Path(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Location> getTrackedPath() {
        return trackedPath;
    }

    public void setVersion(int version) {
        this.pathVersion = version;
    }

    public int getVersion() {
        return this.pathVersion;
    }

    public void addLocationToPath(Location location) {
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
