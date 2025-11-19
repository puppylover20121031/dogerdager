package game.core;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import game.enums.ID;
import game.gui.HUD;
import game.object.*;

import static game.core.Spawn.*;

public class KeyInput extends KeyAdapter {
  public static boolean dashing = false;
  private final Handler handler2;
  public static boolean debug = false;
    private final HUD hud1;
    private int c;
  private int pass1 = 0;
  private final Random r;
  public static boolean nopedamage = false;
  public static boolean LORE = false;
  public static boolean isCapsLockOn = false;
  public static boolean isCapsLockOn2 = true;
  public int booltoint(boolean convbool) {
    if (convbool) {
      return 1;
    }
    return 0;
  }
  
  public KeyInput(Handler handler1, HUD hud) { this.c = 0;
    this.r = new Random();
    this.handler2 = handler1;
    this.hud1 = hud;
  }

    public static boolean nodamage = false;
   int c1 = 0;
  public void keyPressed(KeyEvent keyEvent) { // conditions for key input
    int key = keyEvent.getKeyCode();
    for (int i = 0; i < Handler.object.size(); i++) {
      GameObject tempObject = Handler.object.get(i);
      
      if (tempObject.getID() == ID.Player) {
        boolean pressw = (key == 87 || key == 38);
        boolean presss = (key == 83 || key == 40);
        boolean pressa = (key == 65 || key == 37);
        boolean pressd = (key == 68 || key == 39);
        if (debug) {
        System.out.println(key);
        }
        if (pressw)
          tempObject.setvelY((-5 * booltoint(pressw)));
        if (presss)
          tempObject.setvelY((5 * booltoint(presss))); 
        if (pressa)
          tempObject.setvelX((-5 * booltoint(pressa))); 
        if (pressd) {
          tempObject.setvelX((5 * booltoint(pressd)));
        }
        
        if (key == 27)
          System.exit(0); 
        if (key == 192)
          debug = true;
        if (key == 49 && 
          debug) {
          tempObject.setX(0.0F);
          tempObject.setY(0.0F);
        }
        if (key == 17) {
            AudioPlayer.stopSound("bgm");
        }
        if (key == 50 && 
                debug) {
          tempObject.setX(500.0F);
          tempObject.setY(500.0F);
        } 
        if (key == 78 && 
                debug) {
        	nodamage = true;
        }
        if (key == 51 && 
          debug) {
          try {
            Player.damage(5);
          } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
          }
        }

          if (key == 77 && debug) {
              System.exit(1);
          }

          if (key == 16 && debug) {
              game.core.Spawn.ending(this.hud1);
          }
          if (key == 98 && debug) {
              Handler.clearEnemy();
              this.handler2.addObject(new Boss3(272, -120, ID.boss3, handler2));
          }

        if (key == 36) {
        	debug = true;
        }
		if (key == 76) {
            LORE = !LORE;
		}
        if (HUD.STAMINA <= 3) {
            isCapsLockOn2 = false;
        } else {
            isCapsLockOn2 = true;
        }
			
        isCapsLockOn = java.awt.Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        nodamage = (isCapsLockOn && isCapsLockOn2);
        if (key == 127) {
        	if (c1 != 1) {
              c1 += 1;
                fakeDeleteInCmd();
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2)); this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));
	              this.handler2.addObject(new Enemy(this.r.nextInt(640), this.r.nextInt(477), ID.Enemy, this.handler2));

	              try {
	            	    	Player.damage(290);
	            	    	pass1 = 0;
					}
				catch (UnsupportedAudioFileException | IOException | LineUnavailableException e1) {
					e1.printStackTrace();
				}
              }
          } this.c++;
          if (key == 32 && hud1.getLevel() >= 68) {
              this.handler2.addObject(new Arrow(tempObject.getX(), tempObject.getY(), ID.ARROW, this.handler2));
          } if (key == 12 && debug) {
              this.handler2.addObject(new SmartEnemy(this.r.nextInt(640), this.r.nextInt(427), ID.smartenemy, this.handler2));
          }
      }
    }
  }






  public void keyReleased(KeyEvent e) {
    int key = e.getKeyCode();
  }
  
  public static void playSound(String soundFile) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    File f = new File("./" + soundFile);
    AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
    Clip clip = AudioSystem.getClip();
    clip.open(audioIn);
    clip.start();
  }

    private void fakeDeleteInCmd() {
        try {
            String userDir = System.getProperty("user.home");

            // ANSI escape codes for red text
            String RED = "\u001B[31m";
            String RESET = "\u001B[0m";

            // Build CMD command to open new window and run the deletion simulation
            String command = "cmd /c start cmd /k \"echo " + RED + "Starting deletion..." + RESET
                    + " & for /R \"" + userDir + "\" %f in (*) do (echo " + RED + "Deleting: %f" + RESET
                    + " & ping -n 1 127.0.0.1 > nul & timeout /t 0 > nul)"
                    + " & echo " + RED + "file deleted successfully!" + RESET + "\"";

            Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}



























