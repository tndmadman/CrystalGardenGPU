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
uniform int uFormationType;
uniform int uSurfaceStyle;
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
uniform float uBladeThickness;
uniform int uHopperSteps;
uniform float uHopperInset;
uniform float uBipyramidWaist;
uniform int uDendriteBranches;
uniform float uDendriteAngle;
uniform float uRadialStrength;
uniform float uFormationIrregularity;

out vec3 vWorldPos;
out vec3 vNormal;
out vec3 vColor;
out float vHeightRatio;
out float vSurfaceCoord;
out float vFormationNoise;

const float PI = 3.14159265358979323846;
const int MAX_SIDES = 12;
const int PRISM_BODY_VERTICES = MAX_SIDES * 6;
const int PRISM_VERTICES = MAX_SIDES * 9;
const int BOX_VERTICES = 36;
const int VERTICES_PER_SHARD = 144;

// FormationType ids from FormationType.java
const int FORM_PRISM = 0;
const int FORM_NEEDLE = 1;
const int FORM_BLADE = 2;
const int FORM_CUBE = 3;
const int FORM_HOPPER = 4;
const int FORM_SCALENOHEDRON = 5;
const int FORM_BIPYRAMID = 6;
const int FORM_STARBURST = 7;
const int FORM_FAN = 8;
const int FORM_DENDRITE = 9;

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

vec3 rotateY(vec3 p, float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return vec3(c * p.x + s * p.z, p.y, -s * p.x + c * p.z);
}

void boxVertex(int id, vec3 halfExtents, out vec3 p, out vec3 n, out bool active) {
    active = id >= 0 && id < BOX_VERTICES;
    if (!active) {
        p = vec3(0.0);
        n = vec3(0.0, 1.0, 0.0);
        return;
    }

    int face = id / 6;
    int corner = id % 6;
    vec3 N;
    vec3 U;
    vec3 V;

    if (face == 0) {
        N = vec3(1.0, 0.0, 0.0); U = vec3(0.0, 1.0, 0.0); V = vec3(0.0, 0.0, 1.0);
    } else if (face == 1) {
        N = vec3(-1.0, 0.0, 0.0); U = vec3(0.0, 1.0, 0.0); V = vec3(0.0, 0.0, -1.0);
    } else if (face == 2) {
        N = vec3(0.0, 1.0, 0.0); U = vec3(1.0, 0.0, 0.0); V = vec3(0.0, 0.0, -1.0);
    } else if (face == 3) {
        N = vec3(0.0, -1.0, 0.0); U = vec3(1.0, 0.0, 0.0); V = vec3(0.0, 0.0, 1.0);
    } else if (face == 4) {
        N = vec3(0.0, 0.0, 1.0); U = vec3(1.0, 0.0, 0.0); V = vec3(0.0, 1.0, 0.0);
    } else {
        N = vec3(0.0, 0.0, -1.0); U = vec3(-1.0, 0.0, 0.0); V = vec3(0.0, 1.0, 0.0);
    }

    float su;
    float sv;
    if (corner == 0) { su = -1.0; sv = -1.0; }
    else if (corner == 1) { su = 1.0; sv = -1.0; }
    else if (corner == 2) { su = 1.0; sv = 1.0; }
    else if (corner == 3) { su = -1.0; sv = -1.0; }
    else if (corner == 4) { su = 1.0; sv = 1.0; }
    else { su = -1.0; sv = 1.0; }

    float ne = dot(abs(N), halfExtents);
    float ue = dot(abs(U), halfExtents);
    float ve = dot(abs(V), halfExtents);
    p = N * ne + U * (su * ue) + V * (sv * ve);
    n = N;
}

