#version 120

varying vec2 texcoord;
varying vec3 normal;

void main(void) {
  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
  texcoord = vec2(gl_MultiTexCoord0);
  normal = normalize(vec3(gl_NormalMatrix * gl_Normal ));
}
