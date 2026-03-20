# PongApp

An object-oriented Pong game in Java (Swing) with two game modes: two players locally or against the computer.

## Prerequisites

- **JDK 25** (downloaded automatically via the Gradle Java Toolchain)
- Gradle Wrapper is included in the repo – no separate Gradle installation needed

## Building & Running the Project

### Run the game directly

```bash
./gradlew run
```

### Build a JAR

```bash
./gradlew clean jar
```

The finished JAR is located under `build/libs/`:

```bash
java -jar build/libs/PongApp-1.0.jar
```

## Controls

| Action            | Key (Left) | Key (Right / 2P) |
|-------------------|------------|------------------|
| Paddle up         | `W`        | `↑`              |
| Paddle down       | `S`        | `↓`              |
| Pause             | `P`        | –                |
| Restart           | `R`        | –                |

## Game Modes

At startup a dialog appears to select the mode:

- **2 Players** – both paddles are controlled manually
- **vs. Computer** – the right paddle is controlled by an AI

When **vs. Computer** is selected, the difficulty level is then requested:

| Difficulty | Description |
|------------|-------------|
| **Easy**   | Slow AI with a large tolerance zone |
| **Medium** | Balanced AI (default) |
| **Hard**   | Fast, highly reactive AI |

## Win Condition

The first team to score **10 points** wins. Afterwards the game can be restarted with `R`.

## Documentation

See [DOCUMENTATION.md](DOCUMENTATION.md) for the architecture overview and the UML class diagram.