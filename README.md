# Doger Dager

A fast little arcade dodge-'em-up: you have no weapon — you survive. Descend through
timed floors, weave through enemies and bullet patterns, and outlast multi-phase bosses
on five hearts. Built with libGDX, rendered entirely from primitives.

▶ **Play:** [https://puppylover20121031.itch.io/thedodger](https://unpuppyable.itch.io/the-doger-dager-2)

## Controls

| Action                  | Keyboard      | Gamepad            |
|-------------------------|---------------|--------------------|
| Move                    | WASD / Arrows | Left stick / D-pad |
| Shield (drains stamina) | Shift         | Bumper             |
| Dash (brief i-frames)   | Tab           | A                  |
| Pause                   | Esc           | Start              |
| Fullscreen              | F11           | —                  |

From the menu: `Enter` start · `T` stats · `O` settings · `A` achievements.

Each floor is a timed survival stretch — clear it to heal and move on; every fourth
floor is a boss. Difficulties: **Easy / Normal / Hard / Hardcore** (Hardcore unlocks
once you clear Hard).

## Run from source

Requires **JDK 25**.

```bash
./gradlew :lwjgl3:run
```

## Build a distributable

```bash
./gradlew :lwjgl3:fatJar     # -> lwjgl3/build/libs/DogerDager.jar (self-contained)
```

`java -jar DogerDager.jar` runs anywhere with a JRE. CI (GitHub Actions, Windows) also
packages a `.exe` and attaches both to a Release on `v*` tags.

## Stack

libGDX 1.13.1 · LWJGL3 · Java 25 · Gradle (Kotlin DSL; multi-module `core` + `lwjgl3`).
Gamepad via gdx-controllers; bloom/glitch via a custom FBO post-processor.

## Contributing

Want to help? Open an issue. The wiki documents how the code works.

## Credits

Made by **unpuppyable**. Thanks to [@Kitty-Hivens](https://github.com/Kitty-Hivens) and **jackmann**.
