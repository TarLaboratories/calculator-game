package com.calcgame.main.objects;

import com.calcgame.main.rendering.FontTexture;
import com.calcgame.main.rendering.Mesh;
import com.calcgame.main.rendering.Texture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class TextObject extends ScreenObject {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final float Z_POS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;
    private String text;
    private final FontTexture fontTexture;
    private int width;
    private boolean centered;

    public TextObject(String text, FontTexture fontTexture) {
        this.text = text;
        this.fontTexture = fontTexture;
        this.setMesh(buildMesh());
    }

    private Mesh buildMesh() {
        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Texture texture = fontTexture.getTexture();
        float x = 0;
        for (int i = 0; i < text.length(); i++) {
            FontTexture.CharInfo charInfo = fontTexture.getCharacterInfo(text.charAt(i));

            // Left Top vertex
            positions.add(x);
            positions.add(0.0f);
            positions.add(Z_POS);
            textCoords.add((float) charInfo.startX() / (float) texture.getWidth());
            textCoords.add(0f);
            indices.add(i*VERTICES_PER_QUAD);

            // Left Bottom vertex
            positions.add(x);
            positions.add((float) texture.getHeight());
            positions.add(Z_POS);
            textCoords.add((float) charInfo.startX() / texture.getWidth());
            textCoords.add(1f);
            indices.add(i*VERTICES_PER_QUAD + 1);

            // Right Bottom vertex
            positions.add(x + charInfo.width()); // x
            positions.add((float) texture.getHeight()); //y
            positions.add(Z_POS); //z
            textCoords.add((float) (charInfo.startX() + charInfo.width()) / texture.getWidth());
            textCoords.add(1f);
            indices.add(i*VERTICES_PER_QUAD + 2);

            // Right Top vertex
            positions.add(x + charInfo.width()); // x
            positions.add(0.0f); //y
            positions.add(Z_POS); //z
            textCoords.add((float) (charInfo.startX() + charInfo.width()) / texture.getWidth());
            textCoords.add(0f);
            indices.add(i*VERTICES_PER_QUAD + 3);

            // Add indices for left top and bottom right vertices
            indices.add(i*VERTICES_PER_QUAD);
            indices.add(i*VERTICES_PER_QUAD + 2);

            normals.add(0f);
            normals.add(0f);
            normals.add(1f);
            normals.add(0f);
            normals.add(0f);
            normals.add(1f);
            x += charInfo.width();
        }
        width = (int) x;
        if (centered) {
            for (int i = 0; i < positions.size(); i += 3) {
                positions.set(i, positions.get(i) - getWidth()/2f);
                positions.set(i + 1, positions.get(i + 1) - getHeight()/2f);
            }
        }
        Mesh mesh = new Mesh(positions, textCoords, normals, indices);
        mesh.setTexture(texture);
        return mesh;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        rebuildMesh();
    }

    @Override
    public void setScale(float scale) {
        super.setScale(scale/32);
    }

    @Override
    public void setRotation(float x, float y, float z) {
        super.setRotation(x, y + 180, z + 180);
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return fontTexture.getTexture().getHeight();
    }

    @Override
    public void setCentered(boolean centered) {
        if (centered == this.centered) return;
        this.centered = centered;
        rebuildMesh();
    }

    @Override
    public Vector2f getPadding() {
        return new Vector2f(.5f, .5f);
    }

    private void rebuildMesh() {
        this.getMesh().cleanup();
        this.setMesh(buildMesh());
    }
}
