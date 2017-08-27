#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

#define B 32 // table size
#define B2 66 // B*2 + 2
#define BR 0.03125 // 1 / B

uniform vec4 permu_grad[B2]; // permutation/gradient table

// this is the smoothstep function f(t) = 3t^2 - 2t^3, without the normalization
vec3 s_curve(vec3 t) {
    return t*t*( vec3(3.0, 3.0, 3.0) - vec3(2.0, 2.0, 2.0)*t);
}

vec2 s_curve(vec2 t) {
    return t*t*( vec2(3.0, 3.0) - vec2(2.0, 2.0)*t);
}

float s_curve(float t) {
    return t*t*(3.0-2.0*t);
}

float noise(vec2 v) {
    v = v + vec2(10000.0, 10000.0);

    vec2 i = fract(v * BR) * float(B); // index between 0 and B-1
    vec2 f = fract(v); // fractional position

    // lookup in permutation table
    vec2 p;
    p[0] = permu_grad[ int(i[0]) ].w;
    p[1] = permu_grad[ int(i[0]) + 1 ].w;
    p = p + i[1];

    // compute dot products between gradients and vectors
    vec4 r;
    r[0] = dot( permu_grad[ int(p[0]) ].xy, f);
    r[1] = dot( permu_grad[ int(p[1]) ].xy, f - vec2(1.0, 0.0) );
    r[2] = dot( permu_grad[ int(p[0]) + 1 ].xy, f - vec2(0.0, 1.0) );
    r[3] = dot( permu_grad[ int(p[1]) + 1 ].xy, f - vec2(1.0, 1.0) );

    // interpolate
    f = s_curve(f);
    r = mix( r.xyyy, r.zwww, f[1] );
    return mix( r.x, r.y, f[0] );
}

void main() {
    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;
    float totalAlpha = 0.0;
    float totalSamples = 0.0;
    for(float r = -Radius; r <= Radius; r += 1.0) {
        vec4 sample = texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir);

		// Accumulate average alpha
        totalAlpha = totalAlpha + sample.a;
        totalSamples = totalSamples + 1.0;

		// Accumulate smoothed blur
        float strength = 1.0 - abs(r / Radius);
        totalStrength = totalStrength + strength;
        blurred = blurred + sample;
    }
    gl_FragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);
}
