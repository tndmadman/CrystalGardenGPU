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

out vec3 vWorldPos;
out vec3 vNormal;
out vec3 vColor;
out float vHeightRatio;

const float PI = 3.14159265358979323846;
const int SIDES = 6;

vec3 ringPoint(float angle, float y, float radius) {
    return vec3(cos(angle) * radius, y, sin(angle) * radius);
}

void main() {
    Crystal c = crystals[gl_InstanceID];
    float height = max(c.positionHeight.w, 0.001);
    float radius = c.params.x;
    float shoulderY = height * 0.82;
    float shoulderRadius = radius * 0.82;
    float rotation = fract(c.params.y * 7.137) * (2.0 * PI);

    vec3 localPos;
    vec3 normal;

    if (gl_VertexID < 36) {
        int face = gl_VertexID / 6;
        int corner = gl_VertexID % 6;
        float a0 = rotation + float(face) * (2.0 * PI / float(SIDES));
        float a1 = rotation + float(face + 1) * (2.0 * PI / float(SIDES));

        vec3 b0 = ringPoint(a0, 0.0, radius);
        vec3 b1 = ringPoint(a1, 0.0, radius);
        vec3 t0 = ringPoint(a0, shoulderY, shoulderRadius);
        vec3 t1 = ringPoint(a1, shoulderY, shoulderRadius);

        if (corner == 0) localPos = b0;
        else if (corner == 1) localPos = b1;
        else if (corner == 2) localPos = t1;
        else if (corner == 3) localPos = b0;
        else if (corner == 4) localPos = t1;
        else localPos = t0;

        float mid = 0.5 * (a0 + a1);
        normal = normalize(vec3(cos(mid), 0.10, sin(mid)));
    } else {
        int localId = gl_VertexID - 36;
        int face = localId / 3;
        int corner = localId % 3;
        float a0 = rotation + float(face) * (2.0 * PI / float(SIDES));
        float a1 = rotation + float(face + 1) * (2.0 * PI / float(SIDES));

        vec3 p0 = ringPoint(a0, shoulderY, shoulderRadius);
        vec3 p1 = ringPoint(a1, shoulderY, shoulderRadius);
        vec3 tip = vec3(0.0, height, 0.0);

        if (corner == 0) localPos = p0;
        else if (corner == 1) localPos = p1;
        else localPos = tip;

        normal = normalize(cross(tip - p0, p1 - p0));
    }

    vec3 worldPos = c.positionHeight.xyz + localPos;
    vWorldPos = worldPos;
    vNormal = normal;
    vColor = c.color.rgb;
    vHeightRatio = clamp(height / max(c.params.w, 0.001), 0.0, 1.0);

    gl_Position = uViewProjection * vec4(worldPos, 1.0);
}
