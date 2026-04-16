#version 150

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    // Smooth interpolation
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float turbulence(vec2 p) {
    float t = 0.0;
    float scale = 1.0;

    for (int i = 0; i < 5; i++) {
        t += abs(noise(p * scale)) / scale;
        scale *= 2.0;
    }

    return t;
}

vec2 flow(vec2 p, float time) {
    float n = noise(p * 0.5 + time * 0.1);
    return p + vec2(time * 0.05, time * 0.02);
}

