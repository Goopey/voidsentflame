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
    vec4 color = texture(Sampler0, offsetPos) * ColorModulator;

    vec4 fogColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);

    //----gold foam
    //vec2 flow = vec2(0.015, -0.05) * time * 0.15;
    //vec2 uv1 = texCoord * 2.5 + flow;
    //float n1 = texture(SamplerTex, uv1).r;

    // Secondary distortion
    //vec2 uv2 = texCoord * 4.0 - flow * 1.5;
    //float n2 = texture(SamplerTex, uv2).r;

    // Distort coordinates (important for organic foam look)
    //vec2 distortedUV = uv1 + vec2(n2 - 0.5) * 0.2;
    //float nTex = texture(SamplerTex, distortedUV).r;

    // Foam shaping
    //float foam = smoothstep(0.65, 0.8, nTex);
    // Sharper highlights
    //foam += pow(nTex, 4.0) * 0.2;
    //vec4 goldColor = vec4(GOLD * foam, 0.5);

    fragColor = fogColor;
}