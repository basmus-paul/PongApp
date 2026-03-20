# Pong (Java, OOP) – Dokumentation

## Kurzbeschreibung

Objektorientiertes Pong-Spiel in Java (Swing), aufgeteilt in klar getrennte Schichten:

- **Model** (`pong.model`): Spielobjekte und Logik (Ball, Paddle, Score)
- **Controller** (`pong.input`, `pong.ai`): Tastatureingabe und KI-Steuerung
- **State** (`pong.GameState`): Spielzustand, Update-Schleife, Kollisionen, Punktestand
- **View** (`pong.GamePanel`, `pong.GameFrame`): Rendering und Fenster-Management
- **Util** (`pong.util.GameConstants`): Globale Spielkonstanten

---

## Features

- 2 Modi:
  - `TWO_PLAYERS` (lokal): links `W/S`, rechts `↑/↓`
  - `VS_COMPUTER`: links `W/S`, rechts KI
- Pause: `P`
- Neustart: `R`
- Siegbedingung: erstes Team mit 10 Punkten (konfigurierbar via `GameConstants.MAX_SCORE`)
- Smooth-AI mit einstellbarer Reaktionsverzögerung (`reactionBlend`)

---

## Architekturüberblick

```
PongApp (main)
  └─> GameFrame (JFrame)
        └─> GamePanel (JPanel, Game-Loop via javax.swing.Timer)
              ├─> GameState (Spiellogik, Update-Schleife)
              │     ├─> Paddle (links & rechts)
              │     ├─> Ball
              │     ├─> Score
              │     ├─> InputController (KeyAdapter)
              │     └─> AiController (nur VS_COMPUTER)
              └─> InputController (KeyAdapter, direkt am Panel)
```

---

## UML-Klassendiagramm (PlantUML)

> Kann z.B. mit dem PlantUML-Plugin in IntelliJ IDEA oder VS Code gerendert werden.

```plantuml
@startuml
skinparam classAttributeIconSize 0

package pong {
  class PongApp {
    +{static} main(args: String[]): void
    -{static} askMode(): GameMode
  }

  enum GameMode {
    TWO_PLAYERS
    VS_COMPUTER
  }

  class GameFrame {
    +GameFrame(mode: GameMode)
  }

  class GamePanel {
    -state: GameState
    -input: InputController
    -timer: Timer
    +GamePanel(mode: GameMode)
    #paintComponent(g: Graphics): void
  }

  class GameState {
    -leftPaddle: Paddle
    -rightPaddle: Paddle
    -ball: Ball
    -score: Score
    -mode: GameMode
    -ai: AiController
    -paused: boolean
    +GameState(mode: GameMode)
    +update(dt: double, input: InputController): void
    +togglePause(): void
    +resetMatch(): void
    +getLeftPaddle(): Paddle
    +getRightPaddle(): Paddle
    +getBall(): Ball
    +getScore(): Score
    +getMode(): GameMode
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
GameFrame *-- GamePanel : contains
GamePanel *-- GameState : owns
GamePanel *-- InputController : owns
GameState *-- Paddle : 2
GameState *-- Ball : 1
GameState *-- Score : 1
GameState o-- AiController : optional
GameState ..> InputController : uses
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

## Klassenverantwortlichkeiten

| Klasse | Paket | Aufgabe |
|---|---|---|
| `PongApp` | `pong` | Einstiegspunkt, Moduswahl via `JOptionPane` |
| `GameFrame` | `pong` | Swing-Fenster, hält das `GamePanel` |
| `GamePanel` | `pong` | Rendering (Swing), Game-Loop via `javax.swing.Timer` |
| `GameState` | `pong` | Spielzustand, Update-Logik, Kollisionserkennung, Punktestand |
| `GameMode` | `pong` | Enum: `TWO_PLAYERS` / `VS_COMPUTER` |
| `Paddle` | `pong.model` | Schläger-Position, Bewegung, Kollisionsbox |
| `Ball` | `pong.model` | Ball-Position, Bewegung, Wandreflexion, Paddle-Bounce |
| `Score` | `pong.model` | Punktestand, Siegbedingung |
| `InputController` | `pong.input` | Tastatureingaben via `KeyAdapter` |
| `AiController` | `pong.ai` | KI-Steuerung des rechten Schlägers mit Reaktionsverzögerung |
| `GameConstants` | `pong.util` | Zentrale Spielkonstanten (Größen, Geschwindigkeiten, Farben) |
