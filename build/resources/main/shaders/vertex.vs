#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTextCoord;
out vec3 outNormal;
out vec3 outPosition;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

void main() {
    vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPos;
    outTextCoord = texCoord;
    outNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    outPosition = mvPos.xyz;
}