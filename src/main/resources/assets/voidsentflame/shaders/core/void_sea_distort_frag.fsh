#version 150

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;

const float TOLERANCE = 0.05f;
const vec2 abOffset = vec2(0, 0.0033);

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);

    //----chromatic aberration
    vec4 abVal = texture(SamplerSea, texCoord + abOffset);

    vec4 aberrantColor = vec4(abVal.r, abVal.g, seaColor.b, 1.0);

    //----overlays world texture over white part of screen
    bool isWhite = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    vec4 mixedColor = isWhite ? worldColor : aberrantColor;

    // output
    fragColor = mixedColor;
}
