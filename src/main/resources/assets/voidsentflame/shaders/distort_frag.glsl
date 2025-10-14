#version 300 es

in vec2 uv;
out vec4 outColor;

uniform sampler2D sceneTex;
uniform sampler2D sceneDepth;

uniform Params {
    vec2 waveCenter;
    float radius;
    float time;
    vec2 screenSize;
} p;

void main() {
    vec2 screenPos = uv * p.screenSize;
    vec2 diff = screenPos - p.waveCenter;
    float dist = length(diff);

    // outside radius → no distortion
    if (dist > p.radius) {
        outColor = texture(sceneTex, uv);
        return;
    }

    float strength = smoothstep(p.radius, 0.0, dist);
    float offset = sin(dist * 0.1 + p.time * 2.0) * 10.0 * strength;
    vec2 dir = normalize(diff);
    vec2 sampleUV = uv + dir * offset / p.screenSize;

    outColor = texture(sceneTex, sampleUV);
}
