# TrailTracker

A Minecraft Paper plugin for creating, tracking, and visualizing player movement paths in the Minecraft world.

## Description

TrailTracker allows players to record their movements as paths, save them for future use, and display them to other players using particles. Perfect for creating guided tours, marking important routes, highlighting player-created trails, and enhancing navigation on your server.

### Features

- **Create Paths**: Record your movement to create custom paths
- **Visualize Paths**: Display saved paths using particles
- **Manage Paths**: Add descriptions, view details, and remove paths
- **Path Detection**: Get notified when traveling on or near a path
- **Configurable**: Customize particles, detection radius, and more
- **Permission-Based**: Control who can create, edit, and view paths
- **User-Friendly**: Simple commands with tab completion

## Installation

1. Download the latest TrailTracker.jar from [Releases](https://github.com/yourname/trailtracker/releases)
2. Place the .jar file in your server's `plugins` folder
3. Restart your server or run `/reload confirm`
4. The default configuration will be generated automatically

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/tt help` | Shows all available commands | None |
| `/tt start <name>` | Starts recording a new path | TrailTracker.startstop |
| `/tt stop` | Stops recording the current path | TrailTracker.startstop |
| `/tt list` | Shows all available paths | None |
| `/tt info <path>` | Shows detailed info about a path | TrailTracker.info |
| `/tt describe <path> <desc>` | Sets a description for a path | TrailTracker.startstop |
| `/tt display on <path>` | Starts displaying a path | TrailTracker.display |
| `/tt display off <path>` | Stops displaying a path | TrailTracker.display |
| `/tt remove <path>` | Permanently removes a path | TrailTracker.startstop |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| TrailTracker.startstop | Allows creating, stopping, and removing paths | Op |
| TrailTracker.display | Allows displaying paths | Everyone |
| TrailTracker.info | Allows viewing detailed path information | Everyone |

## Configuration

TrailTracker's behavior can be customized in the `config.yml` file:

```yaml
# Storage settings
path-folder: "paths"  # Where path files are stored
max-path-points: 0    # Maximum points per path (0 = unlimited)
max-path-name-length: 32  # Maximum path name length

# Display settings
default-display-particle: "HAPPY_VILLAGER"  # Particle type for paths
default-path-radius: 3  # Detection radius around paths (blocks)
particle-frequency: 5   # How often to show particles (ticks)

# Messages
travel-message: "Traveling {path-name}"     # Message when on a path
recording-message: "Recording {path-name}"  # Message when recording
```

## Example Usage

**Creating a Tour Path:**
1. Start at the beginning of your tour: `/tt start ServerTour`
2. Walk through all the points of interest
3. Stop recording when complete: `/tt stop`
4. Add a description: `/tt describe ServerTour Official server tour route`

**Viewing the Tour:**
1. Show all available paths: `/tt list`
2. Start following the particles: `/tt display on ServerTour`
3. When done, turn off the display: `/tt display off ServerTour`

## Compatibility

- **Supports**: Paper 1.21.4+
- **Java**: Java 21 or newer

## Contributing

Contributions are welcome! Please check [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

For testing and development information, see [TESTING.md](TESTING.md).

## License

This project is licensed under the [Apache License 2.0](LICENSE) - see the LICENSE file for details.

## Contact

- Author: Alvar Sjögren
- Website: [alvarsjogren.se](https://alvarsjogren.se)
- Issues: [GitHub Issues](https://github.com/yourname/trailtracker/issues)

## Acknowledgments

- Thanks to the Paper team for their amazing API
- All contributors and testers who helped improve this plugin
