#version 120

uniform sampler2D DiffuseSampler;

uniform float Time;

varying vec2 texCoord;
/*
vec3 saturate(vec4 rgba, float adjustment) {
  if (rgba.r > rgba.g && rgba.r > rgba.b) {
    rgba.r *= adjustment;
  }
  if (rgba.g > rgba.b && rgba.g > rgba.r) {
    rgba.g *= adjustment;
  }
  if (rgba.b > rgba.r && rgba.b > rgba.g) {
    rgba.b *= adjustment;
  }

  return rgba;
}
*/
void main() {
    vec4 tex = texture2D(DiffuseSampler, texCoord);

    tex.r *= 1.0 - pow(abs(texCoord.x * 2 - 1), 2.5);
    tex.r *= 1.0 - pow(abs(texCoord.y * 2 - 1), 2.5);

    tex.b *= 1.5;
    tex.g *= 1.1;

    gl_FragColor = tex;
}
