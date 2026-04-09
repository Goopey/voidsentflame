#version 150

#moj_import <minecraft:globals.glsl>

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;
uniform sampler2D SamplerBlend;
uniform sampler2D SamplerHeatWave;

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

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float time = GameTime * SECONDS_PER_DAY;
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);
    vec4 blendColor = texture(SamplerBlend, texCoord);

    //----gold foam
    //vec2 flow = vec2(0.015, -0.05) * time * 0.15;
    //vec2 uv1 = texCoord * 2.5 + flow;
    //float n1 = texture(SamplerHeatWave, uv1).r;

    // Secondary distortion
    //vec2 uv2 = texCoord * 4.0 - flow * 1.5;
    //float n2 = texture(SamplerHeatWave, uv2).r;

    // Distort coordinates (important for organic foam look)
    //vec2 distortedUV = uv1 + vec2(n2 - 0.5) * 0.2;
    //float nTex = texture(SamplerHeatWave, distortedUV).r;

    // Foam shaping
    //float foam = smoothstep(0.65, 0.8, nTex);
    // Sharper highlights
    //foam += pow(nTex, 4.0) * 0.2;
    //vec4 goldColor = vec4(GOLD * foam, 0.5);

    //----heat wave
    float jacked_time = 5.5 * time;

    vec2 heatCoord = texCoord + (1.0 - texCoord.y) * HEAT_STRENGTH * sin(HEAT_SCALE * jacked_time + length(texCoord) * 10.0);
    vec4 heatColor = texture(SamplerBlend, heatCoord);

    //----heat wave padding : expand the sea texture in 3 directions to "pad" the texture
    vec2 blowUpCoord[3] = {{0, -0.5 * HEAT_MASK_HEIGHT}, {HEAT_MASK_HEIGHT, 0}, {-HEAT_MASK_HEIGHT, 0}};
    vec4 offsetSea = vec4(0.0);
    for (int i = 0; i < 3; i++) {
        offsetSea += vec4(1.0) - texture(SamplerSea, texCoord + blowUpCoord[i]);
    }
    offsetSea = vec4(1.0) - offsetSea;

    //----combine textures
    bool isWhite = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    bool isCloseToSea = isWhite && all(greaterThanEqual(offsetSea.rgb, vec3(1.0 - TOLERANCE)));

    vec4 finalHeatColor = isCloseToSea ? blendColor : heatColor;

    //----output
    fragColor = finalHeatColor;
}
