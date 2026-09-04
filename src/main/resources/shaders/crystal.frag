#version 460 core

in vec3 vWorldPos;
in vec3 vNormal;
in vec3 vColor;
in float vHeightRatio;

uniform vec3 uCameraPos;

out vec4 fragColor;

void main() {
    vec3 N = normalize(vNormal);
    vec3 V = normalize(uCameraPos - vWorldPos);
    vec3 L = normalize(vec3(-0.36, 0.82, 0.28));
    vec3 H = normalize(V + L);

    float diffuse = max(dot(N, L), 0.0);
    float specular = pow(max(dot(N, H), 0.0), 72.0);
    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0);

    float internalBands = 0.92 + 0.08 * sin(vWorldPos.y * 13.0 + vWorldPos.x * 2.3 + vWorldPos.z * 1.7);
    float growthGlow = mix(0.28, 1.0, smoothstep(0.0, 0.22, vHeightRatio));

    vec3 color = vColor * (0.12 + diffuse * 0.88) * internalBands;
    color += vColor * fresnel * 1.25;
    color += vec3(0.88, 0.96, 1.0) * specular * 1.15;
    color += vColor * 0.06 * growthGlow;

    float distanceToCamera = length(uCameraPos - vWorldPos);
    float fog = 1.0 - exp(-distanceToCamera * 0.018);
    vec3 fogColor = vec3(0.004, 0.006, 0.012);
    color = mix(color, fogColor, clamp(fog, 0.0, 0.82));

    fragColor = vec4(color, 1.0);
}
