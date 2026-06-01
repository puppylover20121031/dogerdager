package com.unpuppyable.dogerdager.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Segmented chaser built purely from primitives. The head homes at the player
// with a capped turn rate (so it stays dodgeable); the body trails it at a fixed
// spacing. The whole chain is a contact hazard -- with no player weapon yet there
// is no cutting, that hook waits for an offense mechanic.
public final class Centipede extends Entity {

    private static final int SEGMENTS = 11;
    private static final float SPACING = 13f;
    private static final float HEAD_R = 9f;
    private static final float TAIL_R = 5f;
    private static final float SPEED = 130f;
    private static final float TURN = 2.6f;
    private static final float LIFE = 14f;

    private static final Color HEAD = Color.SCARLET;
    private static final Color BAND_A = new Color(0.88f, 0.42f, 0.12f, 1f);
    private static final Color BAND_B = new Color(0.55f, 0.20f, 0.06f, 1f);
    private static final Color LEG = new Color(0.32f, 0.12f, 0.05f, 1f);

    private final Vector2[] seg = new Vector2[SEGMENTS];
    private final Player target;
    private final float arenaW;
    private final float playTop;
    private float heading;
    private float anim;
    private float life = LIFE;

    public Centipede(float x, float y, float arenaW, float playTop, Player target) {
        super(x - HEAD_R, y - HEAD_R, HEAD_R * 2);
        this.target = target;
        this.arenaW = arenaW;
        this.playTop = playTop;
        this.heading = MathUtils.random(MathUtils.PI2);
        for (int i = 0; i < SEGMENTS; i++) {
            seg[i] = new Vector2(x - i * SPACING, y);
        }
    }

    private float radius(int i) {
        return MathUtils.lerp(HEAD_R, TAIL_R, i / (float) (SEGMENTS - 1));
    }

    @Override
    public int contactDamage() {
        return 1;
    }

    @Override
    public void update(float delta) {
        anim += delta;
        life -= delta;
        if (life <= 0) {
            dead = true;
            return;
        }

        Vector2 head = seg[0];
        float want = MathUtils.atan2(target.bounds().y - head.y, target.bounds().x - head.x);
        heading += MathUtils.clamp(deltaAngle(heading, want), -TURN * delta, TURN * delta);

        head.x += MathUtils.cos(heading) * SPEED * delta;
        head.y += MathUtils.sin(heading) * SPEED * delta;

        // soft-bounce the head off the arena walls so it never crawls off-screen
        if (head.x < HEAD_R) {
            head.x = HEAD_R;
            heading = MathUtils.PI - heading;
        } else if (head.x > arenaW - HEAD_R) {
            head.x = arenaW - HEAD_R;
            heading = MathUtils.PI - heading;
        }
        if (head.y < HEAD_R) {
            head.y = HEAD_R;
            heading = -heading;
        } else if (head.y > playTop - HEAD_R) {
            head.y = playTop - HEAD_R;
            heading = -heading;
        }

        // each segment is pulled to sit exactly SPACING behind the one ahead
        for (int i = 1; i < SEGMENTS; i++) {
            Vector2 prev = seg[i - 1];
            Vector2 cur = seg[i];
            float dx = cur.x - prev.x;
            float dy = cur.y - prev.y;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d > 0.0001f) {
                cur.x = prev.x + dx / d * SPACING;
                cur.y = prev.y + dy / d * SPACING;
            }
        }
        bounds.setPosition(head.x - HEAD_R, head.y - HEAD_R);
    }

    private static float deltaAngle(float from, float to) {
        float d = to - from;
        while (d > MathUtils.PI) d -= MathUtils.PI2;
        while (d < -MathUtils.PI) d += MathUtils.PI2;
        return d;
    }

    // the footprint is the union of all segment circles, not one rectangle
    @Override
    public boolean hits(Rectangle other) {
        for (int i = 0; i < SEGMENTS; i++) {
            float r = radius(i);
            Vector2 s = seg[i];
            if (other.x < s.x + r && other.x + other.width > s.x - r
                    && other.y < s.y + r && other.y + other.height > s.y - r) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(ShapeRenderer shapes) {
        // legs: a wiggling pair per body segment, perpendicular to the spine
        shapes.setColor(LEG);
        for (int i = 1; i < SEGMENTS; i++) {
            Vector2 a = seg[i - 1];
            Vector2 b = seg[i];
            float ang = MathUtils.atan2(b.y - a.y, b.x - a.x);
            float nx = -MathUtils.sin(ang);
            float ny = MathUtils.cos(ang);
            float legLen = radius(i) + 4f + MathUtils.sin(anim * 14 + i) * 3f;
            shapes.rectLine(b.x, b.y, b.x + nx * legLen, b.y + ny * legLen, 1.6f);
            shapes.rectLine(b.x, b.y, b.x - nx * legLen, b.y - ny * legLen, 1.6f);
        }

        // body: drawn tail-first so the head sits on top
        for (int i = SEGMENTS - 1; i >= 0; i--) {
            shapes.setColor(i == 0 ? HEAD : (i % 2 == 0 ? BAND_A : BAND_B));
            shapes.circle(seg[i].x, seg[i].y, radius(i));
        }

        // head: forward antennae + a pair of eyes
        Vector2 h = seg[0];
        float fx = MathUtils.cos(heading);
        float fy = MathUtils.sin(heading);
        float sx = -fy;
        float sy = fx;
        shapes.setColor(HEAD);
        shapes.rectLine(h.x, h.y, h.x + (fx + sx * 0.5f) * 13f, h.y + (fy + sy * 0.5f) * 13f, 1.4f);
        shapes.rectLine(h.x, h.y, h.x + (fx - sx * 0.5f) * 13f, h.y + (fy - sy * 0.5f) * 13f, 1.4f);
        shapes.setColor(Color.BLACK);
        shapes.circle(h.x + fx * 3f + sx * 3f, h.y + fy * 3f + sy * 3f, 1.8f);
        shapes.circle(h.x + fx * 3f - sx * 3f, h.y + fy * 3f - sy * 3f, 1.8f);
    }
}
