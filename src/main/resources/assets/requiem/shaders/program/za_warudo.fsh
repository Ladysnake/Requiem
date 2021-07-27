#version 150

// The main texture
uniform sampler2D DiffuseSampler;
// The depth map
uniform sampler2D DepthSampler;
uniform vec2 OutSize;

// Position of the camera
uniform vec3 CameraPosition;
// Position of the center of The World
uniform vec3 Center;
// Radius of The World's distortion
uniform float Radius;
// Saturation of the world outside of the distortion
uniform float OuterSat;
// Time in seconds (+ tick delta)
uniform float STime;

// The magic matrix to get world coordinates from pixel ones
uniform mat4 InverseTransformMatrix;
// The size of the viewport (typically, [0,0,1080,720])
uniform ivec4 ViewPort;

in vec2 texCoord;
in vec4 vPosition;

out vec4 fragColor;

vec4 CalcEyeFromWindow(in float depth)
{
  // derived from https://www.khronos.org/opengl/wiki/Compute_eye_space_from_window_space
  // ndc = Normalized Device Coordinates
  vec3 ndcPos;
  ndcPos.xy = ((2.0 * gl_FragCoord.xy) - (2.0 * ViewPort.xy)) / (ViewPort.zw) - 1;
  ndcPos.z = (2.0 * depth - gl_DepthRange.near - gl_DepthRange.far) / (gl_DepthRange.far - gl_DepthRange.near);
  vec4 clipPos = vec4(ndcPos, 1.);
  vec4 homogeneous = InverseTransformMatrix * clipPos;
  vec4 eyePos = vec4(homogeneous.xyz / homogeneous.w, homogeneous.w);
  return eyePos;
}

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

void main()
{
    // Part 0: obtaining distance of the pixel to the center, in 3D space
    vec3 ndc = vPosition.xyz / vPosition.w; //perspective divide/normalize
    vec2 viewportCoord = ndc.xy * 0.5 + 0.5; //ndc is -1 to 1 in GL. scale for 0 to 1

    // Depth fading
    float sceneDepth = texture(DepthSampler, viewportCoord).x;
    vec3 pixelPosition = CalcEyeFromWindow(sceneDepth).xyz + CameraPosition;

    float pct = distance(pixelPosition, Center);

    // First circle
    float outside = smoothstep(Radius - 1., Radius, pct);
    float inside = smoothstep(Radius + 1., Radius, pct);
    // Second circle
    float outside2 = smoothstep(Radius - 2.5, Radius - 2., pct);
    float inside2 = smoothstep(Radius - 1.5, Radius - 2., pct);

    // Part 1: antifisheye in the sphere of influence

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
    float power = ( 2.0 * 3.141592 / (2.0 * sqrt(dot(m, m))) ) *
  		(inside * -0.2);
    //radius of 1:1 effect
    float bind = (prop < 1.0) ? m.x : m.y;//stick to borders

    //Weird formulas
    vec2 uv;
    if (power < 0.0)//antifisheye
      uv = m + normalize(d) * atan(r * -power * 10.0) * bind / atan(-power * bind * 10.0);
    else
      uv = p;//no effect for power = 0.0

    //Second part of cheat
    //for round effect, not elliptical
    vec3 color = texture(DiffuseSampler, vec2(uv.x, uv.y * prop)).rgb;

    // Part 2: Color schenanigans

    // Drawing the two circles
    color += pow(vec3(1.) * (outside * inside + outside2 * inside2), vec3(3.));
    // Converting from RGB to HSV to play with hue and saturation
    vec3 hsv = rgb2hsv(color);
    hsv[0] = mix(hsv[0], hsv[0] * abs(sin((STime+10)*0.1)), inside); // Color shift inside the first circle
    hsv[1] = mix(hsv[1], hsv[1] * OuterSat, outside); // Desaturate outside the first circle
    color = hsv2rgb(hsv);

    fragColor = vec4(color, 1.0);
}
