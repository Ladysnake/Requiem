// a vertex shader that does not require any context to be passed through uniforms
#version 110

varying vec2 texCoord;

void main(){
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    texCoord = vec2(gl_MultiTexCoord0);
}
