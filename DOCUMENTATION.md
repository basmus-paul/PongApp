# Pong (Java, OOP) – Documentation

## Brief Description

An object-oriented Pong game in Java (Swing), divided into clearly separated layers:

- **Model** (`pong.model`): Game objects and logic (Ball, Paddle, Score)
- **Controller** (`pong.input`, `pong.ai`): Keyboard input and AI control
- **State** (`pong.GameState`): Game state, update loop, collisions, score
- **View** (`pong.GamePanel`, `pong.GameFrame`): Rendering and window management
- **Util** (`pong.util.GameConstants`): Global game constants

---

## Features

- 2 modes:
  - `TWO_PLAYERS` (local): left `W/S`, right `↑/↓`
  - `VS_COMPUTER`: left `W/S`, right AI
- 3 difficulty levels (only `VS_COMPUTER`): `EASY`, `MEDIUM`, `HARD`
- Pause: `P`
- Restart: `R`
- Win condition: first team with 10 points (configurable via `GameConstants.MAX_SCORE`)
- Smooth AI with difficulty-dependent speed, tolerance zone and reaction delay (`reactionBlend`)

---

## Architecture Overview

```
PongApp (main)
  └─> GameFrame (JFrame)
        └─> GamePanel (JPanel, Game-Loop via javax.swing.Timer)
              ├─> GameState (game logic, update loop)
              │     ├─> Paddle (left & right)
              │     ├─> Ball
              │     ├─> Score
              │     ├─> InputController (KeyAdapter)
              │     └─> AiController (only VS_COMPUTER)
              └─> InputController (KeyAdapter, directly on panel)
```

---

## UML Class Diagram (PlantUML)

> Can be rendered e.g. with the PlantUML plugin in IntelliJ IDEA or VS Code.

```plantuml
@startuml
skinparam classAttributeIconSize 0

package pong {
  class PongApp {
    +{static} main(args: String[]): void
    -{static} askMode(): GameMode
    -{static} askDifficulty(): Difficulty
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

  class GameFrame {
    +GameFrame(mode: GameMode, difficulty: Difficulty)
  }

  class GamePanel {
    -state: GameState
    -input: InputController
    -timer: Timer
    +GamePanel(mode: GameMode, difficulty: Difficulty)
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
PongApp ..> GameFrame : creates
PongApp ..> GameMode : uses
PongApp ..> Difficulty : uses
GameFrame *-- GamePanel : contains
GamePanel *-- GameState : owns
GamePanel *-- InputController : owns
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

@enduml
```

---

## Class Responsibilities

| Class | Package | Responsibility |
|---|---|---|
| `PongApp` | `pong` | Entry point, mode selection and difficulty selection via `JOptionPane` |
| `GameFrame` | `pong` | Swing window, holds the `GamePanel` |
| `GamePanel` | `pong` | Rendering (Swing), game loop via `javax.swing.Timer` |
| `GameState` | `pong` | Game state, update logic, collision detection, score |
| `GameMode` | `pong` | Enum: `TWO_PLAYERS` / `VS_COMPUTER` |
| `Difficulty` | `pong` | Enum: `EASY` / `MEDIUM` / `HARD` – controls AI parameters |
| `Paddle` | `pong.model` | Paddle position, movement, collision box |
| `Ball` | `pong.model` | Ball position, movement, wall reflection, paddle bounce |
| `Score` | `pong.model` | Score, win condition |
| `InputController` | `pong.input` | Keyboard input via `KeyAdapter` |
| `AiController` | `pong.ai` | AI control of the right paddle with difficulty-dependent reaction |
| `GameConstants` | `pong.util` | Central game constants (sizes, speeds, colors) |
