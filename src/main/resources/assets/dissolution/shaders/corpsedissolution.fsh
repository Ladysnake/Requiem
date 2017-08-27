#version 120

varying vec2 texcoord;
varying vec4 vPosition;

uniform sampler2D texture;
uniform sampler2D lightmap;
uniform float lighting;
uniform int time;
uniform float animationProgress;

float rand(vec2 co) {
   return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture2D(texture, texcoord);
    vec4 light = texture2D(lightmap, texcoord);

    float gs = (color.r + color.g + color.b);

    float lightRatio = max(lighting, 1) / 17.0;

    float r = color.r - rand(texcoord) * 0.25 + vPosition.r;
    float g = color.g - rand(texcoord) * 0.25 + vPosition.g;
    float b = color.b - rand(texcoord) * 0.25 + vPosition.b;

//    	gl_FragColor = vec4(color.r, color.g, color.b, color.a/* * rand(vec2(g, r))*0.25*/);
    float alpha = 0;
    if(animationProgress * gs < 0.5)
      alpha = color.a;
    else if(animationProgress * gs < 0.8)
      alpha = sin((texcoord.x + texcoord.y)/20.0);// - rand(texcoord);
    if(animationProgress > 0.8)
      alpha *= animationProgress / 0.2;
    gl_FragColor = vec4(color.r * lightRatio, color.g * lightRatio, color.b * lightRatio, alpha);
}
