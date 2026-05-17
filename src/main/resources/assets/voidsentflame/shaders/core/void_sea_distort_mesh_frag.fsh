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
    float time = GameTime * secondsPerDay * 0.5;

    vec2 offsetPos = vec2((texCoord0.x - time), texCoord0.y);
    vec4 color = texture(Sampler0, offsetPos) * ColorModulator;

    fragColor = color;
}