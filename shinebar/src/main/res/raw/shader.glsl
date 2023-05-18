#version 300 es

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

const float _zero = float(0);
const float _one = float(1);
const float _two = float(2);
const float _half = float(0.5);
const float _quater = float(0.25);
const float _pi = float(3.14159265359);

uniform vec2 uViewSize;
uniform vec4 uStartColor;
uniform vec4 uEndColor;
uniform float uMarginTop;
uniform float uDistanceY;
uniform float uTotalScroll;
uniform float ushaderHeight;

out vec4 fragColor;

// Define the Bezier curve as a function B(t)
// where t is a value between 0 and 1
// and the control points are A, B, and C
vec2 curve(float t, vec2 A, vec2 B, vec2 C) {
    return (1.0 - t) * (1.0 - t) * A + 2.0 * (1.0 - t) * t * B + t * t * C;
}

// Find the projection of point P onto the Bezier curve
// by finding the value of t that minimizes the distance
// between P and B(t)
// Newton - Raphson
float projection(vec2 P, vec2 A, vec2 B, vec2 C) {
    // Check if the control points are in a line
    if (abs((B.y - A.y) * (C.x - B.x) - (C.y - B.y) * (B.x - A.x)) < 0.0001) {
        // Control points are in a line, return the t value corresponding to the projection
        // onto the line segment defined by A and C
        float t = dot(P - A, C - A) / dot(C - A, C - A);
        return clamp(t, 0.0, 1.0);
    }

    // Control points are not in a line, continue with the regular projection calculation
    float t = 0.5;
    const float eps = 1.0;
    for (int i = 0; i < 8; i++) {
        vec2 Bt = curve(t, A, B, C);
        vec2 dBt = 2.0 * (1.0 - t) * (B - A) + 2.0 * t * (C - B);

        if (length(Bt - P) < eps)
        break;

        t = t - dot(Bt - P, dBt) / dot(dBt, dBt);
    }

    return t;
}

void main() {
    float progress = uDistanceY / uTotalScroll;
    float drawHeight = (uViewSize.y * 0.75) * (1.0 - progress);

    // the gradient begins with a point as origin and as the progress increases
    // the origin changes to a line whose max length will be half of the view width
    float length = uViewSize.x * progress * _half;

    // control points of Bezier curve
    vec2 A = vec2(-10.0f, uViewSize.y - uMarginTop - drawHeight - ushaderHeight * progress);
    vec2 B = vec2(length, uViewSize.y - 2.0 * uMarginTop - ushaderHeight * progress);
    vec2 C = vec2(uViewSize.x + ushaderHeight * 0.5, uViewSize.y - uMarginTop - ushaderHeight * progress);

    vec2 P = gl_FragCoord.xy;

    float t = projection(P, A, B, C);

    // projection point on the curve
    vec2 Bt = curve(t, A, B, C);


    vec2 origin = vec2(0.0, uViewSize.y);
    vec2 toP = P - origin;
    vec2 toBt = Bt - origin;
    if (P.y > A.y) {
        if (dot(toP, toBt) >= 0.0 && dot(toP, toP) <= dot(toBt, toBt)) {
            float distance_ratio = 1.0 - length(toP) / length(toBt);
            vec3 color = mix(uStartColor.rgb, uEndColor.rgb, distance_ratio);
            fragColor = vec4(color.rgb, distance_ratio);
        }
    }
}