#version 120

varying vec4 vPosition;
varying vec2 texcoord;

void main(void) {
  gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex * vec4(1, 1, 1, 1);
  vPosition = gl_Position + 1;
  texcoord = vec2(gl_MultiTexCoord0);
} ;
