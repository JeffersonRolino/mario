package cosmos;

import components.FontRenderer;
import components.SpriteRenderer;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import renderer.Shader;
import renderer.Texture;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    private int vertexID, fragmentID, shaderProgram;

    private float[] vertexArray = {
         // Positions               Color                        UV Coordinates
         100.0f, -0.5f,   0.0f,     1.0f, 0.0f, 0.0f, 1.0f,      1, 0, // Bottom right - 0
        -0.5f,    100.0f, 0.0f,     0.0f, 1.0f, 0.0f, 1.0f,      0, 1, // Top left     - 1
         100.0f,  100.0f, 0.0f,     0.0f, 0.0f, 1.0f, 1.0f,      1, 1, // Top right    - 2
        -0.5f,   -0.5f,   0.0f,     1.0f, 1.0f, 0.0f, 1.0f,      0, 0  // Bottom left  - 3
    };

    // IMPORTANT: Must be in counter-clockwise order
    private int[] elementArray = {
        2, 1, 0, // Top right triangle
        0, 1, 3  // Bottom left triangle
    };

    private int vaoID, vboID, eboID;

    private Shader defaultShader;
    private Texture testTexture;

    GameObject testObj;
    private boolean firstTime = false;

    public LevelEditorScene(){
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();
    }

    @Override
    public void init(){
        System.out.println("Creating 'test object'");
        this.testObj = new GameObject("test object");
        this.testObj.addComponent(new SpriteRenderer());
        this.testObj.addComponent(new FontRenderer());
        this.addGameObject(this.testObj);

        this.camera = new Camera(new Vector2f());

        this.testTexture = new Texture("assets/images/testImage.png");

        // ************************************************************************
        //  Generate VAO, VBO, and EBO buffer objects, and send to GPU
        // ************************************************************************
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create VBO upload the vertex buffer
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionSize = 3;
        int colorSize = 4;
        int uvSize = 2;
        int vertexSizeBytes = (positionSize + colorSize + uvSize) * Float.BYTES;

        glVertexAttribPointer(0, positionSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionSize * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (positionSize + colorSize) * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    @Override
    public void update(float dt) {
//        camera.position.x -= dt * 50.0f;

        defaultShader.use();

        // Upload texture to shader
        defaultShader.uploadTexture("TEX_SAMPLER", 0);
        glActiveTexture(GL_TEXTURE0);
        testTexture.bind();

        defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());
        defaultShader.uploadFloat("uTime", Time.getTime());

        // Bind the VAO that we are using
        glBindVertexArray(vaoID);

        // Enable the vertex attribute pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbing everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        defaultShader.detach();

        if(!firstTime){
            System.out.println("Creating Game Object 2 in Runtime!");
            GameObject go = new GameObject("game object 2");
            go.addComponent(new SpriteRenderer());
            this.addGameObject(go);
            firstTime = true;
        }

        for(GameObject gameObject : this.gameObjects){
            gameObject.update(dt);
        }
    }
}
