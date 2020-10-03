#version 110

uniform sampler2D DiffuseSampler;
uniform sampler2D OverlaySampler;

uniform vec4 ColorModulate;

varying vec2 texCoord;

void main(){
    vec4 texDiffuse = texture2D(DiffuseSampler, texCoord);
    vec4 texOverlay = texture2D(OverlaySampler, texCoord);
    gl_FragColor = vec4(mix(texDiffuse.rgb, texOverlay.rgb, texOverlay.a), 1.);
}