void orientedBoxVertex(
        int id,
        vec3 center,
        vec3 halfExtents,
        vec3 direction,
        float spin,
        out vec3 p,
        out vec3 n,
        out bool active
) {
    vec3 q;
    vec3 qn;
    boxVertex(id, halfExtents, q, qn, active);
    if (!active) {
        p = vec3(0.0);
        n = vec3(0.0, 1.0, 0.0);
        return;
    }

    vec3 Y = length(direction) > 0.0001 ? normalize(direction) : vec3(0.0, 1.0, 0.0);
    vec3 helper = abs(Y.y) > 0.92 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0);
    vec3 X = normalize(cross(helper, Y));
    vec3 Z = normalize(cross(Y, X));

    float cs = cos(spin);
    float sn = sin(spin);
    vec3 Xs = X * cs + Z * sn;
    vec3 Zs = -X * sn + Z * cs;

    p = center + Xs * q.x + Y * q.y + Zs * q.z;
    n = normalize(Xs * qn.x + Y * qn.y + Zs * qn.z);
}

void prismVertex(
        int id,
        int sides,
        float height,
        float radius,
        float tipRatio,
        float taper,
        float rotation,
        float twist,
        out vec3 p,
        out vec3 n,
        out bool active
) {
    active = false;
    p = vec3(0.0);
    n = vec3(0.0, 1.0, 0.0);

    float shoulderY = height * (1.0 - clamp(tipRatio, 0.02, 0.90));
    float shoulderRadius = radius * max(0.02, taper);
    float step = 2.0 * PI / float(sides);

    if (id < PRISM_BODY_VERTICES) {
        int face = id / 6;
        if (face >= sides) return;
        int corner = id % 6;

        float a0b = rotation + float(face) * step;
        float a1b = rotation + float(face + 1) * step;
        float a0t = a0b + twist;
        float a1t = a1b + twist;

        vec3 b0 = ringPoint(a0b, 0.0, radius);
        vec3 b1 = ringPoint(a1b, 0.0, radius);
        vec3 t0 = ringPoint(a0t, shoulderY, shoulderRadius);
        vec3 t1 = ringPoint(a1t, shoulderY, shoulderRadius);

        if (corner == 0) p = b0;
        else if (corner == 1) p = b1;
        else if (corner == 2) p = t1;
        else if (corner == 3) p = b0;
        else if (corner == 4) p = t1;
        else p = t0;

        float mid = 0.5 * (a0b + a1b);
        float taperSlope = (radius - shoulderRadius) / max(shoulderY, 0.001);
        n = normalize(vec3(cos(mid), taperSlope, sin(mid)));
        active = true;
        return;
    }

    if (id < PRISM_VERTICES) {
        int localId = id - PRISM_BODY_VERTICES;
        int face = localId / 3;
        if (face >= sides) return;
        int corner = localId % 3;

        float a0 = rotation + float(face) * step + twist;
        float a1 = rotation + float(face + 1) * step + twist;
        vec3 p0 = ringPoint(a0, shoulderY, shoulderRadius);
        vec3 p1 = ringPoint(a1, shoulderY, shoulderRadius);
        vec3 tip = vec3(0.0, height, 0.0);

        p = corner == 0 ? p0 : (corner == 1 ? p1 : tip);
        n = normalize(cross(tip - p0, p1 - p0));
        active = true;
    }
}

