#version 150

#define PI 3.14159265359
#define TWO_PI 6.28318530718
#define SIDES 6.

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
// The number of tiles on the vertical axis
uniform float TileCount;
uniform float TileRatioX;

in vec2 texCoord;

out vec4 fragColor;

// Reference to
// http://thndl.com/square-shaped-shaders.html

float hexagon(vec2 st) {
    // Angle and radius from the current pixel
    float a = atan(st.x,st.y)+PI;
    float r = TWO_PI/SIDES;

    // Shaping function that modulate the distance
    float d = cos(floor(.5+a/r)*r-a)*length(st);
    return 1.0-smoothstep(.84,.98,d);
}

float linearstep(float edge0, float edge1, float x) {
    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t;
}

vec2 tile(in vec2 _st, in vec2 shift) {
    float gapx = (1.-TileRatioX)/2.;
    vec2 st = fract(_st * vec2(TileCount*TileRatioX,TileCount) + shift);
    st.x = linearstep(gapx, 1.-gapx, st.x);
    // Remap the space to -1. to 1.
    st = st * 2. -1.;
    return st;
}

void main(){
    //normalized coords with some cheat
    vec2 p = gl_FragCoord.xy / OutSize.x;

    //screen proportion
    float prop = OutSize.x / OutSize.y;
    //center coords
    vec2 m = vec2(0.5, 0.5 / prop);
    //vector from center to current fragment
    vec2 d = p - m;
    // distance of pixel from center
    float r = sqrt(dot(d, d));
    //amount of effect
    float Slider = 0.42;
    float power = ( 2.0 * 3.141592 / (2.0 * sqrt(dot(m, m))) ) *
    (Slider - 0.5);
    //radius of 1:1 effect
    float bind;
    if (power > 0.0) bind = sqrt(dot(m, m));//stick to corners
    else {if (prop < 1.0) bind = m.x; else bind = m.y;}//stick to borders

    //Weird formulas
    vec2 uv;
    if (power > 0.0)//fisheye
    uv = m + normalize(d) * tan(r * power) * bind / tan( bind * power);
    else if (power < 0.0)//antifisheye
    uv = m + normalize(d) * atan(r * -power * 10.0) * bind / atan(-power * bind * 10.0);
    else
    uv = p;//no effect for power = 1.0

    //Second part of cheat
    //for round effect, not elliptical
    vec2 st = vec2(uv.x, uv.y * prop);
//    st.x *= OutSize.x/OutSize.y;
    float paving = 0.0;

    paving = hexagon(tile(st, vec2(0)));
    paving += hexagon(tile(st, vec2(0.5)));

    vec3 col = texture(DiffuseSampler, texCoord).rgb;
    vec3 Gray = vec3(0.3, 0.59, 0.11);
    float Saturation = 1.4;
    float Luma = dot(col, Gray);
    vec3 Chroma = col - Luma;
    col = (Chroma * Saturation) + Luma * paving;
    fragColor = vec4(col, 1.0);
}
