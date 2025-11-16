puppy — Project documentation

Summary

This repository contains a small 2D Java game (decompiled) known as "the doger dager". The project uses AWT for rendering (Canvas + BufferStrategy) and the Slick2D library for audio (Music and Sound). The codebase is organized under src/game and includes core game loop, object model for game entities, GUI and menus, audio utilities, and a simple save manager.

Quick start

- Requirements: Java 8+ (JDK), Slick2D (slick.jar) and its audio dependencies included in this repository (jogg, jorbis, jinput). An IDE is recommended (IntelliJ/IDEA/Eclipse).
- To run from source: compile the src folder and run the main class game.core.Game. The Game constructor creates the window and starts the core loop.
- To run the provided jar: open outjar/game.jar with java -jar outjar/game.jar (requires Java and included native libraries as needed).

Project structure (high-level)

- src/game/core — core systems and entry point
  - Game.java — main class: initializes subsystems (HUD, Handler, Spawn, Menu), loads audio, creates the Window and runs the game loop (tick + render).
  - Handler.java — central registry of GameObject instances; performs per-frame tick and render, and exposes add/remove operations.
  - KeyInput.java — keyboard listener that updates player movement and debug toggles.
  - SaveManager.java — reads/writes persistent high score.
  - Spawn.java — logic for spawning enemies, powerups and triggering ending conditions.
  - AudioPlayer.java — utility to load and play sounds/music into maps for easy access.

- src/game/object — game entities
  - GameObject.java — abstract base class for in-game objects; contains position, velocity, id and core tick/render API.
  - Player.java — player-controlled object: processes input, collision, shooting, health/score changes.
  - Enemy / FastEnemy / SmartEnemy / Boss1 — enemy variants with different movement and attack behavior.
  - EnemyBossBullet / Arrow — projectile objects used by enemies and player.
  - GoodPotion — collectible that affects player state (healing/powerups).

- src/game/gui — GUI and menus
  - HUD.java — heads-up display (health, score, level) and ending animations.
  - Menu.java / Menu2.java — mouse-driven menu screens (main and secondary), handle clicks and rendering.
  - Window.java — small wrapper that creates an AWT window frame and attaches the Game Canvas.

- src/game/enums — small enums
  - ID.java — identifies GameObject types.
  - STATE.java / STATE2.java — game state machines (MENU, GAME, MENU2 and secondary states used for difficulty/ending).

- src/game/logic
  - Trail.java — decorative fading trail used by some objects for visual effects.

Assets

- res/ — audio assets (wav/mp3) used for background music and sound effects.
- outjar/game.jar — compiled game jar (decompiled code comes from this jar).

Important technical details

Game loop
- The Game class implements Runnable and runs a fixed-tick loop targeting 60 ticks per second. tick() updates game logic; render() draws via BufferStrategy.
- requestFocus() is called so keyboard input works when the window opens.
- When HUD indicates game over (health <= 0) or ending conditions are reached, the loop triggers appropriate audio and state changes.

Rendering
- Canvas + BufferStrategy with triple buffering is used. Rendering order: clear screen -> render objects via Handler -> render HUD/menu.
- Rendering may be overridden by ending animations in HUD.

Input
- KeyInput and mouse listeners on menus provide input. Debug mode is detected by JVM arguments (JDWP) and switches to debug behavior.

Audio
- AudioPlayer manages two maps: soundMap (short effects) and musicMap (longer background tracks). Audio is loaded from res/ on startup (bgm and fail sound are preloaded).

Saving
- SaveManager stores and retrieves the high score. The Game class updates and saves the score when it's a new high.

Extending the game (how to add a new enemy)

1. Create a new class under src/game/object that extends GameObject.
2. Implement tick() for movement/AI and render() for drawing. Use ID enum to add a new identifier if needed.
3. Add spawn logic in Spawn.java so it can be instantiated at appropriate levels.
4. Add any audio assets to res/ and load them via AudioPlayer.loadSound or loadMusic.

Build & debugging tips

- If audio or Slick classes cause NoClassDefFoundError, ensure slick.jar and its dependencies (jinput.jar, jogg, jorbis) are on the classpath.
- To enable debug mode inside the game, run the JVM with remote debugging flags (the code detects -agentlib:jdwp) or toggle KeyInput.debug if you modify the source.
- Use an IDE to step through the Game.run loop and inspect Handler's object list when diagnosing rendering/tick issues.

Common entry points to inspect

- game.core.Game — application entry point; orchestrates initialization and loop.
- game.core.Handler — where most objects are updated and drawn; useful for debugging collisions and ordering.
- game.object.Player — to change controls/abilities.
- game.core.Spawn — to tweak difficulty curves and spawn schedules.
- game.gui.HUD — to alter score, health and ending behavior.

Contributing

- Open issues to discuss bugs or feature requests. The original README invites contributors; this project appears decompiled so keep licensing in mind when redistributing.

Notes

- This documentation is a general overview of the codebase to help new contributors navigate the project. For detailed API-level comments consider adding Javadoc comments to public classes and methods in src/game.

Created by: automated documentation generator

