package com.calcgame.main.rendering;

import com.calcgame.main.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.FileNotFoundException;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ShaderProgram shaderProgram, depthShaderProgram;
    private final Transformation transformation;
    private final ShadowMap shadowMap;
    private final Camera camera;

    public static final float FOV = (float) Math.toRadians(60.0f);
    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 1000.f;
    private final Vector3f ambientLight = new Vector3f(1f);
    @SuppressWarnings("FieldCanBeLocal")
    private final float ambientLightFactor = .25f;

    public Renderer() {
        shaderProgram = new ShaderProgram();
        transformation = new Transformation();
        camera = new Camera();
        try {
            shaderProgram.setVertexShader(Utils.getFileContents(Resources.shader("vertex.vs")));
            shaderProgram.setFragmentShader(Utils.getFileContents(Resources.shader("fragment.fs")));
            shaderProgram.link();
            LOGGER.debug("Successfully linked shader program!");
            glClearColor(0, 0, 0, 0);
            shaderProgram.createUniform("projectionMatrix");
            shaderProgram.createUniform("modelViewMatrix");
            shaderProgram.createUniform("txtSampler");
            shaderProgram.createUniform("ambientLight.color");
            shaderProgram.createUniform("ambientLight.factor");
            shaderProgram.createMaterialUniform("material");
            shaderProgram.createLightUniforms("pointLights", "spotLights", "dirLights", 5);
            shaderProgram.setUniform("txtSampler", 0);
            depthShaderProgram = new ShaderProgram();
            depthShaderProgram.setVertexShader(Utils.getFileContents(Resources.shader("depth_vertex.vs")));
            depthShaderProgram.setFragmentShader(Utils.getFileContents(Resources.shader("depth_fragment.fs")));
            depthShaderProgram.link();
            depthShaderProgram.createUniform("orthoProjectionMatrix");
            depthShaderProgram.createUniform("modelLightViewMatrix");
            shadowMap = new ShadowMap();
        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot load shaders: files do not exist!");
            throw new RuntimeException(e);
        }
    }

    public void render(Window window, List<GameObject> gameObjects, Lights lights) {
        clear();
        renderDepthMap(window, gameObjects, lights);
        glViewport(0, 0, window.getWidth(), window.getHeight());
        shaderProgram.bind();
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        shaderProgram.setUniform("ambientLight.color", ambientLight);
        shaderProgram.setUniform("ambientLight.factor", ambientLightFactor);
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);
        shaderProgram.setLightUniforms("pointLights", "spotLights", "dirLight", lights, transformation);
        for (GameObject gameObject : gameObjects) {
            gameObject.render(transformation, viewMatrix, shaderProgram);
        }
        shaderProgram.unbind();
    }

    public void renderDepthMap(Window window, List<GameObject> gameObjects, Lights lights) {
        glBindFramebuffer(GL_FRAMEBUFFER, shadowMap.getDepthMapFBO());
        glViewport(0, 0, ShadowMap.SHADOW_MAP_WIDTH, ShadowMap.SHADOW_MAP_HEIGHT);
        glClear(GL_DEPTH_BUFFER_BIT);
        depthShaderProgram.bind();
        //TODO make depth map
        depthShaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public Camera getCamera() {
        return camera;
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (depthShaderProgram != null) {
            depthShaderProgram.cleanup();
        }
    }

    public Transformation getTransformation() {
        return transformation;
    }
}
