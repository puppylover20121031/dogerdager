package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import java.util.Locale;

/** Persisted player key bindings. */
public final class KeyBind {

    public enum Action {
        MOVE_UP,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        STRAFE,
        SHOOT,
        PAUSE
    }

    private final Preferences prefs;

    public KeyBind() {
        prefs = Gdx.app != null ? Gdx.app.getPreferences("doger-dager-keybinds") : null;
    }

    public int get(Action action) {
        if (prefs == null) {
            return defaultKeyCode(action);
        }
        return prefs.getInteger(prefName(action), defaultKeyCode(action));
    }

    public void set(Action action, int keyCode) {
        if (prefs == null) {
            return;
        }
        prefs.putInteger(prefName(action), keyCode);
        prefs.flush();
    }

    public boolean isPressed(Action action) {
        return Gdx.input.isKeyPressed(get(action));
    }

    public boolean isJustPressed(Action action) {
        return Gdx.input.isKeyJustPressed(get(action));
    }

    public String name(Action action) {
        return keyName(get(action));
    }

    public static int defaultKeyCode(Action action) {
        return switch (action) {
            case MOVE_UP -> Input.Keys.W;
            case MOVE_DOWN -> Input.Keys.S;
            case MOVE_LEFT -> Input.Keys.A;
            case MOVE_RIGHT -> Input.Keys.D;
            case STRAFE -> Input.Keys.TAB;
            case PAUSE -> Input.Keys.ESCAPE;
            case SHOOT -> Input.Keys.SPACE;
        };
    }

    public static String keyName(int keyCode) {
        return switch (keyCode) {
            case Input.Keys.UNKNOWN -> "Unknown";
            case Input.Keys.SPACE -> "Space";
            case Input.Keys.ESCAPE -> "Escape";
            case Input.Keys.UP -> "Up";
            case Input.Keys.DOWN -> "Down";
            case Input.Keys.LEFT -> "Left";
            case Input.Keys.RIGHT -> "Right";
            case Input.Keys.SHIFT_LEFT -> "Shift";
            case Input.Keys.CONTROL_LEFT -> "Ctrl";
            case Input.Keys.ALT_LEFT -> "Alt";
            case Input.Keys.TAB -> "Tab";
            default -> Input.Keys.toString(keyCode);
        };
    }

    private String prefName(Action action) {
        return "bind." + action.name().toLowerCase(Locale.ROOT);
    }
}
