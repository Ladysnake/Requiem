#version 120

varying vec2 texcoord;
varying vec4 vPosition;
uniform sampler2D bgl_RenderedTexture;
uniform int time; // Passed in, see ShaderHelper.java

float rand(vec2 co) {
   return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

float rand2(vec3 co) {
	return fract(sin(dot(co.xyz, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture2D(bgl_RenderedTexture, texcoord);

    float gs = (color.r + color.g + color.b) / 50.0;

    float r = color.r - rand(texcoord) * 0.25 + vPosition.r;
    float g = color.g - rand(texcoord) * 0.25 + vPosition.g;
    float b = color.b - rand(texcoord) * 0.25 + vPosition.b;

    	gl_FragColor = vec4(color.r, color.g, color.b, color.a * rand2(vec2(r, g, b))*0.5);
}
