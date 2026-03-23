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

At launch a menu appears with the following options:

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
| **Easy** | Balanced AI — manageable challenge for most players |
| **Medium** | Fast, highly reactive AI |
| **Hard** | Maximum-speed AI with near-instant reaction — very difficult |

> The difficulty options are greyed out when **2 Players** is selected and become active only when **vs. Computer** is chosen.

### Window Size

Select the game window size using one of three presets:

| Preset | Resolution | Notes |
|--------|-----------|-------|
| **1080p** | 1350 × 900 | Default — suitable for 1080p displays |
| **1440p** | 1800 × 1200 | Suitable for 1440p / QHD displays |
| **4K**    | 3000 × 2000 | Suitable for 4K / UHD displays |

The logical game resolution is always fixed at **900 × 600**. The game rendering scales automatically to fill the chosen window size, so gameplay is identical regardless of which preset you pick. The setting is remembered if you return to the menu mid-game.

> **Note:** Fullscreen mode was intentionally removed due to jitter and stuttering issues on lower refresh-rate displays.

### Start Game button

Press **Start Game** to launch the game with the selected parameters. A **3-second countdown** (3 → 2 → 1) is shown in the centre of the screen before gameplay begins.

## Controls

| Action                  | Key (Left) | Key (Right / 2P) |
|-------------------------|------------|------------------|
| Paddle up               | `W`        | `↑`              |
| Paddle down             | `S`        | `↓`              |
| Pause / toggle          | `P`        | –                |
| Restart (+ new countdown) | `R`      | –                |
| Open / close in-game menu | `Esc`    | –                |

## In-Game Menu

Press **`Esc`** at any time during gameplay to pause the game and open the in-game menu. The game is visible in the background while the menu is shown. From the menu you can:

| Option | Description |
|--------|-------------|
| **Resume** | Close the menu and continue the current game (also: press `Esc` again) |
| **New Game** | Apply the selected settings and start a fresh game |
| **Exit** | Return to the main start menu |

The in-game menu contains the same settings as the start menu (language, game mode, difficulty, window size). Changes take effect when you click **New Game**.

## Win Condition

The first team to score **10 points** wins. Afterwards the game can be restarted with `R`, or you can open the in-game menu with `Esc` to return to the main menu or start a new game.

## Documentation

See [DOCUMENTATION.md](DOCUMENTATION.md) for the architecture overview and the UML class diagram.