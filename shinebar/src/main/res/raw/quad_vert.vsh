#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;

in vec3 inPosition;
in vec2 inTextureCoord;

out vec2 textureCoord;

void main() {
    gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);
    textureCoord = (uSTMatrix * vec4(inTextureCoord.xy, 0, 0)).xy;
}