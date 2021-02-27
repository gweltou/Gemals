#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_position;
uniform sampler2D u_texture;
uniform float u_time;
//varying vec2 v_fragCoord;


float Noise2d(vec2 coord) {
	return fract(sin(dot(coord.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

// Source : https://www.shadertoy.com/view/Md2SR3
// Convert Noise2d() into a "star field" by stomping everthing below fThreshhold to zero.
float NoisyStarField(vec2 vSamplePos, float fThreshhold) {
	float StarVal = Noise2d( vSamplePos );
	if ( StarVal >= fThreshhold )
		StarVal = pow( (StarVal - fThreshhold)/(1.0 - fThreshhold), 6.0 );
	else
		StarVal = 0.0;
	return StarVal;
}


void main() {
    //gl_FragColor = vec4(0.0, 1.0, 0.2, 1.0);
	vec4 col = texture2D(u_texture, vec2(clamp(u_time - v_position.y*0.2, 0.0, 1.0), 0.0));
	float starVal = NoisyStarField(v_position, 0.98) * abs(u_time*2.0 - 1.0);
	col += vec4(starVal, starVal, starVal, 1.0);
	gl_FragColor = col;
}