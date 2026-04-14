#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in vec3 Position;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    fragColor = vec4(vertexColor.rgb, 0.1);
}