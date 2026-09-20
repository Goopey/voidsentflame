#version 150

#moj_import <voidsentflame:interpolation.glsl>

uniform sampler2D SamplerWorld;
uniform sampler2D SamplerDepth;

in vec2 texCoord;
out vec4 fragColor;

const float FOG_START = 0.15;
const float FOG_END = 0.30;
const float FOG_MAX_STRENGTH = 0.40;
//const vec4 FOG_COLOR = vec4(0.74117647, 0.64313725, 0.89803921 , 1.0);
//const vec4 FOG_COLOR = vec4(0.2, 0.0, 0.1058823529 , 1.0);
// TODO : change fog color to proper color
const vec4 FOG_COLOR = vec4(1.0);

void main() {
    vec4 color = texture(SamplerWorld, texCoord);
    float depth = texture(SamplerDepth, texCoord).r;
    float fogValue = clamp(
        (depth - FOG_START) / (FOG_END - FOG_START),
        0.0,
        1.0
    );
    fogValue *= FOG_MAX_STRENGTH;

    fragColor = vec4(mix(
        color.rgb,
        FOG_COLOR.rgb,
        fogValue
    ), 1.0);
//    float fogValue = smoothstep(FOG_START, FOG_END, depth);
//    float fogValue = exponentialPiecewiseInterpolation(depth, FOG_START, FOG_END, FOG_RATE);

//    fragColor = vec4(mix(color.rgb, FOG_COLOR.rgb, min(0.6, fogValue)), color.a);
//    if (depth < FOG_START) {
//        fragColor = vec4(1.0);
//    } else if (depth > FOG_END) {
//        fragColor = vec4(vec3(FOG_END), 1.0);
//    } else {
//        fragColor = vec4(vec3(depth), 1.0);
//    }
}