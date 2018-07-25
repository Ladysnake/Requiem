#version 120

uniform sampler2D DiffuseSampler;

uniform float Time;  // this can be used for animations

varying vec2 texCoord;

void main(){
    vec4 tex = texture2D(DiffuseSampler, texCoord);
    tex.r = tex.g;
    gl_FragColor = tex;
}
