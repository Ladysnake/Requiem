#version 120

varying vec3 normal;
varying vec4 color;
varying float shift;
varying vec3 position;
varying vec4 uv;

struct Light{
    vec4 color;
    vec3 position;
	float radius;
};

uniform int time;
uniform int chunkX;
uniform int chunkZ;
uniform sampler2D sampler;
uniform sampler2D lightmap;
uniform vec3 playerPos;
uniform Light lights[100];
uniform int lightCount;
uniform int maxLights;

float distSq(vec3 a, vec3 b){
	return pow((a.x-b.x),2)+pow((a.y-b.y),2)+pow((a.z-b.z),2);
}

void main()
{
	vec4 baseColor = gl_Color * texture2D(sampler,gl_TexCoord[0].st);
	vec4 light = vec4(0,0,0,1);
	float intensities[100];
	float sumR = 0;
	float sumG = 0;
	float sumB = 0;
	float count = 0;
	bool valid[100];
	for (int i = 0; i < lightCount; i ++){
		if (distSq(lights[i].position,position) <= pow(lights[i].radius,2)){
			valid[i] = true;
		}
		else {
			valid[i] = false;
		}
	}
	for (int i = 0; i < lightCount; i ++){
		if (valid[i]){
			intensities[i] = max(0,1.0f-distSq(lights[i].position,position)/lights[i].radius) * 1.0f * lights[i].color.w;
		}
	}
	float maxIntens = 0;
	float totalIntens = 0;
	for (int i = 0; i < lightCount; i ++){
		if (valid[i]){
			totalIntens += max(0,intensities[i]);
			maxIntens = max(maxIntens,intensities[i]);
		}
	}
	for (int i = 0; i < lightCount; i ++){
		if (valid[i]){
			sumR += (intensities[i]/totalIntens)*lights[i].color.x;
			sumG += (intensities[i]/totalIntens)*lights[i].color.y;
			sumB += (intensities[i]/totalIntens)*lights[i].color.z;
		}
	}
	vec3 lmapDarkness = texture2D(lightmap,gl_TexCoord[1].st*vec2(1/256.0f,1/256.0f)).xyz;
	light = vec4(max(sumR*1.5f,lmapDarkness.x), max(sumG*1.5f,lmapDarkness.y), max(sumB*1.5f,lmapDarkness.z), 1);
	float intens = min(1.0f,maxIntens);
	vec4 color = vec4((baseColor.xyz * (light.xyz * 1.0f)),baseColor.w);
	color.x = min(1.0f,color.x);
	color.y = min(1.0f,color.y);
	color.z = min(1.0f,color.z);
	color.w = min(1.0f,color.w);
	color = (1.0f-intens)*baseColor*vec4(lmapDarkness,1) + intens*color;
	gl_FragColor = vec4(color.xyz * (intens + (1.0f-intens)*lmapDarkness),color.w);
}
