#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D OverlaySampler;

uniform vec4 ColorModulate;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 texDiffuse = texture(DiffuseSampler, texCoord);
    vec4 texOverlay = texture(OverlaySampler, texCoord);
    fragColor = vec4(mix(texDiffuse.rgb, texOverlay.rgb, texOverlay.a), 1.);
}
