#version 130

uniform sampler2D texture;

// The amount of saturation in the resulting image
uniform float saturation = 0.;

varying vec2 texcoord;

void main()
{
    vec3 R0 = texture2D(texture, texcoord).rgb;
    gl_FragColor = vec4(mix(vec3(dot(R0, vec3(0.2125, 0.7154, 0.0721))), R0, saturation), 1);
}
