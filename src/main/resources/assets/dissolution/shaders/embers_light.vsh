#version 120
varying vec3 normal;
varying vec4 color;
varying float shift;
varying vec3 position;
varying vec4 uv;
uniform int time;
uniform int chunkX;
uniform int chunkY;
uniform int chunkZ;
uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform mat4 modelview;

float rand2(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec3 rand3(vec3 co){
    return vec3(rand2(co.xz)-0.5f,rand2(co.yx)-0.5f,rand2(co.zy)-0.5f);
}

void main()
{
    vec4 pos = gl_ModelViewProjectionMatrix * gl_Vertex;

	normal = gl_Normal;
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

	position = gl_Vertex.xyz+vec3(chunkX,chunkY,chunkZ);

	gl_TexCoord[0] = gl_MultiTexCoord0;
	gl_TexCoord[1] = gl_MultiTexCoord1;

	gl_FrontColor = gl_Color;
}
