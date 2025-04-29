# Contributing to TrailTracker

Thank you for considering contributing to TrailTracker! This document provides guidelines and instructions for contributing to make the process smooth for everyone.

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [How to Contribute](#how-to-contribute)
    - [Reporting Bugs](#reporting-bugs)
    - [Suggesting Features](#suggesting-features)
    - [Code Contributions](#code-contributions)
- [Development Workflow](#development-workflow)
    - [Setting Up Your Development Environment](#setting-up-your-development-environment)
    - [Testing](#testing)
    - [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Community](#community)
- [Project Succession](#project-succession)

## Code of Conduct

By participating in this project, you agree to maintain a respectful and inclusive environment for everyone. This includes:

- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community
- Showing empathy towards other community members

## Getting Started

Before you begin:

1. Ensure you have a [GitHub account](https://github.com/signup)
2. Familiarize yourself with the [README.md](README.md) and [TESTING.md](TESTING.md)
3. Check the issues page to see if your bug/feature has already been reported

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue on GitHub with the following information:

- Clear, descriptive title
- Detailed steps to reproduce the bug
- Expected behavior vs. actual behavior
- Server information (Paper version, Java version)
- Any relevant logs or screenshots

### Suggesting Features

We welcome feature suggestions! When suggesting a feature:

- Provide a clear description of the feature
- Explain why this feature would be useful to TrailTracker users
- Consider how it might be implemented
- Indicate if you're willing to help implement it

### Code Contributions

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes with clear commit messages
4. Push to your branch
5. Open a Pull Request

## Development Workflow

### Setting Up Your Development Environment

1. Clone your fork of the repository
2. Set up a Paper test server (version 1.21.4+)
3. Install Java 21 or newer
4. Set up your IDE (IntelliJ IDEA recommended for Java development)
    - Import the project as a Maven project
    - Ensure Java 21 is configured as your project SDK
5. Build the project with Maven: `mvn clean package`
6. Copy the generated JAR from `target/` to your test server's `plugins/` folder

### Testing

Before submitting a Pull Request, please ensure:

1. Your code compiles without errors
2. Your changes work as expected in a test environment
3. You've added appropriate unit tests for new functionality (if applicable)
4. You've followed the testing guidelines in [TESTING.md](TESTING.md)

### Coding Standards

- Follow the existing code style in the project
- Add comments to explain complex logic
- Use meaningful variable and method names
- Keep methods focused on a single responsibility
- Design with extensibility in mind

## Pull Request Process

1. Ensure your code meets all requirements mentioned above
2. Update documentation if your changes affect the user experience
3. Link any relevant issues in your PR description
4. Submit your PR with a clear description of the changes and why they're valuable
5. Be responsive to feedback and questions on your PR

## Community

- Be respectful and constructive in all communications
- Help answer questions from other contributors and users
- Share your knowledge and experience

## Project Succession

TrailTracker is maintained under the Apache License 2.0, which allows for open development and forking. If you're interested in becoming a maintainer:

1. Demonstrate consistent, high-quality contributions
2. Show an understanding of the project's goals and architecture
3. Express your interest in maintaining specific areas or the entire project
4. Current maintainers may grant additional permissions based on contribution history

If the project appears unmaintained (no activity for 6+ months):

1. Try contacting the current maintainers
2. If no response, you're welcome to fork the project and continue development
3. When forking for maintenance, please acknowledge the original project and maintainers

---

Thank you for contributing to TrailTracker! Your efforts help make this plugin better for everyone.