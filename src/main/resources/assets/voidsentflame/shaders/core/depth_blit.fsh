#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <voidsentflame:fov.glsl>
#moj_import <voidsentflame:rdistance.glsl>

uniform sampler2D SamplerDepth;

in vec2 texCoord;
out vec4 fragColor;

const float NEAR = 0.1;

float LinearizeDepth(float depth, float far) {
    float z = depth * 2.0 - 1.0;
    return (NEAR * far) / (far + NEAR - z * (far - NEAR));
}

void main() {
    float d = LinearizeDepth(texture(SamplerDepth, texCoord).r, RDist);
    float dist = length(vec3(1.0, (2.0 * texCoord - 1.0) * vec2(ScreenSize.x/ScreenSize.y, 1.0) * tan(radians(FOV / 2.0))) * d) / (RDist);
    fragColor = vec4(vec3(dist), 1.0);
}
