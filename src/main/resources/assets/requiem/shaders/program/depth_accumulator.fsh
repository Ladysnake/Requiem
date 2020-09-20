#version 110

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;

void main() {
    vec4 tex = texture2D(DiffuseSampler, texCoord);
    gl_FragColor = vec4(gl_FragCoord.z, 1. - gl_FragCoord.z, 0., tex.a);
}
