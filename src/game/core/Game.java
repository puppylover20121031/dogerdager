package game.core;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileWriter;
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
  private Thread thread;
  private boolean running = false;
  private final HUD hud;
  public Graphics g;
  int frames = 0;
  public static Graphics g2;
  private final Spawn spawner;
  private final Handler handler;

  private final Menu menu;
  public Menu2 menu2;

  public static STATE gameState = STATE.MENU2;
  public static STATE2 gameState2 = STATE2.NOPE;

  
  public Game() {
    this.handler = new Handler();
    this.menu = new Menu(this, this.handler); // this is the menu that needs to goto menu2.
    if (gameState == STATE.MENU2) {
        menu2 = new Menu2(this, this.handler);
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

    private static boolean PlayMusic = true;

    public static Map<String, Sound> soundMap = new HashMap<>();
    public static Map<String, Music> musicMap = new HashMap<>();

    private int frames = 0;

    public static STATE gameState = STATE.MENU2;
    public static STATE2 gameState2 = STATE2.NOPE;

public static boolean askYesNo(String message, String title) {
        // A parent frame is optional; using null centers on screen
        int result = JOptionPane.showConfirmDialog(
                null,                  // parent component
                message,               // message
                title,                 // dialog title
                JOptionPane.YES_NO_OPTION,      // option type [InlineCitation-1-Java JOptionPane - GeeksforGeeks](https://www.geeksforgeeks.org/java/java-joptionpane/) [InlineCitation-3-JOptionPane (Java SE 22 & JDK 22)](https://docs.oracle.com/en/java/javase/22/docs/api/java.desktop/javax/swing/JOptionPane.html)
                JOptionPane.QUESTION_MESSAGE    // message type
        );

        // Validate result: YES_OPTION = 0, NO_OPTION = 1 [InlineCitation-2-How to Create Pop Window in Java | Delft Stack](https://www.delftstack.com/howto/java/java-pop-up-window/)
        return result == JOptionPane.YES_OPTION;
    }

    public static void checkItsTimeFile() {
        Path path = Path.of(System.getProperty("user.home"), "Desktop", "its_time.txt");
        Path path2 = Path.of(System.getProperty("user.home"), "OneDrive", "Desktop", "its_time.txt");

        if (Files.exists(path) | Files.exists(path2)) {
            System.out.println("File detected!");

            String command = "cmd /c start cmd /k \"echo oh you came back?";

            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
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
    public SaveManager2 savemanager2;

    @Serial
    private static final long serialVersionUID = -3462486173394796704L;



    public Game() {
        this.savemanager = new SaveManager();
        this.savemanager2 = new SaveManager2();
        this.handler = new Handler();
        this.hud = new HUD();
        this.spawner = new Spawn(this.handler, this.hud, savemanager2);
        new game.devchat();
        this.menu = new Menu(this, this.handler);
        this.menu2 = new Menu2(this, this.handler, savemanager2);
        this.menu3 = new Menu3(this, this.handler, savemanager2);


        if (PlayMusic) {

            AudioPlayer.loadSound("bgm", "res/song.wav");
            AudioPlayer.loopSound("bgm");
            AudioPlayer.playSound("bgm");

        }

        addKeyListener(new KeyInput(this.handler, this.hud));
        addMouseListener(this.menu);
        addMouseListener(this.menu2);
        addMouseListener(this.menu3);
        AudioPlayer.loadSound("fail", "res/losing.wav");

        hud.setScore(this.savemanager.getHighScore());

        new Window(WIDTH, HEIGHT, "the doger dager", this);
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
        if (savemanager2.getBool()) {
            checkItsTimeFile();
        }
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


        if (gameState2 != STATE2.NOPE) {
            removeMouseListener(this.menu2);
        }
        if (gameState != STATE.MENU2) {
            removeMouseListener(this.menu2);
        }else {
            addMouseListener(this.menu2);
        }
        if (gameState != STATE.MENU) {
            removeMouseListener(this.menu);
        } else {
            addMouseListener(this.menu);
        }
        if (gameState != STATE.MENU2) {
            removeMouseListener(this.menu2);
        } else {
            addMouseListener(this.menu2);
        }


        if (hud.getHealth() == 0) {
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
