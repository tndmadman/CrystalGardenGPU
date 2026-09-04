package com.crystalgarden;

public final class MineralPresetLibrary {
    public static final String[] NAMES = {
            "Quartz Cathedral",
            "Amethyst Geode",
            "Citrine Spires",
            "Smoky Quartz Field",
            "Emerald Pocket",
            "Aquamarine Columns",
            "Tourmaline Grove",
            "Ruby Corundum Cluster",
            "Sapphire Corundum Cluster",
            "Fluorite Cubes",
            "Galena Blocks",
            "Pyrite Citadel",
            "Bismuth Hopper Towers",
            "Selenite Blades",
            "Kyanite Fans",
            "Stibnite Needle Spray",
            "Aragonite Starburst",
            "Calcite Dogtooth Cluster",
            "Sulfur Bipyramids",
            "Native Copper Dendrites"
    };

    public static final String[] DESCRIPTIONS = {
            "Quartz habit: tall six-sided prisms with sharp terminations and clustered satellite points.",
            "Amethyst habit: purple quartz packed into geode-like pockets with zoned internal color bands.",
            "Citrine habit: warm golden quartz spires with slender prismatic growth and bright glassy faces.",
            "Smoky quartz habit: darker translucent-looking hexagonal prisms with subdued brown-gray zoning.",
            "Beryl / emerald habit: stout six-sided green columns concentrated into mineral pockets.",
            "Aquamarine / beryl habit: long clean pale-blue hexagonal columns with low distortion.",
            "Tourmaline habit: narrow strongly striated rods with mixed dark, green, and pink coloration.",
            "Corundum habit: chunky ruby-red bipyramidal/barrel-like crystals in compact groups.",
            "Corundum habit: deep blue bipyramidal crystals with hard bright facet highlights.",
            "Fluorite habit: blocky cubic crystals, often violet-green, growing as overlapping cube clusters.",
            "Galena habit: dense lead-gray metallic cubes with very sharp reflective faces.",
            "Pyrite habit: brass-gold metallic cubes arranged into geometric citadel-like clusters.",
            "Bismuth habit: stepped hopper/terraced crystals with strong oxide-film iridescence.",
            "Gypsum / selenite habit: pale translucent-looking flat blades growing in loose clusters.",
            "Kyanite habit: blue flattened blades arranged into directional fan-shaped sprays.",
            "Stibnite habit: long silver-black metallic needles forming dense radiating sprays.",
            "Aragonite habit: warm radial sprays with many outward-leaning prismatic points.",
            "Calcite dogtooth habit: scalenohedral tooth-like crystals with sharp asymmetric-looking facets.",
            "Sulfur habit: vivid yellow orthorhombic-inspired bipyramids in sparse clustered mounds.",
            "Native copper habit: metallic branching dendritic forms with oxidized teal color variation."
    };

    private MineralPresetLibrary() {
    }

    public static void apply(GardenSettings s, int preset) {
        if (preset < 0 || preset >= NAMES.length) {
            throw new IllegalArgumentException("Unknown mineral preset: " + preset);
        }

        resetCommon(s);

        switch (preset) {
            case 0 -> quartz(s);
            case 1 -> amethyst(s);
            case 2 -> citrine(s);
            case 3 -> smokyQuartz(s);
            case 4 -> emerald(s);
            case 5 -> aquamarine(s);
            case 6 -> tourmaline(s);
            case 7 -> ruby(s);
            case 8 -> sapphire(s);
            case 9 -> fluorite(s);
            case 10 -> galena(s);
            case 11 -> pyrite(s);
            case 12 -> bismuth(s);
            case 13 -> selenite(s);
            case 14 -> kyanite(s);
            case 15 -> stibnite(s);
            case 16 -> aragonite(s);
            case 17 -> calcite(s);
            case 18 -> sulfur(s);
            case 19 -> copper(s);
            default -> throw new IllegalStateException("Unhandled mineral preset: " + preset);
        }

        s.selectedMineralPreset[0] = preset;
        s.randomizeSeed();
        s.clampRanges();
    }