void bladeVertex(
        int id,
        float height,
        float radius,
        float rotation,
        out vec3 p,
        out vec3 n,
        out bool active
) {
    float shoulderY = height * (1.0 - clamp(uTipRatio, 0.05, 0.65));
    float halfWidth = radius * 2.25;
    float halfDepth = radius * max(0.04, uBladeThickness) * 0.72;

    if (id < BOX_VERTICES) {
        boxVertex(id, vec3(halfWidth, shoulderY * 0.5, halfDepth), p, n, active);
        if (active) {
            p.y += shoulderY * 0.5;
            p = rotateY(p, rotation);
            n = rotateY(n, rotation);
        }
        return;
    }

    active = false;
    p = vec3(0.0);
    n = vec3(0.0, 1.0, 0.0);
    int topId = id - BOX_VERTICES;
    if (topId < 12) {
        int face = topId / 3;
        int corner = topId % 3;
        vec3 corners[4] = vec3[4](
                vec3(-halfWidth, shoulderY, -halfDepth),
                vec3(halfWidth, shoulderY, -halfDepth),
                vec3(halfWidth, shoulderY, halfDepth),
                vec3(-halfWidth, shoulderY, halfDepth)
        );
        vec3 a = corners[face];
        vec3 b = corners[(face + 1) % 4];
        vec3 tip = vec3(0.0, height, 0.0);
        p = corner == 0 ? a : (corner == 1 ? b : tip);
        n = normalize(cross(tip - a, b - a));
        p = rotateY(p, rotation);
        n = rotateY(n, rotation);
        active = true;
    }
}

void bipyramidVertex(
        int id,
        int sides,
        float height,
        float radius,
        float rotation,
        bool scalenohedron,
        out vec3 p,
        out vec3 n,
        out bool active
) {
    active = false;
    p = vec3(0.0);
    n = vec3(0.0, 1.0, 0.0);

    int maxVertices = sides * 6;
    if (id >= maxVertices) return;

    int face = id / 6;
    int local = id % 6;
    float step = 2.0 * PI / float(sides);
    float a0 = rotation + float(face) * step;
    float a1 = rotation + float(face + 1) * step;
    float waistY = height * clamp(uBipyramidWaist, 0.15, 0.85);

    float irregular = clamp(uFormationIrregularity, 0.0, 0.75);
    float mod0 = scalenohedron ? mix(0.72, 1.28, float(face & 1)) : 1.0;
    float mod1 = scalenohedron ? mix(0.72, 1.28, float((face + 1) & 1)) : 1.0;
    mod0 *= 1.0 + irregular * 0.18 * sin(float(face) * 2.7 + rotation);
    mod1 *= 1.0 + irregular * 0.18 * cos(float(face) * 2.3 - rotation);

    vec3 r0 = ringPoint(a0, waistY, radius * mod0);
    vec3 r1 = ringPoint(a1, waistY, radius * mod1);
    vec3 top = vec3(0.0, height, 0.0);
    vec3 bottom = vec3(0.0, 0.0, 0.0);

    if (local < 3) {
        int c = local;
        p = c == 0 ? r0 : (c == 1 ? r1 : top);
        n = normalize(cross(top - r0, r1 - r0));
    } else {
        int c = local - 3;
        p = c == 0 ? r1 : (c == 1 ? r0 : bottom);
        n = normalize(cross(bottom - r1, r0 - r1));
    }
    active = true;
}

void hopperVertex(
        int id,
        float height,
        float radius,
        float rotation,
        out vec3 p,
        out vec3 n,
        out bool active
) {
    int steps = clamp(uHopperSteps, 1, 4);
    int level = id / BOX_VERTICES;
    int boxId = id - level * BOX_VERTICES;
    if (level >= steps) {
        active = false;
        p = vec3(0.0);
        n = vec3(0.0, 1.0, 0.0);
        return;
    }

    float levelHeight = height / float(steps);
    float shrink = max(0.16, 1.0 - float(level) * clamp(uHopperInset, 0.02, 0.45));
    float halfY = levelHeight * 0.46;
    vec3 center = vec3(0.0, levelHeight * (float(level) + 0.5), 0.0);
    vec3 extents = vec3(radius * shrink, halfY, radius * shrink);
    orientedBoxVertex(boxId, center, extents, vec3(0.0, 1.0, 0.0), rotation + float(level) * 0.17, p, n, active);
}

