#version 120

uniform sampler2D texture;

void main() {
  vec4 color = texture2D(texture, texcoord);
  gl_FragColor = vec4(color.r * 0.9, color.g * 0.9, color.b * 1.1, color.a);
}