    private static void resetCommon(GardenSettings s) {
        s.activeCount[0] = 16_000;
        s.distribution[0] = 3;
        s.spacing[0] = 0.46f;
        s.jitter[0] = 0.55f;
        s.sparsity[0] = 0.12f;
        s.clusterCount[0] = 12;
        s.clusterRadius[0] = 3.2f;
        s.spiralTurns[0] = 11.0f;

        s.minRadius[0] = 0.055f;
        s.maxRadius[0] = 0.20f;
        s.minHeight[0] = 0.35f;
        s.maxHeight[0] = 5.0f;
        s.heightPower[0] = 2.2f;
        s.growthMin[0] = 0.08f;
        s.growthMax[0] = 0.72f;
        s.growthMultiplier[0] = 1.0f;
        s.pulseStrength[0] = 0.12f;

        s.formationType[0] = FormationType.PRISM.id();
        s.surfaceStyle[0] = SurfaceStyle.SMOOTH.id();
        s.sides[0] = 6;
        s.taper[0] = 0.82f;
        s.tipRatio[0] = 0.22f;
        s.twistTurns[0] = 0.0f;
        s.tilt[0] = 0.06f;
        s.bend[0] = 0.025f;
        s.motionStrength[0] = 0.0f;
        s.motionSpeed[0] = 0.65f;
        s.shardsPerCluster[0] = 3;
        s.shardSpread[0] = 2.0f;
        s.shardScale[0] = 0.62f;
        s.shardLean[0] = 0.35f;
        s.bladeThickness[0] = 0.20f;
        s.hopperSteps[0] = 3;
        s.hopperInset[0] = 0.22f;
        s.bipyramidWaist[0] = 0.50f;
        s.dendriteBranches[0] = 3;
        s.dendriteAngle[0] = 0.72f;
        s.radialStrength[0] = 0.90f;
        s.formationIrregularity[0] = 0.12f;

        s.mineralScale[0] = 0.23f;
        s.mineralWarp[0] = 0.8f;
        s.mineralContrast[0] = 1.0f;
        s.thirdColorMix[0] = 0.28f;
        s.colorVariation[0] = 0.08f;

        s.ambient[0] = 0.12f;
        s.lightIntensity[0] = 0.92f;
        s.specularStrength[0] = 1.25f;
        s.specularPower[0] = 88.0f;
        s.fresnelStrength[0] = 1.25f;
        s.emission[0] = 0.045f;
        s.bandScale[0] = 13.0f;
        s.bandStrength[0] = 0.05f;
        s.metallic[0] = 0.0f;
        s.roughness[0] = 0.18f;
        s.iridescence[0] = 0.0f;
        s.surfaceScale[0] = 1.0f;
        s.fogDensity[0] = 0.016f;
        s.fogMax[0] = 0.80f;
        s.exposure[0] = 1.05f;
        setColor(s.fogColor, 0.004f, 0.006f, 0.012f);
        setColor(s.lightDirection, -0.36f, 0.82f, 0.28f);
    }

    private static void quartz(GardenSettings s) {
        s.activeCount[0] = 18_000;
        s.distribution[0] = 3;
        s.clusterCount[0] = 11;
        s.clusterRadius[0] = 2.8f;
        s.sides[0] = 6;
        s.shardsPerCluster[0] = 4;
        s.shardSpread[0] = 2.4f;
        s.minRadius[0] = 0.055f;
        s.maxRadius[0] = 0.22f;
        s.maxHeight[0] = 7.0f;
        s.fresnelStrength[0] = 1.75f;
        s.roughness[0] = 0.08f;
        setColors(s, 0.30f, 0.58f, 0.82f, 0.70f, 0.88f, 1.00f, 0.95f, 0.98f, 1.00f);
    }

    private static void amethyst(GardenSettings s) {
        s.activeCount[0] = 17_000;
        s.clusterCount[0] = 9;
        s.clusterRadius[0] = 2.5f;
        s.shardsPerCluster[0] = 5;
        s.shardSpread[0] = 2.8f;
        s.maxHeight[0] = 6.4f;
        s.surfaceStyle[0] = SurfaceStyle.BANDED.id();
        s.bandStrength[0] = 0.16f;
        s.bandScale[0] = 11.0f;
        s.emission[0] = 0.09f;
        s.fresnelStrength[0] = 1.65f;
        setColors(s, 0.18f, 0.04f, 0.36f, 0.62f, 0.12f, 0.92f, 0.94f, 0.45f, 1.00f);
    }

    private static void citrine(GardenSettings s) {
        s.activeCount[0] = 19_000;
        s.maxHeight[0] = 7.8f;
        s.heightPower[0] = 1.7f;
        s.minRadius[0] = 0.045f;
        s.maxRadius[0] = 0.16f;
        s.shardsPerCluster[0] = 3;
        s.shardLean[0] = 0.24f;
        s.roughness[0] = 0.10f;
        setColors(s, 0.55f, 0.22f, 0.015f, 0.96f, 0.58f, 0.06f, 1.00f, 0.88f, 0.38f);
    }

