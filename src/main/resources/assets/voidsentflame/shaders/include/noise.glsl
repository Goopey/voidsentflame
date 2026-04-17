#version 150

//#####################################################################
//                          Random functions
//#####################################################################

float hash(vec2 v) {
    return fract(sin(dot(v, vec2(127.1, 311.7))) * 43758.5453);
}

float random (vec2 v) {
    return fract(sin(dot(v.xy, vec2(12.9898,78.233))) * 43758.5453123);
}


vec2 fade(vec2 t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

//#####################################################################
//                          Noise functions
//#####################################################################

float basicNoise(vec2 v) {
    vec2 i = floor(v);
    vec2 f = fract(v);

    // Smooth interpolation
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float mixNoise(vec2 v) {
    vec2 i = floor(v);
    vec2 f = fract(v);

    // Four corners in 2D of a tile
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) +
        (c - a)* u.y * (1.0 - u.x) +
        (d - b) * u.x * u.y;
}

float signedNoise(vec2 v) {
    vec2 i = floor(v);
    vec2 f = fract(v);

    vec2 u = fade(f);

    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    float value = mix(mix(a, b, u.x), mix(c, d, u.x), u.y);

    return value * 2.0 - 1.0;
}

//##########################################################
//                          FBM
//##########################################################

float fbm(vec2 vec) {
    // Initial values
    float value = 0.0;
    float amplitude = .5;
    float frequency = 0.;

    // Loop of octaves
    for (int i = 0; i < 6; i++) {
        value += amplitude * mixNoise(vec);
        vec *= 2.;
        amplitude *= .5;
    }

    return value;
}

float ABV(vec2 vec) {
    float amplitude = .5;
    float value = 0.0;

    for (int i = 0; i < 6; i++) {
        value += amplitude * abs(signedNoise(vec));
        vec *= 1.;
        amplitude *= 0.89;
    }

    return value;
}

float ABVRidge(vec2 vec) {
    float value = ABV(vec);
    value = abs(value);
    value = 1.0 - value;
    value = value * value;
    return value;
}

//##########################################################
//                      Miscellaneous
//##########################################################

float turbulence(vec2 vec, float scale) {
    float t = 0.0;

    for (int i = 0; i < 5; i++) {
        t += abs(basicNoise(vec * scale)) / scale;
        scale *= 2.0;
    }

    return t;
}

vec2 flow(vec2 vec, float time) {
    float n = basicNoise(vec * 0.5 + time * 0.1);
    return vec + vec2(time * 0.05, time * 0.02);
}

float turbulenceRidge(vec2 vec, float scale) {
    float t = 0.0;

    for (int i = 0; i < 5; i++) {
        t += abs(ABVRidge(vec * scale)) / scale;
        scale *= 2.0;
    }

    return t;
}

vec2 flowRidge(vec2 vec, float time) {
    float n = ABVRidge(vec * 0.5 + time * 0.1);
    return vec + vec2(time * 0.05, time * 0.02);
}
