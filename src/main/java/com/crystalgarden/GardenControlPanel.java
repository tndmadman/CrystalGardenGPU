package com.crystalgarden;

import imgui.ImGui;

public final class GardenControlPanel {
    public record Actions(boolean regenerate, boolean exitRequested) {
    }

    public Actions draw(GardenSettings s) {
        boolean regenerate = false;
        boolean generationChanged = false;
        boolean exitRequested = false;

        ImGui.begin("Crystal Garden Lab");
        ImGui.text("GPU procedural crystal sandbox");
        ImGui.text("FPS: " + Math.round(ImGui.getIO().getFramerate()) + "   Active: " + s.activeCount[0]);
        ImGui.separator();

        if (ImGui.button("New Garden")) {
            s.randomizeSeed();
            regenerate = true;
        }
        ImGui.sameLine();
        if (ImGui.button("Regrow Same Seed")) {
            regenerate = true;
        }
        ImGui.sameLine();
        if (ImGui.button("Randomize Everything")) {
            s.randomizeAll();
            regenerate = true;
        }

        if (ImGui.collapsingHeader("Lifecycle / Live Generation")) {
            if (ImGui.inputInt("Seed", s.seed)) generationChanged = true;
            ImGui.checkbox("Live regenerate on generator changes", s.liveRegenerate);
            ImGui.checkbox("Auto new garden", s.autoRegenerate);
            ImGui.sliderFloat("Auto regen seconds", s.autoRegenerateSeconds, 1.0f, 60.0f);
            ImGui.checkbox("Pause growth", s.pauseGrowth);
            ImGui.textWrapped("R = new seed, G = regrow same seed, F1 = randomize all, Tab = toggle this panel, Esc = release/capture mouse.");
        }

        if (ImGui.collapsingHeader("Population / Distribution")) {
            if (ImGui.sliderInt("Crystal count", s.activeCount, 256, GardenSettings.MAX_CRYSTALS)) generationChanged = true;
            if (ImGui.combo("Distribution", s.distribution, GardenSettings.DISTRIBUTIONS)) generationChanged = true;
            if (ImGui.sliderFloat("Spacing / field scale", s.spacing, 0.10f, 1.20f)) generationChanged = true;
            if (ImGui.sliderFloat("Placement jitter", s.jitter, 0.0f, 1.5f)) generationChanged = true;
            if (ImGui.sliderFloat("Empty / sparse fraction", s.sparsity, 0.0f, 0.80f)) generationChanged = true;
            if (ImGui.sliderInt("Cluster count", s.clusterCount, 1, 64)) generationChanged = true;
            if (ImGui.sliderFloat("Cluster radius", s.clusterRadius, 0.2f, 12.0f)) generationChanged = true;
            if (ImGui.sliderFloat("Spiral turns", s.spiralTurns, 1.0f, 40.0f)) generationChanged = true;
        }

        if (ImGui.collapsingHeader("Crystal Size / Growth")) {
            if (ImGui.sliderFloat("Minimum radius", s.minRadius, 0.005f, 0.40f)) generationChanged = true;
            if (ImGui.sliderFloat("Maximum radius", s.maxRadius, 0.01f, 0.80f)) generationChanged = true;
            if (ImGui.sliderFloat("Minimum height", s.minHeight, 0.02f, 4.0f)) generationChanged = true;
            if (ImGui.sliderFloat("Maximum height", s.maxHeight, 0.10f, 16.0f)) generationChanged = true;
            if (ImGui.sliderFloat("Height distribution power", s.heightPower, 0.25f, 6.0f)) generationChanged = true;
            if (ImGui.sliderFloat("Minimum growth speed", s.growthMin, 0.005f, 1.5f)) generationChanged = true;
            if (ImGui.sliderFloat("Maximum growth speed", s.growthMax, 0.01f, 3.0f)) generationChanged = true;
            ImGui.sliderFloat("Live growth multiplier", s.growthMultiplier, 0.0f, 8.0f);
            ImGui.sliderFloat("Growth pulse", s.pulseStrength, 0.0f, 0.95f);
        }

        if (ImGui.collapsingHeader("Geometry / Complexity")) {
            ImGui.sliderInt("Polygon sides", s.sides, 3, 12);
            ImGui.sliderFloat("Shoulder taper", s.taper, 0.15f, 1.35f);
            ImGui.sliderFloat("Tip length ratio", s.tipRatio, 0.02f, 0.70f);
            ImGui.sliderFloat("Twist turns", s.twistTurns, -3.0f, 3.0f);
            ImGui.sliderFloat("Random tilt", s.tilt, 0.0f, 1.2f);
            ImGui.sliderFloat("Field bend", s.bend, 0.0f, 1.0f);
            ImGui.sliderFloat("Living sway", s.motionStrength, 0.0f, 0.50f);
            ImGui.sliderFloat("Sway speed", s.motionSpeed, 0.0f, 5.0f);
            ImGui.separator();
            ImGui.sliderInt("Shards per crystal cluster", s.shardsPerCluster, 1, GardenSettings.MAX_SHARDS_PER_CLUSTER);
            ImGui.sliderFloat("Satellite spread", s.shardSpread, 0.0f, 7.0f);
            ImGui.sliderFloat("Satellite size", s.shardScale, 0.1f, 1.2f);
            ImGui.sliderFloat("Satellite outward lean", s.shardLean, 0.0f, 1.5f);
        }

        if (ImGui.collapsingHeader("Mineral Field / Colors")) {
            if (ImGui.sliderFloat("Mineral scale", s.mineralScale, 0.01f, 1.5f)) generationChanged = true;
            if (ImGui.sliderFloat("Domain warp", s.mineralWarp, 0.0f, 6.0f)) generationChanged = true;
            if (ImGui.sliderFloat("Mineral contrast", s.mineralContrast, 0.05f, 4.0f)) generationChanged = true;
            if (ImGui.colorEdit3("Palette A", s.paletteA)) generationChanged = true;
            if (ImGui.colorEdit3("Palette B", s.paletteB)) generationChanged = true;
            if (ImGui.colorEdit3("Palette C", s.paletteC)) generationChanged = true;
            if (ImGui.sliderFloat("Third color mix", s.thirdColorMix, 0.0f, 1.0f)) generationChanged = true;
            if (ImGui.sliderFloat("Per-crystal color variation", s.colorVariation, 0.0f, 0.65f)) generationChanged = true;
        }

        if (ImGui.collapsingHeader("Material / Lighting / Fog")) {
            ImGui.sliderFloat("Ambient", s.ambient, 0.0f, 1.0f);
            ImGui.sliderFloat("Light intensity", s.lightIntensity, 0.0f, 3.0f);
            ImGui.sliderFloat("Specular strength", s.specularStrength, 0.0f, 4.0f);
            ImGui.sliderFloat("Specular sharpness", s.specularPower, 2.0f, 256.0f);
            ImGui.sliderFloat("Fresnel edge glow", s.fresnelStrength, 0.0f, 4.0f);
            ImGui.sliderFloat("Emission", s.emission, 0.0f, 1.5f);
            ImGui.sliderFloat("Internal band scale", s.bandScale, 0.5f, 60.0f);
            ImGui.sliderFloat("Internal band strength", s.bandStrength, 0.0f, 0.60f);
            ImGui.sliderFloat("Fog density", s.fogDensity, 0.0f, 0.10f);
            ImGui.sliderFloat("Maximum fog", s.fogMax, 0.0f, 1.0f);
            ImGui.sliderFloat("Exposure", s.exposure, 0.2f, 3.0f);
            ImGui.colorEdit3("Fog / background color", s.fogColor);
            ImGui.sliderFloat3("Light direction", s.lightDirection, -1.0f, 1.0f);
        }

        if (ImGui.collapsingHeader("Camera")) {
            ImGui.sliderFloat("Field of view", s.fov, 25.0f, 110.0f);
            ImGui.sliderFloat("Move speed", s.cameraSpeed, 0.5f, 40.0f);
            ImGui.sliderFloat("Shift speed", s.cameraBoost, 1.0f, 100.0f);
            ImGui.sliderFloat("Mouse sensitivity", s.mouseSensitivity, 0.0002f, 0.0100f, "%.4f");
        }

        if (ImGui.collapsingHeader("Presets")) {
            if (ImGui.button("Needle Forest")) {
                s.applyPreset(0);
                regenerate = true;
            }
            ImGui.sameLine();
            if (ImGui.button("Amethyst Cathedral")) {
                s.applyPreset(1);
                regenerate = true;
            }
            if (ImGui.button("Alien Reef")) {
                s.applyPreset(2);
                regenerate = true;
            }
            ImGui.sameLine();
            if (ImGui.button("Crystal Storm")) {
                s.applyPreset(3);
                regenerate = true;
            }
            if (ImGui.button("Obsidian Spires")) {
                s.applyPreset(4);
                regenerate = true;
            }
        }

        ImGui.separator();
        if (ImGui.button("Exit Application")) {
            exitRequested = true;
        }

        s.clampRanges();
        if (generationChanged && s.liveRegenerate[0]) {
            regenerate = true;
        }

        ImGui.end();
        return new Actions(regenerate, exitRequested);
    }
}
