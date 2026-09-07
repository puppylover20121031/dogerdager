package com.unpuppyable.dogerdager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A global, screen-independent error toast. Call {@link #show(String)} from anywhere -
 * network code, game logic, any Screen - and the message is drawn on top of whatever is
 * currently on screen for a few seconds, then fades out.
 * <p>
 * It draws itself once per frame from {@link DogerDager#render()}, after the post-processor,
 * which is the only point in the app that runs regardless of which Screen is active - so a
 * message shown right before a screen switch still gets seen, unlike a Screen's own drawing.
 */
public class ErrorNotifier {

    private static final float DURATION_SECONDS = 3f;
    private static final float FADE_SECONDS = 0.5f;
    private static final float TOP_MARGIN = 40f;
    private static final float LINE_GAP = 28f;

    private static final class Toast {
        final String message;
        float remaining;
        Toast(String message, float remaining) {
            this.message = message;
            this.remaining = remaining;
        }
    }

    private static final List<Toast> active = new ArrayList<>();
    private static SpriteBatch batch;
    private static BitmapFont font;
    private static GlyphLayout layout;

    private ErrorNotifier() {}

    /** Safe to call from any thread (e.g. a websocket callback), not just the render thread. */
    public static void show(String message) {
        Gdx.app.postRunnable(() -> active.add(new Toast(message, DURATION_SECONDS)));
    }

    /** Called once per frame from DogerDager.render(). Not meant to be called from a Screen. */
    static void render(float delta) {
        if (active.isEmpty()) return;
        if (batch == null) {
            batch = new SpriteBatch();
            font = new BitmapFont();
            font.getData().setScale(1.5f);
            layout = new GlyphLayout();
        }

        float screenW = Gdx.graphics.getWidth();
        float y = Gdx.graphics.getHeight() - TOP_MARGIN;

        batch.begin();
        Iterator<Toast> it = active.iterator();
        while (it.hasNext()) {
            Toast toast = it.next();
            toast.remaining -= delta;
            if (toast.remaining <= 0f) {
                it.remove();
                continue;
            }
            float alpha = Math.min(1f, toast.remaining / FADE_SECONDS);
            layout.setText(font, toast.message);
            font.setColor(1f, 1f, 1f, alpha);
            font.draw(batch, toast.message, (screenW - layout.width) / 2f, y);
            y -= LINE_GAP;
        }
        batch.end();
    }

    static void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        active.clear();
    }
}
