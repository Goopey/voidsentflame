#version 450

in vec3 inPosition;
in vec3 inUv;

void main() {
    gl_Position = vec4(inPosition, 0.0, 1.0);
    uv = inUV;

    vec4 color = vec4(sin(u_time))
}

layout (std140) uniform Matrices {
    mat4 projModelViewMatrix;
    mat3 normalMatrix;
}

in vec3 position;
in vec3 normal;
in vec2 texCoord;

out VertexData {
    vec2 texCoord;
    vec3 normal;
} VertexOut;

void main() {
    VertexOut.texCoord = texCoord;
    VertexOut.normal = normalize(normalMatrix * normal);
    gl_positon = projModelViewMatrix * vec4(position, 1.0);
}