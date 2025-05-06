# TrailTracker

A Minecraft Paper plugin for creating, tracking, and visualizing player movement paths in the Minecraft world.

## Description

TrailTracker allows players to record their movements as paths, save them for future use, and display them to other players using particles. Perfect for creating guided tours, marking important routes, highlighting player-created trails, and enhancing navigation on your server.

### Features

- **Create Paths**: Record your movement to create custom paths
- **Visualize Paths**: Display saved paths using customizable particles
- **Manage Paths**: Add descriptions, modify properties, view details, and remove paths
- **Path Detection**: Get notified when traveling on or near a path
- **Configurable**: Customize particles, detection radius, notification frequency and more
- **Permission-Based**: Control who can create, edit, and view paths
- **User-Friendly**: Simple commands with tab completion
- **API**: Developer API for integrating with other plugins

## Requirements

- **Minecraft Version**: 1.21.x (required)
- **Server Software**: Paper (or compatible fork)
- **Java**: Java 21 or newer

**Important:** This plugin requires Minecraft 1.21.x or newer. It will not load on older server versions.

## Installation

1. Download the latest TrailTracker.jar from [Releases](https://github.com/alvarsjogren/trailtracker/releases)
2. Ensure your server is running **Paper 1.21.1+ or compatible fork**
3. Place the .jar file in your server's `plugins` folder
4. Restart your server or run `/reload confirm`
5. The default configuration will be generated automatically

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/tt help` | Shows all available commands | None |
| `/tt start <name>` | Starts recording a new path | TrailTracker.startstop |
| `/tt stop` | Stops recording the current path | TrailTracker.startstop |
| `/tt list` | Shows all available paths | None |
| `/tt info <path>` | Shows detailed info about a path | TrailTracker.info |
| `/tt describe <path> <desc>` | Sets a description for a path | TrailTracker.startstop |
| `/tt display <path>` | Toggles display of a path | TrailTracker.display |
| `/tt remove <path>` | Permanently removes a path | TrailTracker.startstop |
| `/tt modify <path> <action> <value>` | Modifies path properties | TrailTracker.startstop |

### Modify Command Actions

The `/tt modify` command supports the following actions:

- `radius <number>` - Change the path detection radius
- `particle <type>` - Change the particle used for display
- `description <text>` - Change the path description

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| TrailTracker.startstop | Allows creating, stopping, modifying and removing paths | Op |
| TrailTracker.display | Allows displaying paths | Everyone |
| TrailTracker.info | Allows viewing detailed path information | Everyone |

## Configuration

TrailTracker's behavior can be customized in the `config.yml` file:

```yaml
### Storage settings ###
# Folder where path files are stored
path-folder: "paths"

# Maximum number of locations to store per path (0 = unlimited)
max-path-points: 0

# Maximum allowed path name length
max-path-name-length: 32

### Display settings ###
# Particle to use for displaying paths
# Available options: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Particle.html
default-display-particle: "HAPPY_VILLAGER"

# Default radius around path points where players are considered "on path"
default-path-radius: 3

# How frequently to display particles (in ticks, 20 ticks = 1 second)
particle-frequency: 5

### Notification settings ###
# How often to repeat path notifications while on the same path (in milliseconds)
path-notification-reminder: 5000

### Messages ###
# Action bar text when traveling paths. {path-name} will be replaced with the path name
travel-message: "Traveling {path-name}"

# Action bar text when recording paths. {path-name} will be replaced with the path name
recording-message: "Recording {path-name}"

### Plugin metrics ###
# Enable or disable bStats metrics collection
enable-metrics: true
```

## Example Usage

**Creating a Tour Path:**
1. Start at the beginning of your tour: `/tt start ServerTour`
2. Walk through all the points of interest
3. Stop recording when complete: `/tt stop`
4. Add a description: `/tt describe ServerTour Official server tour route`
5. Modify the path properties: `/tt modify ServerTour radius 5`

**Viewing the Tour:**
1. Show all available paths: `/tt list`
2. Start following the particles: `/tt display ServerTour`
3. When done, turn off the display: `/tt display ServerTour`

## For Developers

TrailTracker provides a public API for other plugins to interact with:

```java
// Check if API is available
if (TrailTrackerProvider.isAPIAvailable()) {
    // Get the API instance
    TrailTrackerAPI api = TrailTrackerProvider.getAPI();
    
    // Get all completed paths
    Map<String, PathInfo> paths = api.getCompletedPaths();
    
    // Get information about a specific path
    PathInfo pathInfo = api.getCompletedPath("MyPath");
    
    // Check if a path exists and is completed
    boolean exists = api.isPathCompleted("MyPath");
}
```

The API provides read-only access to path data for integration with other plugins.

## Upgrading from Previous Versions

When upgrading from an older version of TrailTracker:

1. Back up your `plugins/TrailTracker/paths` folder
2. Replace the old .jar file with the new one
3. Restart your server
4. Your existing paths will be automatically migrated

## Contributing

Contributions are welcome! Please check [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

For testing and development information, see [TESTING.md](TESTING.md).

## License

This project is licensed under the [Apache License 2.0](LICENSE) - see the LICENSE file for details.

## Contact

- Author: Alvar Sjögren
- Website: [alvarsjogren.se](https://alvarsjogren.se)
- Issues: [GitHub Issues](https://github.com/alvarsjogren/trailtracker/issues)

## Acknowledgments

- Thanks to the Paper team for their amazing API
- All contributors and testers who helped improve this plugin
