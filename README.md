# PongApp

An object-oriented Pong game in Java (Swing) with local and LAN online multiplayer modes.

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

### Online Multiplayer (LAN) button

Press **Online Multiplayer (LAN)** to open the LAN multiplayer sub-menu and start or join an online session (see [Online Multiplayer (LAN)](#online-multiplayer-lan) below).

## Controls

### Local game

| Action                  | Key (Left) | Key (Right / 2P) |
|-------------------------|------------|------------------|
| Paddle up               | `W`        | `↑`              |
| Paddle down             | `S`        | `↓`              |
| Pause / toggle          | `P`        | –                |
| Restart (+ new countdown) | `R`      | –                |
| Open / close in-game menu | `Esc`    | –                |

### Online game

Both players can use **either** `W`/`S` **or** `↑`/`↓` to move their paddle.
`Esc` opens the online overlay menu (Resume / Disconnect).

## In-Game Menu

### Local game

Press **`Esc`** at any time during gameplay to pause the game and open the in-game menu. The game is visible in the background while the menu is shown. From the menu you can:

| Option | Description |
|--------|-------------|
| **Resume** | Close the menu and continue the current game (also: press `Esc` again) |
| **New Game** | Apply the selected settings and start a fresh game |
| **Exit** | Return to the main start menu |

The in-game menu contains the same settings as the start menu (language, game mode, difficulty, window size). Changes take effect when you click **New Game**.

### Online game

Press **`Esc`** to open the online overlay menu:

| Option | Description |
|--------|-------------|
| **Resume** | Close the overlay and continue the game |
| **Disconnect** | End the session and return to the main menu |

## Win Condition

The first team to score **10 points** wins. Afterwards the game can be restarted with `R`, or you can open the in-game menu with `Esc` to return to the main menu or start a new game.

---

## Online Multiplayer (LAN)

PongApp supports **LAN-only online multiplayer** over TCP. Both players must be on the **same local network** (or the same machine for testing).

> **Limitation:** There is no NAT traversal or relay support — internet play is not possible without a VPN or port forwarding.

### Default port

`7777` (TCP). Make sure your firewall allows inbound connections on this port on the host machine.

### How to host

1. From the main menu click **Online Multiplayer (LAN)**.
2. Click **Host Game**.
3. Your LAN IP addresses and port are displayed (e.g. `192.168.1.20:7777`). Share one of these with the other player.
4. Click **Start Hosting**. The screen shows *"Waiting for player to join…"*.
5. When the other player connects the game starts automatically. **You control the left paddle.**

### How to join

1. From the main menu click **Online Multiplayer (LAN)**.
2. Click **Join Game**.
3. Enter the host's IP address (provided by the host in step 3 above) and the port (`7777` by default).
4. Click **Connect**. The game starts automatically once the host accepts your connection. **You control the right paddle.**

### Troubleshooting

| Symptom | Possible fix |
|---------|-------------|
| "Port Unavailable" on the host | Another process is using port 7777 – close it and retry |
| "Connection Failed" on the joiner | Check that both devices are on the same network, the IP is correct, and the host's firewall allows port 7777 |
| Connection drops mid-game | A "Disconnected" dialog is shown; both players are returned to the main menu |

---

## Documentation

See [DOCUMENTATION.md](DOCUMENTATION.md) for the architecture overview and the UML class diagram.