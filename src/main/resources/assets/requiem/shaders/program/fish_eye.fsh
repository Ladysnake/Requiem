/*
Source: https://www.shadertoy.com/view/4s2GRR
*/
#version 150
uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float Slider;

#define EPSILON 0.000011

in vec2 texCoord;

out vec4 fragColor;

void main(void)
{
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
  vec3 col = texture(DiffuseSampler, vec2(uv.x, uv.y * prop)).rgb;

  fragColor = vec4(col, 1.0);
}
