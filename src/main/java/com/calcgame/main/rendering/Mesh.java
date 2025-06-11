package com.calcgame.main.rendering;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import com.calcgame.main.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

public class Mesh {
    private static final Logger LOGGER = LogManager.getLogger();

    public Mesh(List<Float> positions, List<Float> textCoords, List<Float> normals, List<Integer> indices) {
        this(Utils.toFloatArray(positions), Utils.toFloatArray(textCoords), Utils.toFloatArray(normals), Utils.toIntArray(indices));
    }

    public Material getMaterial() {
        return material;
    }

    protected static class Face {
        private final Mesh.IdxGroup[] idxGroups;

        public Face(String v1, String v2, String v3) {
            idxGroups = new Mesh.IdxGroup[3];
            idxGroups[0] = parseLine(v1);
            idxGroups[1] = parseLine(v2);
            idxGroups[2] = parseLine(v3);
        }

        private Mesh.IdxGroup parseLine(String line) {
            Mesh.IdxGroup idxGroup = new Mesh.IdxGroup();

            String[] lineTokens = line.split("/");
            int length = lineTokens.length;
            idxGroup.idxPos = Integer.parseInt(lineTokens[0]) - 1;
            if (length > 1) {
                String textCoord = lineTokens[1];
                idxGroup.idxTextCoord = !textCoord.isEmpty() ? Integer.parseInt(textCoord) - 1 : Mesh.IdxGroup.NO_VALUE;
                if (length > 2) {
                    idxGroup.idxVecNormal = Integer.parseInt(lineTokens[2]) - 1;
                }
            }

            return idxGroup;
        }

        public Mesh.IdxGroup[] getFaceVertexIndices() {
            return idxGroups;
        }
    }

    protected static class MeshParameters {
        public String fileName;
        public Texture texture;

        public MeshParameters(String fileName) {
            this.fileName = fileName;
        }

