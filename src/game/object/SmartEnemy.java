package game.object;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import game.core.Handler;
import game.enums.ID;
import game.trail.Trail;

public class SmartEnemy extends GameObject {
    private final Handler handler;
    private GameObject player;
    private final float speed = 2.5f; // adjust to change follow speed

    public SmartEnemy(int x, int y, ID id, Handler handler) {
        super(x, y, id);
        this.handler = handler;
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
        if (this.player == null) {
            findPlayer();
        }

        if (this.player != null) {
            // direction from enemy to player
            float diffX = this.player.getX() - this.x;
            float diffY = this.player.getY() - this.y;
            float distance = (float) Math.sqrt(diffX * diffX + diffY * diffY);

            if (distance != 0) {
                // normalized direction times speed
                this.velX = (diffX / distance) * speed;
                this.velY = (diffY / distance) * speed;
            } else {
                this.velX = 0;
                this.velY = 0;
            }
        }

        this.x += this.velX;
        this.y += this.velY;

        this.handler.addObject(new Trail(this.x, this.y, ID.Trail, Color.green, 16, 16, 0.05F, this.handler));
    }

    public void render(Graphics g) {
        g.setColor(Color.green);
        g.fillRect((int) this.x, (int) this.y, 16, 16);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) this.x, (int) this.y, 16, 16);
    }
}
