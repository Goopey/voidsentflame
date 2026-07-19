#version 150

uniform sampler2D SamplerIn;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = texture(SamplerIn, texCoord);
}
