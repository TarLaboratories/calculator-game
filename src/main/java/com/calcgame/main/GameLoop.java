package com.calcgame.main;

import com.calcgame.main.buttons.FuncButton;
import com.calcgame.main.objects.TextObject;
import com.calcgame.main.rendering.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.*;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class GameLoop {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final float MOUSE_SENSITIVITY = .5f;
    private Renderer renderer;

    private Window window;
    private GameState state;
    private List<GameObject> objects;
    private Lights lights;

    private final Map<String, Runnable> keyHoldCallbacks = new HashMap<>();
    private final Map<Integer, List<String>> keysToCallbackIds = new HashMap<>();

    private boolean lockRotation;
    private Vector2f prevMousePos;

    private boolean initialised = false;

    public void init(GameState state) {
        this.state = state;
        glfwInit();
        window = new Window.Builder().build(state, "Test", 800, 1000);
        window.select();
        window.setVisible(true);
        GL.createCapabilities();
        lockRotation = true;
        prevMousePos = new Vector2f(window.getWidth() / 2.0f, window.getHeight() / 2.0f);
        renderer = new Renderer();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        addKeyHoldCallback("moveW", GLFW_KEY_W, () -> renderer.getCamera().movePosition(0, 0, -0.2f));
        addKeyHoldCallback("moveS", GLFW_KEY_S, () -> renderer.getCamera().movePosition(0, 0, 0.2f));
        addKeyHoldCallback("moveA", GLFW_KEY_A, () -> renderer.getCamera().movePosition(-0.2f, 0, 0));
        addKeyHoldCallback("moveD", GLFW_KEY_D, () -> renderer.getCamera().movePosition(0.2f, 0, 0));
        addKeyHoldCallback("moveUp", GLFW_KEY_SPACE, () -> renderer.getCamera().movePosition(0, 0.2f, 0));
        addKeyHoldCallback("moveDown", GLFW_KEY_LEFT_SHIFT, () -> renderer.getCamera().movePosition(0, -0.2f, 0));
        window.addKeyCallback("toggleRotation", GLFW_KEY_F, (action, mod) -> toggleRotationLock());
        /*state.getEvent(Events.MOUSE_CLICK).addListener(new Action( "placeObject") {
            @Override
            protected void redoInternal() {
                assert getContext() != null;
                if ((int) getContext().data().get("button") != GLFW_MOUSE_BUTTON_RIGHT) return;
                objects.add(new GameObject(Mesh.loadMesh("cube", Texture.getTexture("cube"))));
                Vector3f pos = new Vector3f(renderer.getCamera().getPosition());
                objects.getLast().setPosition(pos.add(getViewDirection().mul(4)));
                objects.getLast().setScale(.1f);
            }

            @Override
            protected void undoInternal() {
                objects.removeLast();
            }

            @Override
            public boolean undoable() {
                return true;
            }
        }, "placeOnClick");*/
        objects = new ArrayList<>();
        GameObject screen = new GameObject(Mesh.loadMesh(Resources.model("screen"), Texture.getTexture("cube")));
        screen.setPosition(FuncButton.X_POS, FuncButton.Y_POS, FuncButton.Z_POS);
        TextObject screenText = new TextObject("", FontTexture.loadFont(Resources.font()), new Vector3f(0, -.5f, -.51f));
        screenText.setDynamicTextSupplier((gameState) -> {
            String prefix = gameState.inShop ? "$" + gameState.numToString(gameState.getMoney()) : "Goal: " + gameState.numToString(gameState.getGoal());
            String suffix = gameState.getScreen();
            StringBuilder out = new StringBuilder(prefix);
            while (out.length() + suffix.length() < 16) out.append(' ');
            out.append(suffix);
            return out.toString();
        });
        screen.addChild(screenText, true);
        screen.setScale(.5f);
        objects.add(screen);
        lights = new Lights();
        lights.addLight(new SpotLight(new PointLight(new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), 2), new Vector3f(0, 0, -1), 90));
        //lights.addLight(new PointLight(new Vector3f(1), new Vector3f(0, 0, -10), 5));
        window.setCursorPos(window.getWidth() / 2.0, window.getHeight() / 2.0);
        initialised = true;
    }

    public void toggleRotationLock() {
        lockRotation = !lockRotation;
        if (!lockRotation) window.setCursorPos(window.getWidth() / 2.0, window.getHeight() / 2.0);
    }

    public void addKeyHoldCallback(String id, int key, Runnable callback) {
        keyHoldCallbacks.put(id, callback);
        if (!keysToCallbackIds.containsKey(key)) keysToCallbackIds.put(key, new ArrayList<>());
        keysToCallbackIds.get(key).add(id);
    }

    public void render(Window window) {
        renderer.render(window, objects, lights);
    }

    public void update() {
        cleanupDestroyedObjects();
        selectGameObject(objects, renderer.getCamera());
        objects.forEach((obj) -> obj.update(state));
    }

    public void cleanupDestroyedObjects() {
        objects.forEach(GameObject::cleanupDestroyedChildren);
        objects.removeIf(GameObject::isDestroyed);
    }

    public void input() {
        for (int key : keysToCallbackIds.keySet()) {
            if (window.isKeyPressed(key)) {
                for (String callbackId : keysToCallbackIds.get(key)) {
                    if (keyHoldCallbacks.get(callbackId) != null)
                        keyHoldCallbacks.get(callbackId).run();
                }
            }
        }
        if (!lockRotation) {
            window.hideCursor();
            Vector2f rotation = window.getMouse().sub(prevMousePos);
            renderer.getCamera().moveRotation(rotation.y * MOUSE_SENSITIVITY, rotation.x * MOUSE_SENSITIVITY, 0);
            window.setCursorPos(window.getWidth() / 2.0, window.getHeight() / 2.0);
        } else {
            window.showCursor();
        }
        //window.updateSize();
    }

    public GameLoop(GameState state) {
        init(state);
    }

    public void loop() {
        try {
            double secsPerUpdate = 1.0d / 30.0d;
            double previous = Utils.getTime();
            double steps = 0.0;
            while (!window.shouldClose()) {
                double loopStartTime = Utils.getTime();
                double elapsed = loopStartTime - previous;
                previous = loopStartTime;
                steps += elapsed;
                window.swapBuffers();
                glfwPollEvents();
                input();
                while (steps >= secsPerUpdate) {
                    update();
                    steps -= secsPerUpdate;
                }
                render(window);
                sync(loopStartTime);
            }
        } catch (Exception exception) {
            LOGGER.error("Caught unexpected exception, exiting", exception);
        } finally {
            window.destroy();
            cleanup();
        }
    }

    private void sync(double loopStartTime) {
        float loopSlot = 1f / 50;
        double endTime = loopStartTime + loopSlot;
        while (Utils.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.info("Thread was interrupted");
                throw new RuntimeException(e);
            }
        }
    }

    public Vector3f getViewDirection() {
        Vector3f dir = new Vector3f();
        Camera camera = renderer.getCamera();
        if (lockRotation) {
            //float dist = window.getMouse().sub(new Vector2f(window.getWidth()/2f, window.getHeight()/2f)).length()/64;
            dir.add(camera.getViewMatrix().positiveX(new Vector3f()).mul(-window.getMouse().x + window.getWidth()/2f).div(2));
            dir.add(camera.getViewMatrix().positiveY(new Vector3f()).mul(window.getMouse().y - window.getHeight()/2f).div(2));
            dir.add(camera.getViewMatrix().positiveZ(new Vector3f()).mul(Renderer.Z_FAR + Renderer.Z_NEAR).div(2));
        } else dir = camera.getViewMatrix().positiveZ(dir);
        return dir.normalize().negate();
    }

    public void selectGameObject(List<GameObject> gameObjects, Camera camera) {
        GameObject selectedGameObject = null;
        Matrix4f viewMatrix = renderer.getTransformation().getViewMatrix(camera);
        float closestDistance = Float.POSITIVE_INFINITY;
        Vector3f dir = getViewDirection();
        for (GameObject gameObject : gameObjects) {
            gameObject.setSelected(false);
            float result = gameObject.intersectRay(camera.getPosition(), dir, renderer.getTransformation(), viewMatrix);
            if (Float.isFinite(result) && result >= 0 && result < closestDistance) {
                closestDistance = result;
                selectedGameObject = gameObject;
            }
        }

        if (selectedGameObject != null) {
            selectedGameObject.setSelected(true);
        }
    }

    public void addObject(GameObject obj) {
        LOGGER.trace("Adding a game object at {}", obj.getPosition());
        if (!initialised) {
            LOGGER.warn("Attempt to add an object when game loop has not yet completed initialization!");
            return;
        }
        objects.add(obj);
    }

    public void cleanup() {
        renderer.cleanup();
        for (GameObject gameObject : objects) {
            gameObject.getMesh().cleanup();
        }
        Texture.cleanup();
        glfwTerminate();
    }
}
