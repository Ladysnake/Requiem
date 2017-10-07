#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

void main() {
    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;

    for(float r = -Radius; r <= Radius; r += 1.0) {
        float strength = (Radius+1.0-abs(r))/Radius;
        totalStrength += strength;
        blurred += texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir) * strength;
    }

    gl_FragColor = vec4(blurred.rgb / totalStrength, 1.0);
}