        public MeshParameters(String fileName, Texture texture) {
            this.fileName = fileName;
            this.texture = texture;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MeshParameters that = (MeshParameters) o;
            return Objects.equals(fileName, that.fileName) && Objects.equals(texture, that.texture);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileName, texture);
        }
    }

    private static final Map<MeshParameters, Mesh> meshes = new HashMap<>();

    private final List<Triangle> triangles;
    private Texture texture;
    private final Material material;
    private final int vaoId;

    private final int vboId;
    private final int idxVboId;
    private final int normalsVboId;
    private final int textCoordsVboId;

    private final int vertexCount;

    private int createVbo(int[] data) {
        int vboId = glGenBuffers();
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        buffer.put(data).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        MemoryUtil.memFree(buffer);
        return vboId;
    }

    private int createVbo(int index, float[] data, int size) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        MemoryUtil.memFree(buffer);
        return vboId;
    }

    private int createVbo(int index, float[] data) {
        return createVbo(index, data, 3);
    }

    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices) {
        this(positions, textCoords, normals, indices, new Material(
                new Vector4f(0f, 0f, 0f, 0), // new Vector4f(0.59f, 0.45f, 0.35f, 1),
                new Vector4f(0, 0, 0, 1),
                new Vector4f(0.59f, 0.45f, 0.35f, 1),
                true,
                1
        ));
    }

    public Mesh(float[] positions, float[] textCoords, float[] normals, int[] indices, Material material) {
        vertexCount = indices.length;
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        idxVboId = createVbo(indices);
        vboId = createVbo(0, positions);
        textCoordsVboId = createVbo(1, textCoords, 2);
        normalsVboId = createVbo(2, normals);
        triangles = new ArrayList<>();
        this.material = material;
        for (int i = 0; i < indices.length/3; i++) {
            triangles.add(new Triangle(
                    new Vector3f(
                            positions[3*indices[3*i]],
                            positions[3*indices[3*i] + 1],
                            positions[3*indices[3*i] + 2]
                    ),
                    new Vector3f(
                        positions[3*indices[3*i + 1]],
                        positions[3*indices[3*i + 1] + 1],
                        positions[3*indices[3*i + 1] + 2]
                    ),
                    new Vector3f(
                            positions[3*indices[3*i + 2]],
                            positions[3*indices[3*i + 2] + 1],
                            positions[3*indices[3*i + 2] + 2]
                    )
            ));
        }

        glBindVertexArray(0);
    }

    public List<Triangle> getTriangles() {
        return triangles;
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void render() {
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glActiveTexture(GL_TEXTURE0);
        if (isTextured())
            glBindTexture(GL_TEXTURE_2D, texture.getId());

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
        if (isTextured())
            glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glDeleteBuffers(idxVboId);
        glDeleteBuffers(normalsVboId);
        glDeleteBuffers(textCoordsVboId);
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public boolean isTextured() {
        return texture != null;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    private static Mesh loadMeshInternal(String fileName) {
        List<String> lines;
        try {
            lines = Utils.getFileContents(Resources.model(fileName)).lines().toList();
        } catch (FileNotFoundException e) {
            LOGGER.warn("Missing model file: {}", fileName);
            return loadMeshInternal(Resources.model("cube"));
        }

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Face> faces = new ArrayList<>();

        for (String line : lines) {
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v":
                    // Geometric vertex
                    Vector3f vec3f = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    vertices.add(vec3f);
                    break;
                case "vt":
                    // Texture coordinate
                    Vector2f vec2f = new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]));
                    textures.add(vec2f);
                    break;
                case "vn":
                    // Vertex normal
                    Vector3f vec3fNorm = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    normals.add(vec3fNorm);
                    break;
                case "f":
                    Face face = new Face(tokens[1], tokens[2], tokens[3]);
                    faces.add(face);
                    if (tokens.length > 4) {
                        faces.add(new Face(tokens[1], tokens[3], tokens[4]));
                    }
                    break;
                default:
                    // Ignore other lines
                    break;
            }
        }
        return reorderLists(vertices, textures, normals, faces);
    }

    private static Mesh reorderLists(List<Vector3f> posList, List<Vector2f> textCoordList,
                                     List<Vector3f> normList, List<Face> facesList) {

        List<Integer> indices = new ArrayList<>();
        for (Face face : facesList) {
            posList.add(new Vector3f(posList.get(face.idxGroups[0].idxPos)));
            face.idxGroups[0].idxPos = posList.size() - 1;
            posList.add(new Vector3f(posList.get(face.idxGroups[1].idxPos)));
            face.idxGroups[1].idxPos = posList.size() - 1;
            posList.add(new Vector3f(posList.get(face.idxGroups[2].idxPos)));
            face.idxGroups[2].idxPos = posList.size() - 1;
        }
        // Create position array in the order it has been declared
        float[] posArr = new float[posList.size() * 3];
        int i = 0;
        for (Vector3f pos : posList) {
            posArr[i * 3] = pos.x;
            posArr[i * 3 + 1] = pos.y;
            posArr[i * 3 + 2] = pos.z;
            i++;
        }
        float[] textCoordArr = new float[posList.size() * 2];
        float[] normArr = new float[posList.size() * 3];

        for (Face face : facesList) {
            IdxGroup[] faceVertexIndices = face.getFaceVertexIndices();
            for (IdxGroup indValue : faceVertexIndices) {
                processFaceVertex(indValue, textCoordList, normList,
                        indices, textCoordArr, normArr);
            }
        }
        int[] indicesArr;
        indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();
        return new Mesh(posArr, textCoordArr, normArr, indicesArr);
    }

    private static void processFaceVertex(IdxGroup indices, List<Vector2f> textCoordList,
                                          List<Vector3f> normList, List<Integer> indicesList,
                                          float[] texCoordArr, float[] normArr) {

        // Set index for vertex coordinates
        int posIndex = indices.idxPos;
        indicesList.add(posIndex);

        // Reorder texture coordinates
        if (indices.idxTextCoord >= 0) {
            Vector2f textCoord = textCoordList.get(indices.idxTextCoord);
            texCoordArr[posIndex * 2] = textCoord.x;
            texCoordArr[posIndex * 2 + 1] = 1 - textCoord.y;
        }
        if (indices.idxVecNormal >= 0) {
            // Reorder vector normals
            Vector3f vecNorm = normList.get(indices.idxVecNormal);
            normArr[posIndex * 3] = vecNorm.x;
            normArr[posIndex * 3 + 1] = vecNorm.y;
            normArr[posIndex * 3 + 2] = vecNorm.z;
        }
    }

    protected static class IdxGroup {
        public static final int NO_VALUE = -1;

        public int idxPos;
        public int idxTextCoord;
        public int idxVecNormal;

        public IdxGroup() {
            idxPos = NO_VALUE;
            idxTextCoord = NO_VALUE;
            idxVecNormal = NO_VALUE;
        }
    }

    public static Mesh loadMesh(String fileName) {
        MeshParameters params = new MeshParameters(fileName);
        if (!meshes.containsKey(params))
            meshes.put(params, loadMeshInternal(fileName));
        return meshes.get(params);
    }

    public static Mesh loadMesh(String fileName, Texture texture) {
        MeshParameters params = new MeshParameters(fileName, texture);
        if (!meshes.containsKey(params)) {
            Mesh mesh = loadMeshInternal(fileName);
            mesh.setTexture(texture);
            meshes.put(params, mesh);
        }
        return meshes.get(params);
    }

    public static Mesh quad(Vector3f a, Vector3f b, Vector3f c, Vector3f d, Vector3f normal, Vector4f color) {
        float[] vertices = Utils.getVertexArray(a, b, c, d);
        int[] indices = new int[] {
                1, 2, 3,
                1, 3, 4
        };
        float[] textCoords = new float[] {
                0, 0,
                0, 1,
                1, 1,
                1, 0
        };
        return new Mesh(vertices, textCoords, new float[] {normal.x, normal.y, normal.z}, indices, new Material(
                new Vector4f(color),
                new Vector4f(color),
                new Vector4f(color),
                false,
                1
        ));
    }
}