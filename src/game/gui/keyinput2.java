package game.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import game.core.Handler;
import game.core.SaveManager2;

public class keyinput2 extends KeyAdapter {

    private final Handler handler2;
    private final HUD hud1;
    public keyinput2(Handler handler1, HUD hud, SaveManager2 manager2) {
        this.handler2 = handler1;
        this.hud1 = hud;

    }

    public void keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.getKeyCode();
        for (int i = 0; i < Handler.object.size(); i++) {
            Menu2.hardcorebutton = (key == 17);
        }
    }

}
