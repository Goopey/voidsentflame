#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;
uniform sampler2D SamplerBlend;

//----white mask
const float TOLERANCE = 0.05f;
//----luminance
const vec3 WEIGHTS = vec3(0.333, 0.333, 0.333);
//----chromatic aberration
const vec2 AB_OFFSET = vec2(0, 0.0033);
const vec3 AB_WEIGHTS = vec3(0.550, 0.587, 0.114);
//----heatwave
const float HEAT_STRENGTH = 0.02;
const vec2 HEAT_OFFSET = vec2(0, 0.2);
const vec2 HEAT_SCALE = vec2(0, 0.5);
//----gametime
const float SECONDS_PER_DAY = 1200.00;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float time = GameTime * SECONDS_PER_DAY;
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);
    vec4 blendColor = texture(SamplerBlend, texCoord);

    //----heat wave
    float jacked_time = 5.5 * time;

    vec2 heatCoord = texCoord + (1.0 - texCoord.y) * HEAT_STRENGTH * sin(HEAT_SCALE * jacked_time + length(texCoord) * 10.0);
    //vec4 offsetSea = texture(SamplerSea, texCoord + HEAT_OFFSET);
    //bool isCloseToSea = all(greaterThanEqual(offsetSea.rgb, vec3(1.0 - TOLERANCE)));
    vec4 heatColor = texture(SamplerBlend, heatCoord);
    //vec4 finalHeatColor = isCloseToSea ? heatColor : blendColor;

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
    vec4 mixedColor = isWhite ? heatColor : aberrantSeaColor;

    // output
    fragColor = mixedColor;
}
