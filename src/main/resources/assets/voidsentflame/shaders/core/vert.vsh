#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>

// basic inputs, position, color, uv0, uv2, normal
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler0; // texture sampler
uniform sampler2D Sampler1; // lightmap sampler
uniform sampler2D Sampler2; // screen map

// basic outputs, fog, texture coordinates and color
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

void main() {
    vec3 pos = Position + ModelOffset;

    texCoord0 = UV0;
    vertexColor = pos.y > 0 ? vec4(1.0, 1.0, 1.0, 1.0) : vec4(0.0, 0.0, 0.0, 1.0);
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}