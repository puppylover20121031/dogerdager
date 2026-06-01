package com.unpuppyable.dogerdager;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;

/**
 * Single active gamepad, polled once per frame from the game loop and queried by
 * screens. Mirrors the keyboard: analog/dpad move, a held shield button, and
 * edge-detected face buttons (no gdx "justPressed" for pads, so we diff state).
 */
public final class Pad {

    private static final float DEAD = 0.30f;

    private static float mx;
    private static float my;
    private static boolean shield;
    private static boolean upE, downE, leftE, rightE, aE, bE, startE;
    private static boolean pUp, pDown, pLeft, pRight, pA, pB, pStart;
    private static boolean rE, lE, pR, pL;

    private Pad() {
    }

    public static void poll() {
        Controller c = Controllers.getCurrent();
        if (c == null) {
            mx = 0f;
            my = 0f;
            shield = false;
            upE = downE = leftE = rightE = aE = bE = startE = false;
            return;
        }
        ControllerMapping m = c.getMapping();
        float lx = dead(c.getAxis(m.axisLeftX));
        float ly = dead(c.getAxis(m.axisLeftY));
        boolean dUp = c.getButton(m.buttonDpadUp);
        boolean dDown = c.getButton(m.buttonDpadDown);
        boolean dLeft = c.getButton(m.buttonDpadLeft);
        boolean dRight = c.getButton(m.buttonDpadRight);

        mx = clamp(lx + (dRight ? 1f : 0f) - (dLeft ? 1f : 0f));
        my = clamp(-ly + (dUp ? 1f : 0f) - (dDown ? 1f : 0f));   // axis Y is +down; we want +up
        shield = c.getButton(m.buttonL1) || c.getButton(m.buttonR1);

        boolean up = dUp || ly < -0.5f;
        boolean down = dDown || ly > 0.5f;
        boolean left = dLeft || lx < -0.5f;
        boolean right = dRight || lx > 0.5f;
        boolean a = c.getButton(m.buttonA);
        boolean b = c.getButton(m.buttonB);
        boolean start = c.getButton(m.buttonStart);
        boolean L = c.getButton(m.buttonL1);
        boolean R = c.getButton(m.buttonR1);

        upE = up && !pUp;       pUp = up;
        downE = down && !pDown; pDown = down;
        leftE = left && !pLeft; pLeft = left;
        rightE = right && !pRight; pRight = right;
        aE = a && !pA;          pA = a;
        bE = b && !pB;          pB = b;
        startE = start && !pStart; pStart = start;
        lE = L && !pL; pL = L;
        rE = R && !pR; pR = R;
    }

    private static float dead(float v) {
        return Math.abs(v) < DEAD ? 0f : v;
    }

    private static float clamp(float v) {
        return Math.max(-1f, Math.min(1f, v));
    }

    public static float moveX() { return mx; }
    public static float moveY() { return my; }
    public static boolean shield() { return shield; }
    public static boolean justA() { return aE; }
    public static boolean justB() { return bE; }
    public static boolean justStart() { return startE; }
    public static boolean justUp() { return upE; }
    public static boolean justDown() { return downE; }
    public static boolean justLeft() { return leftE; }
    public static boolean justRight() { return rightE; }
    public static boolean justL() { return lE; }
    public static boolean justR() { return rE; }
}
