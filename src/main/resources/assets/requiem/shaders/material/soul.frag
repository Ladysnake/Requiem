#include frex:shaders/api/fragment.glsl
#include frex:shaders/api/sampler.glsl
#include frex:shaders/lib/math.glsl

void frx_startFragment(inout frx_FragmentData fragData) {
    fragData.spriteColor = mix(fragData.spriteColor, texture(frxs_baseColor, vec2(frx_texcoord.s, frx_texcoord.t + 0.5)), fragData.vertexColor.a);
    fragData.vertexColor.a = 1.0;
    // Stolen from luminance_glow.frag
    float e = frx_luminance(fragData.spriteColor.rgb);
    fragData.emissivity = e * e;
}
