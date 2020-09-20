#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D PlayerSampler;
uniform sampler2D PlayerDepthSampler;

varying vec2 texCoord;

void main(){
    vec4 tex = texture2D(DiffuseSampler, texCoord);
    float depth = texture2D(DepthSampler, texCoord).x;
    vec4 packedPlayerDepth = texture2D(PlayerDepthSampler, texCoord);
    float playerNear = packedPlayerDepth.r;
    float playerFar = 1. - packedPlayerDepth.g;
    float playerAlpha = texture2D(PlayerSampler, texCoord).r;

    if (depth <= playerFar) { // scene intersects with player
        gl_FragColor = vec4(1., 1., 0., 1.);
    } else {    // scene behind player
        gl_FragColor = vec4(mix(vec3(dot(tex.rgb, vec3(0.2125, 0.7154, 0.0721))), tex.rgb, 1 - playerAlpha), tex.a);
    }
}
