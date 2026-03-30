#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
out vec2 texCoord;

void main() {
    vec3 pos = Position + ModelOffset;
    texCoord = UV0;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