    private static void smokyQuartz(GardenSettings s) {
        s.activeCount[0] = 15_000;
        s.maxHeight[0] = 7.2f;
        s.maxRadius[0] = 0.24f;
        s.surfaceStyle[0] = SurfaceStyle.BANDED.id();
        s.bandStrength[0] = 0.12f;
        s.roughness[0] = 0.16f;
        s.fresnelStrength[0] = 1.35f;
        setColors(s, 0.055f, 0.045f, 0.040f, 0.22f, 0.16f, 0.12f, 0.55f, 0.48f, 0.40f);
    }

    private static void emerald(GardenSettings s) {
        s.activeCount[0] = 12_000;
        s.clusterCount[0] = 14;
        s.clusterRadius[0] = 2.1f;
        s.minRadius[0] = 0.09f;
        s.maxRadius[0] = 0.32f;
        s.minHeight[0] = 0.45f;
        s.maxHeight[0] = 4.8f;
        s.heightPower[0] = 1.5f;
        s.surfaceStyle[0] = SurfaceStyle.STRIATED.id();
        s.surfaceScale[0] = 1.5f;
        s.shardsPerCluster[0] = 3;
        s.shardSpread[0] = 1.8f;
        setColors(s, 0.01f, 0.16f, 0.07f, 0.02f, 0.62f, 0.22f, 0.22f, 1.00f, 0.48f);
    }

    private static void aquamarine(GardenSettings s) {
        s.activeCount[0] = 16_000;
        s.maxHeight[0] = 8.2f;
        s.minRadius[0] = 0.05f;
        s.maxRadius[0] = 0.18f;
        s.tilt[0] = 0.025f;
        s.bend[0] = 0.0f;
        s.shardsPerCluster[0] = 2;
        s.fresnelStrength[0] = 1.9f;
        s.roughness[0] = 0.06f;
        setColors(s, 0.05f, 0.48f, 0.62f, 0.18f, 0.82f, 0.92f, 0.72f, 0.96f, 1.00f);
    }

    private static void tourmaline(GardenSettings s) {
        s.formationType[0] = FormationType.NEEDLE.id();
        s.surfaceStyle[0] = SurfaceStyle.STRIATED.id();
        s.activeCount[0] = 19_000;
        s.maxHeight[0] = 8.8f;
        s.minRadius[0] = 0.035f;
        s.maxRadius[0] = 0.13f;
        s.sides[0] = 6;
        s.shardsPerCluster[0] = 3;
        s.surfaceScale[0] = 2.4f;
        s.bandStrength[0] = 0.10f;
        s.colorVariation[0] = 0.24f;
        setColors(s, 0.012f, 0.018f, 0.016f, 0.02f, 0.42f, 0.18f, 0.95f, 0.18f, 0.50f);
    }

    private static void ruby(GardenSettings s) {
        s.formationType[0] = FormationType.BIPYRAMID.id();
        s.activeCount[0] = 10_000;
        s.minRadius[0] = 0.12f;
        s.maxRadius[0] = 0.38f;
        s.minHeight[0] = 0.45f;
        s.maxHeight[0] = 3.8f;
        s.bipyramidWaist[0] = 0.58f;
        s.shardsPerCluster[0] = 2;
        s.fresnelStrength[0] = 1.6f;
        s.emission[0] = 0.07f;
        setColors(s, 0.22f, 0.005f, 0.02f, 0.78f, 0.015f, 0.08f, 1.00f, 0.22f, 0.34f);
    }

    private static void sapphire(GardenSettings s) {
        s.formationType[0] = FormationType.BIPYRAMID.id();
        s.activeCount[0] = 10_000;
        s.minRadius[0] = 0.12f;
        s.maxRadius[0] = 0.36f;
        s.minHeight[0] = 0.45f;
        s.maxHeight[0] = 3.8f;
        s.bipyramidWaist[0] = 0.56f;
        s.shardsPerCluster[0] = 2;
        s.specularStrength[0] = 1.55f;
        setColors(s, 0.008f, 0.025f, 0.18f, 0.015f, 0.18f, 0.78f, 0.22f, 0.54f, 1.00f);
    }

    private static void fluorite(GardenSettings s) {
        s.formationType[0] = FormationType.CUBE.id();
        s.activeCount[0] = 10_500;
        s.minRadius[0] = 0.16f;
        s.maxRadius[0] = 0.48f;
        s.minHeight[0] = 0.30f;
        s.maxHeight[0] = 1.8f;
        s.shardsPerCluster[0] = 4;
        s.shardSpread[0] = 2.2f;
        s.shardLean[0] = 0.08f;
        s.fresnelStrength[0] = 1.8f;
        s.roughness[0] = 0.09f;
        setColors(s, 0.16f, 0.62f, 0.28f, 0.48f, 0.12f, 0.78f, 0.84f, 0.46f, 1.00f);
    }

