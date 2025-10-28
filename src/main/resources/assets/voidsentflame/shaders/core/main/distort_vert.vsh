#version 150

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler2;
uniform sampler2D Sampler0;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

const float sphereRadius2 = 256.0;
const float sphereRadius = 16.0;

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

void main() {
    vec3 pos = Position + ModelOffset;
    float x = pos.x;
    float z = pos.z;

    float r2 = sphereRadius2 * sphereRadius2;
    float xz2 = x*x + z*z;
    float y = 0.0;
    if (xz2 < r2) {
        y = sqrt(r2 - xz2);
    }

    vec3 displacedPosition = vec3(x, pos.y + y/sphereRadius - sphereRadius, z);
    
    texCoord0 = UV0;
    vertexColor = Color * minecraft_sample_lightmap(Sampler2, UV2);
    sphericalVertexDistance = fog_spherical_distance(displacedPosition);
    cylindricalVertexDistance = fog_cylindrical_distance(displacedPosition);
    gl_Position = ProjMat * ModelViewMat * vec4(displacedPosition, 1.0);
}