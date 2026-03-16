package game.core;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.Music;
import org.newdawn.slick.Sound;
import game.enums.STATE;
import game.enums.STATE2;
import game.gui.HUD;
import game.gui.Menu;
import game.gui.Menu2;
import game.gui.Menu3;
import game.gui.Window;

import javax.swing.*;

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
    private final Menu3 menu3;
    public static Graphics g2;
    private Graphics g;

    public static Map<String, Sound> soundMap = new HashMap<>();
    public static Map<String, Music> musicMap = new HashMap<>();

    private int frames = 0;

    public static STATE gameState = STATE.MENU2;
    public static STATE2 gameState2 = STATE2.NOPE;

    public static void checkItsTimeFile() {
        Path path = Path.of(System.getProperty("user.home"), "Downloads", "its_time.txt");

        if (Files.exists(path)) {
            System.out.println("File detected!");

            // DO SOMETHING HERE
            String command = "cmd /c start cmd /k \"echo oh you came back?";

            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            command = "cmd /c start cmd /k \"echo thanks for playing again!";

            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            System.out.println("File not found.");
        }
    }

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
        this.menu3 = new Menu3(this, this.handler);
        this.savemanager = new SaveManager();

        AudioPlayer.loadSound("bgm", "res/song.wav");
        AudioPlayer.loopSound("bgm");
        AudioPlayer.playSound("bgm");

        addKeyListener(new KeyInput(this.handler, this.hud));
        addMouseListener(this.menu);
        addMouseListener(this.menu2);
        AudioPlayer.loadSound("fail", "res/losing.wav");

        if (gameState2 != STATE2.NOPE) {
            removeMouseListener(this.menu2);
        }
        if (gameState != STATE.MENU2) {
            removeMouseListener(this.menu2);
        }
        if (gameState != STATE.MENU) {
            removeMouseListener(this.menu);
        }
        hud.setScore(this.savemanager.getHighScore());

        new Window(WIDTH, HEIGHT, "the doger dager", this);
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
        checkItsTimeFile();
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

            if (running) {
                try {
                    render();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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

        if (gameState == STATE.GAME) {
            spawner.tick();
            removeMouseListener(menu2);
            removeMouseListener(menu3);
        } else if (gameState == STATE.MENU) {
            menu.tick();
        } else if (gameState == STATE.MENU2) {
            removeMouseListener(menu3);
            addMouseListener(this.menu2);
            menu2.tick();
        } else if (gameState == STATE.MENU3) {
            addMouseListener(this.menu3);
            removeMouseListener(menu2);
            menu3.tick();
        }

        if (hud.getHealth() <= 6) {
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

    private void render() throws IOException {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }


        g = bs.getDrawGraphics();
        g2 = g;

        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH, HEIGHT);


        if (gameState == STATE.GAME) {
            handler.render(g);
            hud.render(g, gameState);
        } else if (gameState == STATE.MENU) {
            menu.render(g);
        } else if (gameState == STATE.MENU2) {
            Menu2.render(g);
        } else if (gameState == STATE.MENU3) {
            Menu3.render(g);
        }

        g.dispose();
        bs.show();
    }



    public static float clamp(float value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void main(String[] args) {

        new Game();
    }
}
