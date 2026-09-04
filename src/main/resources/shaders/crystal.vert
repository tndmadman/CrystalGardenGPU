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
uniform int uShardCount;
uniform float uShardSpread;
uniform float uShardScale;
uniform float uShardLean;

out vec3 vWorldPos;
out vec3 vNormal;
out vec3 vColor;
out float vHeightRatio;

const float PI = 3.14159265358979323846;
const int MAX_SIDES = 12;
const int BODY_VERTICES = MAX_SIDES * 6;
const int VERTICES_PER_SHARD = MAX_SIDES * 9;

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

vec3 deform(vec3 localPos, Crystal c, uint instanceId, int shard, vec2 shardDir, float shardLean) {
    float height = max(c.positionHeight.w, 0.001);
    float y = clamp(localPos.y / max(height, 0.001), 0.0, 1.0);
    uint shardSalt = uint(shard) * 977u;
    float seedAngle = rand01(instanceId * 131u + 17u + shardSalt) * 2.0 * PI;
    vec2 tiltDir = vec2(cos(seedAngle), sin(seedAngle));

    vec2 staticTilt = tiltDir * (uTilt * height * 0.18 * y);

    float fieldAngle = sin(c.positionHeight.x * 0.17 + float(uSeed & 255u) * 0.013)
                     + cos(c.positionHeight.z * 0.13 - float(uSeed & 127u) * 0.019);
    vec2 bendDir = vec2(cos(fieldAngle * PI), sin(fieldAngle * PI));
    vec2 fieldBend = bendDir * (uBend * height * 0.22 * y * y);

    float motionPhase = rand01(instanceId * 197u + 61u + shardSalt) * 2.0 * PI;
    vec2 motionDir = vec2(cos(seedAngle + 1.7), sin(seedAngle + 1.7));
    float sway = sin(uTime * uMotionSpeed + motionPhase + y * 2.3);
    vec2 liveMotion = motionDir * (sway * uMotionStrength * height * 0.14 * y * y);

    vec2 satelliteLean = shardDir * shardLean * localPos.y * 0.42;
    localPos.xz += staticTilt + fieldBend + liveMotion + satelliteLean;
    return localPos;
}

void main() {
    Crystal c = crystals[gl_InstanceID];
    uint instanceId = uint(gl_InstanceID);
    int shard = gl_VertexID / VERTICES_PER_SHARD;
    int vertexId = gl_VertexID - shard * VERTICES_PER_SHARD;
    int sides = clamp(uSides, 3, MAX_SIDES);

    float baseHeight = max(c.positionHeight.w, 0.001);
    float baseRadius = max(c.params.x, 0.00001);
    float height = baseHeight;
    float radius = baseRadius;
    vec2 shardOffset = vec2(0.0);
    vec2 shardDir = vec2(0.0);
    float satelliteLean = 0.0;
    float shardColorScale = 1.0;

    if (shard > 0) {
        uint salt = uint(shard) * 733u;
        float r0 = rand01(instanceId * 173u + 31u + salt);
        float r1 = rand01(instanceId * 211u + 47u + salt);
        int satellites = max(uShardCount - 1, 1);
        float angle = (float(shard - 1) / float(satellites)) * 2.0 * PI;
        angle += (r0 - 0.5) * 0.85;
        shardDir = vec2(cos(angle), sin(angle));

        float spread = baseRadius * uShardSpread * mix(0.72, 1.28, r1);
        shardOffset = shardDir * spread;

        float scale = max(0.05, uShardScale) * mix(0.55, 1.0, r0);
        height = baseHeight * scale;
        radius = baseRadius * max(0.08, uShardScale) * mix(0.45, 0.82, r1);
        satelliteLean = uShardLean * mix(0.55, 1.15, r1);
        shardColorScale = mix(0.78, 1.18, r0);
    }

    float tipRatio = clamp(uTipRatio, 0.02, 0.90);
    float shoulderY = height * (1.0 - tipRatio);
    float shoulderRadius = radius * max(0.02, uTaper);
    float baseRotation = rand01(instanceId * 89u + 7u + uint(shard) * 313u) * 2.0 * PI;
    float twist = uTwistTurns * 2.0 * PI;
    if (shard > 0) {
        twist += (rand01(instanceId * 251u + uint(shard) * 101u) - 0.5) * PI * 0.35;
    }
    float step = 2.0 * PI / float(sides);

    vec3 localPos = vec3(0.0);
    vec3 normal = vec3(0.0, 1.0, 0.0);
    int face;

    if (vertexId < BODY_VERTICES) {
        face = vertexId / 6;
        int corner = vertexId % 6;

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
        int localId = vertexId - BODY_VERTICES;
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

    if (face >= sides) {
        localPos = vec3(0.0);
        normal = vec3(0.0, 1.0, 0.0);
    }

    localPos.xz += shardOffset;
    localPos = deform(localPos, c, instanceId, shard, shardDir, satelliteLean);

    if (shard > 0) {
        normal = normalize(normal + vec3(-shardDir.x * satelliteLean * 0.18, 0.0, -shardDir.y * satelliteLean * 0.18));
    }

    vec3 worldPos = c.positionHeight.xyz + localPos;
    vWorldPos = worldPos;
    vNormal = normal;
    vColor = c.color.rgb * shardColorScale;
    vHeightRatio = clamp(baseHeight / max(c.params.w, 0.001), 0.0, 1.0);

    gl_Position = uViewProjection * vec4(worldPos, 1.0);
}
