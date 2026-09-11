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
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.unpuppyable.dogerdager.multiplayer.client.WebsocketClient;
import com.unpuppyable.dogerdager.multiplayer.host.ServerLogs;
import com.unpuppyable.dogerdager.multiplayer.host.Websocket;

import java.util.*;

public class MultiplayerScreen extends ScreenAdapter {

    private static final float BUTTON_WIDTH = 220f;
    private static final float BUTTON_HEIGHT = 34f;
    private static final int PORT = 9090;

    private static MultiplayerScreen instance;

    // Every "screen" this lobby can show. setState(...) is the only place allowed to move
    // between them - nothing else should manually add/remove widgets from `content`.
    private enum LobbyState { IDLE, HOST_LOBBY, JOIN_FORM, CLIENT_LOBBY }

    // --- screen infra ---
    private final DogerDager game;
    private final PostProcessor post;
    private final Stage stage = new Stage(new FitViewport(PlayScreen.WORLD_W, PlayScreen.WORLD_H));
    private final Preferences prefs = Gdx.app.getPreferences("doger-dager");
    private Music bgm;
    private boolean switching;
    private LobbyState state;

    // --- lobby / networking state ---
    private Websocket wsInstance;
    private WebsocketClient clientInstance;
    private Collection<String> players;

    // --- UI widgets ---
    // `root` holds the title, which never disappear. `content` holds whatever
    // is specific to the current LobbyState, and gets torn down and rebuilt by setState(...).
    private final VisTable root = new VisTable();
    private final VisTable content = new VisTable();
    private final LinkedHashMap<String, VisLabel> userList = new LinkedHashMap<>();
    private final VisTable userListTable = new VisTable();
    private VisTextButton joinButton;
    private VisTextButton connectButton;
    private VisTextButton hostButton;
    private VisTextButton startGame;
    private VisTextField promptName;
    private VisTextField promptUri;

    // --- keyboard/pad navigation ---
    private final List<Actor> choices = new ArrayList<>();
    private int index = 0;
    private boolean waitingForInput = false;
    private Actor pendingAction;

    public MultiplayerScreen(DogerDager game, PostProcessor post) {
        this.game = game;
        this.post = post;
        instance = this;

        if (Settings.prefs.getBoolean("set.music", true)) {
            this.bgm = Gdx.audio.newMusic(Gdx.files.internal("menu.mp3"));
            this.bgm.setLooping(true);
            this.bgm.setVolume(1f);
            this.bgm.play(); //menu music
        }
        stage.addActor(root);
        root.setFillParent(true);

        var title = new VisLabel("MULTIPLAYER");
        title.setFontScale(3f);
        root.add(title).padBottom(20).row();

        root.add(content).row();

        // reopening this screen while already hosting/joined (e.g. navigated back into it)
        // should land straight in the matching lobby instead of the idle Host/Join menu.
        if (DogerDager.getMultiplayer()) {
            wsInstance = Websocket.getInstance();
            clientInstance = WebsocketClient.getClientInstance();
            if (wsInstance != null) {
                players = wsInstance.users.values();
                setState(LobbyState.HOST_LOBBY);
            } else if (clientInstance != null) {
                players = clientInstance.players;
                setState(LobbyState.CLIENT_LOBBY);
            } else {
                setState(LobbyState.IDLE);
            }
        } else {
            setState(LobbyState.IDLE);
        }
    }

    /** The single place that tears down the old screen and builds the new one. */
    private void setState(LobbyState newState) {
        content.clearChildren();
        choices.clear();
        index = 0;
        state = newState;
        switch (state) {
            case IDLE -> buildIdle();
            case HOST_LOBBY -> buildHostLobby();
            case JOIN_FORM -> buildJoinForm();
            case CLIENT_LOBBY -> buildClientLobby();
        }
    }

