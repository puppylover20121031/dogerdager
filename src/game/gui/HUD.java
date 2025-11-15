package game.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import game.core.KeyInput;
import game.core.Game;
import game.core.Handler;
import game.enums.STATE;

public class HUD {

    public static int HEALTH = 300;
    public static double STAMINA = 1200;
    public int endingTimer;

    private int greenValue = 255;
    private int score = 0;
    private int level = 1;
    public int won = 0;

    public int endingStep = 0;       // tracks animation steps
    public static boolean showEnding = false; // true when ending animation should show

    private Font font;

    public void render(Graphics g, STATE gameState) {
        // Draw health bar
        greenValue = (int) Game.clamp(greenValue, 0, 255);
        g.setColor(Color.gray);
        g.fillRect(15, 15, 600, 32);
        g.setColor(new Color(0, 255, 0));
        g.fillRect(15, 15, HEALTH * 2, 32);
        g.setColor(Color.white);
        g.drawRect(15, 15, 600, 32);

        // Draw stamina bar
        g.setColor(Color.gray);
        g.fillRect(15, 75, 600, 32);
        g.setColor(new Color(75, greenValue, 0));
        g.fillRect(15, 75, (int) STAMINA / 2, 32);
        g.setColor(Color.white);
        g.drawRect(15, 75, 600, 32);

        // Draw score and level
        g.drawString("Score: " + score, 10, 120);
        g.drawString("Level: " + level, 10, 140);
        g.drawString("Did you win?: " + (won > 0 ? "YES" : "NO"), 10, 160);

        // Debug info
        if (KeyInput.debug) {
            g.drawString("DEBUG MODE", 10, 190);
        }

        // Ending animation
        if (showEnding) {
            drawEndingAnimation(g);
        }
    }

    public void tick() {
        HEALTH = (int) Game.clamp(HEALTH, 0, 300);
        STAMINA = Game.clamp((float) STAMINA, 0, 1200);

        greenValue = HEALTH * 2;
        score++;

        // Example stamina depletion
        if (KeyInput.isCapsLockOn) {
            STAMINA = Math.max(0, STAMINA - 5);
        } else if (STAMINA < 1200) {
            STAMINA++;
        }

        // Player death
        if (HEALTH <= 0) {
            System.exit(0);
        }
    }

    public void setScore(int s) { score = s; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public void setLevel(int l) { level = l; }
    public int getHealth() { return HEALTH; }

    public void drawEndingAnimation(Graphics g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(Color.white);

        int step = endingStep;

        switch (step) {
            case 0 -> g.drawString("The war between Bob the puppy and Bob the builder...", 40, 240);
            case 1 -> {
                drawASCII(g, ASCII_BOB, 60, 100);
                g.drawString("There was a puppy named Bob.", 200, 360);
                g.drawString("Bob loved tacos!", 200, 385);
            }
            case 2 -> g.drawString("Bob loved his friend Jayden even more!", 100, 240);
            case 3 -> {
                drawASCII(g, ASCII_BOBBUILDER, 50, 100);
                g.drawString("One day Bob saw 'Bob the Builder' on TV...", 100, 360);
            }
            case 4 -> g.drawString("Bob hated that show because it was named after him.", 80, 240);
            case 5 -> g.drawString("Bob went to Keith Chapman's house to ask why.", 80, 240);
            case 6 -> {
                g.drawString("Keith: 'None of your business.'", 100, 240);
                g.drawString("Bob thought: 'Why did he say that?'", 100, 270);
            }
            case 7 -> g.drawString("Bob went on a journey to destroy 'Bob the Builder'!", 50, 240);
            case 8 -> g.drawString("To be continued...", 200, 240);
            default -> g.drawString("The End", 250, 240);
        }
    }

    private void drawASCII(Graphics g, String[] ascii, int x, int y) {
        for (int i = 0; i < ascii.length; i++) {
            g.drawString(ascii[i], x, y + i * 18);
        }
    }

    private final String[] ASCII_BOB = {
            "  /\\_/\\",
            " ( o.o )",
            "  > ^ <"
    };

    private final String[] ASCII_BOBBUILDER = {
            "   ____",
            "  |____|",
            "  | || |",
            "   ||||"
    };
}
