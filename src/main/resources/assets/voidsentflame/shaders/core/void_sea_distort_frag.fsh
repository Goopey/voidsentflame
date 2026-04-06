#version 150

uniform sampler2D SamplerSea;
uniform sampler2D SamplerWorld;

const float TOLERANCE = 0.05f;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 seaColor = texture(SamplerSea, texCoord);
    vec4 worldColor = texture(SamplerWorld, texCoord);

    // TODO : add effects
    // overlays world texture over white part of screen
    bool isWhite = all(greaterThanEqual(seaColor.rgb, vec3(1.0 - TOLERANCE)));
    vec4 color = isWhite ? worldColor : seaColor;

    fragColor = color;
}