    private static void galena(GardenSettings s) {
        s.formationType[0] = FormationType.CUBE.id();
        s.surfaceStyle[0] = SurfaceStyle.METALLIC.id();
        s.activeCount[0] = 8_000;
        s.minRadius[0] = 0.18f;
        s.maxRadius[0] = 0.55f;
        s.minHeight[0] = 0.25f;
        s.maxHeight[0] = 1.6f;
        s.shardsPerCluster[0] = 3;
        s.metallic[0] = 0.95f;
        s.roughness[0] = 0.20f;
        s.specularStrength[0] = 2.25f;
        s.fresnelStrength[0] = 0.65f;
        setColors(s, 0.10f, 0.11f, 0.13f, 0.26f, 0.28f, 0.32f, 0.62f, 0.66f, 0.72f);
    }

    private static void pyrite(GardenSettings s) {
        s.formationType[0] = FormationType.CUBE.id();
        s.surfaceStyle[0] = SurfaceStyle.METALLIC.id();
        s.activeCount[0] = 9_000;
        s.minRadius[0] = 0.14f;
        s.maxRadius[0] = 0.48f;
        s.minHeight[0] = 0.22f;
        s.maxHeight[0] = 1.5f;
        s.shardsPerCluster[0] = 4;
        s.metallic[0] = 1.0f;
        s.roughness[0] = 0.14f;
        s.specularStrength[0] = 2.35f;
        s.surfaceScale[0] = 1.4f;
        setColors(s, 0.28f, 0.16f, 0.02f, 0.72f, 0.48f, 0.08f, 1.00f, 0.82f, 0.28f);
    }

    private static void bismuth(GardenSettings s) {
        s.formationType[0] = FormationType.HOPPER.id();
        s.surfaceStyle[0] = SurfaceStyle.IRIDESCENT.id();
        s.activeCount[0] = 6_500;
        s.minRadius[0] = 0.16f;
        s.maxRadius[0] = 0.46f;
        s.minHeight[0] = 0.5f;
        s.maxHeight[0] = 3.6f;
        s.shardsPerCluster[0] = 2;
        s.hopperSteps[0] = 4;
        s.hopperInset[0] = 0.18f;
        s.metallic[0] = 0.82f;
        s.roughness[0] = 0.16f;
        s.iridescence[0] = 1.0f;
        s.surfaceScale[0] = 1.7f;
        s.specularStrength[0] = 2.0f;
        setColors(s, 0.24f, 0.10f, 0.42f, 0.05f, 0.64f, 0.82f, 0.94f, 0.46f, 0.08f);
    }

    private static void selenite(GardenSettings s) {
        s.formationType[0] = FormationType.BLADE.id();
        s.surfaceStyle[0] = SurfaceStyle.BANDED.id();
        s.activeCount[0] = 12_000;
        s.minRadius[0] = 0.07f;
        s.maxRadius[0] = 0.24f;
        s.minHeight[0] = 0.6f;
        s.maxHeight[0] = 6.8f;
        s.bladeThickness[0] = 0.16f;
        s.shardsPerCluster[0] = 4;
        s.shardSpread[0] = 2.8f;
        s.shardLean[0] = 0.42f;
        s.fresnelStrength[0] = 1.95f;
        s.bandStrength[0] = 0.08f;
        s.roughness[0] = 0.06f;
        setColors(s, 0.48f, 0.58f, 0.66f, 0.82f, 0.88f, 0.92f, 1.00f, 1.00f, 0.98f);
    }

    private static void kyanite(GardenSettings s) {
        s.formationType[0] = FormationType.FAN.id();
        s.surfaceStyle[0] = SurfaceStyle.STRIATED.id();
        s.activeCount[0] = 8_500;
        s.minRadius[0] = 0.08f;
        s.maxRadius[0] = 0.26f;
        s.minHeight[0] = 0.7f;
        s.maxHeight[0] = 5.8f;
        s.bladeThickness[0] = 0.12f;
        s.shardsPerCluster[0] = 7;
        s.shardSpread[0] = 2.4f;
        s.shardScale[0] = 0.82f;
        s.shardLean[0] = 0.78f;
        s.radialStrength[0] = 0.95f;
        s.surfaceScale[0] = 2.2f;
        setColors(s, 0.025f, 0.10f, 0.32f, 0.10f, 0.36f, 0.78f, 0.42f, 0.68f, 1.00f);
    }

