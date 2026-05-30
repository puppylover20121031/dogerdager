package game.gui;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import game.core.KeyInput;
import game.core.ArrowCode;
import game.core.Game;
import game.core.Handler;
import game.core.SaveManager;
import game.enums.ID;
import game.enums.STATE;
import game.object.BossArrow;
import game.object.GameObject;

import javax.imageio.ImageIO;

public class HUD {

    public static int HEALTH = 300;
    public static double STAMINA = 1200;
    private final SaveManager savemanager;
    public int endingTimer;

    private int greenValue = 255;
    private int score = 0;
    private int score2;
    private int level = 1;
    public int won = 0;
    Image img;
    public int endingStep = 0;       // tracks animation steps
    public static boolean showEnding = false; // true when ending animation should show
GameObject arrow;

    private Font font;

    public HUD() {
        this.savemanager = new SaveManager();
        score = this.savemanager.getHighScore();
    }

    public void render(Graphics g, STATE gameState, ArrowCode arrowcode) throws IOException {
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
        g.drawString("Score: " + score2, 10, 120);
        g.drawString("Level: " + level, 10, 140);
        g.drawString("Did you win?: " + (won > 0 ? "YES" : "NO"), 10, 160);
        g.drawString("Cooldown: " + arrowcode.timer, 10, 240);

        // Debug info
        if (KeyInput.debug) {
            g.drawString("DEBUG MODE", 10, 190);
        }

    }

    public void tick() {
        STAMINA = Game.clamp((float) STAMINA, 0, 1200);

        if (game.core.Game.gameState2 == game.enums.STATE2.HARDCORE) {
            HEALTH = (int) Game.clamp(HEALTH, 0, 10);
        } else {
            HEALTH = (int) Game.clamp(HEALTH, 0, 300);
        }

        greenValue = HEALTH * 2;
        score++;

        for (int i = 0; i < Handler.object.size(); i++) {
            if (Handler.object.get(i).getID() == ID.BossArrow) {
                arrow = Handler.object.get(i);
                break;
            }
        }
        if (arrow != null) {
            if (arrow.isHit()) {
                HEALTH -= 10;
                Handler.object.remove(arrow);
            }
        }
        // Example stamina depletion
        if (KeyInput.isCapsLockOn) {
            STAMINA = Math.max(0, STAMINA - 5);
        } else if (STAMINA < 1200) {
            STAMINA++;
        }

        // Player death
        if (HEALTH <= 0) {
            try {
                Thread.sleep(1500);
                System.exit(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setScore(int s) { score = s; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public void setLevel(int l) { level = l; }
    public int getHealth() { return HEALTH; }
    public void setScoreH(int s) { score2 = s; }
    public int getScoreH() { return score2; }


}
