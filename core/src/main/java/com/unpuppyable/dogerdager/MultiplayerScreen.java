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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

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
    private static boolean clientConnected;
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


    private Music bgm;

    public MultiplayerScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
        clientConnected = false;
        build(post);
    }

    private void build(PostProcessor post) {
        root.clear();
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

        //HOST BUTTON
        hostButton = new VisTextButton("HOST");
        hostButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rollOutHost();
            }
        });
        root.add(hostButton).width(220).height(34).padTop(8).row();

        //JOIN button
        joinButton = new VisTextButton("JOIN");
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rollOutJoin();
            }
        });
        root.add(joinButton).width(220).height(34).padTop(8).row();
        promptName = new VisTextField("Insert Name");
        root.add(promptName);
        stage.addActor(root);
    }

    private void handleKeys(PostProcessor post) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            switching = true;
            DogerDager.setMultiplayer(false);
            game.setScreen(new MenuScreen(game, post));
            dispose();
            return;
        }
    }

    private void rollOutHost() {
        //getting player name
        if (!promptName.isEmpty()) {
            prefs.putString("user.name", promptName.getText());
        } else {
            return;
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
        root.removeActor(hostButton);
        root.removeActor(promptName);
        //adding pool with your ip and the port
        try {
            myIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ServerLogs.write("Couldn't obtain Host address: "+e);
            return;
        }
        VisLabel ipTextPool = new VisLabel(myIp+":"+port);
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
        //userlist
        createUserList();
    }

    private void start() {
        switching = true;
        game.setScreen(new MenuScreen(game, post));
        dispose();
    }

    private void rollOutJoin() {
        root.removeActor(joinButton);
        root.removeActor(hostButton);
        //making prompt for serverAddress
        promptUri = new VisTextField("Insert Server Address");
        root.add(promptUri).row();
        // connect button
        connectButton = new VisTextButton("Connect");
        connectButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
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
        });
        root.add(connectButton).width(220).height(34).padTop(8).row();
    }

    public static void onClientVerified() {
        Gdx.app.postRunnable(() -> {
            root.removeActor(promptName);
            root.removeActor(promptUri);
            root.removeActor(connectButton);
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
        stage.dispose();
    }
}
