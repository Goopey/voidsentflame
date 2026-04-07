#version 150

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;

const float TOLERANCE = 0.05f;
const vec2 rOffset = vec2(0, 0.0025);
const vec2 gOffset = vec2(0.0025, -0.0025);
const vec2 bOffset = vec2(-0.0025, 0);

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);

    //----chromatic aberration
    vec4 rVal = texture(SamplerSea, texCoord + rOffset);
    vec4 gVal = texture(SamplerSea, texCoord + gOffset);
    vec4 bVal = texture(SamplerSea, texCoord + bOffset);

    vec4 aberrantColor = vec4(rVal.r, rVal.g, seaColor.b, 1.0);

    //----overlays world texture over white part of screen
    bool isWhite = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    vec4 mixedColor = isWhite ? worldColor : aberrantColor;

    // output
    fragColor = mixedColor;
}
