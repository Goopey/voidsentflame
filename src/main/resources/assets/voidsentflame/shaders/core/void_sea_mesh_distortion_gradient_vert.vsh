#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <voidsentflame:chunkoffset.glsl>

// basic inputs, position, color, uv0, uv2, normal
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

// basic outputs, fog, texture coordinates and color
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

void main() {
    vec3 pos = Position + ModelOffset + COffset;
    float layer = max(min(-26.5 - COffset.y, 16.0), 0.0);
    layer = 0.5 * layer * (layer + 1) / 320;
    float grad = ((Position.y + 64.0) / 320.0) - layer;

    texCoord0 = UV0;
    vertexColor = vec4(grad, grad, grad, 0.1);
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}