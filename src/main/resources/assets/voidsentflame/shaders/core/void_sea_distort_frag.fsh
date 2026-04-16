#version 150

#moj_import <minecraft:globals.glsl>
#moj_import <voidsentflame:chunkoffset.glsl>

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;
uniform sampler2D SamplerBlend;
uniform sampler2D SamplerHeatWave;
//uniform sampler2D SamplerDistortionGradient;

//----white mask
const float TOLERANCE = 0.05f;
//----luminance
const vec3 WEIGHTS = vec3(0.333, 0.333, 0.333);
//----chromatic aberration
const vec2 AB_OFFSET = vec2(0, 0.012);
const vec3 AB_WEIGHTS = vec3(0.360, 0.360, 0);
//----heatwave
const float HEAT_STRENGTH = 0.02;
const vec2 HEAT_OFFSET = vec2(0, -0.25);
const vec2 HEAT_SCALE = vec2(0, 0.5);
const float HEAT_MASK_HEIGHT = 0.1;
const vec3 HEAT_WEIGHTS = vec3(0.360, 0.360, 0);
//----gold foam
const vec3 GOLD = vec3(1.0, 0.85, 0.25);
//----gametime
const float SECONDS_PER_DAY = 1200.00;

// waves constants
const float waveFrequency = 0.15;
const float waveAmplitude = 1.5;
const float timeFrequency = 4.0;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float time = GameTime * SECONDS_PER_DAY;
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);
    vec4 blendColor = texture(SamplerBlend, texCoord);
//    vec4 gradientColor = texture(SamplerDistortionGradient, texCoord);

    // calculate wave height
    float waves = sin((COffset.x - COffset.z + timeFrequency * time) * waveFrequency) * waveAmplitude - waveAmplitude;

    //----heat wave
    float jacked_time = 5.5 * time;

    vec3 heatWaveMask = texture(SamplerHeatWave, texCoord).rgb;
    bool submerged = all(greaterThanEqual(heatWaveMask, vec3(1.0 - TOLERANCE)));
    float heightStrength = 1.0 - min((64 + COffset.y) / 180, 1.0);

//    vec3 heatMask = 1.0 - ((1.0 - heatWaveMask) * (1.0 - gradientColor.rbg));
//    if (!submerged) {
//        float layer = 0.1171 + (max(42.5 + COffset.y, 0) + 16.0) / 64;
//        heatMask = vec3(min(layer, heatMask.r));
//    }
    vec2 heatCoord = texCoord +
        heightStrength * HEAT_STRENGTH *
        (1.0 - texCoord.y) * (1.0 - heatWaveMask.r) *
        sin(HEAT_SCALE * jacked_time + length(texCoord) * 10.0);
    vec4 heatColor = texture(SamplerBlend, heatCoord);

    //----combine textures
    bool isNotSea = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    bool isCloseToSea = isNotSea && submerged;
//    if (isNotSea) {
//      //----bake in lighting
//      heatColor += vec4((1.0 - heatMask.r) / 10, (1.0 - heatMask.g) / 10, 0.0, 1.0);
//      //----terrain gold foam
//      // TODO : add terrain gold foam
//      //heatColor += vec4((1.0 - heatMask.r), (1.0 - heatMask.r), 0.0, 1.0);
//    }

    vec4 finalHeatColor = isCloseToSea ? blendColor : heatColor;

    //----output
    fragColor = finalHeatColor;
}
