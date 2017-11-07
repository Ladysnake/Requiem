#version 120

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
varying vec2 texCoord;

float toLum (vec4 color){
    return 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;
}

vec4 toLinear (vec4 color){
    return pow(color,vec4(2.2));
}

float toLinear (float value){
    return pow(value,2.2);
}

vec4 toGamma (vec4 color){
    return pow(color,vec4(1.0/2.2));
}

float toGamma (float value){
    return pow(value,1.0/2.2);
}

void main() {
    vec4 color = texture2D(DiffuseSampler, texCoord);
    vec4 bloom = texture2D(DiffuseSampler, texCoord)*2.0;

    color=toLinear(color);
    bloom=toLinear(bloom);

    vec4 bloomed=color+bloom;

    vec4 reinhard=bloomed*1.3;
    float L = toLum(reinhard);
    float nL = (L)/(1.0+L);
    float scale = nL / L;
    reinhard *= scale;

    gl_FragColor = toGamma( mix(bloomed,reinhard,0.5) );
}
