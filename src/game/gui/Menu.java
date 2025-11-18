package game.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import game.core.Game;
import game.core.Handler;
import game.enums.STATE;

public class Menu
  extends MouseAdapter
{// this is a unused class that has play and quit buttons
  Game game;
  private final Handler handler;
  private HUD hud;
  
  public Menu(Game game1, Handler handler1) {
    this.handler = handler1;
    this.game = game1;
  }
  
  public void mousePressed(MouseEvent e) {
    int mx = e.getX();
    int my = e.getY();


      if (mouseOver(mx, my, 200, 250, 200, 64)) {
          Game.gameState = STATE.MENU2;

      }
    } 
 

  
  public void mouseReleased(MouseEvent e) {}


    boolean mouseOver(int mx, int my, int x, int y, int width, int height) {
        if (mx > x && mx < x + width) {
            return my > y && my < y + height;
        }

        return false;
    }


  
  public void tick() {}

  
  public void render(Graphics g) {
    Font fnt = new Font("arial", Font.BOLD, 50);
    Font fnt2 = new Font("arial", Font.BOLD, 30);
    
    g.setFont(fnt2);
    g.setColor(Color.gray);
    g.drawString("Quit", 270, 200);

    g.setFont(fnt2);
    g.setColor(Color.gray);
    g.drawString("Play", 270, 290);
    
    g.setColor(Color.white);
    g.drawRect(200, 150, 200, 64);
    
    g.drawRect(200, 250, 200, 64);
  }
}
