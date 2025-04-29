package se.alvarsjogren.trailTracker.utilities;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

/**
 * Custom JSON adapter for serializing and deserializing Bukkit Location objects.
 *
 * This class enables GSON to properly convert Location objects to/from JSON format
 * for persistent storage. Without this adapter, Location objects cannot be directly
 * serialized due to their complex structure and references to Bukkit objects.
 *
 * The adapter stores:
 * - World name (as a string)
 * - X, Y, Z coordinates (as doubles)
 */
public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    /**
     * Serializes a Location object to a JsonElement.
     * Converts the Location's world name and coordinates into a JSON object.
     *
     * @param loc The Location to serialize
     * @param type The type of the object being serialized
     * @param context The serialization context
     * @return A JsonElement representing the Location
     * @throws JsonIOException If serialization fails
     */
    @Override
    public JsonElement serialize(Location loc, Type type, JsonSerializationContext context) throws JsonIOException {
        JsonObject obj = new JsonObject();
        // Store the world name as a string
        obj.addProperty("world", loc.getWorld().getName());
        // Store the coordinates as doubles
        obj.addProperty("x", loc.getX());
        obj.addProperty("y", loc.getY());
        obj.addProperty("z", loc.getZ());
        return obj;
    }

    /**
     * Deserializes a JsonElement back into a Location object.
     * Reconstructs a Location from stored world name and coordinates.
     *
     * @param json The JsonElement to deserialize
     * @param typeOfT The type of the object to deserialize to
     * @param context The deserialization context
     * @return The deserialized Location object
     * @throws JsonParseException If deserialization fails or world is not found
     */
    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        // Get the world by name
        World world = Bukkit.getWorld(obj.get("world").getAsString());
        if (world == null) {
            throw new JsonParseException("World not found: " + obj.get("world").getAsString());
        }

        // Extract coordinates
        double x = obj.get("x").getAsDouble();
        double y = obj.get("y").getAsDouble();
        double z = obj.get("z").getAsDouble();

        // Create and return a new Location
        return new Location(world, x, y, z);
    }
}