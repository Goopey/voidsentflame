#version 150

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

in vec2 texCoord;
out vec4 fragColor;

void main(){
    fragColor = texture(Sampler0, texCoord) * ColorModulator;
}
