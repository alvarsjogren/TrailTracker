# TrailTracker Version Compatibility Guide

This document explains the version compatibility strategy for TrailTracker, including how to handle multiple Minecraft versions with a single codebase and how to maintain backward compatibility.

## Version Support Strategy

TrailTracker uses a multi-pronged approach to maintain compatibility across Minecraft versions:

1. **Version Detection** - Detecting the server version at runtime
2. **Graceful Degradation** - Fallback mechanisms for unsupported features
3. **Core Mechanics Preservation** - Essential functionality works consistently across versions
4. **Robust Error Handling** - Catching and managing version-specific exceptions

## Supported Minecraft Versions

| Minecraft Version | Support Level     | Notes                                            |
|-------------------|-------------------|--------------------------------------------------|
| 1.21.x            | Full Support      | Primary development target                       |
| 1.20.x            | Compatible        | Should work with minimal issues                  |
| 1.19.x            | Compatible        | Core features work with possibly different particles |
| 1.18.x and older  | Basic Support     | Basic functionality, some particle types may not work |

## How Version Compatibility Works

### 1. Version Detection

The `VersionCompatibility` class detects the server version at startup:

```java
// Direct version string detection from Bukkit
String versionString = Bukkit.getBukkitVersion();
```

This provides reliable version information across different server implementations.

### 2. Particle Compatibility

TrailTracker handles particle compatibility through fallbacks:

```java
// If the configured particle isn't available, try these alternatives
for (String fallback : new String[]{"HAPPY_VILLAGER", "VILLAGER_HAPPY", "HEART", "CRIT"}) {
    try {
        return Particle.valueOf(fallback);
    } catch (IllegalArgumentException ignored) {
        // Try the next fallback
    }
}
```

### 3. Block Centering Preservation

Path points are consistently centered in blocks regardless of Minecraft version:

```java
// Always use toCenterLocation for consistent block centering
checkLocation = checkLocation.toCenterLocation();
```

This maintains the core mechanic of having particles appear at block centers.

## API Usage Guidelines

When developing features for TrailTracker, follow these guidelines:

- **Use the Paper API** rather than Bukkit when possible
- **Handle exceptions** for any version-specific features
- **Provide fallbacks** for features not available in older versions
- **Test across versions** before releasing changes

## Testing Multi-Version Support

To ensure compatibility, test your changes against multiple Minecraft versions:

1. Build the plugin with Maven
2. Test on a 1.21.x server (primary target)
3. Test on at least one older version (e.g., 1.19.x)
4. Verify that particles display properly at block centers
5. Confirm that error handling works correctly

## Updating for New Minecraft Versions

When a new Minecraft version is released:

1. Update the Paper API dependency in `pom.xml`
2. Test the plugin with the new version
3. Add any necessary fallbacks or compatibility code
4. Update the version compatibility documentation
5. Release a new version with appropriate semantic versioning

## Dependencies Management

TrailTracker carefully manages dependencies to ensure compatibility:

- **Paper API** - Depends on the target version but includes backward compatibility
- **Google Gson** - Version-independent JSON serialization
- **bStats** - Plugin metrics (compatible across Minecraft versions)

## Conclusion

TrailTracker's compatibility strategy ensures that:

1. Core mechanics (block-centered particles, path tracking) work consistently across versions
2. The plugin degrades gracefully on older versions
3. Users get appropriate feedback about compatibility
4. Particles display properly even if the configured type isn't available

This approach allows server administrators to use TrailTracker across a wide range of Minecraft versions while maintaining a consistent experience.