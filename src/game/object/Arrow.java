package game.object;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import game.core.Game;
import game.core.Handler;
import game.enums.ID;
import game.enums.STATE2;

public class Arrow extends GameObject {// unused arrows that pops up when you press space.
  private final Handler handler;
    private GameObject player;
    private final float speed = 10.0f; // adjust to change follow speed

  public Arrow(float f, float g, ID id, Handler handler1) {
    super(f, g, id);
    this.handler = handler1;
    this.velX = 5.0F;
    this.velY = 5.0F;
    findPlayer();
  }

    private void findPlayer() {
        for (int i = 0; i < Handler.object.size(); i++) {
            if (Handler.object.get(i).getID() == ID.Player) {
                this.player = Handler.object.get(i);
                break;
            }
        }
    }
  public void tick() {
    this.x += this.velX;
    this.y += this.velY;
     collision();

  }

  public void render(Graphics g) {
    g.setColor(Color.BLUE);
    g.fillRect((int)this.x, (int)this.y, 32, 32);
  }

  public Rectangle getBounds() {
    return new Rectangle((int)this.x, (int)this.y, 32, 32);
  }

  public void removeself() {
    this.handler.removeObject(this);
  }

  private void collision() {
    for (int i = 0; i < Handler.object.size(); i++) {
      GameObject tempObject = Handler.object.get(i);

        if (tempObject.getID() == ID.Enemy || tempObject.getID() == ID.fastenemy || tempObject.getID() == ID.smartenemy || tempObject.getID() == ID.goodPotion) {
            if (getBounds().intersects(tempObject.getBounds())) {
                Handler.object.remove(tempObject);
                removeself();
            }
        }
        if (tempObject.getID() == ID.boss3) {
            if (getBounds().intersects(tempObject.getBounds())) {
                tempObject.health -= 100;
            }
        }
    }
  }
}
