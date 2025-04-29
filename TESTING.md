# TrailTracker Testing Guide

This document provides a comprehensive testing plan for the TrailTracker plugin. Follow these steps to verify all functionality is working correctly before deploying to a production server.

## Setup for Testing

1. Set up a test Paper server (version 1.21.4 or compatible)
2. Install the TrailTracker plugin in the plugins directory
3. Start the server and verify the plugin loads without errors
4. Create a testing user with all permissions: `/op <your_username>`

## Basic Functionality Tests

### Plugin Loading

- [ ] Plugin loads without errors on server startup
- [ ] Check server logs for any warnings or errors
- [ ] Verify config.yml is generated correctly
- [ ] Verify paths folder is created

### Command Registration

- [ ] Run `/tt` without parameters and verify help message appears
- [ ] Run `/tt help` and verify all commands are listed
- [ ] Verify tab completion works for the main command

## Path Creation Tests

### Starting a Path

- [ ] Run `/tt start testpath1` and verify success message
- [ ] Walk around in different directions creating a path
- [ ] Verify the recording message appears in the action bar
- [ ] Run `/tt start anotherpath` while recording and verify error message
- [ ] Try creating a path with an invalid name (special characters) and verify error message
- [ ] Try creating a very long path name (beyond max-path-name-length) and verify error message

### Stopping a Path

- [ ] Run `/tt stop` and verify success message
- [ ] Verify the recording message no longer appears
- [ ] Run `/tt stop` again and verify error message (not tracking)

## Path Management Tests

### Listing Paths

- [ ] Run `/tt list` and verify your created paths appear
- [ ] Create multiple paths and check they all appear in the list

### Path Info

- [ ] Run `/tt info testpath1` and verify detailed information appears
- [ ] Check that the path point count matches your expectations
- [ ] Verify detection radius is displayed correctly
- [ ] Verify status (completed vs recording) is accurate
- [ ] Try getting info for a non-existent path and verify error message

### Adding Descriptions

- [ ] Run `/tt describe testpath1 This is a test path` and verify success
- [ ] Run `/tt list` and verify the description is updated
- [ ] Run `/tt info testpath1` and verify the description is updated
- [ ] Try describing a non-existent path and verify error message

### Removing Paths

- [ ] Run `/tt remove testpath1` and verify success message
- [ ] Run `/tt list` and verify the path no longer appears
- [ ] Run `/tt start testpath1` again to recreate the path
- [ ] Start tracking this path, then try to remove it and verify error message
- [ ] Stop tracking, then remove successfully

## Path Display Tests

### Displaying Paths

- [ ] Create a path by recording your movement
- [ ] Run `/tt display on testpath1` and verify success message
- [ ] Verify particles appear along the path
- [ ] Run `/tt display on testpath1` again and verify already displaying message
- [ ] Try displaying a non-existent path and verify error message

### Multiple Path Display

- [ ] Create two separate paths: `/tt start path1` (record and stop), then `/tt start path2` (record and stop)
- [ ] Display both paths: `/tt display on path1` and `/tt display on path2`
- [ ] Verify both paths show particles correctly
- [ ] Check that you can see different paths in different colors/styles (if configured)

### Stopping Display

- [ ] Run `/tt display off path1` and verify success message
- [ ] Verify path1 particles no longer appear
- [ ] Verify path2 particles still appear
- [ ] Run `/tt display off path2` and verify success message
- [ ] Verify no path particles appear
- [ ] Try turning off a non-displayed path and verify error message

## Path Detection Tests

### On-Path Messages

- [ ] Create a path with `/tt start pathdetect`
- [ ] Travel along the path with recording turned off
- [ ] Verify the "Traveling pathdetect" message appears in the action bar
- [ ] Move away from the path and verify the message disappears

### Detection Radius

- [ ] Create a path with the default detection radius
- [ ] Walk near but not exactly on the path
- [ ] Verify detection still works within the radius distance
- [ ] Walk beyond the radius and verify detection stops

## Permission Tests

### Permission Enforcement

- [ ] Create a user without permissions: `/deop <your_username>`
- [ ] Try `/tt start testpath` and verify permission error
- [ ] Try `/tt stop` and verify permission error
- [ ] Try `/tt remove testpath` and verify permission error
- [ ] Try `/tt display on testpath` and verify it works (default permission)
- [ ] Try `/tt info testpath` and verify it works (default permission)
- [ ] Restore permissions: `/op <your_username>`

## Persistence Tests

### Data Saving

- [ ] Create several paths with different names and properties
- [ ] Restart the server
- [ ] Run `/tt list` and verify all paths are still present
- [ ] Display a path and verify particles appear correctly
- [ ] Run `/tt info` for a path and verify all metadata is preserved

### Path Re-creation

- [ ] Create a path with the same name as a removed path
- [ ] Verify the path is created as new rather than using old data

## Edge Case Tests

### World Change

- [ ] Create a path in one world
- [ ] Change to a different world (if multi-world server)
- [ ] Verify paths don't transfer across worlds unless intended

### Long Paths

- [ ] Create a very long path (1000+ points) by recording extensive movement
- [ ] Verify the plugin still performs well with long paths
- [ ] Check that displaying long paths doesn't cause performance issues

### Special Characters

- [ ] Test with path names containing spaces
- [ ] Test path descriptions with special characters and formatting

## Performance Tests

### Multiple Users

- [ ] Have multiple players create and display paths simultaneously
- [ ] Verify performance remains stable
- [ ] Check server resource usage (CPU, memory) during heavy usage

### Many Paths

- [ ] Create 10+ paths with different properties
- [ ] Display several paths simultaneously
- [ ] Verify performance remains stable

## Testing Notes

- Document any unexpected behavior, bugs, or issues found during testing
- Note any performance bottlenecks or areas for improvement
- Verify error messages are clear and helpful
- Check that permissions are working as expected

## Final Verification

- [ ] All tests have passed without critical errors
- [ ] Plugin functionality matches documentation
- [ ] Performance is acceptable under load
- [ ] User experience is smooth and intuitive

---

## Bug Reporting Template

When reporting bugs, please include the following information:

### Bug Description
[Detailed description of the issue]

### Steps to Reproduce
1.
2.
3.

### Expected Behavior
[What should happen]

### Actual Behavior
[What actually happens]

### Server Information
- Server version:
- Java version:
- TrailTracker version:
- Other relevant plugins:

### Error Messages/Logs
```
[Paste any relevant error messages or logs here]
```