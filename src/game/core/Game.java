package game.core;

import java.awt.Canvas;
import java.awt.Color;
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
    private final SaveManager savemanager;

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


    public Game() {
        this.handler = new Handler();
        this.hud = new HUD();
        this.spawner = new Spawn(this.handler, this.hud);

        new game.devchat();
        this.menu = new Menu(this, this.handler);
        this.menu2 = new Menu2(this, this.handler);

        this.savemanager = new SaveManager();

        AudioPlayer.loadSound("bgm", "res/song.wav");
        AudioPlayer.playSound("bgm");

        addKeyListener(new KeyInput(this.handler, this.hud));
        addMouseListener(this.menu);
        addMouseListener(this.menu2);

        if (gameState2 != STATE2.NOPE) {
            removeMouseListener(this.menu2);
        }
        if (gameState != STATE.GAME) {
            removeMouseListener(this.menu);
        }
        hud.setScore(this.savemanager.getHighScore());

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
        this.savemanager.setHighScore(hud.getScore());
        if (this.savemanager.getHighScore() < hud.getScore()) {
            this.savemanager.save();
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


    public static float clamp(float value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void main(String[] args) {
        new Game();
    }
}
