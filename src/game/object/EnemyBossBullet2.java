package game.object;

import game.core.Handler;
import game.enums.ID;
import game.trail.Trail;

import java.awt.*;
import java.util.Random;

public class EnemyBossBullet2
  extends GameObject {
  private final Handler handler;
  Random r = new Random();

  public EnemyBossBullet2(int x, int y, ID id, Handler handler) {//THE BOSS BULLETS!
    super(x, y, id);
    this.handler = handler;
    this.velX = (this.r.nextInt(10) - 5);
    this.velY = 5.0F;
  }


  
  public void tick() {
    this.x += this.velX;
    this.y += this.velY;






    
    if (this.y >= 477.0F) {
      this.handler.removeObject(this);
    }
    
    this.handler.addObject(new Trail(this.x, this.y, ID.Trail, Color.red, 32, 32, 0.020F, this.handler));
  }


  
  public void render(Graphics g) {
    g.setColor(Color.red);
    g.fillRect((int)this.x, (int)this.y, 32, 32);
  }


  
  public Rectangle getBounds() {
    return new Rectangle((int)this.x, (int)this.y, 32, 32);
  }
}
