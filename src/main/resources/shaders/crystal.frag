#version 460 core

in vec3 vWorldPos;
in vec3 vNormal;
in vec3 vColor;
in float vHeightRatio;
in float vSurfaceCoord;
in float vFormationNoise;

uniform vec3 uCameraPos;
uniform vec3 uLightDirection;
uniform vec3 uFogColor;
uniform int uSurfaceStyle;
uniform float uAmbient;
uniform float uLightIntensity;
uniform float uSpecularStrength;
uniform float uSpecularPower;
uniform float uFresnelStrength;
uniform float uEmission;
uniform float uBandScale;
uniform float uBandStrength;
uniform float uMetallic;
uniform float uRoughness;
uniform float uIridescence;
uniform float uSurfaceScale;
uniform float uFogDensity;
uniform float uFogMax;
uniform float uExposure;

out vec4 fragColor;

const float PI2 = 6.28318530717958647692;

vec3 rainbow(float t) {
    return 0.55 + 0.45 * cos(PI2 * (vec3(0.00, 0.33, 0.67) + t));
}

void main() {
    vec3 N = normalize(vNormal);
    vec3 V = normalize(uCameraPos - vWorldPos);
    vec3 L = normalize(uLightDirection);
    vec3 H = normalize(V + L);

    float diffuse = max(dot(N, L), 0.0);
    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0);
    float roughness = clamp(uRoughness, 0.0, 1.0);
    float specPower = mix(max(2.0, uSpecularPower), 5.0, roughness);
    float specular = pow(max(dot(N, H), 0.0), specPower);

    float scale = max(0.05, uSurfaceScale);
    float surfaceMod = 1.0;
    float surfaceHighlight = 0.0;

    if (uSurfaceStyle == 1) {
        // Longitudinal growth striations as seen strongly on tourmaline/stibnite.
        float stripe = abs(sin((vWorldPos.y * 8.0 + vWorldPos.x * 17.0 + vWorldPos.z * 13.0) * scale));
        surfaceMod *= 0.82 + 0.18 * pow(stripe, 5.0);
        surfaceHighlight = pow(1.0 - stripe, 10.0) * 0.18;
    } else if (uSurfaceStyle == 2) {
        // Terraced horizontal growth steps.
        float stepCoord = fract(vSurfaceCoord * (5.0 + scale * 5.0));
        float edge = 1.0 - smoothstep(0.0, 0.10, stepCoord);
        surfaceMod *= 0.86 + edge * 0.18;
        surfaceHighlight = edge * 0.26;
    } else if (uSurfaceStyle == 4) {
        // Color zoning / growth bands.
        float band = sin((vSurfaceCoord * 18.0 + vFormationNoise * 4.0) * scale);
        surfaceMod *= 0.88 + 0.12 * band;
    }

    float metallic = clamp(uMetallic, 0.0, 1.0);
    if (uSurfaceStyle == 3) {
        metallic = max(metallic, 0.82);
    }

    float iri = clamp(uIridescence, 0.0, 1.0);
    if (uSurfaceStyle == 5) {
        iri = max(iri, 0.72);
    }

    vec3 baseColor = max(vColor * surfaceMod, vec3(0.0));
    if (iri > 0.001) {
        float phase = fract(fresnel * 0.72
                + vSurfaceCoord * 0.21 * scale
                + dot(vWorldPos.xz, vec2(0.071, 0.053))
                + vFormationNoise * 0.18);
        vec3 oxide = rainbow(phase);
        baseColor = mix(baseColor, oxide, iri * (0.28 + fresnel * 0.72));
    }

    float bandWave = sin(vWorldPos.y * uBandScale + vWorldPos.x * 2.3 + vWorldPos.z * 1.7);
    float internalBands = 1.0 + bandWave * uBandStrength;
    float growthGlow = mix(0.22, 1.0, smoothstep(0.0, 0.28, vHeightRatio));

    float diffuseWeight = mix(1.0, 0.34, metallic);
    vec3 color = baseColor * (uAmbient + diffuse * uLightIntensity * diffuseWeight) * internalBands;

    vec3 dielectricSpec = vec3(0.90, 0.96, 1.0);
    vec3 specularColor = mix(dielectricSpec, max(baseColor, vec3(0.05)), metallic);
    color += specularColor * specular * uSpecularStrength * mix(1.0, 1.55, metallic);
    color += baseColor * fresnel * uFresnelStrength * mix(1.0, 0.45, metallic);
    color += baseColor * uEmission * growthGlow;
    color += specularColor * surfaceHighlight * (0.35 + uSpecularStrength * 0.18);

    // Simple filmic-ish exposure mapping keeps very bright metallic/iridescent presets usable.
    color = vec3(1.0) - exp(-max(color, vec3(0.0)) * max(0.01, uExposure));

    float distanceToCamera = length(uCameraPos - vWorldPos);
    float fog = 1.0 - exp(-distanceToCamera * max(0.0, uFogDensity));
    color = mix(color, uFogColor, clamp(fog, 0.0, clamp(uFogMax, 0.0, 1.0)));

    fragColor = vec4(color, 1.0);
}
