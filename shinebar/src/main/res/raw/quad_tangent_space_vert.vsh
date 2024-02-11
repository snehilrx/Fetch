#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;

in vec3 inPosition;
in vec2 inTextureCoord;

out vec2 textureCoord;
out mat3 matrixTBN;

void main() {

    gl_Position = uMVPMatrix * vec4(inPosition.xyz, 1);

    // output texture coordinates
    textureCoord = (uSTMatrix * vec4(inTextureCoord.xy, 0, 0)).xy;

    // hardcoded values as far as we know what it should be for screen-space quad
    vec3 nomral = vec3(0.0f, 1.0f, 0.0f);
    vec3 tangent = vec3(1.0f, 0.0f, 0.0f);
    vec3 biNormal =vec3(0.0f, 1.0f, 0.0f);

    vec3 T = normalize(vec3(uSTMatrix * vec4(tangent, 0.0)));
    vec3 B = normalize(vec3(uSTMatrix * vec4(biNormal, 0.0)));
    vec3 N = normalize(vec3(uSTMatrix * vec4(nomral, 0.0)));

    // output TBN matrix
    matrixTBN = mat3(T, B, N);
}