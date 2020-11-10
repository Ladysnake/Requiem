#version 130

// The main texture
uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;

// Time in seconds (+ tick delta)
uniform float STime;

// The magic matrix to get world coordinates from pixel ones
uniform mat4 InverseTransformMatrix;
// The size of the viewport (typically, [0,0,1080,720])
uniform ivec4 ViewPort;

varying vec2 texCoord;
varying vec4 vPosition;

vec3 cam_origin;
mat3 cam_rotation;
vec2 frag_coord;

vec3 rotateX(float a, vec3 v)
{
    return vec3(v.x, cos(a) * v.y + sin(a) * v.z, cos(a) * v.z - sin(a) * v.y);
}

vec3 rotateY(float a, vec3 v)
{
    return vec3(cos(a) * v.x + sin(a) * v.z, v.y, cos(a) * v.z - sin(a) * v.x);
}

vec3 project(vec3 p)
{
    // transpose the rotation matrix. unfortunately tranpose() is not available.
    mat3 cam_rotation_t = mat3(vec3(cam_rotation[0].x, cam_rotation[1].x, cam_rotation[2].x),
    vec3(cam_rotation[0].y, cam_rotation[1].y, cam_rotation[2].y),
    vec3(cam_rotation[0].z, cam_rotation[1].z, cam_rotation[2].z));

    // transform into viewspace
    p = cam_rotation_t * (p - cam_origin);

    // project
    return vec3(p.xy / p.z, p.z);
}

float manhattanDistance(vec2 p1, vec2 p2) {
    float d1 = abs(p1.x - p2.x);
    float d2 = abs(p1.y - p2.y);
    return d1 + d2;
}

vec3 orb(float rad, vec3 coord)
{
    vec3 col = vec3(1., 1., 1.);
    // return the orb sprite
    return 4.0 * (smoothstep(rad, 0.0, manhattanDistance(coord.xy, frag_coord))) *
    col * (clamp(coord.z, 0.0, 1.0));
}

vec3 traverseUniformGrid(vec3 ro, vec3 rd)
{
    vec3 increment = vec3(1.0) / rd;
    vec3 intersection = ((floor(ro) + round(rd * 0.5 + vec3(0.5))) - ro) * increment;

    increment = abs(increment);
    ro += rd * 1e-3;

    vec3 orb_accum = vec3(0.0);

    // traverse the uniform grid
    for(int i = 0; i < 10; i += 1)
    {
        // viewspace position of the cell's corner
        vec3 rp = floor(ro + rd * min(intersection.x, min(intersection.y, intersection.z)));

        // get the screenspace position of the cell's corner
        vec3 coord = project(rp + vec3(0.5));

        float rmask = smoothstep(0.0, 0.1, manhattanDistance(frag_coord, coord.xy));

        // calculate the initial radius
        float rad = 0.5 / coord.z * (1.0 - smoothstep(0.0, 50.0, length(rp)));

        // adjust the radius
        rad *= 0.1;// + 0.1 * sin(rp.x + iTime * 5.0) * cos(rp.y + iTime * 10.0) * cos(rp.z);

        orb_accum += orb(rad, coord) * vec3(sin(rp.x), sin(rp.y), sin(rp.z))
        * mix(1.0, rmask, 1.0);
        //orb_accum.r = smoothstep(0.01, 0.0, abs(coord.x - frag_coord.x));
        //orb_accum.g = smoothstep(0.01, 0.0, abs(coord.y - frag_coord.y));

        // step to the next ray-cell intersection
        intersection += increment * step(intersection.xyz, intersection.yxy) *
        step(intersection.xyz, intersection.zzx);
    }

    return orb_accum;
}

void main()
{
    vec3 color = texture2D(DiffuseSampler, texCoord).rgb;

    // get the normalised device coordinates
    vec2 uv = vPosition.xy / vPosition.xy;
    frag_coord = uv * 2.0 - vec2(1.0);
    frag_coord.x *= ViewPort.x / ViewPort.y;

    // zoom in
    frag_coord *= 1.5;

    cam_origin = rotateX(1.,
    rotateY(STime*0.05, vec3(0.0, 0.0, -5.0 + 5.0 * cos(3.14))));

    // calculate the rotation matrix
    vec3 cam_w = normalize(vec3(cos(1.) * 10.0, 0.0, 0.0) - cam_origin);
    vec3 cam_u = normalize(cross(cam_w, vec3(0.0, 1.0, 0.0)));
    vec3 cam_v = normalize(cross(cam_u, cam_w));

    cam_rotation = mat3(cam_u, cam_v, cam_w);

    vec3 ro = cam_origin,rd = cam_rotation * vec3(frag_coord, 1.0);

    // render the particles
    vec3 particleColor = traverseUniformGrid(ro, rd);
    particleColor = sqrt(particleColor * 0.8);

    gl_FragColor = vec4(color + particleColor, 1.0);
}
