# Pong (Java, OOP) – Documentation

## Brief Description

An object-oriented Pong game in Java (Swing), divided into clearly separated layers:

- **Model** (`pong.model`): Game objects and logic (Ball, Paddle, Score)
- **Controller** (`pong.input`, `pong.ai`): Keyboard input and AI control
- **State** (`pong.GameState`): Game state, update loop, collisions, score
- **View** (`pong.GamePanel`, `pong.GameFrame`): Rendering and window management
- **Menu** (`pong.MenuPanel`, `pong.MenuFrame`): Pre-game menu with language, mode, and difficulty selection
- **i18n** (`pong.i18n.Lang`): All user-visible strings in English and German
- **Util** (`pong.util.GameConstants`): Global game constants

---

## Features

- 2 modes:
  - `TWO_PLAYERS` (local): left `W/S`, right `↑/↓`
  - `VS_COMPUTER`: left `W/S`, right AI
- 3 difficulty levels (only `VS_COMPUTER`): `EASY`, `MEDIUM`, `HARD`
- Full-screen pre-game menu with radio-button lists for language, mode and difficulty
  - Difficulty options are greyed out (disabled) when 2 Players is selected
  - **Fullscreen** checkbox launches the game in fullscreen mode (remembered when returning to the menu)
  - **Start Game** button launches the game with the chosen parameters
- Language selection: **English** / **Deutsch** — switches all UI text instantly; remembered when returning to the menu
- Fullscreen rendering scales to the actual screen size
- Pause toggle: `P`
- Restart: `R`
- In-game menu: `Esc` (pauses game, overlay menu on top of the game)
  - **Resume** – close overlay and continue game (also press `Esc` again)
  - **New Game** – apply chosen settings and start a fresh game
  - **Exit** – return to the main menu
  - All game settings (language, mode, difficulty, fullscreen) available in the overlay
- Win condition: first team with 10 points (configurable via `GameConstants.MAX_SCORE`)
- Smooth AI with difficulty-dependent speed, tolerance zone and reaction delay (`reactionBlend`)

---

## Architecture Overview

```
PongApp (main)
  └─> MenuFrame (JFrame)
        └─> MenuPanel (JPanel, pre-game menu)
              │  [on Start Game click]
              └─> GameFrame (JFrame)
                    ├─> GamePanel (JPanel, Game-Loop via javax.swing.Timer)  [content pane]
                    │     ├─> GameState (game logic, update loop)
                    │     │     ├─> Paddle (left & right)
                    │     │     ├─> Ball
                    │     │     ├─> Score
                    │     │     ├─> InputController (KeyAdapter)
                    │     │     └─> AiController (only VS_COMPUTER)
                    │     └─> InputController (KeyAdapter, directly on panel)
                    └─> InGameMenuPanel (JPanel, glass pane, Esc-key overlay menu)
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
    -{static} currentFullscreen: boolean
    +{static} main(args: String[]): void
    +{static} startGame(): void
    +{static} updatePreferences(lang: Lang, fullscreen: boolean): void
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

  class MenuFrame {
    +MenuFrame(initialLang: Lang, initialFullscreen: boolean, onStart: Consumer<MenuResult>)
  }

  class MenuPanel {
    -lang: Lang
    +MenuPanel(initialLang: Lang, initialFullscreen: boolean, onStart: Consumer<MenuResult>)
    -refreshLabels(): void
    -applyDifficultyEnabled(enabled: boolean): void
  }

  class MenuPanel.MenuResult <<record>> {
    +mode: GameMode
    +difficulty: Difficulty
    +lang: Lang
    +fullscreen: boolean
  }

  class GameFrame {
    +GameFrame(mode: GameMode, difficulty: Difficulty, lang: Lang, fullscreen: boolean)
  }

  class InGameMenuPanel {
    -lang: Lang
    +InGameMenuPanel(initialMode: GameMode, initialDifficulty: Difficulty, initialLang: Lang, initialFullscreen: boolean, onResume: Runnable, onNewGame: Consumer<MenuResult>, onExit: Runnable)
    #paintComponent(g: Graphics): void
  }

  class GamePanel {
    -state: GameState
    -input: InputController
    -timer: Timer
    -onReturnToMenu: Runnable
    -lang: Lang
    -onEscPressed: Runnable
    +GamePanel(mode: GameMode, difficulty: Difficulty, lang: Lang, onReturnToMenu: Runnable)
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
    +labelFullscreen(): String
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
MenuFrame *-- MenuPanel : contains
MenuPanel ..> GameMode : uses
MenuPanel ..> Difficulty : uses
MenuPanel ..> Lang : uses
MenuPanel +-- MenuPanel.MenuResult : defines
PongApp ..> GameFrame : creates
GameFrame *-- GamePanel : contains
GameFrame *-- InGameMenuPanel : glass pane
GamePanel *-- GameState : owns
GamePanel *-- InputController : owns
GamePanel ..> Lang : uses
InGameMenuPanel ..> GameMode : uses
InGameMenuPanel ..> Difficulty : uses
InGameMenuPanel ..> Lang : uses
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
| `PongApp` | `pong` | Entry point; stores the selected language and fullscreen preference across sessions; `startGame()` shows the menu; `updatePreferences()` persists in-game settings changes |
| `MenuFrame` | `pong` | Swing window that hosts `MenuPanel`; disposes itself when "Start Game" is clicked |
| `MenuPanel` | `pong` | Pre-game menu: language radio buttons, game-mode radio buttons, difficulty radio buttons (greyed out when 2 Players is selected), fullscreen checkbox, "Start Game" button |
| `MenuPanel.MenuResult` | `pong` | Record returned by `MenuPanel` carrying mode, difficulty, language, and fullscreen flag |
| `GameFrame` | `pong` | Game window; hosts `GamePanel` as content pane and `InGameMenuPanel` as glass pane; wires ESC-key logic to show/hide the overlay; handles fullscreen; scales game to screen |
| `GamePanel` | `pong` | Rendering (Swing), game loop via `javax.swing.Timer`; handles `P`/`R`/`Esc` hotkeys; scales drawing to fill actual component size (fullscreen fix); uses `Lang` for all overlay text |
| `InGameMenuPanel` | `pong` | Semi-transparent glass-pane overlay shown on `Esc`; provides Resume / New Game / Exit buttons plus all game settings (language, mode, difficulty, fullscreen); game stays visible behind it |
| `GameState` | `pong` | Game state, update logic, collision detection, score |
| `GameMode` | `pong` | Enum: `TWO_PLAYERS` / `VS_COMPUTER` |
| `Difficulty` | `pong` | Enum: `EASY` / `MEDIUM` / `HARD` – controls AI parameters |
| `Lang` | `pong.i18n` | Enum: `EN` / `DE` – provides every user-visible string in the selected language |
| `Paddle` | `pong.model` | Paddle position, movement, collision box |
| `Ball` | `pong.model` | Ball position, movement, wall reflection, paddle bounce |
| `Score` | `pong.model` | Score, win condition |
| `InputController` | `pong.input` | Keyboard input via `KeyAdapter` |
| `AiController` | `pong.ai` | AI control of the right paddle with difficulty-dependent reaction |
| `GameConstants` | `pong.util` | Central game constants (sizes, speeds, colors) |
