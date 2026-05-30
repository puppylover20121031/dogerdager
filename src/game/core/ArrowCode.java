package game.core;

import game.enums.ID;
import game.gui.HUD;
import game.object.Arrow;
import game.object.GameObject;

public class ArrowCode {

    private Handler handler2;
    private HUD hud1;

    public int timer = 0;

    public ArrowCode(Handler handler1, HUD hud) {
        this.handler2 = handler1;
        this.hud1 = hud;
    }

    public void createit() {
        for (int i = 0; i < Handler.object.size(); i++) {
            GameObject tempObject = Handler.object.get(i);
            if (tempObject.getID() == ID.Player && timer < 100) {
                this.handler2.addObject(new Arrow(tempObject.getX(), tempObject.getY(), ID.ARROW, this.handler2));
                timer = 6000;
            }
        }
    }

    public void tick() {

        if (timer != 0) {
            timer -= 100;
        }

    }

}
