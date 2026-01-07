#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:globals.glsl>

// basic inputs, position, color, uv0, uv2, normal
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler0; // texture sampler
uniform sampler2D Sampler1; // lightmap sampler
uniform sampler2D Sampler2; // screen map

// world position
uniform vec3 ChunkOffset;

// basic outputs, fog, texture coordinates and color
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 normal;

// curvature constants
const float sphereRadius4 = 65536.0;
const float sphereRadius2 = 256.0;
const float sphereRadius = 16.0;
const float sphereCurvature = 1.25;

// waves constants
const float waveFrequency = 0.15;
const float waveAmplitude = 1.5;
const float secondsPerDay = 1200.00;
const float timeFrequency = 4.0;

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

void main() {
    vec3 pos = Position;
    float x = pos.x;
    float z = pos.z;
    vec3 sinPos = Position + ModelOffset;
    float sx = sinPos.x;
    float sz = sinPos.z;

    float xz2 = x*x + z*z;
    float y = 0.0;
    if (xz2 < sphereRadius4) {
        y = sqrt(sphereRadius4 - xz2);
    }

    float time = GameTime * secondsPerDay;
    float curvature = (y * sphereCurvature)/sphereRadius - sphereCurvature * sphereRadius;
    float waves = sin((sx - sz + timeFrequency * time) * waveFrequency) * waveAmplitude - waveAmplitude;

    vec3 displacedPosition = vec3(x, pos.y + curvature + waves, z);
    
    texCoord0 = UV0;
    vertexColor = Color * minecraft_sample_lightmap(Sampler1, UV2);
    sphericalVertexDistance = fog_spherical_distance(displacedPosition);
    cylindricalVertexDistance = fog_cylindrical_distance(displacedPosition);
    gl_Position = ProjMat * ModelViewMat * vec4(displacedPosition, 1.0);
}