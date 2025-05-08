package se.alvarsjogren.trailTracker.utilities;

import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utility class for managing particle compatibility in TrailTracker.
 */
public class ParticleUtilities {

    /**
     * Set of particles that require additional data parameters and cannot be used
     * with the simplified particle spawning method.
     */
    private static final ArrayList<Particle> PROBLEMATIC_PARTICLES = new ArrayList<>(Arrays.asList(
            Particle.BLOCK,
            Particle.BLOCK_MARKER,
            Particle.DUST_PILLAR,
            Particle.ELDER_GUARDIAN,
            Particle.ITEM,
            Particle.ITEM_COBWEB,
            Particle.ITEM_SLIME,
            Particle.ITEM_SNOWBALL
    ));

    /**
     * Checks if a particle is problematic and cannot be used with the simplified spawning method.
     *
     * @param particle The particle to check
     * @return true if the particle requires additional data and can't be used, false otherwise
     */
    public static boolean isProblematicParticle(Particle particle) {
        return PROBLEMATIC_PARTICLES.contains(particle);
    }

    /**
     * Gets a copy of the set of problematic particles.
     *
     * @return A list of particles that require additional data parameters
     */
    public static ArrayList<Particle> getProblematicParticles() {
        return PROBLEMATIC_PARTICLES;
    }

    /**
     * Gets a safe default particle that is known to work with the simplified spawning method.
     *
     * @return A particle that's safe to use without additional parameters
     */
    public static Particle getDefaultParticle() {
        return Particle.HAPPY_VILLAGER;
    }
}