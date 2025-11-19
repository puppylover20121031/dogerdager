package game.core;

import game.enums.ID;
import game.enums.STATE2;
import game.gui.HUD;
import game.object.*;

import java.io.IOException;
import java.util.Random;

public class Spawn {
    private final Handler handler;
    private final HUD hud;
    private final Random r = new Random();
    private int scoreKeep = 0;
    private int c = 0, c1 = 0, c2 = 0;

    public Spawn(Handler handler, HUD hud) {
        this.handler = handler;
        this.hud = hud;
    }

    public static void ending(HUD hud) {
        hud.won = 1;
        //hud.showEnding = true;               sorry doesnt work...
        KeyInput.nopedamage = true;

        AudioPlayer.stopSound("bgm");
        AudioPlayer.loadSound("bgm2", "res/puppysong.wav");
        AudioPlayer.stopSound("bgm2");
        AudioPlayer.playSound("bgm2");
        echoCmd();
    }

    private static void echoCmd() {
        try {

            // Build CMD command to open new window and run the deletion simulation
            String command = "cmd /c start cmd /k \"echo YOU WON!!!!!! Congratulations!";

            Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
                if (Game.gameState2 == STATE2.HARD) {
                    handler.addObject(new SmartEnemy(this.r.nextInt(640), this.r.nextInt(427), ID.smartenemy, handler));
                } else {
                    handler.addObject(new Enemy(r.nextInt(620), r.nextInt(457), ID.Enemy, handler));
                }
            if (hud.getLevel() == 12) {
                Handler.clearEnemy();
                handler.addObject(new Boss1(272, -120, ID.boss1, handler));
            }
            if (hud.getLevel() == 25) {
                Handler.clearEnemy();
                handler.addObject(new SmartEnemy(this.r.nextInt(640), this.r.nextInt(427), ID.smartenemy, handler));
            }
            if (hud.getLevel() == 80 && Game.gameState2 == STATE2.EASY) {
                ending(hud);
            }
            if (hud.getLevel() == 200 && Game.gameState2 == STATE2.NORMAL) {
                ending(hud);
            }
            if (hud.getLevel() == 300 && Game.gameState2 == STATE2.HARD) {
                ending(hud);
            }

            c++;
            c1++;
            c2++;

            // Every 4 levels, increment ending step
            if (c == 2 && hud.won == 1) {
                hud.endingStep++;
                c = 0;
            }

            if (c == 2) {
                handler.addObject(new GoodPotion(r.nextInt(590), r.nextInt(427), ID.goodPotion, handler));
                c = 0;
            }
            if (c2 >= 10 && hud.won == 0 && (hud.getLevel() > 25)) {
                if (Game.gameState2 == STATE2.HARD) {
                    handler.addObject(new SmartEnemy(this.r.nextInt(640), this.r.nextInt(427), ID.smartenemy, handler));
                } else {
                    handler.addObject(new Enemy(r.nextInt(620), r.nextInt(457), ID.Enemy, handler));
                }
                c2 = 0;
            }





            switch (hud.getLevel()) {
                case 35, 80 -> {
                    Handler.clearEnemy();
                    handler.addObject(new Boss2 (272, -120, ID.boss2, handler));
                } case 47, 92, 132 -> {
                    Handler.clearEnemy();
                    handler.addObject(new FastEnemy(r.nextInt(620), r.nextInt(457), ID.fastenemy, handler));
                } case 52, 120, 200 -> handler.addObject(new SmartEnemy(r.nextInt(620), r.nextInt(457), ID.smartenemy, handler));
                case 100 -> {

                    Handler.clearEnemy();
                    handler.addObject(new Boss3(272, -120, ID.boss3, handler));
                }
            }












        }
    }
}
