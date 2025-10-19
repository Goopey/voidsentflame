#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    // Wave parameters
    float waveSpeed = 16.0;     // Speed of wave animation
    float waveStrength = 0.2; // How strong the wave distortion is
    float waveFrequency = 10.0; // How many waves there are

    // Apply sine wave to texture coordinates
    float offsetX = sin(texCoord0.y + GameTime * waveSpeed) * waveStrength;
    float offsetY = cos(texCoord0.x + GameTime * waveSpeed) * waveStrength;

    vec2 distortedCoord = texCoord0 + vec2(offsetX, offsetY);

    // Sample the texture with distorted coordinates
    vec4 color = texture2D(Sampler0, distortedCoord);

    fragColor = color;
}