void dendriteVertex(
        int id,
        uint instanceId,
        float height,
        float radius,
        float rotation,
        out vec3 p,
        out vec3 n,
        out bool active
) {
    int segment = id / BOX_VERTICES;
    int boxId = id - segment * BOX_VERTICES;
    int branchCount = clamp(uDendriteBranches, 1, 3);

    if (segment > branchCount) {
        active = false;
        p = vec3(0.0);
        n = vec3(0.0, 1.0, 0.0);
        return;
    }

    if (segment == 0) {
        vec3 center = vec3(0.0, height * 0.5, 0.0);
        vec3 extents = vec3(radius * 0.34, height * 0.5, radius * 0.34);
        orientedBoxVertex(boxId, center, extents, vec3(0.0, 1.0, 0.0), rotation, p, n, active);
        return;
    }

    float level = 0.25 + float(segment) * 0.18;
    vec3 start = vec3(0.0, height * level, 0.0);
    float branchPhase = rotation + float(segment - 1) * (2.0 * PI / float(branchCount));
    branchPhase += (rand01(instanceId * 761u + uint(segment) * 43u) - 0.5) * uFormationIrregularity * 1.8;
    float theta = clamp(uDendriteAngle, 0.10, 1.35);
    vec3 direction = normalize(vec3(cos(branchPhase) * sin(theta), cos(theta), sin(branchPhase) * sin(theta)));
    float lengthScale = 0.24 + 0.10 * rand01(instanceId * 823u + uint(segment) * 59u);
    float branchLength = height * lengthScale;
    vec3 end = start + direction * branchLength;
    vec3 center = (start + end) * 0.5;
    vec3 extents = vec3(radius * 0.23, branchLength * 0.5, radius * 0.23);
    orientedBoxVertex(boxId, center, extents, direction, branchPhase * 0.5, p, n, active);
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

    vec2 formationLean = shardDir * shardLean * localPos.y * 0.42;
    localPos.xz += staticTilt + fieldBend + liveMotion + formationLean;
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

    float shardRandom = rand01(instanceId * 173u + 31u + uint(shard) * 733u);
    float shardRandom2 = rand01(instanceId * 211u + 47u + uint(shard) * 733u);

    if (uFormationType == FORM_STARBURST) {
        int rays = max(uShardCount, 1);
        float angle = (float(shard) / float(rays)) * 2.0 * PI;
        angle += (shardRandom - 0.5) * uFormationIrregularity;
        shardDir = vec2(cos(angle), sin(angle));
        shardOffset = shardDir * baseRadius * uShardSpread * 0.45;
        height = baseHeight * mix(0.72, 1.08, shardRandom2);
        radius = baseRadius * mix(0.62, 1.0, shardRandom);
        satelliteLean = max(uShardLean, uRadialStrength) * mix(0.80, 1.18, shardRandom2);
        shardColorScale = mix(0.88, 1.12, shardRandom);
    } else if (uFormationType == FORM_FAN) {
        int fanCount = max(uShardCount, 1);
        float t = fanCount > 1 ? float(shard) / float(fanCount - 1) - 0.5 : 0.0;
        float baseAngle = rand01(instanceId * 337u + 91u) * 2.0 * PI;
        float angle = baseAngle + t * 1.55 + (shardRandom - 0.5) * uFormationIrregularity * 0.6;
        shardDir = vec2(cos(angle), sin(angle));
        shardOffset = shardDir * baseRadius * uShardSpread * abs(t) * 0.65;
        height = baseHeight * mix(0.68, 1.05, shardRandom2);
        radius = baseRadius * mix(0.70, 1.0, shardRandom);
        satelliteLean = max(uShardLean, uRadialStrength) * mix(0.55, 1.05, abs(t) * 2.0);
        shardColorScale = mix(0.86, 1.14, shardRandom);
    } else if (shard > 0) {
        int satellites = max(uShardCount - 1, 1);
        float angle = (float(shard - 1) / float(satellites)) * 2.0 * PI;
        angle += (shardRandom - 0.5) * (0.85 + uFormationIrregularity);
        shardDir = vec2(cos(angle), sin(angle));

        float spread = baseRadius * uShardSpread * mix(0.72, 1.28, shardRandom2);
        shardOffset = shardDir * spread;

        float scale = max(0.05, uShardScale) * mix(0.55, 1.0, shardRandom);
        height = baseHeight * scale;
        radius = baseRadius * max(0.08, uShardScale) * mix(0.45, 0.82, shardRandom2);
        satelliteLean = uShardLean * mix(0.55, 1.15, shardRandom2);
        shardColorScale = mix(0.78, 1.18, shardRandom);
    }

    float baseRotation = rand01(instanceId * 89u + 7u + uint(shard) * 313u) * 2.0 * PI;
    float twist = uTwistTurns * 2.0 * PI;
    twist += (shardRandom - 0.5) * uFormationIrregularity * PI * 0.45;

    vec3 localPos = vec3(0.0);
    vec3 normal = vec3(0.0, 1.0, 0.0);
    bool active = false;

    if (uFormationType == FORM_CUBE) {
        float halfY = max(radius * 0.58, height * 0.5);
        orientedBoxVertex(
                vertexId,
                vec3(0.0, halfY, 0.0),
                vec3(radius, halfY, radius),
                vec3(0.0, 1.0, 0.0),
                baseRotation,
                localPos,
                normal,
                active
        );
    } else if (uFormationType == FORM_HOPPER) {
        hopperVertex(vertexId, height, radius, baseRotation, localPos, normal, active);
    } else if (uFormationType == FORM_BLADE || uFormationType == FORM_FAN) {
        bladeVertex(vertexId, height, radius, baseRotation, localPos, normal, active);
    } else if (uFormationType == FORM_SCALENOHEDRON) {
        bipyramidVertex(vertexId, max(4, min(sides, 8)), height, radius, baseRotation, true, localPos, normal, active);
    } else if (uFormationType == FORM_BIPYRAMID) {
        bipyramidVertex(vertexId, max(4, min(sides, 8)), height, radius, baseRotation, false, localPos, normal, active);
    } else if (uFormationType == FORM_DENDRITE) {
        dendriteVertex(vertexId, instanceId + uint(shard) * 101u, height, radius, baseRotation, localPos, normal, active);
    } else if (uFormationType == FORM_NEEDLE || uFormationType == FORM_STARBURST) {
        prismVertex(
                vertexId,
                max(3, min(sides, 6)),
                height,
                radius * 0.58,
                max(uTipRatio, 0.28),
                min(uTaper, 0.72),
                baseRotation,
                twist,
                localPos,
                normal,
                active
        );
    } else {
        prismVertex(
                vertexId,
                sides,
                height,
                radius,
                uTipRatio,
                uTaper,
                baseRotation,
                twist,
                localPos,
                normal,
                active
        );
    }

    if (!active) {
        localPos = vec3(0.0);
        normal = vec3(0.0, 1.0, 0.0);
    }

    float surfaceCoord = clamp(localPos.y / max(height, 0.001), 0.0, 1.0);
    localPos.xz += shardOffset;
    localPos = deform(localPos, c, instanceId, shard, shardDir, satelliteLean);

    if (length(shardDir) > 0.001 && satelliteLean > 0.001) {
        normal = normalize(normal + vec3(-shardDir.x * satelliteLean * 0.18, 0.0, -shardDir.y * satelliteLean * 0.18));
    }

    vec3 worldPos = c.positionHeight.xyz + localPos;
    vWorldPos = worldPos;
    vNormal = normal;
    vColor = c.color.rgb * shardColorScale;
    vHeightRatio = clamp(baseHeight / max(c.params.w, 0.001), 0.0, 1.0);
    vSurfaceCoord = surfaceCoord;
    vFormationNoise = rand01(instanceId * 997u + uint(shard) * 67u + 13u);

    gl_Position = uViewProjection * vec4(worldPos, 1.0);
}
