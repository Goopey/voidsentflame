#version 150

uniform sampler2D SamplerIn;
uniform sampler2D SamplerDepth;

in vec2 texCoord;
out vec4 fragColor;

float near = 0.1;
float far  = 1000.0;

float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main() {
    float d = LinearizeDepth(texture(SamplerDepth, texCoord).r);
    float dist = length(vec3(1.0, (2.0 * texCoord - 1.0) * vec2(854.0/480.0, 1.0) * tan(radians(90 / 2.0))) * d);

    if (mod(dist, 1.0) <= 0.05) {
        fragColor = vec4(1.0, 1.0, 1.0, 1.0);
    } else {
        fragColor = vec4(texture(SamplerIn, texCoord).rgb, 1.0);
    }
}
