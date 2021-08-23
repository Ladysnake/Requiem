#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PlayerSampler;

in vec2 texCoord;

out vec4 fragColor;

void main(){
  vec4 tex = texture(DiffuseSampler, texCoord);
  vec4 playerTex = texture(PlayerSampler, texCoord);
  fragColor = vec4(mix(vec3(dot(tex.rgb, vec3(0.2125, 0.7154, 0.0721))), tex.rgb, 1. - playerTex.r), tex.a);
}
