#version 150

#define PI 3.14159265359
#define TWO_PI 6.28318530718

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

// Conversion functions between the RGB and HSV color spaces
// Source: http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl
vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = c.g < c.b ? vec4(c.bg, K.wz) : vec4(c.gb, K.xy);
    vec4 q = c.r < p.x ? vec4(p.xyw, c.r) : vec4(c.r, p.yzx);

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec4 inTexel = texture(DiffuseSampler, texCoord);

    vec3 hsvColor = rgb2hsv(inTexel.rgb);
    // Hue is represented as a trigonometric circle where red is 0 and blue is 1
    // We want to shift only the hues close to red (= cos(h) close to 1)
    // We also want to ignore low Saturation values, as those may give aberrant hue
    // Finally, we're shifting only by a small amount: red gets shifted by 0.2, making it yellow
    float hueShift = smoothstep(0.7, 1.0, cos(TWO_PI*hsvColor.x)) * smoothstep(0.2, 1.0, hsvColor.y) * 0.2;
    hsvColor.x += hueShift;
    // Make red derivatives appear darker (lower Value)
    hsvColor.z -= hueShift*0.5;
    vec3 outColor = hsv2rgb(hsvColor);

    fragColor = vec4(outColor.rgb, 1.0);
}
