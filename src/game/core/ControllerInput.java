package game.core;

import java.io.File;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;

import game.enums.ID;
import game.object.Arrow;
import game.object.GameObject;

public class ControllerInput {
    private Controller controller;
    private boolean controllerFound;
    private boolean fireHeld;
    private float currentX = 0f;
    private float currentY = 0f;
    private static final float DEAD_ZONE = 0.4f;

    public ControllerInput() {
        configureNativePath();
        try {
            findController();
        } catch (Throwable t) {
            controllerFound = false;
            System.err.println("JInput controller initialization failed: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            System.err.println("Make sure JInput native libraries are available and java.library.path includes them.");
        }
    }

    private void configureNativePath() {
        File nativeDir = new File("natives");
        if (!nativeDir.exists()) {
            nativeDir.mkdirs();
        }
        String nativePath = nativeDir.getAbsolutePath();
        System.setProperty("net.java.games.input.librarypath", nativePath);
        System.setProperty("java.library.path", nativePath);
        System.out.println("JInput native path set to: " + nativePath);
    }

    private void findController() {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        Controller fallback = null;
        for (int i = 0; i < controllers.length; i++) {
            Controller c = controllers[i];
            System.out.println("JInput detected controller: " + c.getType() + " - " + c.getName());
            if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                controller = c;
                controllerFound = true;
                System.out.println("JInput controller selected: " + c.getName());
                printControllerComponents(c);
                return;
            }
            String lower = c.getName().toLowerCase();
            if (fallback == null && (lower.contains("xbox") || lower.contains("controller") || lower.contains("gamepad"))) {
                fallback = c;
            }
        }
        if (fallback != null) {
            controller = fallback;
            controllerFound = true;
            System.out.println("JInput fallback controller selected: " + fallback.getName());
            printControllerComponents(fallback);
            return;
        }
        if (controllers.length > 0) {
            controller = controllers[0];
            controllerFound = true;
            System.out.println("JInput default controller selected: " + controller.getName());
            printControllerComponents(controller);
            return;
        }
        controllerFound = false;
        System.out.println("No JInput controller found.");
    }

    public boolean isConnected() {
        return controllerFound;
    }

    public void poll(Handler handler) {
        if (!controllerFound || controller == null) {
            return;
        }

        if (!controller.poll()) {
            return;
        }

        Event event = new Event();
        while (controller.getEventQueue().getNextEvent(event)) {
            Component component = event.getComponent();
            float value = event.getValue();

            if (component.isAnalog()) {
                if (component.getIdentifier() == Component.Identifier.Axis.X
                        || component.getIdentifier() == Component.Identifier.Axis.RX) {
                    currentX = value;
                }
                if (component.getIdentifier() == Component.Identifier.Axis.Y
                        || component.getIdentifier() == Component.Identifier.Axis.RY) {
                    currentY = value;
                }
                if (component.getIdentifier() == Component.Identifier.Axis.POV) {
                    processPov(value);
                }
            } else {
                boolean pressed = value == 1.0f;
                if (component.getIdentifier() == Component.Identifier.Button._0
                        || component.getIdentifier() == Component.Identifier.Button._1
                        || component.getIdentifier() == Component.Identifier.Button._2
                        || component.getIdentifier() == Component.Identifier.Button._3) {
                    if (pressed && !fireHeld) {
                        spawnArrow(handler);
                    }
                    fireHeld = pressed;
                }
            }
        }

        int moveX = 0;
        int moveY = 0;

        if (currentX < -DEAD_ZONE) {
            moveX = -5;
        } else if (currentX > DEAD_ZONE) {
            moveX = 5;
        }

        if (currentY < -DEAD_ZONE) {
            moveY = -5;
        } else if (currentY > DEAD_ZONE) {
            moveY = 5;
        }

        setPlayerVelocity(handler, moveX, moveY);
    }

    private void processPov(float povValue) {
        if (povValue == Component.POV.OFF) {
            return;
        }
        currentX = 0f;
        currentY = 0f;
        if (povValue == Component.POV.UP || povValue == Component.POV.UP_LEFT || povValue == Component.POV.UP_RIGHT) {
            currentY = -1f;
        }
        if (povValue == Component.POV.DOWN || povValue == Component.POV.DOWN_LEFT || povValue == Component.POV.DOWN_RIGHT) {
            currentY = 1f;
        }
        if (povValue == Component.POV.LEFT || povValue == Component.POV.UP_LEFT || povValue == Component.POV.DOWN_LEFT) {
            currentX = -1f;
        }
        if (povValue == Component.POV.RIGHT || povValue == Component.POV.UP_RIGHT || povValue == Component.POV.DOWN_RIGHT) {
            currentX = 1f;
        }
    }

    private void setPlayerVelocity(Handler handler, int x, int y) {
        for (int i = 0; i < Handler.object.size(); i++) {
            GameObject tempObject = Handler.object.get(i);
            if (tempObject.getID() == game.enums.ID.Player) {
                tempObject.setvelX(x);
                tempObject.setvelY(y);
            }
        }
    }

    private void spawnArrow(Handler handler) {
        for (int i = 0; i < Handler.object.size(); i++) {
            GameObject tempObject = Handler.object.get(i);
            if (tempObject.getID() == game.enums.ID.Player) {
                handler.addObject(new Arrow(tempObject.getX(), tempObject.getY(), game.enums.ID.ARROW, handler));
                return;
            }
        }
    }

    private void printControllerComponents(Controller c) {
        System.out.println("Controller components for " + c.getName() + ":");
        for (Component component : c.getComponents()) {
            System.out.println("  " + component.getIdentifier() + " = " + component.getName() + " (" + component.getPollData() + ")");
        }
    }
}

