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

    // vec2 modPos = vec2(mod(x, 1.0), mod(z, 1.0));
    vec2 offsetPos = vec2((texCoord0.x - time), texCoord0.y);
    vec4 color = texture(Sampler0, offsetPos) * ColorModulator;

    //----gold foam texture
    vec2 pFlow = flow(vertexPos.xz, time);
    float t = turbulence(pFlow);
    float veins = smoothstep(0.6, 0.8, t);
    vec4 mixedColor = vec4(mix(color.rgb, GOLD, veins), 1.0);

    vec4 fogColor = apply_fog(mixedColor, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);

    fragColor = fogColor;
}