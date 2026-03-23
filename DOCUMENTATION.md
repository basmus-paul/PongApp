# Pong (Java, OOP) – Documentation

## Brief Description

An object-oriented Pong game in Java (Swing), divided into clearly separated layers:

- **Model** (`pong.model`): Game objects and logic (Ball, Paddle, Score)
- **Controller** (`pong.input`, `pong.ai`): Keyboard input and AI control
- **State** (`pong.GameState`): Game state, update loop, collisions, score
- **View** (`pong.GamePanel`, `pong.GameFrame`): Rendering and window management
- **Menu** (`pong.MenuPanel`, `pong.MenuFrame`): Pre-game menu with language, mode, difficulty and window-size selection
- **Online** (`pong.online`): LAN TCP multiplayer – server, client, online-specific panels and menus (kept fully separate from `GameMode`)
- **i18n** (`pong.i18n.Lang`): All user-visible strings in English and German
- **Util** (`pong.util.GameConstants`): Global game constants

---

## Features

- 2 modes:
  - `TWO_PLAYERS` (local): left `W/S`, right `↑/↓`
  - `VS_COMPUTER`: left `W/S`, right AI
- 3 difficulty levels (only `VS_COMPUTER`): `EASY`, `MEDIUM`, `HARD`
- **3-second countdown** (3 → 2 → 1) shown in the centre of the screen before gameplay begins; also resets on `R` (match restart)
- Pre-game menu with radio-button groups for language, mode, difficulty and window size
  - Difficulty options are greyed out (disabled) when 2 Players is selected
  - **Window Size** radio group selects one of three presets (1080p, 1440p, 4K); preference is remembered when returning to the menu
  - **Start Game** button launches the game with the chosen parameters
- Fixed logical resolution: **900 × 600** (`GameConstants.WIDTH` / `GameConstants.HEIGHT`). The game panel scales its rendering to fill the chosen window-size preset.
- Window-size presets (game-panel / client-area size):

  | Preset | Width × Height | Notes |
  |--------|---------------|-------|
  | `1080p` | 1350 × 900 | Default |
  | `1440p` | 1800 × 1200 | QHD displays |
  | `4K`    | 3000 × 2000 | UHD displays |

- **Fullscreen mode removed** intentionally — it caused jitter and stuttering on lower refresh-rate displays
- Language selection: **English** / **Deutsch** — switches all UI text instantly; remembered when returning to the menu
- Real-time simulation independent of rendering speed and display refresh rate (fixed-timestep game loop thread; frame time clamped to avoid spiral-of-death)
- Thread-safe keyboard input: key state is tracked with a `ConcurrentHashMap`-backed set, safe for concurrent reads from the game loop thread and writes from the EDT
- Reduced repaint latency: `repaint()` is called directly from the game loop thread (no extra EDT queue hop via `invokeLater`); `repaintPending` is reset only after an actual frame has been painted, ensuring at most one outstanding repaint request at all times
- Pause toggle: `P`
- Restart (+ new 3-second countdown): `R`
- In-game menu: `Esc` (pauses game, overlay menu on top of the game)
  - **Resume** – close overlay and continue game (also press `Esc` again)
  - **New Game** – apply chosen settings and start a fresh game
  - **Exit** – return to the main menu
  - All game settings (language, mode, difficulty, window size) available in the overlay
- Win condition: first team with 10 points (configurable via `GameConstants.MAX_SCORE`)
- Smooth AI with difficulty-dependent speed, tolerance zone and reaction delay (`reactionBlend`)
- **LAN online multiplayer** over TCP (port 7777):
  - Host/join flow accessible via the "Online Multiplayer (LAN)" button in the main menu
  - Host displays LAN IP addresses so the joiner can connect
  - Authoritative server simulation; client sends inputs only; server broadcasts snapshots
  - Clean disconnect handling with dialog and return to main menu
  - Kept entirely separate from `GameMode` / `Difficulty` / `AiController`
  - `EASY`: balanced challenge (former Medium parameters)
  - `MEDIUM`: fast and reactive AI (former Hard parameters)
  - `HARD`: maximum-speed AI with near-instant reaction — significantly more difficult than Medium

