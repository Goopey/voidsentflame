#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0; // texture
uniform sampler2D Sampler1; // normal map or ripple map
uniform sampler2D Sampler2; // screen map

const float secondsPerDay = 1200.00;

in vec3 Position;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec3 pos = ModelOffset;
    float x = pos.x;
    float z = pos.z;

    float time = GameTime * secondsPerDay * 0.5;

    // vec2 modPos = vec2(mod(x, 1.0), mod(z, 1.0));
    vec2 offsetPos = vec2((texCoord0.x - time), texCoord0.y);
    vec4 color = texture2D(Sampler0, offsetPos) * ColorModulator;

    vec4 fogColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);

    fragColor = fogColor;
}