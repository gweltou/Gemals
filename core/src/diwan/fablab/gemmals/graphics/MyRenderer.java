package diwan.fablab.gemmals.graphics;

import java.util.ArrayDeque;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;


public class MyRenderer implements Disposable {
	public static final String VERT_SHADER =  
			"attribute vec2 a_position;\n" +
			"attribute vec4 a_color;\n" +			
			"uniform mat4 u_projModelView;\n" + 
			"varying vec4 vColor;\n" +			
			"void main() {\n" +  
			"	vColor = a_color;\n" +
			"	gl_Position =  u_projModelView * vec4(a_position.xy, 0.0, 1.0);\n" +
			"}";
	
	public static final String FRAG_SHADER = 
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
			"varying vec4 vColor;\n" + 			
			"void main() {\n" +  
			"	gl_FragColor = vColor;\n" + 
			"}";
	
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

	private final int maxVertices = 32000;
	private final int maxIndices = 96000;

	//The array which holds all the data, interleaved like so:
	//    x, y, r, g, b, a
	//    x, y, r, g, b, a, 
	//    x, y, r, g, b, a, 
	//    ... etc ...
	private final float[] vertices;
	private final short[] indices;

	//public MyCamera camera;
	private final Mesh mesh;
	private final ShaderProgram shader;
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

	
	public MyRenderer() {
		shader = createShaderProgram(VERT_SHADER, FRAG_SHADER);

		mesh = new Mesh(false, maxVertices, maxIndices,
				new VertexAttribute(Usage.Position, 2, "a_position"),
				new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));

		vertices = new float[maxVertices * (mesh.getVertexAttributes().vertexSize / 4)];
		numVertices = 0;
		vertexIdx = 0;
		indices = new short[maxIndices];

		matrixStack.push(new Affine2());
		transform = matrixStack.getFirst();

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
		matrixStack.pop();
		transform = matrixStack.getFirst();
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
	private void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
		//we don't want to hit any index out of bounds exception...
		//so we need to flush the batch if we can't store any more verts
		if (numVertices == maxVertices)
			flush();

		//now we push the vertex data into our array
		//we are assuming (0, 0) is lower left, and Y is up
		rawVertex(x1, y1);	//bottom left vertex
		indices[numIndices++] = (short) (numVertices-1);
		rawVertex(x2, y2);	//top left vertex
		indices[numIndices++] = (short) (numVertices-1);
		rawVertex(x3, y3);	//bottom right vertex
		indices[numIndices++] = (short) (numVertices-1);
	}
	 */

	/*
	public void texturedSquare(float xPos, float yPos, float width, float height, Vector2 uv0, Vector2 uv1, Vector2 uv2) {	}
	*/

	/** Calls circle(Vector2, float, int)} by estimating the number of segments needed for a smooth circle. */
	/*
	public void circle (Vector2 pos, float radius) {
		circle(pos.x, pos.y, radius, Math.max(1,  ((int) Math.sqrt(radius*camera.PPU))<<2 ));
	}*/

	/*
	private void rawVertex(float x, float y) {
		vertices[vertexIdx++] = x;
		vertices[vertexIdx++] = y;
		vertices[vertexIdx++] = color.r;
		vertices[vertexIdx++] = color.g;
		vertices[vertexIdx++] = color.b;
		vertices[vertexIdx++] = color.a;
		numVertices++;
	}
	 */


	public void flush() {
		if (numVertices == 0) return;

		//Gdx.app.log("renderer", "n_verts: " + numVertices + ", n_indices: " + numIndices);

		//no need for depth...
		//Gdx.gl.glDepthMask(false);
		//enable blending, for alpha
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		shader.bind();
		shader.setUniformMatrix("u_projModelView", projectionMatrix);

		mesh.setVertices(vertices, 0, vertexIdx);
		mesh.setIndices(indices, 0, numIndices);
		mesh.render(shader, GL20.GL_TRIANGLES);

		vertexIdx = 0;
		numVertices = 0;
		numIndices = 0;

		//re-enable depth to reset states to their default
		//Gdx.gl.glDepthMask(true);
	}
	
	public void dispose() {
		mesh.dispose();
		shader.dispose();
	}
}
