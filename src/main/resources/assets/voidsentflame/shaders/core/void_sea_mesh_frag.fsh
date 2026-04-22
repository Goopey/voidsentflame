#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <voidsentflame:noise.glsl>

uniform sampler2D Sampler0; // texture
uniform sampler2D Sampler1; // normal map or ripple map
uniform sampler2D Sampler2; // screen map

//----gold foam
const vec3 GOLD = vec3(1.0, 0.85, 0.25);

const float secondsPerDay = 1200.00;

in vec3 Position;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 vertexPos;

out vec4 fragColor;

void main() {
    vec3 pos = ModelOffset;
    float x = pos.x;
    float z = pos.z;

    float time = GameTime * secondsPerDay * 0.5;
    vec2 offsetPos = vec2((texCoord0.x - time), texCoord0.y);

    //----gold foam texture
    vec2 q = vec2(0.);
    q.x = fbm(vertexPos.xz + 1.5 * time);
    q.y = fbm(vertexPos.xz + vec2(1.0));

    vec2 r = vec2(0.);
    r.x = fbm(vertexPos.xz + 1.0 * q + vec2(1.7, 9.2)+ 0.15 * time);
    r.y = fbm(vertexPos.xz + 1.0 * q + vec2(8.3, 2.8)+ 0.126 * time);

    float f = ABVRidge((vertexPos.xz / 8.0) + r + vec2(time));

    vec4 color = vec4(1.0);
    color = mix(color,
        vec4(0.981, 0.995, 0.153, 1.0),
        clamp((f * f) * 4.0, 0.0, 1.0));
    color = mix(color,
        vec4(1.000, 0.754, 0.123, 1.0),
        clamp(length(r.x), 0.0, 1.0));
    color *= (f * f * f + 0.6 * f * f + 0.5 * f);
    color *= color * color * color * color * color;

    //----yellow cloud texture
    q.x = fbm2(vertexPos.xz  + 0.00 * time);
    q.y = fbm2(vertexPos.xz  + vec2(1.0));
    r.x = fbm2(vertexPos.xz  + 1.0 * q + vec2(1.7, 9.2)+ 0.15 * time);
    r.y = fbm2(vertexPos.xz  + 1.0 * q + vec2(8.3, 2.8)+ 0.126 * time);
    f = fbm2(vertexPos.xz / 4.0 + r + vec2(time));

    vec4 color2 = vec4(1.0);
    color2 = mix(color2,
        vec4(0.795, 0.197, 0.067, 1.000),
        clamp((f * f) * 1.0, 0.0, 1.0));
    color2 = mix(color2,
        vec4(0.770,0.561,0.163,1.000),
        clamp(length(q), 0.0, 1.0));
    color2 = mix(color2,
        vec4(0.805,0.703,0.112,1.000),
        clamp(length(r.x)/0.5, 0.0, 1.0));
    color2 = (f * f * f + 0.6 * f * f + 0.5 * f) * color2 * 1.5;

    //----combine noise textures
    color += color2;
    color = clamp(color, vec4(0.0), vec4(vec3(0.94), 1.0));

    //----calculate fog
    vec4 fogColor = apply_fog(
        color, 0.0, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd,
        FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);

    fragColor = fogColor;
}