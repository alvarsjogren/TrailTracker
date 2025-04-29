package se.alvarsjogren.trailTracker.utilities;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages particle effects with version-specific implementations.
 *
 * This utility class handles differences in particle API between Minecraft versions,
 * providing a consistent interface for displaying particles regardless of server version.
 */
public class ParticleManager {

    private final JavaPlugin plugin;

    /**
     * Creates a new ParticleManager with the specified plugin instance.
     *
     * @param plugin The TrailTracker plugin instance
     */
    public ParticleManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Displays a particle at the specified location for a player.
     * Uses a consistent implementation to match the original plugin behavior.
     *
     * @param player The player who should see the particle
     * @param location The location where the particle should appear
     * @param particle The type of particle to display
     */
    public void displayParticle(Player player, Location location, Particle particle) {
        try {
            // Use the same implementation regardless of version
            // This preserves the original plugin's particle behavior
            displayParticleImpl(player, location, particle);
        } catch (Exception e) {
            // If there's any error with the particle (like an incompatible type),
            // try to use a fallback particle instead
            plugin.getLogger().warning("Error displaying particle: " + e.getMessage());
            try {
                player.spawnParticle(Particle.HAPPY_VILLAGER, location, 1);
            } catch (Exception ignored) {
                // If even the fallback fails, silently ignore
            }
        }
    }

    /**
     * Displays multiple particles along a path for a player.
     *
     * @param player The player who should see the particles
     * @param locations List of locations where particles should appear
     * @param particle The type of particle to display
     */
    public void displayPathParticles(Player player, Iterable<Location> locations, Particle particle) {
        for (Location location : locations) {
            displayParticle(player, location, particle);
        }
    }

    /**
     * Spawns particles at the specified location.
     * Uses the same implementation across all versions to maintain consistency with the original design.
     *
     * @param player The player who should see the particle
     * @param location The location where the particle should appear
     * @param particle The type of particle to display
     */
    private void displayParticleImpl(Player player, Location location, Particle particle) {
        // Use the simple implementation from the original code
        // This ensures consistency with how particles were displayed in the original plugin
        player.spawnParticle(
                particle,
                location,
                1
        );
    }

    /**
     * Safely get a particle type from config, falling back to a default if the
     * specified particle doesn't exist in this Minecraft version.
     *
     * @param configName The name of the particle from config
     * @return A valid Particle for this server version
     */
    public Particle getParticleFromConfig(String configName) {
        try {
            return Particle.valueOf(configName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Particle type '" + configName + "' not found in this Minecraft version.");

            // Try multiple fallbacks in case some particles don't exist in older versions
            for (String fallback : new String[]{"HAPPY_VILLAGER", "VILLAGER_HAPPY", "HEART", "CRIT"}) {
                try {
                    return Particle.valueOf(fallback);
                } catch (IllegalArgumentException ignored) {
                    // Try the next fallback
                }
            }

            // If all fallbacks fail, return the first available particle
            return Particle.values()[0];
        }
    }
}