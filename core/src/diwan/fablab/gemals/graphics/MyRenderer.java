package diwan.fablab.gemals.graphics;

import java.util.ArrayDeque;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;


public class MyRenderer implements Disposable {

	protected static ShaderProgram createShaderProgram(String vertexShader, String fragShader) {
		ShaderProgram.pedantic = false;
		ShaderProgram shader = new ShaderProgram(vertexShader, fragShader);
		String log = shader.getLog();
		if (!shader.isCompiled())
			throw new GdxRuntimeException(log);		
		if (log!=null && log.length()!=0)
			System.out.println("Shader Log: "+log);
		return shader;
	}

	private final int maxVertices = 128000;
	private final int maxIndices = 384000;

	//The array which holds all the data, interleaved like so:
	//    x, y, r, g, b, a
	//    x, y, r, g, b, a, 
	//    x, y, r, g, b, a, 
	//    ... etc ...
	private final float[] vertices;
	private final short[] indices;

	//public MyCamera camera;
	private final Mesh mesh;
	private final Mesh skyMesh;

	private final ShaderProgram defaultShader;
	private final ShaderProgram skyShader;

	private final Color color = new Color(1, 1, 1, 1);
	private Affine2 transform;
	public final ArrayDeque<Affine2> matrixStack = new ArrayDeque<>();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Vector2 tmpv1 = new Vector2();
	private final Vector2 tmpv2 = new Vector2();
	private final Vector2 tmpv3 = new Vector2();
	private int numVertices;
	private int numIndices;
	private int vertexIdx;

	private Texture skyTexture;

	
	public MyRenderer() {
		String vertexShader = Gdx.files.internal("shaders/default_vertex.glsl").readString();
		String fragmentShader = Gdx.files.internal("shaders/default_fragment.glsl").readString();
		defaultShader = MyRenderer.createShaderProgram(vertexShader,fragmentShader);

		vertexShader = Gdx.files.internal("shaders/sky_vertex.glsl").readString();
		fragmentShader = Gdx.files.internal("shaders/sky_fragment.glsl").readString();
		skyShader = MyRenderer.createShaderProgram(vertexShader, fragmentShader);

		mesh = new Mesh(false, maxVertices, maxIndices,
				new VertexAttribute(Usage.Position, 2, "a_position"),
				new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));

		skyMesh = new Mesh(true, 4, 6,
				new VertexAttribute(Usage.Position, 2, "a_position"));
		skyMesh.setVertices(new float[] {-1f, -1f, 1f, -1f, 1f, 1f, -1, 1f});
		skyMesh.setIndices(new short[] {0, 2, 3, 0, 1, 2});

		vertices = new float[maxVertices * (mesh.getVertexAttributes().vertexSize / 4)];
		numVertices = 0;
		vertexIdx = 0;
		indices = new short[maxIndices];

		matrixStack.push(new Affine2());
		transform = matrixStack.getFirst();

		skyTexture = new Texture(Gdx.files.internal("textures/sky.png"));

		System.out.println("Renderer created");
	}

	//public void setCamera(MyCamera cam) { camera = cam;	}
	
	public void setColor(float r, float g, float b, float a) {
		this.color.set(r, g, b, a);
	}
	
	public void setColor(Color color) {
		this.color.set(color);
	}
	
	public void setProjectionMatrix(Matrix4 matrix) {
		projectionMatrix.set(matrix);
	}


	public void translate(float x, float y) {
		transform.translate(x, y);
	}


	public void scale(float s) {
		transform.scale(s, s);
	}


	public void pushMatrix() {
		matrixStack.push(transform);
		transform = new Affine2();
		transform.preMul(matrixStack.getFirst());
	}

	public void pushMatrix(Affine2 matrix) {
		matrixStack.push(transform);
		transform = new Affine2(matrix);
		transform.preMul(matrixStack.getFirst());
	}
	
	public void popMatrix() {
		transform = matrixStack.pop();
	}


	public void triangles(float[] vertices, short[] indices) {
		for (int i = 0; i < vertices.length; i += 2) {
			tmpv1.set(vertices[i], vertices[i+1]);
			transform.applyTo(tmpv1);
			this.vertices[vertexIdx++] = tmpv1.x;
			this.vertices[vertexIdx++] = tmpv1.y;
			this.vertices[vertexIdx++] = color.r;
			this.vertices[vertexIdx++] = color.g;
			this.vertices[vertexIdx++] = color.b;
			this.vertices[vertexIdx++] = color.a;
		}
		for (short index : indices) {
			this.indices[numIndices++] = (short) (index + numVertices);
		}
		numVertices += vertices.length/2;
	}


	/*
	public void texturedSquare(float xPos, float yPos, float width, float height, Vector2 uv0, Vector2 uv1, Vector2 uv2) {	}
	*/

	/** Calls circle(Vector2, float, int)} by estimating the number of segments needed for a smooth circle. */
	/*
	public void circle (Vector2 pos, float radius) {
		circle(pos.x, pos.y, radius, Math.max(1,  ((int) Math.sqrt(radius*camera.PPU))<<2 ));
	}*/

	public void renderSky(float timeOfDay) {
		skyTexture.bind();
		skyShader.bind();
		skyShader.setUniformi("u_texture", 0);

		if (timeOfDay >= 0.5f) timeOfDay = 1.0f - timeOfDay;
		skyShader.setUniformf("u_time", timeOfDay);

		skyMesh.render(skyShader, GL20.GL_TRIANGLES);
	}


	public void flush() {
		if (numVertices == 0) return;

		//if (MathUtils.random() < 0.01f)
		//	Gdx.app.log("renderer", "n_verts: " + numVertices + ", n_indices: " + numIndices);

		//no need for depth...
		//Gdx.gl.glDepthMask(false);
		//enable blending, for alpha
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		defaultShader.bind();
		defaultShader.setUniformMatrix("u_projModelView", projectionMatrix);

		mesh.setVertices(vertices, 0, vertexIdx);
		mesh.setIndices(indices, 0, numIndices);
		mesh.render(defaultShader, GL20.GL_TRIANGLES);

		vertexIdx = 0;
		numVertices = 0;
		numIndices = 0;

		//re-enable depth to reset states to their default
		//Gdx.gl.glDepthMask(true);
	}
	
	public void dispose() {
		mesh.dispose();
		skyMesh.dispose();
		defaultShader.dispose();
		skyShader.dispose();
	}
}