---

## Architecture Overview

```
PongApp (main)
  └─> MenuFrame (JFrame)
        └─> MenuPanel (JPanel, pre-game menu)
              │  [on Start Game click]
              ├─> GameFrame (JFrame)
              │     ├─> GamePanel (JPanel, Game-Loop via dedicated Thread / fixed-timestep accumulator)  [content pane]
              │     │     ├─> GameState (game logic, update loop)
              │     │     │     ├─> Paddle (left & right)
              │     │     │     ├─> Ball
              │     │     │     ├─> Score
              │     │     │     ├─> InputController (KeyAdapter)
              │     │     │     └─> AiController (only VS_COMPUTER)
              │     │     └─> InputController (KeyAdapter, directly on panel)
              │     └─> InGameMenuPanel (JPanel, glass pane, Esc-key overlay menu)
              │
              │  [on Online Multiplayer click]
              └─> OnlineMenuFrame (JFrame, CardLayout: main / host / join)
                    │  [host flow]
                    ├─> OnlineServer (TCP ServerSocket, game loop thread, snapshot broadcaster)
                    │     └─> OnlineGameState (authoritative physics, identical to GameState)
                    │  [join flow]
                    ├─> OnlineClient (TCP Socket, background read loop)
                    │
                    └─> OnlineGameFrame (JFrame)  [shown for both host and joiner]
                          ├─> OnlineGamePanel (JPanel, renders from GameSnapshot, sends input)
                          └─> OnlineInGameMenuPanel (JPanel, glass pane, Resume / Disconnect)
```

### Online networking model

```
Host machine                              Joiner machine
─────────────────────────────────────────────────────────
OnlineServer                              OnlineClient
  ├─ runs authoritative OnlineGameState     ├─ connects to host
  ├─ reads local keyboard input             ├─ reads snapshots → updates latestSnapshot
  ├─ reads remote INPUT messages            └─ sends INPUT messages on key events
  └─ broadcasts SNAPSHOT after each step

Protocol (TCP, binary):
  Client→Server  MSG_INPUT    1B type | 4B seq | 1B up | 1B down
  Server→Client  MSG_SNAPSHOT 1B type | 8B tick | 8B×6 positions+vel | 4B×2 score | 1B×2 flags | 4B countdown
  Handshake      Server→Client: 1B version | 1B role
```

---

## UML Class Diagram (PlantUML)

> Can be rendered e.g. with the PlantUML plugin in IntelliJ IDEA or VS Code.

