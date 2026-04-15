#version 150

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;
uniform sampler2D SamplerHeatWave;
uniform sampler2D SamplerDistortionGradient;

//----white mask
const float TOLERANCE = 0.05f;
//----chromatic aberration
const vec2 AB_OFFSET = vec2(0, 0.0033);
const vec3 AB_WEIGHTS = vec3(0.7, 0.7, 0);

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);
    vec4 gradientColor = texture(SamplerDistortionGradient, texCoord);

    //----chromatic aberration
    vec4 abVal1 = texture(SamplerSea, texCoord + AB_OFFSET);

    vec4 aberrantSeaColor = vec4(
        (1.0 - AB_WEIGHTS.x) * (seaColor.r) + (AB_WEIGHTS.x) * (abVal1.r),
        (1.0 - AB_WEIGHTS.y) * (seaColor.g) + (AB_WEIGHTS.y) * (abVal1.g),
        seaColor.b,
        1.0
    );

    //----bake in lighting and gold foam
    vec3 heatWaveMask = texture(SamplerHeatWave, texCoord).rgb;
    vec3 heatMask = 1.0 - ((1.0 - heatWaveMask) * (1.0 - gradientColor.rbg));

    //----overlays world texture over white part of screen
    bool isNotSea = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));

    if (isNotSea) {
        //bake in lighting
        float lighting = (1.0 - heatMask.r);
        worldColor += vec4(lighting / 8, lighting / 8, 0.05, 1.0);
        //terrain gold foam
        // TODO : add terrain gold foam
        //worldColor += vec4((1.0 - heatMask.r), (1.0 - heatMask.r), 0.0, 1.0);
    }

    //----call colors from other effects
    vec4 mixedColor = isNotSea ? worldColor : aberrantSeaColor;

    // output
    fragColor = mixedColor;
}
