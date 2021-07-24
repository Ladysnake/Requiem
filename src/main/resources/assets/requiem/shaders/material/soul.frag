#include frex:shaders/api/fragment.glsl
#include frex:shaders/api/sampler.glsl

void frx_startFragment(inout frx_FragmentData fragData) {
    fragData.spriteColor = mix(fragData.spriteColor, texture(frxs_baseColor, vec2(frx_texcoord.s, frx_texcoord.t + 0.5)), fragData.vertexColor.a);
    fragData.vertexColor.a = 1.0;
}
