package game.object;

import game.core.Game;
import game.core.Handler;
import game.enums.ID;
import game.trail.Trail;

import java.awt.*;
import java.util.Random;

public class Boss3Left
  extends GameObject { // the BOSS!
  private final Handler handler;
    private int timer = 104;
    private int timer2 = 4;
    private boolean one = false;
    private boolean one2 = false;
  Random r = new Random();

  public Boss3Left(int x, int y, ID id, Handler handler1) {
    super(x, y, id);
    this.handler = handler1;
    this.velX = 0.0F;
    this.velY = 4.8F;
    this.ready = false;
  }



  public void tick() {
    this.x += this.velX;
    this.y += this.velY;
      if (this.timer <= 0) {
          this.timer2--;
      }
      if (this.timer2 <= 0) {
          if (this.velX == 0.0F && ready) {
              this.velX = 2.0F;
          }
          if (this.velX > 0.0F) {
              this.velX += 0.005F;
          } else if (this.velX < 0.0F) {
              this.velX -= 0.005F;
          }
          if (!one2) {
              this.velX = Game.clamp(this.velX, -5, 5);
          }
      }
      if (this.timer <= 0 && !one) {
          this.velY = 0.0F;
          one = true;
      } else {
          this.timer--;
      }
      if (one && !one2) {
          this.velX = 2.0F;
      }
      if (this.x <= 0.0F || this.x >= 444.0F) {
          this.velX *= -1.0F;
      }
      if (this.timer2 <= 0 && !one2 && one) {
          this.velX = 0.0F;
          one2 = true;
      } else if (one && !one2) {
          this.timer2--;
      } if (one2) {
          this.ready = true;
      }

    this.handler.addObject(new Trail(this.x, this.y, ID.Trail, Color.red, 96, 96, 0.08F, this.handler));
  }


  
  public void render(Graphics g) {
    g.setColor(Color.red);
    g.fillRect((int)this.x, (int)this.y, 96, 96);
  }


  
  public Rectangle getBounds() {
    return new Rectangle((int)this.x, (int)this.y, 96, 96);
  }
}
