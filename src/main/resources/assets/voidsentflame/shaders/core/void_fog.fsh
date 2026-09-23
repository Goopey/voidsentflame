#version 150

#moj_import <voidsentflame:interpolation.glsl>

uniform sampler2D SamplerWorld;
uniform sampler2D SamplerDepth;

in vec2 texCoord;
out vec4 fragColor;

const float FOG_START = 0.15;
const float FOG_END = 0.30;
const float FOG_MAX_STRENGTH = 0.40;
const vec4 FOG_COLOR = vec4(0.2, 0.0, 0.1058823529 , 1.0);

void main() {
    vec4 color = texture(SamplerWorld, texCoord);
    float depth = texture(SamplerDepth, texCoord).r;

    float fogValue = clamp(
        (depth - FOG_START) / (FOG_END - FOG_START),
        0.0,
        1.0
    );
    fogValue *= FOG_MAX_STRENGTH;

    vec3 fogGradient = (1.0 - fogValue) * color.rgb + fogValue * FOG_COLOR.rgb;

    fragColor = vec4(fogGradient, color.a * fogValue);
}