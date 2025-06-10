package com.calcgame.main.rendering;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private static final Logger LOGGER = LogManager.getLogger();

    private final int programId;
    private final Map<String, Integer> uniforms;

    private int vertexShaderId = 0;
    private int fragmentShaderId = 0;

    public ShaderProgram() {
        programId = glCreateProgram();
        uniforms = new HashMap<>();
        if (programId == 0) LOGGER.error("Failed to create shader program!");
    }

    public void setVertexShader(String code) {
        if (vertexShaderId == 0) vertexShaderId = createShader(code, GL_VERTEX_SHADER);
        else LOGGER.warn("Overwriting existing vertex shader source code! (shader id is {})", vertexShaderId);
    }

    public void setFragmentShader(String code) {
        if (fragmentShaderId == 0) fragmentShaderId = createShader(code, GL_FRAGMENT_SHADER);
        else LOGGER.warn("Overwriting existing fragment shader source code! (shader id is {})", fragmentShaderId);
    }

    protected int createShader(String code, int type) {
        int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            LOGGER.error("Error creating shader. Type: {}", type);
        }
        glShaderSource(shaderId, code);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            LOGGER.error("Error compiling shader code: {}", glGetShaderInfoLog(shaderId, 1024));
        }
        glAttachShader(programId, shaderId);
        return shaderId;
    }

    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            LOGGER.warn("Could not find uniform: {}", uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, Vector3f value) {
        validateUniform(uniformName);
        glUniform3f(uniforms.get(uniformName), value.x ,value.y, value.z);
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    private void validateUniform(String uniformName) {
        if (!uniforms.containsKey(uniformName)) {
            LOGGER.error("Invalid uniform: {}", uniformName);
        }
    }

    public void createPointLightUniform(String uniformName) {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
    }

    public void createMaterialUniform(String uniformName) {
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
        createUniform(uniformName + ".reflectance");
    }

    public void setUniform(String uniformName, PointLight pointLight) {
        setUniform(uniformName + ".color", pointLight.getColor());
        setUniform(uniformName + ".position", pointLight.getPosition());
        setUniform(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName + ".att.constant", att.constant());
        setUniform(uniformName + ".att.linear", att.linear());
        setUniform(uniformName + ".att.exponent", att.exponent());
    }

    public void setUniform(String uniformName, SpotLight spotLight) {
        setUniform(uniformName + ".pl", spotLight.getPointLight());
        setUniform(uniformName + ".conedir", spotLight.getConeDir());
        setUniform(uniformName + ".cutoff", spotLight.getCutoff());
    }

    public void setUniform(String uniformName, DirLight dirLight) {
        setUniform(uniformName + ".direction", dirLight.getDirection());
        setUniform(uniformName + ".color", dirLight.getColor());
        setUniform(uniformName + ".intensity", dirLight.getIntensity());
    }

    public void setUniform(String uniformName, Material material) {
        setUniform(uniformName + ".ambient", material.getAmbientColour());
        setUniform(uniformName + ".diffuse", material.getDiffuseColour());
        setUniform(uniformName + ".specular", material.getSpecularColour());
        setUniform(uniformName + ".reflectance", material.getReflectance());
    }

    public void setLightUniforms(String pointLightUniform, String spotLightUniform, String dirLightUniform, Lights lights, Transformation transformation) {
        int i = 0;
        for (PointLight pointLight : lights.getPointLights()) {
            setUniform("%s[%d]".formatted(pointLightUniform, i), transformation.transformLight(pointLight));
            i++;
        }
        i = 0;
        for (SpotLight spotLight : lights.getSpotLights()) {
            setUniform("%s[%d]".formatted(spotLightUniform, i), transformation.transformLight(spotLight));
            i++;
        }
        i = 0;
        for (DirLight dirLight : lights.getDirLights()) {
            setUniform("%s[%d]".formatted(dirLightUniform, i), dirLight);
            i++;
        }
    }

    public void link() {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            LOGGER.error("Error linking shader code: {}", glGetProgramInfoLog(programId, 1024));
        }
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            LOGGER.warn("Warning validating shader code: {}", glGetProgramInfoLog(programId, 1024));
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    public void createLightUniforms(String pointLights, String spotLights, String dirLight, int cnt) {
        for (int i = 0; i < cnt; i++) {
            createPointLightUniform("%s[%d]".formatted(pointLights, i));
            createSpotLightUniform("%s[%d]".formatted(spotLights, i));
            createDirLightUniform("%s[%d]".formatted(dirLight, i));
        }
    }

    public void createSpotLightUniform(String unformName) {
        createPointLightUniform(unformName + ".pl");
        createUniform(unformName + ".conedir");
        createUniform(unformName + ".cutoff");
    }

    public void createDirLightUniform(String uniformName) {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".intensity");
    }
}
