#version 460 core

struct Crystal {
    vec4 positionHeight;
    vec4 params;
    vec4 color;
};

layout(std430, binding = 0) readonly buffer CrystalBuffer {
    Crystal crystals[];
};

uniform mat4 uViewProjection;
uniform float uTime;
uniform uint uSeed;
uniform int uSides;
uniform float uTaper;
uniform float uTipRatio;
uniform float uTwistTurns;
uniform float uTilt;
uniform float uBend;
uniform float uMotionStrength;
uniform float uMotionSpeed;

out vec3 vWorldPos;
out vec3 vNormal;
out vec3 vColor;
out float vHeightRatio;

const float PI = 3.14159265358979323846;
const int MAX_SIDES = 12;
const int BODY_VERTICES = MAX_SIDES * 6;

uint hash(uint x) {
    x += x << 10u;
    x ^= x >> 6u;
    x += x << 3u;
    x ^= x >> 11u;
    x += x << 15u;
    return x;
}

float rand01(uint x) {
    return float(hash(x ^ hash(uSeed + 0x9E3779B9u))) / 4294967295.0;
}

vec3 ringPoint(float angle, float y, float radius) {
    return vec3(cos(angle) * radius, y, sin(angle) * radius);
}

vec3 deform(vec3 localPos, Crystal c, uint instanceId) {
    float height = max(c.positionHeight.w, 0.001);
    float y = clamp(localPos.y / height, 0.0, 1.0);
    float seedAngle = rand01(instanceId * 131u + 17u) * 2.0 * PI;
    vec2 tiltDir = vec2(cos(seedAngle), sin(seedAngle));

    vec2 staticTilt = tiltDir * (uTilt * height * 0.18 * y);

    float fieldAngle = sin(c.positionHeight.x * 0.17 + float(uSeed & 255u) * 0.013)
                     + cos(c.positionHeight.z * 0.13 - float(uSeed & 127u) * 0.019);
    vec2 bendDir = vec2(cos(fieldAngle * PI), sin(fieldAngle * PI));
    vec2 fieldBend = bendDir * (uBend * height * 0.22 * y * y);

    float motionPhase = rand01(instanceId * 197u + 61u) * 2.0 * PI;
    vec2 motionDir = vec2(cos(seedAngle + 1.7), sin(seedAngle + 1.7));
    float sway = sin(uTime * uMotionSpeed + motionPhase + y * 2.3);
    vec2 liveMotion = motionDir * (sway * uMotionStrength * height * 0.14 * y * y);

    localPos.xz += staticTilt + fieldBend + liveMotion;
    return localPos;
}

void main() {
    Crystal c = crystals[gl_InstanceID];
    uint instanceId = uint(gl_InstanceID);
    int sides = clamp(uSides, 3, MAX_SIDES);
    float height = max(c.positionHeight.w, 0.001);
    float radius = max(c.params.x, 0.00001);
    float tipRatio = clamp(uTipRatio, 0.02, 0.90);
    float shoulderY = height * (1.0 - tipRatio);
    float shoulderRadius = radius * max(0.02, uTaper);
    float baseRotation = rand01(instanceId * 89u + 7u) * 2.0 * PI;
    float twist = uTwistTurns * 2.0 * PI;
    float step = 2.0 * PI / float(sides);

    vec3 localPos = vec3(0.0);
    vec3 normal = vec3(0.0, 1.0, 0.0);
    int face;

    if (gl_VertexID < BODY_VERTICES) {
        face = gl_VertexID / 6;
        int corner = gl_VertexID % 6;

        if (face < sides) {
            float a0b = baseRotation + float(face) * step;
            float a1b = baseRotation + float(face + 1) * step;
            float a0t = a0b + twist;
            float a1t = a1b + twist;

            vec3 b0 = ringPoint(a0b, 0.0, radius);
            vec3 b1 = ringPoint(a1b, 0.0, radius);
            vec3 t0 = ringPoint(a0t, shoulderY, shoulderRadius);
            vec3 t1 = ringPoint(a1t, shoulderY, shoulderRadius);

            if (corner == 0) localPos = b0;
            else if (corner == 1) localPos = b1;
            else if (corner == 2) localPos = t1;
            else if (corner == 3) localPos = b0;
            else if (corner == 4) localPos = t1;
            else localPos = t0;

            float mid = 0.5 * (a0b + a1b);
            float taperSlope = (radius - shoulderRadius) / max(shoulderY, 0.001);
            normal = normalize(vec3(cos(mid), taperSlope, sin(mid)));
        }
    } else {
        int localId = gl_VertexID - BODY_VERTICES;
        face = localId / 3;
        int corner = localId % 3;

        if (face < sides) {
            float a0 = baseRotation + float(face) * step + twist;
            float a1 = baseRotation + float(face + 1) * step + twist;
            vec3 p0 = ringPoint(a0, shoulderY, shoulderRadius);
            vec3 p1 = ringPoint(a1, shoulderY, shoulderRadius);
            vec3 tip = vec3(0.0, height, 0.0);

            if (corner == 0) localPos = p0;
            else if (corner == 1) localPos = p1;
            else localPos = tip;

            normal = normalize(cross(tip - p0, p1 - p0));
        }
    }

    // Faces above the selected side count collapse into degenerate triangles.
    if (face >= sides) {
        localPos = vec3(0.0);
        normal = vec3(0.0, 1.0, 0.0);
    }

    localPos = deform(localPos, c, instanceId);
    vec3 worldPos = c.positionHeight.xyz + localPos;

    vWorldPos = worldPos;
    vNormal = normal;
    vColor = c.color.rgb;
    vHeightRatio = clamp(height / max(c.params.w, 0.001), 0.0, 1.0);

    gl_Position = uViewProjection * vec4(worldPos, 1.0);
}
