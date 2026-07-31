#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <voidsentflame:fov.glsl>
#moj_import <voidsentflame:renderdistance.glsl>

uniform sampler2D SamplerIn;
uniform sampler2D SamplerDepth;

in vec2 texCoord;
out vec4 fragColor;

const float NEAR = 0.1;
const float far = 128.0;

float LinearizeDepth(float depth, float far) {
    float z = depth * 2.0 - 1.0;
    return (NEAR * far) / (far + NEAR - z * (far - NEAR));
}

void main() {
//    float far = RENDER_DISTANCE * 8;
    float d = LinearizeDepth(texture(SamplerDepth, texCoord).r, far);
    float dist = length(vec3(1.0, (2.0 * texCoord - 1.0) * vec2(ScreenSize.x/ScreenSize.y, 1.0) * tan(radians(FOV / 2.0))) * d) / (far * 0.5);
    dist = 1.0 - dist;
    vec4 world = texture(SamplerIn, texCoord);

    fragColor = vec4(vec3(dist), 1.0);
//    fragColor = vec4(world.r, world.g, world.b, world.a);
//    fragColor = vec4(world.r * dist, world.g * dist, world.b * dist, world.a);
}
