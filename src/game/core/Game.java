package game.core;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.Music;
import org.newdawn.slick.Sound;

import game.enums.STATE;
import game.enums.STATE2;
import game.gui.HUD;
import game.gui.Menu;
import game.gui.Menu2;
import game.gui.Window;

public class Game extends Canvas implements Runnable {

    static final int WIDTH = 640;
    static final int HEIGHT = 477;

    private Thread thread;
    private boolean running = false;

    private final HUD hud;
    private final Spawn spawner;
    private final Handler handler;

    private final Menu menu;
    private final Menu2 menu2;

    public static Graphics g2;
    private Graphics g;

    public static Map<String, Sound> soundMap = new HashMap<>();
    public static Map<String, Music> musicMap = new HashMap<>();

    private int frames = 0;

    public static STATE gameState = STATE.MENU2;
    public static STATE2 gameState2 = STATE2.NOPE;

    boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean()
            .getInputArguments().toString().contains("-agentlib:jdwp");

    @Serial
    private static final long serialVersionUID = -3462486173394796704L;

    // ASCII ART ARRAYS ---------------------------------------------------------

    private final String[] ASCII_BOB = {
            "        /^-----^\\",
            "       V  o o  V",
            "        |  Y  |",
            "         \\ Q /",
            "         / - \\",
            "         |    \\",
            "         |     \\     )",
            "         || (___\\===="
    };

    private final String[] ASCII_BUILDER = {
            "     _____",
            "   _|[_]|_",
            "  (  °  ° )",
            "   |  ^  |",
            "  /| --- |\\",
            " /_|_____|_\\",
            "    | | |"
    };

    private final String[] ASCII_BANNED = {
            "  ###########",
            "  #  BANNED #",
            "  ###########"
    };

    private final String[] ASCII_PORTAL = {
            "     @@@@@@@@",
            "   @@////////@@",
            "  @//  @@    //@",
            " @//   @@     //@",
            " @//@       //@@",
            "  @//@@   ////@",
            "   @@////////@@",
            "     @@@@@@@@"
    };

    private final String[] ASCII_WATER = {
            "     ~ ~ ~ ~ ~ ~ ~ ~",
            "   ~~~~~~~~~~~~~~~~~~~",
            " ~~~~~~~~   ~~~~~~~~~~~",
            "   ~~~~~  SPLASH ~~~~~",
            " ~~~~~~~~~~~~~~~~~~~~~~"
    };

    private final String[] ASCII_SLEEP = {
            "     |\\__/|",
            "     (- ω -)",
            "     (\")_(\")",
            "",
            "       Z",
            "        Z",
            "         Z"
    };

    // -------------------------------------------------------------------------

    public Game() {
        this.handler = new Handler();
        this.hud = new HUD();
        this.spawner = new Spawn(this.handler, this.hud);

        new game.devchat();
        this.menu = new Menu(this, this.handler);
        this.menu2 = new Menu2(this, this.handler);

        AudioPlayer.loadSound("bgm", "res/song.wav");
        AudioPlayer.playSound("bgm");

        addKeyListener(new KeyInput(this.handler, this.hud));
        addMouseListener(this.menu);
        addMouseListener(this.menu2);

        new Window(WIDTH, HEIGHT, "the doger dager", this);
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch (Exception ignored) {}
    }

    // RUN LOOP ----------------------------------------------------------------

    @Override
    public void run() {
        requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1_000_000_000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                try {
                    tick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                delta--;
            }

            if (running) render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frames = 0;
            }
        }
        stop();
    }

    private void tick() throws Exception {
        handler.tick();
        hud.tick();
        if (hud.getLevel() >= 115 && Game.gameState2 == STATE2.EASY) {
            Spawn.ending(hud);
        }

        if (hud.getScore() > 0 && hud.getScore() % 100 == 0) {
            AudioPlayer.playSound("treasure");
        }

        if (gameState == STATE.GAME) {
            spawner.tick();
            removeMouseListener(menu2);
        } else if (gameState == STATE.MENU) {
            menu.tick();
        } else if (gameState == STATE.MENU2) {
            menu2.tick();
        }

        if (hud.getHealth() <= 0) {
            AudioPlayer.playSound("fail");
        }

        if (isDebug) {
            KeyInput.debug = true;
        }
    }

    // RENDER ------------------------------------------------------------------

    private void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }if (HUD.showEnding) {
            hud.drawEndingAnimation(g);
        }


        g = bs.getDrawGraphics();
        g2 = g;

        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // ENDING ANIMATION OVERRIDE
        if (hud.won == 1) {
            drawEndingAnimation(g);
            g.dispose();
            bs.show();
            return;
        }

        if (gameState == STATE.GAME) {
            handler.render(g);
            hud.render(g, gameState);
        } else if (gameState == STATE.MENU) {
            menu.render(g);
        } else if (gameState == STATE.MENU2) {
            menu2.render(g);
        }

        g.dispose();
        bs.show();
    }

    // ASCII ENDING -------------------------------------------------------------

    private void drawASCII(Graphics g, String[] arr, int x, int y) {
        for (int i = 0; i < arr.length; i++) {
            g.drawString(arr[i], x, y + i * 18);
        }
    }

    private void drawEndingAnimation(Graphics g) {
        int t = hud.endingTimer;
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(Color.white);

        // 0–60 : text intro
        if (t < 60) {
            g.drawString("The war between Bob the puppy and Bob the builder...", 40, 240);
        }
        // Bob appears
        else if (t < 180) {
            drawASCII(g, ASCII_BOB, 60, 100);
            g.drawString("There was a puppy named Bob.", 200, 360);
            g.drawString("Bob loved tacos!", 200, 385);
        }
        // Builder appears
        else if (t < 300) {
            drawASCII(g, ASCII_BUILDER, 350, 100);
            g.drawString("But Bob hated Bob the builder!", 80, 360);
        }
        // BANNED stamp
        else if (t < 360) {
            drawASCII(g, ASCII_BUILDER, 350, 100);
            drawASCII(g, ASCII_BANNED, 330, 230);
        }
        // Portal
        else if (t < 480) {
            drawASCII(g, ASCII_PORTAL, 200, 120);
            g.drawString("Bob entered the portal to Bobsville...", 120, 360);
        }        // Slip
        else if (t < 600) {
            drawASCII(g, ASCII_BUILDER, 350, 120);
            drawASCII(g, ASCII_WATER, 40, 300);
            g.drawString("Splash! Bob the builder slipped!", 120, 360);
        }
        // Final sleep scene
        else {
            drawASCII(g, ASCII_SLEEP, 260, 150);
            g.drawString("Bob woke up... it was all just a dream.", 140, 360);
            g.drawString("THE END", 260, 420);
        }
    }

    // UTILITY -----------------------------------------------------------------

    public static float clamp(float value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void main(String[] args) {
        new Game();
    }
}
