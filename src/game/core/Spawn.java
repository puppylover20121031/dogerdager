package game.core;

import java.util.Random;
import game.gui.HUD;
import game.object.Enemy;
import game.object.FastEnemy;
import game.object.Boss1;
import game.object.SmartEnemy;
import game.object.GoodPotion;
import game.enums.ID;
import game.enums.STATE2;

public class Spawn {
    private Handler handler;
    private HUD hud;
    private int scoreKeep = 0;
    private final Random r = new Random();
    private int c = 0, c1 = 0, c2 = 0;

    public Spawn(Handler handler, HUD hud) {
        this.handler = handler;
        this.hud = hud;
    }

    public void tick() throws Exception {
        scoreKeep++;
        if (scoreKeep >= 200) {
            scoreKeep = 0;
            hud.setLevel(hud.getLevel() + 1);

            // Example enemy spawn logic
            if (hud.getLevel() == 5)
                handler.addObject(new FastEnemy(r.nextInt(620), r.nextInt(457), ID.fastenemy, handler));
            if (hud.getLevel() == 8 || hud.getLevel() == 6 || hud.getLevel() == 10)
                handler.addObject(new Enemy(r.nextInt(620), r.nextInt(457), ID.Enemy, handler));
            if (hud.getLevel() == 12) {
                handler.clearEnemy();
                handler.addObject(new Boss1(272, -120, ID.Enemy, handler));
            }
            if (hud.getLevel() == 60 && Game.gameState2 == STATE2.EASY) {
                ending(hud);
            }
            if (hud.getLevel() == 120 && Game.gameState2 == STATE2.NORMAL) {
                ending(hud);
            }
            if (hud.getLevel() == 200 && Game.gameState2 == STATE2.HARD) {
                ending(hud);
            }

            // Level 26 background music switch
            if (hud.getLevel() == 26) {
                handler.clearEnemy();
                AudioPlayer.loadSound("bgm", "res/song2.wav");
                AudioPlayer.playSound("bgm");
            }
            c++; c1++; c2++;

            // Every 4 levels, increment ending step
            if (c == 2 && hud.won == 1) {
                hud.endingStep++;
                c = 0;
            }

            if (c == 2) { handler.addObject(new GoodPotion(r.nextInt(590), r.nextInt(427), ID.goodPotion, handler)); c = 0; }
            if ((hud.getLevel() >= 20 && c2 == 25) && hud.won == 0) {
                handler.addObject(new Enemy(r.nextInt(620), r.nextInt(457), ID.enemy, handler));
                c2 = 0;
            }
        }
    }

    public static void ending(HUD hud) {
        hud.won = 1;
        //hud.showEnding = true;               sorry doesnt work...
        KeyInput.nopedamage = true;
        AudioPlayer.stopSound("bgm");
        AudioPlayer.loadSound("bgm2", "res/puppysong.wav");
        AudioPlayer.stopSound("bgm2");
        AudioPlayer.playSound("bgm2");
        Handler.clearEnemy();
    }
}
