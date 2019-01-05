#version 120

varying vec4 vPosition;
varying vec2 texcoord;


void main(void) {
  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
  vPosition = gl_Position;
  texcoord = vec2(gl_MultiTexCoord0);
}
