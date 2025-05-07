#version 150
// Author: @patriciogv - 2015
// Tittle: Turbulence

#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D DiffuseSampler;

uniform vec2 OutSize;
uniform float STime;

in vec2 texCoord;

out vec4 fragColor;

//	Simplex 3D Noise
//	by Ian McEwan, Ashima Arts
//
vec4 permute(vec4 x){ return mod(((x*34.0)+1.0)*x, 289.0); }
vec4 taylorInvSqrt(vec4 r){ return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(vec3 v){
    const vec2  C = vec2(1.0/6.0, 1.0/3.0);
    const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);

    // First corner
    vec3 i  = floor(v + dot(v, C.yyy));
    vec3 x0 =   v - i + dot(i, C.xxx);

    // Other corners
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    //  x0 = x0 - 0. + 0.0 * C
    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1. + 3.0 * C.xxx;

    // Permutations
    i = mod(i, 289.0);
    vec4 p = permute(permute(permute(
    i.z + vec4(0.0, i1.z, i2.z, 1.0))
    + i.y + vec4(0.0, i1.y, i2.y, 1.0))
    + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    // Gradients
    // ( N*N points uniformly over a square, mapped onto an octahedron.)
    float n_ = 1.0/7.0;// N=7
    vec3  ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z *ns.z);//  mod(p,N*N)

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);// mod(j,N)

    vec4 x = x_ *ns.x + ns.yyyy;
    vec4 y = y_ *ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0)*2.0 + 1.0;
    vec4 s1 = floor(b1)*2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    //Normalise gradients
    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    // Mix final noise value
    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m*m, vec4(dot(p0, x0), dot(p1, x1),
    dot(p2, x2), dot(p3, x3)));
}

#define OCTAVES 3
float turbulence (in vec2 st) {
    // Initial values
    float value = 0.0;
    float amplitude = .5;
    float frequency = 0.;
    //
    // Loop of octaves
    for (int i = 0; i < OCTAVES; i++) {
        value += amplitude * abs(snoise(vec3(st, STime/10.)));
        st *= 2.;
        amplitude *= .5;
    }
    return value;
}

vec3 desaturate(vec3 color, float f) {
    vec3 grayXfer = vec3(0.3, 0.59, 0.11);
    vec3 gray = vec3(dot(grayXfer, color));
    return mix(color, gray, f);
}

mat4 brightnessMatrix(float brightness)
{
    return mat4(1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    brightness, brightness, brightness, 1);
}

mat4 contrastMatrix(float contrast)
{
    float t = (1.0 - contrast) / 2.0;

    return mat4(contrast, 0, 0, 0,
    0, contrast, 0, 0,
    0, 0, contrast, 0,
    t, t, t, 1);

}

mat4 saturationMatrix(float saturation)
{
    vec3 luminance = vec3(0.3086, 0.6094, 0.0820);

    float oneMinusSat = 1.0 - saturation;

    vec3 red = vec3(luminance.x * oneMinusSat);
    red+= vec3(saturation, 0, 0);

    vec3 green = vec3(luminance.y * oneMinusSat);
    green += vec3(0, saturation, 0);

    vec3 blue = vec3(luminance.z * oneMinusSat);
    blue += vec3(0, 0, saturation);

    return mat4(red, 0,
    green, 0,
    blue, 0,
    0, 0, 0, 1);
}

void main() {
    //    vec2 st = gl_FragCoord.xy/OutSize.xy;
    //    vec3 color = texture(DiffuseSampler, st).rgb;
    //    st.x *= OutSize.x/OutSize.y;
    //
    //    float t = turbulence(st*5.0);
    //    color *= 1. - pow(1. - t, 2. /* bigger number = better sight */);
    //
    //    fragColor = vec4(color, 1.0);

    vec2 uv = gl_FragCoord.xy / OutSize.xy;
    uv *=  1.0 - uv.yx;//vec2(1.0)- uv.yx; -> 1.-u.yx; Thanks FabriceNeyret !
    float vig = uv.x*uv.y * 50.0;// multiply with sth for intensity
    vig = clamp(pow(vig, 0.25), 0., 1.);// change pow for modifying the extend of the  vignette

    vec2 st = gl_FragCoord.xy/OutSize.xy;
    vec3 color = texture(DiffuseSampler, st).rgb;

    // brighter and desaturated vision
    if (!(color.r >= 50./255. && color.r <= 65./255. && color.g >= 205./255. && color.g <= 220./255. && color.b >= 215./255. && color.b <= 230./255.)) {
        color *= 3.;
        vec3 greyScale = vec3(.5, .5, .5);
        color = mix(vec3(dot(color, greyScale)), color, .5);

        st.x *= OutSize.x/OutSize.y;
        st = floor(st*100.)/75.;

        float t = turbulence(st*5.);
        // float t2 = turbulence(st*10.);

        vec3 overlayColor = t * vec3(0.05, 0.07, 0.09)*5.;
        vec3 sculkifiedColor = mix(color, vec3(0.0), clamp(overlayColor.g*10.*(1.-vig), 0., 1.));
        fragColor = vec4(sculkifiedColor, sculkifiedColor);
    } else {
        fragColor = vec4(color, 1.0);
    }
}