    private static void stibnite(GardenSettings s) {
        s.formationType[0] = FormationType.NEEDLE.id();
        s.surfaceStyle[0] = SurfaceStyle.METALLIC.id();
        s.activeCount[0] = 13_000;
        s.minRadius[0] = 0.018f;
        s.maxRadius[0] = 0.075f;
        s.minHeight[0] = 1.4f;
        s.maxHeight[0] = 10.5f;
        s.heightPower[0] = 1.4f;
        s.shardsPerCluster[0] = 6;
        s.shardSpread[0] = 1.7f;
        s.shardLean[0] = 0.50f;
        s.metallic[0] = 0.94f;
        s.roughness[0] = 0.18f;
        s.specularStrength[0] = 2.0f;
        setColors(s, 0.035f, 0.045f, 0.055f, 0.22f, 0.25f, 0.30f, 0.72f, 0.76f, 0.82f);
    }

    private static void aragonite(GardenSettings s) {
        s.formationType[0] = FormationType.STARBURST.id();
        s.surfaceStyle[0] = SurfaceStyle.BANDED.id();
        s.activeCount[0] = 7_500;
        s.minRadius[0] = 0.055f;
        s.maxRadius[0] = 0.16f;
        s.minHeight[0] = 0.7f;
        s.maxHeight[0] = 5.0f;
        s.shardsPerCluster[0] = 8;
        s.shardSpread[0] = 1.3f;
        s.shardScale[0] = 0.90f;
        s.shardLean[0] = 1.05f;
        s.radialStrength[0] = 1.30f;
        s.bandStrength[0] = 0.08f;
        setColors(s, 0.38f, 0.22f, 0.10f, 0.82f, 0.61f, 0.34f, 0.96f, 0.90f, 0.68f);
    }

    private static void calcite(GardenSettings s) {
        s.formationType[0] = FormationType.SCALENOHEDRON.id();
        s.activeCount[0] = 10_000;
        s.minRadius[0] = 0.10f;
        s.maxRadius[0] = 0.34f;
        s.minHeight[0] = 0.6f;
        s.maxHeight[0] = 5.5f;
        s.bipyramidWaist[0] = 0.43f;
        s.formationIrregularity[0] = 0.24f;
        s.shardsPerCluster[0] = 4;
        s.shardSpread[0] = 2.3f;
        s.fresnelStrength[0] = 1.45f;
        setColors(s, 0.52f, 0.44f, 0.25f, 0.92f, 0.76f, 0.42f, 1.00f, 0.96f, 0.78f);
    }

    private static void sulfur(GardenSettings s) {
        s.formationType[0] = FormationType.BIPYRAMID.id();
        s.activeCount[0] = 7_000;
        s.sparsity[0] = 0.24f;
        s.minRadius[0] = 0.11f;
        s.maxRadius[0] = 0.34f;
        s.minHeight[0] = 0.45f;
        s.maxHeight[0] = 3.6f;
        s.bipyramidWaist[0] = 0.46f;
        s.shardsPerCluster[0] = 3;
        s.emission[0] = 0.055f;
        s.roughness[0] = 0.22f;
        setColors(s, 0.70f, 0.44f, 0.00f, 1.00f, 0.82f, 0.02f, 1.00f, 0.98f, 0.24f);
    }

    private static void copper(GardenSettings s) {
        s.formationType[0] = FormationType.DENDRITE.id();
        s.surfaceStyle[0] = SurfaceStyle.METALLIC.id();
        s.activeCount[0] = 6_000;
        s.distribution[0] = 3;
        s.clusterCount[0] = 16;
        s.clusterRadius[0] = 2.6f;
        s.minRadius[0] = 0.045f;
        s.maxRadius[0] = 0.15f;
        s.minHeight[0] = 0.8f;
        s.maxHeight[0] = 5.8f;
        s.shardsPerCluster[0] = 2;
        s.dendriteBranches[0] = 3;
        s.dendriteAngle[0] = 0.78f;
        s.formationIrregularity[0] = 0.28f;
        s.metallic[0] = 0.92f;
        s.roughness[0] = 0.28f;
        s.specularStrength[0] = 1.75f;
        s.colorVariation[0] = 0.22f;
        setColors(s, 0.20f, 0.055f, 0.018f, 0.68f, 0.22f, 0.055f, 0.02f, 0.42f, 0.34f);
    }

    private static void setColors(
            GardenSettings s,
            float ar, float ag, float ab,
            float br, float bg, float bb,
            float cr, float cg, float cb
    ) {
        setColor(s.paletteA, ar, ag, ab);
        setColor(s.paletteB, br, bg, bb);
        setColor(s.paletteC, cr, cg, cb);
    }

    private static void setColor(float[] color, float r, float g, float b) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
    }
}