    private void buildIdle() {
        // name goes first - you need it before either Host or Join makes sense
        if (prefs.getString("user.name", null) == null) {
            promptName = new VisTextField("Insert Name");
        } else {
            promptName = new VisTextField(prefs.getString("user.name"));
        }
        content.add(promptName).width(BUTTON_WIDTH).padBottom(15).row();
        choices.add(promptName);

        hostButton = new VisTextButton("HOST");
        hostButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onHostClicked();
            }
        });
        content.add(hostButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(8).row();
        choices.add(hostButton);

        joinButton = new VisTextButton("JOIN");
        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                setState(LobbyState.JOIN_FORM);
            }
        });
        content.add(joinButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
        choices.add(joinButton);
    }

    private void buildHostLobby() {
        VisLabel portLabel = new VisLabel("Server open on port: " + PORT);
        portLabel.setColor(Color.GRAY);
        content.add(portLabel).padBottom(15).row();

        startGame = new VisTextButton("Start");
        startGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                start();
            }
        });
        content.add(startGame).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(20).row();
        choices.add(startGame);

        buildUserList();
    }

    private void buildJoinForm() {
        // keep the name field around - you can still fix a typo before connecting
        content.add(promptName).width(BUTTON_WIDTH).padBottom(15).row();
        choices.add(promptName);

        promptUri = new VisTextField("Insert Server Address");
        content.add(promptUri).width(BUTTON_WIDTH).padBottom(8).row();
        choices.add(promptUri);

        connectButton = new VisTextButton("Connect");
        connectButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                connect();
            }
        });
        content.add(connectButton).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).row();
        choices.add(connectButton);
    }

    private void buildClientLobby() {
        buildUserList();
    }

    private void buildUserList() {
        VisLabel userListTitle = new VisLabel("Players");
        userListTitle.setFontScale(2f);
        content.add(userListTitle).width(BUTTON_WIDTH).padTop(20).row();
        content.add(userListTable).row();
        userList.clear();
        for (String name : players) userList.put(name, new VisLabel(name));
        refreshUserList();
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
            Websocket.dispose();
            WebsocketClient.dispose();
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
                setState(LobbyState.JOIN_FORM);
                return;
            } else if (choice == hostButton) {
                if (hostButton.isDisabled()) return;
                onHostClicked();
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
                ErrorNotifier.show("no action found.");
                return;
            }
        }
        for (Actor a : choices) {
            a.setColor(a == choice ? Color.YELLOW : Color.WHITE);
        }
    }

    private void onHostClicked() {
        //getting player name
        if (!promptName.isEmpty() || !promptName.getText().equals("Insert Name")) {
            prefs.putString("user.name", promptName.getText());
        } else {
            if (prefs.getString("user.name", null) == null || prefs.getString("user.name").equals("Insert Name")) {
                ErrorNotifier.show("Please insert your name.");
                return;
            }
        }
        try {
            wsInstance = Websocket.startServer(PORT);
        } catch (InterruptedException e) {
            ServerLogs.write("Start was interrupted: "+e);
            ErrorNotifier.show("Start was interrupted, check multiplayer.logs");
            return;
        }
        DogerDager.setMultiplayer(true);
        //describing your name to put into users
        String host = prefs.getString("user.name");
        wsInstance.users.put(null, host);
        players = wsInstance.users.values();
        setState(LobbyState.HOST_LOBBY);
    }

    private void start() {
        switching = true;
        game.setScreen(new MenuScreen(game, post));
        dispose();
    }

    private void connect() {
        if (promptUri.isEmpty() || promptUri.getText().equals("Insert Server Address")) {
            ErrorNotifier.show("Server Address cannot be empty");
            return;
        }
        if (promptName.isEmpty() || promptName.getText().equals("Insert Name")) {
            ErrorNotifier.show("Name cannot be empty");
            return;
        }
        // Prevent double-clicking
        connectButton.setDisabled(true);
        connectButton.setText("Connecting...");
        try {
            clientInstance = WebsocketClient.startClient(
                    promptUri.getText(), promptName.getText());
            if (clientInstance == null) {
                ErrorNotifier.show("Invalid address");
                connectButton.setDisabled(false);
                connectButton.setText("Connect");
            }
        } catch (InterruptedException e) {
            ErrorNotifier.show("Something went wrong, please try again (report in multiplayer.logs)");
            connectButton.setDisabled(false);
            connectButton.setText("Connect");
        }
    }

    public static void onClientVerified() {
        Gdx.app.postRunnable(() -> {
            if (instance != null) instance.handleClientVerified();
        });
    }

    private void handleClientVerified() {
        players = new HashSet<>(clientInstance.players);
        setState(LobbyState.CLIENT_LOBBY);
    }

    private void refreshUserList() {
        userListTable.clearChildren();
        for (VisLabel l : userList.values()) userListTable.add(l).row();
        userListTable.invalidateHierarchy();
    }

    public static void addToUserList(String name) {
        Gdx.app.postRunnable(() -> {
            if (instance != null) instance.addPlayer(name);
        });
    }

    private void addPlayer(String name) {
        userList.put(name, new VisLabel(name));
        refreshUserList();
    }

    public static void deleteFromUserList(String name) {
        Gdx.app.postRunnable(() -> {
            if (instance != null) instance.removePlayer(name);
        });
    }

    private void removePlayer(String name) {
        if (userList.remove(name) == null) {
            ServerLogs.write("deleteFromUserList: no such player '" + name + "'");
            return;
        }
        refreshUserList();
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
        if (instance == this) instance = null;
    }
}
