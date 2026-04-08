#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;

//----white mask
const float TOLERANCE = 0.05f;
//----luminance
const vec3 WEIGHTS = vec3(0.333, 0.333, 0.333);
//----chromatic aberration
const vec2 AB_OFFSET = vec2(0, 0.0033);
const vec3 AB_WEIGHTS = vec3(0.550, 0.587, 0.114);
//----texture bleeding
const int SAMPLES = 8;
const vec2 BLEED_DIR = vec2(0.0, -1.0);
const float BLEED_STRENGTH = 0.02;
//----gametime
const float SECONDS_PER_DAY = 1200.00;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float time = GameTime * SECONDS_PER_DAY;
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);

    //----ghost trail
    float mask = 1.0 - (seaColor.r * WEIGHTS.x + seaColor.g * WEIGHTS.y + seaColor.b * WEIGHTS.z);

    // heat distortion
    vec2 distortion = vec2(
        sin(texCoord.y * 25.0 + time * 5.0),
        cos(texCoord.x * 25.0 + time * 5.0)
    ) * 0.005;

    // apply distortion
    vec2 distortedUV = texCoord + distortion * mask;
    vec4 distortedColor = texture(SamplerWorld, distortedUV);
    vec4 distortedWorldColor = mix(distortedColor, seaColor, mask);

    //----chromatic aberration
    vec4 abVal1 = texture(SamplerSea, texCoord + AB_OFFSET);

    vec4 aberrantSeaColor = vec4(
        (1.0 - AB_WEIGHTS.x) * (seaColor.r) + (AB_WEIGHTS.x) * (abVal1.r),
        (1.0 - AB_WEIGHTS.y) * (seaColor.g) + (AB_WEIGHTS.y) * (abVal1.g),
        seaColor.b,
        1.0
    );

    //----overlays world texture over white part of screen
    bool isWhite = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    //call colors from other effects
    vec4 mixedColor = isWhite ? distortedWorldColor : aberrantSeaColor;

    // output
    fragColor = distortedWorldColor;
}
