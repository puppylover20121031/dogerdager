package com.unpuppyable.dogerdager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.unpuppyable.dogerdager.multiplayer.client.ClientMessages;
import com.unpuppyable.dogerdager.multiplayer.client.WebsocketClient;
import com.unpuppyable.dogerdager.multiplayer.host.Messages;
import com.unpuppyable.dogerdager.multiplayer.host.ServerLogs;
import com.unpuppyable.dogerdager.multiplayer.host.Websocket;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.*;

public class MultiplayerScreen extends ScreenAdapter {

    private final DogerDager game;
    private final PostProcessor post;
    private final Stage stage = new Stage(new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H));
    private static VisTable root = new VisTable();
    private boolean switching;
    private static String myIp;
    private static int port = 9090;
    private static Websocket wsInstance;
    private static WebsocketClient clientInstance;
    private static Preferences prefs = Gdx.app.getPreferences("doger-dager");
    private static Collection<String> players;

    private static final LinkedHashMap<String, VisLabel> userList = new LinkedHashMap<>();
    private static final VisTable userListTable = new VisTable();
    private static VisLabel userListTitle;
    private static VisLabel error;
    private static VisTextButton joinButton;
    private static VisTextButton connectButton;
    private static VisTextButton hostButton;
    private static VisTextButton startGame;
    private static VisTextField promptName;
    private static VisTextField promptUri;

    private KeyBind keyBind = new KeyBind();

    private static int index = 0;
    private static List<Actor> choices = new ArrayList<>();
    private boolean waitingForInput = false;
    private Actor pendingAction;
    private Music bgm;

    public MultiplayerScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
        build(post);
    }

    private void build(PostProcessor post) {
        root.clear();
        stage.addActor(root);
        if (Settings.prefs.getBoolean("set.music", true)) {
            this.bgm = Gdx.audio.newMusic(Gdx.files.internal("menu.mp3"));
            this.bgm.setLooping(true);
            this.bgm.setVolume(1f);
            this.bgm.play(); //menu music
        }
        root.setFillParent(true);

        var title = new VisLabel("MULTIPLAYER");
        title.setFontScale(3f);
        root.add(title).padBottom(8).row();
        if (DogerDager.getMultiplayer()) {
            wsInstance = Websocket.getInstance();
            clientInstance = WebsocketClient.getClientInstance();
            if (clientInstance != null) players = clientInstance.players;
            if (wsInstance != null) {
                players = wsInstance.users.values();
                startGame = new VisTextButton("Start");
                startGame.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        start();
                    }
                });
                root.add(startGame).width(220).height(34).padTop(15).row();
                choices.add(startGame);
            }
            createUserList();
            return;
        }
        //HOST BUTTON
        hostButton = new VisTextButton("HOST");
        hostButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rollOutHost();
            }
        });
        root.add(hostButton).width(220).height(34).padTop(8).row();
        choices.add(hostButton);

        //JOIN button
        joinButton = new VisTextButton("JOIN");
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rollOutJoin();
            }
        });
        root.add(joinButton).width(220).height(34).padTop(8).row();
        choices.add(joinButton);
        //name prompt
        if (prefs.getString("user.name", null)==null) {
            promptName = new VisTextField("Insert Name");
        } else {
            promptName = new VisTextField(prefs.getString("user.name"));
        }
        root.add(promptName);
        choices.add(promptName);
    }

    private void handleKeys(PostProcessor post) {
        if (waitingForInput) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Pad.justB()) {
                waitingForInput = false;
                pendingAction = null;
            }
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Pad.justB()) {
            switching = true;
            DogerDager.setMultiplayer(false);
            game.setScreen(new MenuScreen(game, post));
            dispose();
            return;
        }
        if (choices.isEmpty()) return;

        if (!waitingForInput) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP) || Pad.justUp()) {
                index = (index - 1 + choices.size()) % (choices.size());
                if (index == (index - 1 + choices.size()) % (choices.size())) return;
                game.menuMove();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Pad.justDown()) {
                index = (index + 1) % (choices.size());
                if (index == (index + 1) % (choices.size())) return;
                game.menuMove();
            }
        }
        Actor choice = choices.get(index);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Pad.justA()) {
            if (stage.getKeyboardFocus() instanceof VisTextField) {
                stage.setKeyboardFocus(null);
                return;
            }
            if (choice == joinButton) {
                if (joinButton.isDisabled()) return;
                rollOutJoin();
                return;
            } else if (choice == hostButton) {
                if (hostButton.isDisabled()) return;
                rollOutHost();
                return;
            } else if (choice == connectButton) {
                if (connectButton.isDisabled()) return;
                connect();
                return;
            } else if (choice == startGame) {
                if (startGame.isDisabled()) return;
                start();
                return;
            } else if (choice instanceof VisTextField field) {
                stage.setKeyboardFocus(field);
                pendingAction = field;
                waitingForInput = true;
                //implement keyboard widget (for now only physical keyboard)
                return;
            } else {
                notifyError("no action found.");
                return;
            }
        }
        for (Actor a : choices) {
            a.setColor(a == choice ? Color.YELLOW : Color.WHITE);
        }
    }

    private void rollOutHost() {
        index = 0;
        //getting player name
        if (!promptName.isEmpty() || !promptName.getText().equals("Insert Name")) {
            prefs.putString("user.name", promptName.getText());
        } else {
            if (prefs.getString("user.name", null) == null || prefs.getString("user.name").equals("Insert Name")) {
                notifyError("Please insert your name.");
                return;
            }
        }
        try {
            wsInstance = Websocket.startServer(port);
        } catch (InterruptedException e) {
            ServerLogs.write("Start was interrupted: "+e);
            //show error
            return;
        }
        DogerDager.setMultiplayer(true);
        //removing join, prompt and host:
        root.removeActor(joinButton);
        choices.remove(joinButton);
        root.removeActor(hostButton);
        choices.remove(hostButton);
        root.removeActor(promptName);
        choices.remove(promptName);
        //adding pool with your port
        VisLabel ipTextPool = new VisLabel("Server open on port: "+port);
        root.add(ipTextPool).padBottom(9).row();
        //describing your name to put into users
        String host = prefs.getString("user.name");
        wsInstance.users.put(null, host);
        players = wsInstance.users.values();
        //start game button
        startGame = new VisTextButton("Start");
        startGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                start();
            }
        });
        root.add(startGame).width(220).height(34).padTop(15).row();
        choices.add(startGame);
        //userlist
        createUserList();
    }

    private void start() {
        switching = true;
        game.setScreen(new MenuScreen(game, post));
        dispose();
    }

    private void rollOutJoin() {
        index = 0;
        root.removeActor(joinButton);
        choices.remove(joinButton);
        root.removeActor(hostButton);
        choices.remove(hostButton);
        //making prompt for serverAddress
        promptUri = new VisTextField("Insert Server Address");
        root.add(promptUri).row();
        choices.add(promptUri);
        // connect button
        connectButton = new VisTextButton("Connect");
        connectButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                connect();
            }
        });
        root.add(connectButton).width(220).height(34).padTop(8).row();
        choices.add(connectButton);
    }

    private static void connect() {
        index = 0;
        if (promptUri.isEmpty() || promptUri.getText().equals("Insert Server Address")) {
            notifyError("Server Address cannot be empty");
            return;
        }
        if (promptName.isEmpty() || promptName.getText().equals("Insert Name")) {
            notifyError("Name cannot be empty");
            return;
        }
        // Prevent double-clicking
        connectButton.setDisabled(true);
        connectButton.setText("Connecting...");
        try {
            clientInstance = WebsocketClient.startClient(
                    promptUri.getText(), promptName.getText());
            if (clientInstance == null) {
                notifyError("Invalid address");
                connectButton.setDisabled(false);
                connectButton.setText("Connect");
            }
        } catch (InterruptedException e) {
            notifyError("Something went wrong, please try again");
            connectButton.setDisabled(false);
            connectButton.setText("Connect");
        }
    }

    public static void onClientVerified() {
        Gdx.app.postRunnable(() -> {
            root.removeActor(promptName);
            choices.remove(promptName);
            root.removeActor(promptUri);
            choices.remove(promptUri);
            root.removeActor(connectButton);
            choices.remove(connectButton);
            root.removeActor(error);  // clear any previous error
            players = new HashSet<>(clientInstance.players);
            createUserList();
        });
    }

    public static void notifyError(String message) {
        Gdx.app.postRunnable(() -> {
                root.removeActor(error);
                error = new VisLabel(message);
                root.add(error).row();
                root.invalidate();
        });
    }

    private static void createUserList() {
        userListTitle = new VisLabel("Players");
        userListTitle.setFontScale(2f);
        root.add(userListTitle).width(220).height(34).padTop(10).row();
        root.add(userListTable).row();
        userList.clear();
        for (String name : players) userList.put(name, new VisLabel(name));
        refreshUserList();
    }

    private static void refreshUserList() {
        userListTable.clearChildren();
        for (VisLabel l : userList.values()) userListTable.add(l).row();
        userListTable.invalidateHierarchy();
    }

    public static void addToUserList(String name) {
        Gdx.app.postRunnable(() -> {
            userList.put(name, new VisLabel(name));
            refreshUserList();
        });
    }

    public static void deleteFromUserList(String name) {
        Gdx.app.postRunnable(() -> {
            if (userList.remove(name) == null) {
                ServerLogs.write("deleteFromUserList: no such player '" + name + "'");
                return;
            }
            refreshUserList();
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        render(delta, post);
    }

    public void render(float delta, PostProcessor post) {
        if (switching) return;
        handleKeys(post);
        if (switching) return;
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        if (switching) return;
        stage.draw();
    }


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (bgm != null) bgm.stop();
        choices.clear();
        stage.dispose();
    }
}
