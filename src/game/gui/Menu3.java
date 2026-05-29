package game.gui;

import game.core.Game;
import game.core.Handler;
import game.core.Spawn;
import game.enums.ID;
import game.enums.STATE;
import game.enums.STATE2;
import game.object.Enemy;
import game.object.Player;
import game.object.SmartEnemy;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class Menu3
  extends MouseAdapter {// this is to select the diffilculty(aka gamestate2)
	Game game;
  private final Handler handler;
  private final Random r = new Random();
  private final Spawn spawner;
  private HUD hud;
  private static boolean play = false;

  public Menu3(Game game2, Handler handler1) {
    this.handler = handler1;
    this.spawner = new Spawn(handler, hud);
  }
  
  public void mousePressed(MouseEvent e) {
    int mx = e.getX();
    int my = e.getY();
    if (Game.gameState != STATE.GAME) {

    if (mouseOver(mx, my, 200, 150, 200, 64)) {
        Game.gameState = STATE.MENU2;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    } else if (mouseOver(mx, my, 200, 250, 200, 64)) {
        Game.gameState = STATE.MENU2;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    } else if (mouseOver(mx, my, 200, 350, 200, 64)) {
        Game.gameState = STATE.MENU2;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

    }
  }}



    boolean mouseOver(int mx, int my, int x, int y, int width, int height) {
    if (mx > x && mx < x + width) {
        return my > y && my < y + height;
    } 
    
    return false;
  }


  
  public void tick() {}

  
  public static void render(Graphics g) {
    Font fnt = new Font("arial", 1, 50);
    Font fnt2 = new Font("arial", 1, 30);
    g.setFont(fnt);
    g.setColor(Color.gray);
    g.drawString("achievements", 230, 60);
    
    g.setFont(fnt2);
    g.setColor(Color.gray);
    g.drawString("beat the game in normal", 205, 200);

      g.setFont(fnt2);
      g.setColor(Color.gray);
      g.drawString("2", 205, 290);

      g.setFont(fnt2);
      g.setColor(Color.red);
      g.drawString("3", 205, 400);

      g.setColor(Color.white);
      g.drawRect(200, 150, 200, 64);

      g.setColor(Color.white);
      g.drawRect(200, 350, 200, 64);
    
    g.drawRect(200, 250, 200, 64);
  }
}
