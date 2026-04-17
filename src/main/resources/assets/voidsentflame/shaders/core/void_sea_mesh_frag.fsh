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

    float time = GameTime * secondsPerDay;
    vec2 offsetPos = vec2((texCoord0.x - time), texCoord0.y);

    //----gold foam texture
    vec2 q = vec2(0.);
    q.x = fbm(vertexPos.xz + 1.5 * time);
    q.y = fbm(vertexPos.xz + vec2(1.0));

    vec2 r = vec2(0.);
    r.x = fbm(vertexPos.xz + 1.0 * q + vec2(1.7, 9.2)+ 0.15 * time);
    r.y = fbm(vertexPos.xz + 1.0 * q + vec2(8.3, 2.8)+ 0.126 * time);
//
    float f = ABVRidge((vertexPos.xz / 4.0) + r + vec2(time));

    vec4 color = mix(vec4(0.990, 0.990, 0.750, 1.0),
                vec4(0.850, 0.750, 0.270, 1.0),
                clamp((f * f) * 4.0, 0.0, 1.0));
    color = mix(color,
                vec4(0.0, 0.0, 0.0, 1.0),
                clamp(length(q), 0.0, 1.0));
    color = mix(color,
                vec4(1.000, 0.950, 0.150, 1.0),
                clamp(length(r.x), 0.0, 1.0));
    color *= (f * f * f + 0.6 * f * f + 0.5 * f);

    //----calculate fog
    vec4 fogColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);

    fragColor = fogColor;
}