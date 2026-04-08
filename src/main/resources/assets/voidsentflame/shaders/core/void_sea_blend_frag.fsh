#version 150

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;

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

    //----chromatic aberration
    vec4 abVal1 = texture(SamplerSea, texCoord + AB_OFFSET);

    vec4 aberrantSeaColor = vec4(
        (1.0 - AB_WEIGHTS.x) * (seaColor.r) + (AB_WEIGHTS.x) * (abVal1.r),
        (1.0 - AB_WEIGHTS.y) * (seaColor.g) + (AB_WEIGHTS.y) * (abVal1.g),
        seaColor.b,
        1.0
    );

    //aberrantSeaColor = aberrantSeaColor + aberrantSeaColor * aberrantSeaColor * aberrantSeaColor * aberrantSeaColor;

    //----overlays world texture over white part of screen
    bool isWhite = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    //call colors from other effects
    vec4 mixedColor = isWhite ? worldColor : aberrantSeaColor;

    // output
    fragColor = mixedColor;
}
