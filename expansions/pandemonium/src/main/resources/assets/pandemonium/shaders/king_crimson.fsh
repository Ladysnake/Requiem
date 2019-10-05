#version 120

uniform sampler2D texture;

varying vec2 texcoord;        // the texture coordinate of the current pixel
varying vec3 normal;

void main(){
  vec4 tex = texture2D(texture, texcoord);
  tex.r = length(1 - tex.rgb);
  tex.gb = vec2(0);
  gl_FragColor = tex;
}
