// Adapted from a shader by @patriciogv
// https://thebookofshaders.com/edit.php#11/circleWave-noise.frag

#version 150

uniform sampler2D DiffuseSampler;

// Time in seconds
uniform float STime;
// Screen dimensions
uniform vec2 OutSize;
// Intensity modifier
uniform float SolidIntensity;
uniform float RaysIntensity;
uniform float Zoom;
uniform vec3 OverlayColor;

in vec2 texCoord;

out vec4 fragColor;

vec2 random2(vec2 st){
  st = vec2( dot(st,vec2(127.1,311.7)),
  dot(st,vec2(269.5,183.3)) );
  return -1.0 + 2.0*fract(sin(st)*43758.5453123);
}

// Value Noise by Inigo Quilez - iq/2013
// https://www.shadertoy.com/view/lsf3WH
float noise(vec2 st) {
  vec2 i = floor(st);
  vec2 f = fract(st);

  vec2 u = f*f*(3.0-2.0*f);

  return mix( mix( dot( random2(i + vec2(0.0,0.0) ), f - vec2(0.0,0.0) ),
  dot( random2(i + vec2(1.0,0.0) ), f - vec2(1.0,0.0) ), u.x),
  mix( dot( random2(i + vec2(0.0,1.0) ), f - vec2(0.0,1.0) ),
  dot( random2(i + vec2(1.0,1.0) ), f - vec2(1.0,1.0) ), u.x), u.y);
}

float shape(vec2 st, float a, float radius) {
  float r = length(st)*2.0;
  float m = abs(mod(a+STime/4.,3.14*2.)-3.14)/4.6;
  float f = radius;
  m += noise(st+STime*0.1)*.5;
  f += sin(a*1.)*noise(st+STime*.2) * 0.5;
  f += (sin(a*20.)*.1*pow(m,2.));
  // Up the contrast to make streaks
  return f*f*(smoothstep(f,f+1.0,r));
}

float streaks(vec2 st, float a, float radius) {
  float f = radius;
  f += sin(a*20.)*noise(st+STime*.2) * 0.5;
  // Up the contrast
  return pow(f, 10.0);
}

void main() {
  vec2 st = gl_FragCoord.xy / OutSize.xy;
  vec3 tex = texture(DiffuseSampler, st).rgb;
  st = (vec2(0.5)-st) * Zoom;
  float a = atan(st.y, st.x);
  float value = shape(st, a, 0.8);
  vec3 color = mix(tex, OverlayColor, value * SolidIntensity);
  color += streaks(st, a, 0.8) * value * RaysIntensity;

  fragColor = vec4(color, 1.0);
}
