#version 120
// glsl version. We're way behind the recommended but blame mojang

// varying variables are given by the vertex shader. See VertexBase.vsh
varying vec2 texcoord;        // the texture coordinate of the current pixel
varying vec4 vPosition;       // the screen position of the current pixel

// uniform variables are given in the java code
uniform sampler2D texture;    // represents what's currently displayed on the screen
uniform sampler2D lightmap;   // Minecraft's world lightmap
uniform float lighting;       // a value representing the light because I don't know how to get the lightmap lol
uniform int time;             // the system time, for animations (not used here)
uniform float animationProgress;  // the progress of the corpse's decay [0, 1]

/**
 * We don't have a rand function in opengl so here's a makeshift one instead.
 * @param co the seed of the random.
 */
float rand(vec2 co) {
   return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture2D(texture, texcoord);    // gets the color of the current pixel
    vec4 light = texture2D(lightmap, texcoord);   // supposed to get the light of the current pixel

    float gs = (color.r + color.g + color.b);     // greyscale

    float lightRatio = max(lighting, 1) / 17.0;   // the light ratio, definitely not supposed to work like that

    float alpha = 0;
    if(animationProgress * gs < 0.5)
      alpha = color.a;
    else if(animationProgress * gs < 0.8)         // lighter pixels get transparent first
      alpha = sin((texcoord.x + texcoord.y)/20.0);
    if(animationProgress > 0.8)
      alpha *= animationProgress / 0.2;
    // gl_FragColor is a variable given by the graphic card. It's what you're supposed to alter in the end.
    gl_FragColor = vec4(color.r * lightRatio, color.g * lightRatio, color.b * lightRatio, alpha);
}
