#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0; // texture
uniform sampler2D Sampler1; // normal map or ripple map
uniform sampler2D Sampler2; // screen map

in vec3 Position;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    fragColor = vertexColor;
}