```plantuml
@startuml
skinparam classAttributeIconSize 0

package pong {
  class PongApp {
    -{static} currentLang: Lang
    -{static} currentPreset: WindowPreset
    +{static} main(args: String[]): void
    +{static} startGame(): void
    +{static} updatePreferences(lang: Lang, preset: WindowPreset): void
  }

  enum GameMode {
    TWO_PLAYERS
    VS_COMPUTER
  }

  enum Difficulty {
    EASY
    MEDIUM
    HARD
  }

  enum WindowPreset {
    P1080
    P1440
    P4K
    +label: String
    +width: int
    +height: int
    +toDimension(): Dimension
  }

  class MenuFrame {
    +MenuFrame(initialLang: Lang, initialPreset: WindowPreset, onStart: Consumer<MenuResult>)
  }

  class MenuPanel {
    -lang: Lang
    +MenuPanel(initialLang: Lang, initialPreset: WindowPreset, onStart: Consumer<MenuResult>)
    -refreshLabels(): void
    -applyDifficultyEnabled(enabled: boolean): void
    -presetButton(preset: WindowPreset): JRadioButton
    -selectedPreset(): WindowPreset
  }

  class MenuPanel.MenuResult <<record>> {
    +mode: GameMode
    +difficulty: Difficulty
    +lang: Lang
    +preset: WindowPreset
  }

  class GameFrame {
    +GameFrame(mode: GameMode, difficulty: Difficulty, lang: Lang, preset: WindowPreset)
  }

  class InGameMenuPanel {
    -lang: Lang
    +InGameMenuPanel(initialMode: GameMode, initialDifficulty: Difficulty, initialLang: Lang, initialPreset: WindowPreset, onResume: Runnable, onNewGame: Consumer<MenuResult>, onExit: Runnable)
    #paintComponent(g: Graphics): void
  }

  class GamePanel {
    -{static} STEP: double
    -{static} MAX_FRAME_TIME: double
    -{static} MAX_UPDATES_PER_FRAME: int
    -{static} COUNTDOWN_START: double
    -state: GameState
    -input: InputController
    -countdown: double
    -running: boolean
    -repaintPending: boolean
    -gameThread: Thread
    -onReturnToMenu: Runnable
    -lang: Lang
    -onEscPressed: Runnable
    +GamePanel(mode: GameMode, difficulty: Difficulty, lang: Lang, onReturnToMenu: Runnable)
    -startGameLoop(): void
    +addNotify(): void
    +removeNotify(): void
    +setOnEscPressed(callback: Runnable): void
    +setLang(lang: Lang): void
    +pause(): void
    +resume(): void
    +stopAndReturnToMenu(): void
    #paintComponent(g: Graphics): void
  }

  class GameState {
    -leftPaddle: Paddle
    -rightPaddle: Paddle
    -ball: Ball
    -score: Score
    -mode: GameMode
    -difficulty: Difficulty
    -ai: AiController
    -paused: boolean
    +GameState(mode: GameMode, difficulty: Difficulty)
    +update(dt: double, input: InputController): void
    +togglePause(): void
    +resetMatch(): void
    +getLeftPaddle(): Paddle
    +getRightPaddle(): Paddle
    +getBall(): Ball
    +getScore(): Score
    +getMode(): GameMode
    +getDifficulty(): Difficulty
    +isPaused(): boolean
  }
}

package pong.i18n {
  enum Lang {
    EN
    DE
    +displayName: String
    +labelLanguage(): String
    +labelGameMode(): String
    +mode2Players(): String
    +modeVsComputer(): String
    +labelDifficulty(): String
    +diffNote(): String
    +diffEasy(): String
    +diffMedium(): String
    +diffHard(): String
    +btnStart(): String
    +btnResume(): String
    +btnNewGame(): String
    +btnExit(): String
    +inGameMenuTitle(): String
    +labelWindowSize(): String
    +statusBar(mode: GameMode, diff: Difficulty): String
    +diffLabel(diff: Difficulty): String
    +pauseTitle(): String
    +pauseHint(): String
    +winnerText(score: Score): String
    +gameOverHint(): String
  }
}

package pong.model {
  class Paddle {
    -x: double
    -y: double
    -width: int
    -height: int
    -velocityY: double
    +Paddle(x: double, y: double)
    +setVelocityY(vy: double): void
    +update(dt: double): void
    +getBounds(): Rectangle2D
    +centerY(): double
    +reset(y: double): void
    +getX(): double
    +getY(): double
    +getWidth(): int
    +getHeight(): int
  }

  class Ball {
    -x: double
    -y: double
    -size: int
    -vx: double
    -vy: double
    +Ball(x: double, y: double)
    +update(dt: double): void
    +getBounds(): Rectangle2D
    +bounceFromPaddle(p: Paddle): void
    +randomServe(direction: int): void
    +getX(): double
    +getY(): double
    +getSize(): int
    +getVx(): double
    +getVy(): double
    +setPosition(x: double, y: double): void
  }

  class Score {
    -left: int
    -right: int
    +leftScores(): void
    +rightScores(): void
    +getLeft(): int
    +getRight(): int
    +isGameOver(): boolean
    +winnerText(): String
    +reset(): void
  }
}

package pong.input {
  class InputController {
    -down: Set<Integer>
    +keyPressed(e: KeyEvent): void
    +keyReleased(e: KeyEvent): void
    +isDown(keyCode: int): boolean
  }
  note right of InputController : down is backed by ConcurrentHashMap.newKeySet()\nfor safe concurrent access between game loop and EDT
}

package pong.ai {
  class AiController {
    -maxSpeed: double
    -deadZone: double
    -reactionBlend: double
    -targetY: double
    +AiController(maxSpeed: double, deadZone: double, reactionBlend: double)
    +update(aiPaddle: Paddle, ball: Ball, dt: double): void
  }
}

package pong.util {
  class GameConstants <<utility>> {
    +{static} WIDTH: int
    +{static} HEIGHT: int
    +{static} FPS: int
    +{static} DT: double
    +{static} PADDLE_WIDTH: int
    +{static} PADDLE_HEIGHT: int
    +{static} PADDLE_SPEED: double
    +{static} BALL_SIZE: int
    +{static} BALL_SPEED: double
    +{static} BALL_SPEEDUP_FACTOR: double
    +{static} MAX_SCORE: int
    +{static} BG: Color
    +{static} FG: Color
    +{static} ACCENT: Color
  }
}

' Relationships
PongApp ..> MenuFrame : creates
PongApp ..> Lang : uses
PongApp ..> WindowPreset : uses
MenuFrame *-- MenuPanel : contains
MenuPanel ..> GameMode : uses
MenuPanel ..> Difficulty : uses
MenuPanel ..> Lang : uses
MenuPanel ..> WindowPreset : uses
MenuPanel +-- MenuPanel.MenuResult : defines
PongApp ..> GameFrame : creates
GameFrame *-- GamePanel : contains
GameFrame *-- InGameMenuPanel : glass pane
GameFrame ..> WindowPreset : uses
GamePanel *-- GameState : owns
GamePanel *-- InputController : owns
GamePanel ..> Lang : uses
InGameMenuPanel ..> GameMode : uses
InGameMenuPanel ..> Difficulty : uses
InGameMenuPanel ..> Lang : uses
InGameMenuPanel ..> WindowPreset : uses
InGameMenuPanel ..> MenuPanel.MenuResult : creates
GameState *-- Paddle : 2
GameState *-- Ball : 1
GameState *-- Score : 1
GameState o-- AiController : optional
GameState ..> InputController : uses
GameState ..> Difficulty : uses
AiController ..> Paddle : controls
AiController ..> Ball : reads
Ball ..> Paddle : reads (bounceFromPaddle)
GameState ..> GameMode : uses
GamePanel ..> GameMode : uses
GameState ..> GameConstants : uses
Ball ..> GameConstants : uses
Paddle ..> GameConstants : uses
Score ..> GameConstants : uses
AiController ..> GameConstants : uses
Lang ..> GameMode : uses
Lang ..> Difficulty : uses
Lang ..> Score : uses

@enduml
```

