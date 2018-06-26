#version 120
// glsl version. We're way behind the recommended but blame mojang

// varying variables are given by the vertex shader. See VertexBase.vsh
varying vec2 texcoord;        // the texture coordinate of the current pixel
varying vec3 normal;

// uniform variables are given in the java code
uniform sampler2D texture;    // represents what's currently displayed on the screen
uniform sampler2D lightmap;   // Minecraft's world lightmap
uniform vec2 lightmapCoords;
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
    vec4 light = texture2D(lightmap, lightmapCoords);   // gets the light of the entity
    // vec4 light = vec4((normal.y / 2 + 0.5) / 2 + 0.5);

    color *= vec4(light.rgb, 1.0);

    float gs = (color.r + color.g + color.b);     // greyscale

    color = mix(color, vec4(gs, gs, gs, color.a), 0.5);

    // gl_FragColor is a variable given by the graphic card. It's what you're supposed to alter in the end.
    gl_FragColor = color;
}
