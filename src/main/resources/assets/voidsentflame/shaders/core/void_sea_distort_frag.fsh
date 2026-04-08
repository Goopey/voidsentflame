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
const vec2 AB_OFFSET = vec2(0, 0.012);
const vec3 AB_WEIGHTS = vec3(0.360, 0.360, 0);
//----heatwave
const float HEAT_STRENGTH = 0.02;
const vec2 HEAT_OFFSET = vec2(0, -0.25);
const vec2 HEAT_SCALE = vec2(0, 0.5);
const float HEAT_MASK_HEIGHT = 0.1;
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
    vec4 heatColor = texture(SamplerBlend, heatCoord);
    //----heat chromatic aberration
    heatColor = vec4(
        heatColor.r,
        heatColor.g,
        heatColor.b,
        1.0
    );

    // expand the sea texture in 3 directions to "pad" the texture
    vec2 blowUpCoord[3] = {{0, -0.5 * HEAT_MASK_HEIGHT}, {HEAT_MASK_HEIGHT, 0}, {-HEAT_MASK_HEIGHT, 0}};
    vec4 offsetSea = vec4(0.0);
    for (int i = 0; i < 3; i++) {
        offsetSea += vec4(1.0) - texture(SamplerSea, texCoord + blowUpCoord[i]);
    }
    offsetSea = vec4(1.0) - offsetSea;

    bool isWhite = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    bool isCloseToSea = isWhite && all(greaterThanEqual(offsetSea.rgb, vec3(1.0 - TOLERANCE)));

    vec4 finalHeatColor = isCloseToSea ? blendColor : heatColor;

    // output
    fragColor = finalHeatColor;
}
