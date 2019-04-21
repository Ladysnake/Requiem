#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D PlayerSampler;

varying vec2 texCoord;

void main(){
  vec4 tex = texture2D(DiffuseSampler, texCoord);
  vec4 playerTex = texture2D(PlayerSampler, texCoord);
  gl_FragColor = vec4(mix(vec3(dot(tex.rgb, vec3(0.2125, 0.7154, 0.0721))), tex.rgb, 1 - playerTex.r), tex.a);
}
