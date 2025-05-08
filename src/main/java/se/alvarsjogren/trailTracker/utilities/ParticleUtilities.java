package se.alvarsjogren.trailTracker.utilities;

import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
     */
    public static boolean isProblematicParticle(Particle particle) {
        return PROBLEMATIC_PARTICLES.contains(particle);
    }

    /**
     * Gets a copy of the set of problematic particles.
     */
    public static ArrayList<Particle> getProblematicParticles() {
        return PROBLEMATIC_PARTICLES;
    }

    /**
     * Gets a safe default particle that is known to work with the simplified spawning method.
     */
    public static Particle getDefaultParticle() {
        return Particle.HAPPY_VILLAGER;
    }
}