---

## Class Responsibilities

| Class | Package | Responsibility |
|---|---|---|
| `PongApp` | `pong` | Entry point; stores the selected language and window-size preset across sessions; `startGame()` shows the menu; `startOnline()` shows the online multiplayer menu; `updatePreferences()` persists in-game settings changes |
| `MenuFrame` | `pong` | Swing window that hosts `MenuPanel`; disposes itself when "Start Game" or "Online Multiplayer" is clicked |
| `MenuPanel` | `pong` | Pre-game menu: language radio buttons, game-mode radio buttons, difficulty radio buttons (greyed out when 2 Players is selected), window-size preset radio buttons, "Start Game" and "Online Multiplayer (LAN)" buttons |
| `MenuPanel.MenuResult` | `pong` | Record returned by `MenuPanel` carrying mode, difficulty, language, and window-size preset |
| `WindowPreset` | `pong` | Enum of the three available window-size presets (1080p: 1350×900, 1440p: 1800×1200, 4K: 3000×2000); each preset carries its label, width, and height |
| `GameFrame` | `pong` | Game window; sets the game-panel preferred size to the chosen preset before `pack()`; hosts `GamePanel` as content pane and `InGameMenuPanel` as glass pane; wires ESC-key logic to show/hide the overlay |
| `GamePanel` | `pong` | Rendering (Swing), game loop via a dedicated `Thread` with a fixed-timestep accumulator (real-time simulation independent of rendering speed); 3-second countdown before gameplay begins (also resets on `R`); handles `P`/`R`/`Esc` hotkeys; scales drawing to fill actual component size; calls `repaint()` directly from the game loop thread (no extra EDT queue hop) and resets `repaintPending` inside `paintComponent` after each actual frame is painted; uses `Lang` for all overlay text |
| `InGameMenuPanel` | `pong` | Semi-transparent glass-pane overlay shown on `Esc`; provides Resume / New Game / Exit buttons plus all game settings (language, mode, difficulty, window size); game stays visible behind it |
| `GameState` | `pong` | Game state, update logic, collision detection, score |
| `GameMode` | `pong` | Enum: `TWO_PLAYERS` / `VS_COMPUTER` |
| `Difficulty` | `pong` | Enum: `EASY` / `MEDIUM` / `HARD` – controls AI parameters |
| `Lang` | `pong.i18n` | Enum: `EN` / `DE` – provides every user-visible string in the selected language |
| `Paddle` | `pong.model` | Paddle position, movement, collision box |
| `Ball` | `pong.model` | Ball position, movement, wall reflection, paddle bounce |
| `Score` | `pong.model` | Score, win condition |
| `InputController` | `pong.input` | Thread-safe keyboard input via `KeyAdapter`; key state is stored in a `ConcurrentHashMap`-backed set so reads from the game loop thread and writes from the EDT are safe without additional synchronization |
| `AiController` | `pong.ai` | AI control of the right paddle with difficulty-dependent reaction |
| `GameConstants` | `pong.util` | Central game constants (sizes, speeds, colors) |
| `Protocol` | `pong.online` | Network protocol constants: default port (7777), message type bytes, protocol version, role constants |
| `GameSnapshot` | `pong.online` | Immutable record of the authoritative game state sent from server to both players; includes read/write helpers for the binary wire format |
| `NetUtil` | `pong.online` | Discovers viable LAN IPv4 addresses by enumerating network interfaces; used on the host screen |
| `OnlineGameState` | `pong.online` | Authoritative physics state owned by `OnlineServer`; identical physics to `GameState` but takes explicit left/right paddle velocities; not exposed to clients |
| `OnlineServer` | `pong.online` | TCP server: binds port 7777, accepts one client, runs fixed-timestep game loop, reads remote `INPUT` messages, broadcasts `SNAPSHOT` to local renderer and remote client |
| `OnlineClient` | `pong.online` | TCP client: connects to host, reads `SNAPSHOT` messages on a background thread, sends `INPUT` messages on key events |
| `OnlineGamePanel` | `pong.online` | Renders from the latest `GameSnapshot`; forwards key events to `OnlineServer.setLocalInput` (host) or `OnlineClient.sendInput` (joiner); starts the appropriate loop in `addNotify()` |
| `OnlineInGameMenuPanel` | `pong.online` | Minimal glass-pane overlay for online sessions: Resume (close overlay) and Disconnect (end session, return to menu) |
| `OnlineGameFrame` | `pong.online` | Game window for online sessions; hosts `OnlineGamePanel` and `OnlineInGameMenuPanel`; handles clean disconnect on window close, Disconnect button, or unexpected connection loss |
| `OnlineMenuFrame` | `pong.online` | Three-card menu (main / host / join); host card shows LAN addresses and starts the server; join card accepts IP/port and connects the client; error dialogs shown on bind/connect failure |
