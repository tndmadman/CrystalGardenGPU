#version 460 core

in vec3 vWorldPos;
in vec3 vNormal;
in vec3 vColor;
in float vHeightRatio;

uniform vec3 uCameraPos;
uniform vec3 uLightDirection;
uniform vec3 uFogColor;
uniform float uAmbient;
uniform float uLightIntensity;
uniform float uSpecularStrength;
uniform float uSpecularPower;
uniform float uFresnelStrength;
uniform float uEmission;
uniform float uBandScale;
uniform float uBandStrength;
uniform float uFogDensity;
uniform float uFogMax;
uniform float uExposure;

out vec4 fragColor;

void main() {
    vec3 N = normalize(vNormal);
    vec3 V = normalize(uCameraPos - vWorldPos);
    vec3 L = normalize(uLightDirection);
    vec3 H = normalize(V + L);

    float diffuse = max(dot(N, L), 0.0);
    float specular = pow(max(dot(N, H), 0.0), max(2.0, uSpecularPower));
    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0);

    float bandWave = sin(vWorldPos.y * uBandScale + vWorldPos.x * 2.3 + vWorldPos.z * 1.7);
    float internalBands = 1.0 + bandWave * uBandStrength;
    float growthGlow = mix(0.22, 1.0, smoothstep(0.0, 0.28, vHeightRatio));

    vec3 color = vColor * (uAmbient + diffuse * uLightIntensity) * internalBands;
    color += vColor * fresnel * uFresnelStrength;
    color += vec3(0.88, 0.96, 1.0) * specular * uSpecularStrength;
    color += vColor * uEmission * growthGlow;

    // Simple filmic-ish exposure mapping keeps very bright procedural presets usable.
    color = vec3(1.0) - exp(-max(color, vec3(0.0)) * max(0.01, uExposure));

    float distanceToCamera = length(uCameraPos - vWorldPos);
    float fog = 1.0 - exp(-distanceToCamera * max(0.0, uFogDensity));
    color = mix(color, uFogColor, clamp(fog, 0.0, clamp(uFogMax, 0.0, 1.0)));

    fragColor = vec4(color, 1.0);
}
