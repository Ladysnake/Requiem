#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

varying vec2 texCoord;

void main(){
    float depth = texture2D(DepthSampler, texCoord).x;
    vec4 packedPlayerData = texture2D(DiffuseSampler, texCoord);
    float playerNear = packedPlayerData.r;
    if (depth < playerNear) { // scene obscures player
        gl_FragColor = vec4(0.);
    } else {
        gl_FragColor = vec4(packedPlayerData.a);
    }
}
