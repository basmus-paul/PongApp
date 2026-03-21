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

## Startup Menu

At launch a full-screen menu appears with the following options:

### Language

Choose between **English** and **Deutsch**. The entire UI (menu labels, in-game text, overlays) switches language immediately when you select a different option. The language is also remembered when you return to the menu mid-game.

### Game Mode

| Option | Description |
|--------|-------------|
| **2 Players** | Both paddles are controlled manually |
| **vs. Computer** | The right paddle is controlled by an AI |

### Difficulty

| Difficulty | Description |
|------------|-------------|
| **Easy** | Slow AI with a large tolerance zone |
| **Medium** | Balanced AI (default) |
| **Hard** | Fast, highly reactive AI |

> The difficulty options are greyed out when **2 Players** is selected and become active only when **vs. Computer** is chosen.

### Start Game button

Press **Start Game** to launch the game with the selected parameters.

### Fullscreen

Check the **Fullscreen** checkbox to launch the game in fullscreen mode. Uncheck it to play in a window. The setting is remembered if you return to the menu mid-game. Gameplay speed (ball and paddle movement) is identical in both windowed and fullscreen mode, regardless of display refresh rate.

## Controls

| Action                  | Key (Left) | Key (Right / 2P) |
|-------------------------|------------|------------------|
| Paddle up               | `W`        | `↑`              |
| Paddle down             | `S`        | `↓`              |
| Pause / toggle          | `P`        | –                |
| Restart                 | `R`        | –                |
| Open / close in-game menu | `Esc`    | –                |

## In-Game Menu

Press **`Esc`** at any time during gameplay to pause the game and open the in-game menu. The game is visible in the background while the menu is shown. From the menu you can:

| Option | Description |
|--------|-------------|
| **Resume** | Close the menu and continue the current game (also: press `Esc` again) |
| **New Game** | Apply the selected settings and start a fresh game |
| **Exit** | Return to the main start menu |

The in-game menu contains the same settings as the start menu (language, game mode, difficulty, fullscreen). Changes take effect when you click **New Game**.

## Win Condition

The first team to score **10 points** wins. Afterwards the game can be restarted with `R`, or you can open the in-game menu with `Esc` to return to the main menu or start a new game.

## Documentation

See [DOCUMENTATION.md](DOCUMENTATION.md) for the architecture overview and the UML class diagram.