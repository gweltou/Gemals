attribute vec2 a_position;
attribute vec4 a_color;	// Color of the vertices
uniform mat4 u_projModelView;
varying vec4 v_color;

void main() {
    v_color = a_color;
	gl_Position =  u_projModelView * vec4(a_position.xy, 0.0, 1.0);
}