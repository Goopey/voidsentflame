#version 150

#moj_import <voidsentflame:interpolation.glsl>

uniform sampler2D SamplerWorld;
uniform sampler2D SamplerDepth;

in vec2 texCoord;
out vec4 fragColor;

const float FOG_START = 0.15;
const float FOG_END = 0.6;
const float FOG_RATE = 0.08;
const vec4 FOG_COLOR = vec4(0.74117647, 0.64313725, 0.89803921 , 1.0);

void main() {
    vec4 color = texture(SamplerWorld, texCoord);
    float depth = texture(SamplerDepth, texCoord).r;
    float fogValue = smoothstep(FOG_START, FOG_END, depth);
//    float fogValue = exponentialPiecewiseInterpolation(depth, FOG_START, FOG_END, FOG_RATE);

//    fragColor = vec4(vec3(smoothstep(FOG_START, FOG_END, depth)), 1.0);
    fragColor = vec4(mix(color.rgb, FOG_COLOR.rgb, min(0.6, fogValue)), color.a);
}