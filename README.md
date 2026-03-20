# PongApp

Ein objektorientiertes Pong-Spiel in Java (Swing) mit zwei Spielmodi: Zwei Spieler lokal oder gegen den Computer.

## Voraussetzungen

- **JDK 25** (wird automatisch über die Gradle Java Toolchain heruntergeladen)
- Gradle Wrapper ist im Repo enthalten – kein separates Gradle-Install nötig

## Projekt bauen & starten

### Spiel direkt starten

```bash
./gradlew run
```

### JAR erzeugen

```bash
./gradlew clean jar
```

Das fertige JAR liegt unter `build/libs/`:

```bash
java -jar build/libs/PongApp-1.0.jar
```

## Steuerung

| Aktion            | Taste (Links) | Taste (Rechts / 2P) |
|-------------------|---------------|----------------------|
| Schläger hoch     | `W`           | `↑`                  |
| Schläger runter   | `S`           | `↓`                  |
| Pause             | `P`           | –                    |
| Neustart          | `R`           | –                    |

## Spielmodi

Beim Start erscheint ein Dialog zur Moduswahl:

- **2 Spieler** – beide Schläger werden manuell gesteuert
- **Gegen Computer** – der rechte Schläger wird von einer KI gesteuert

## Siegbedingung

Das erste Team, das **10 Punkte** erzielt, gewinnt. Danach kann mit `R` neu gestartet werden.

## Dokumentation

Siehe [DOCUMENTATION.md](DOCUMENTATION.md) für den Architekturüberblick und das UML-Klassendiagramm.