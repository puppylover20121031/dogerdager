package game.object;

import game.core.Game;
import game.core.Handler;
import game.enums.ID;
import game.trail.Trail;
import java.awt.*;
import java.util.Random;

public class Boss3 extends GameObject {
    private final Handler handler;
    private int timer = 80;
    private int timer2 = 50;
    private final Random r = new Random();

    // New: health, phases and attack timers
    private int health = 1200;
    private int phase = 1;
    private int attackTimer = 0;
    private int burstTimer = 0;
    private boolean WentDown1 = false;
    private boolean spawned = false;
    private boolean ready = false;
    private boolean shieldActive = false;
    private int shieldTimer = 0;

    GameObject boss3Left;
    GameObject boss3Right;

    public Boss3(int x, int y, ID id, Handler handler1) {
        super(x, y, id);
        this.handler = handler1;
        this.velX = 0.0F;
        this.velY = 3.5F;
    }

    public void tick() {
        // movement
        this.x += this.velX;
        this.y += this.velY;

        if (this.timer <= 0) {
            this.velY = 0.0F;
        } else {
            this.timer--;
        }

        if (this.timer <= 0) this.timer2--;
        if (this.timer2 <= 0) {
            if (this.velX == 0.0F && ready) this.velX = 2.0F;

            if (this.velX > 0.0F) this.velX += 0.005F;
            else if (this.velX < 0.0F) this.velX -= 0.005F;

            this.velX = Game.clamp(this.velX, -5, 5);
        }

        // bounce side to side
        if (this.x <= 0.0F || this.x >= 444.0F) this.velX *= -1.0F;

        // spawn left/right components once boss stops descending
        if (this.velY == 0) WentDown1 = true;
        if (!spawned && WentDown1) {
            boss3Left = new Boss3Left((int)this.x - 100, (int)this.y - 500, ID.boss3Left, this.handler);
            boss3Right = new Boss3Right((int)this.x + 100, (int)this.y - 500, ID.boss3Right, this.handler);
            this.handler.addObject(boss3Left);
            this.handler.addObject(boss3Right);
            spawned = true;
        }
        if (WentDown1 && boss3Left != null && boss3Right != null) {
            if (boss3Left.ready && boss3Right.ready) ready = true;
        }

        // Update shield timer
        if (shieldActive) {
            shieldTimer--;
            if (shieldTimer <= 0) shieldActive = false;
        }

        // Phase logic based on health thresholds
        int prevPhase = phase;
        if (health > 800) phase = 1;
        else if (health > 400) phase = 2;
        else phase = 3;

        if (phase != prevPhase) {
            // On phase change: temporary shield + spawn a short minion wave
            shieldActive = true;
            shieldTimer = 120; // shield lasts a bit
            for (int i = 0; i < 3; i++) {
                handler.addObject(new Enemy(this.r.nextInt(600), (int)this.y + 120, ID.Enemy, handler));
            }
        }

        // Attack patterns controlled by timers and phase
        attackTimer++;
        burstTimer++;

        if (!shieldActive && ready) {
            if (phase == 1) {
                // slow twin shot occasionally
                if (attackTimer > 40) {
                    if (r.nextInt(4) == 0) {
                        handler.addObject(new EnemyBossBullet((int)this.x + 12, (int)this.y + 60, ID.Enemy, handler));
                        handler.addObject(new EnemyBossBullet((int)this.x + 84, (int)this.y + 60, ID.Enemy, handler));
                    }
                    attackTimer = 0;
                }
            } else if (phase == 2) {
                // more frequent twin + occasional radial burst
                if (attackTimer > 26) {
                    handler.addObject(new EnemyBossBullet((int)this.x + 12, (int)this.y + 60, ID.Enemy, handler));
                    handler.addObject(new EnemyBossBullet((int)this.x + 84, (int)this.y + 60, ID.Enemy, handler));
                    attackTimer = 0;
                }
                if (burstTimer > 160) {
                    // radial-ish burst using many random bullets for visual variety
                    for (int i = 0; i < 12; i++) {
                        handler.addObject(new EnemyBossBullet2((int)this.x + 48 + r.nextInt(40) - 20,
                                (int)this.y + 48, ID.Enemy, handler));
                    }
                    burstTimer = 0;
                }
            } else { // phase 3 - aggressive
                if (attackTimer > 12) {
                    // fast salvo
                    for (int i = 0; i < 3; i++) {
                        handler.addObject(new EnemyBossBullet((int)this.x + 10 + i*28, (int)this.y + 60, ID.Enemy, handler));
                    }
                    attackTimer = 0;
                }
                if (burstTimer > 100) {
                    // big chaotic burst
                    for (int i = 0; i < 20; i++) {
                        handler.addObject(new EnemyBossBullet2((int)this.x + r.nextInt(96), (int)this.y + 48, ID.Enemy, handler));
                    }
                    burstTimer = 0;
                }
            }
        }

        // trailing effect
        this.handler.addObject(new Trail(this.x, this.y, ID.Trail, Color.red, 96, 96, 0.08F, this.handler));

        // Remove if health depleted (drop a potion)
        if (health <= 0) {
            handler.addObject(new GoodPotion((int)this.x + 30, (int)this.y + 100, ID.goodPotion, handler));
            handler.removeObject(this);
        }
    }

    // expose method for being damaged by player bullets
    public void damage(int amt) {
        if (shieldActive) return;
        health -= amt;
        // brief flash or temporary shield on big hit
        if (amt > 50) {
            shieldActive = true;
            shieldTimer = 40;
        }
    }

    public void render(Graphics g) {
        // color changes by phase
        if (phase == 1) g.setColor(new Color(180, 30, 30));
        else if (phase == 2) g.setColor(new Color(230, 100, 30));
        else g.setColor(new Color(255, 40, 100));

        g.fillRect((int)this.x, (int)this.y, 96, 96);

        // draw shield as translucent circle
        if (shieldActive) {
            Color c = new Color(100, 180, 255, 120);
            g.setColor(c);
            g.fillOval((int)this.x - 16, (int)this.y - 16, 128, 128);
        }

        // draw HP bar above boss
        int barW = 96;
        int hpBar = (int) ((health / 1200.0) * barW);
        g.setColor(Color.black);
        g.fillRect((int)this.x, (int)this.y - 8, barW, 6);
        g.setColor(Color.green);
        g.fillRect((int)this.x, (int)this.y - 8, Math.max(0, hpBar), 6);
        g.setColor(Color.white);
        g.drawRect((int)this.x, (int)this.y - 8, barW, 6);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)this.x, (int)this.y, 96, 96);
    }
}
