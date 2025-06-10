package com.calcgame.main.rendering;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.lwjgl.opengl.GL30.*;

public class Texture {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, Texture> loadedTextures = new HashMap<>();
    private final int id;
    private final int width;
    private final int height;

    private Texture(String fileName) throws IOException {
        this(Texture.class.getResourceAsStream(fileName));
    }

    Texture(InputStream stream) throws IOException {
        PNGDecoder decoder = new PNGDecoder(stream);
        ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
        decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
        buf.flip();
        id = glGenTextures();
        width = decoder.getWidth();
        height = decoder.getHeight();
        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public Texture(int width, int height, int pixelFormat) {
        this.id = glGenTextures();
        this.width = width;
        this.height = height;
        glBindTexture(GL_TEXTURE_2D, this.id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, this.width, this.height, 0, pixelFormat, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public static Texture getTexture(String fileName) {
        if (loadedTextures.containsKey(fileName))
            return loadedTextures.get(fileName);
        try {
            Texture texture = new Texture(Resources.texture(fileName));
            loadedTextures.put(fileName, texture);
            return texture;
        } catch (IOException e) {
            if (Objects.equals(fileName, "cube")) {
                LOGGER.warn("Cannot find fallback texture ({} at {}), returning null", fileName, Resources.texture(fileName));
                return null;
            }
            LOGGER.warn("Missing texture: {} (at {})", fileName, Resources.texture(fileName));
            return getTexture("cube");
        }
    }

    public static void cleanup() {
        LOGGER.info("Cleaning up loaded textures");
        for (String fileName : loadedTextures.keySet()) {
            glDeleteBuffers(loadedTextures.get(fileName).getId());
        }
        loadedTextures.clear();
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Texture texture = (Texture) o;
        return id == texture.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
