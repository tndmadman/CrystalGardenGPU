package com.crystalgarden;

import java.util.concurrent.ThreadLocalRandom;

public final class GardenSettings {
    public static final int MAX_CRYSTALS = 65_536;
    public static final int MAX_SHARDS_PER_CLUSTER = 8;
    public static final String[] DISTRIBUTIONS = {"Grid", "Radial", "Spiral", "Clusters"};
    public static final String[] FORMATIONS = FormationType.labels();
    public static final String[] SURFACE_STYLES = SurfaceStyle.labels();

    // Generation / lifecycle
    public final int[] seed = {1337};
    public final int[] selectedMineralPreset = {0};
    public final boolean[] liveRegenerate = {true};
    public final boolean[] autoRegenerate = {false};
    public final float[] autoRegenerateSeconds = {10.0f};
    public final boolean[] pauseGrowth = {false};

    // Population / placement
    public final int[] activeCount = {16_384};
    public final int[] distribution = {0};
    public final float[] spacing = {0.46f};
    public final float[] jitter = {0.72f};
    public final float[] sparsity = {0.13f};
    public final int[] clusterCount = {14};
    public final float[] clusterRadius = {3.8f};
    public final float[] spiralTurns = {11.0f};

    // Crystal dimensions / growth
    public final float[] minRadius = {0.055f};
    public final float[] maxRadius = {0.19f};
    public final float[] minHeight = {0.35f};
    public final float[] maxHeight = {4.8f};
    public final float[] heightPower = {2.6f};
    public final float[] growthMin = {0.08f};
    public final float[] growthMax = {0.72f};
    public final float[] growthMultiplier = {1.0f};
    public final float[] pulseStrength = {0.18f};

    // Geometry / motion / formation
    public final int[] formationType = {FormationType.PRISM.id()};
    public final int[] surfaceStyle = {SurfaceStyle.SMOOTH.id()};
    public final int[] sides = {6};
    public final float[] taper = {0.82f};
    public final float[] tipRatio = {0.18f};
    public final float[] twistTurns = {0.0f};
    public final float[] tilt = {0.0f};
    public final float[] bend = {0.0f};
    public final float[] motionStrength = {0.0f};
    public final float[] motionSpeed = {0.65f};
    public final int[] shardsPerCluster = {1};
    public final float[] shardSpread = {2.2f};
    public final float[] shardScale = {0.62f};
    public final float[] shardLean = {0.42f};

    // Formation-specific controls
    public final float[] bladeThickness = {0.20f};
    public final int[] hopperSteps = {3};
    public final float[] hopperInset = {0.22f};
    public final float[] bipyramidWaist = {0.50f};
    public final int[] dendriteBranches = {3};
    public final float[] dendriteAngle = {0.72f};
    public final float[] radialStrength = {0.90f};
    public final float[] formationIrregularity = {0.12f};

    // Mineral field / palette
    public final float[] mineralScale = {0.23f};
    public final float[] mineralWarp = {1.0f};
    public final float[] mineralContrast = {1.0f};
    public final float[] paletteA = {0.04f, 0.52f, 0.88f};
    public final float[] paletteB = {0.66f, 0.12f, 0.96f};
    public final float[] paletteC = {0.04f, 0.86f, 0.58f};
    public final float[] thirdColorMix = {0.38f};
    public final float[] colorVariation = {0.12f};

    // Lighting / material / atmosphere
    public final float[] ambient = {0.12f};
    public final float[] lightIntensity = {0.88f};
    public final float[] specularStrength = {1.15f};
    public final float[] specularPower = {72.0f};
    public final float[] fresnelStrength = {1.25f};
    public final float[] emission = {0.06f};
    public final float[] bandScale = {13.0f};
    public final float[] bandStrength = {0.08f};
    public final float[] metallic = {0.0f};
    public final float[] roughness = {0.18f};
    public final float[] iridescence = {0.0f};
    public final float[] surfaceScale = {1.0f};
    public final float[] fogDensity = {0.018f};
    public final float[] fogMax = {0.82f};
    public final float[] exposure = {1.0f};
    public final float[] fogColor = {0.004f, 0.006f, 0.012f};
    public final float[] lightDirection = {-0.36f, 0.82f, 0.28f};

    // Camera
    public final float[] fov = {65.0f};
    public final float[] cameraSpeed = {7.0f};
    public final float[] cameraBoost = {18.0f};
    public final float[] mouseSensitivity = {0.0022f};

    public void randomizeSeed() {
        seed[0] = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }

    public void applyMineralPreset(int preset) {
        MineralPresetLibrary.apply(this, preset);
    }

    public void randomizeAll() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        randomizeSeed();

        activeCount[0] = r.nextInt(5_000, 36_001);
        distribution[0] = r.nextInt(DISTRIBUTIONS.length);
        spacing[0] = r.nextFloat(0.25f, 0.75f);
        jitter[0] = r.nextFloat(0.10f, 1.15f);
        sparsity[0] = r.nextFloat(0.02f, 0.36f);
        clusterCount[0] = r.nextInt(5, 25);
        clusterRadius[0] = r.nextFloat(1.5f, 6.5f);
        spiralTurns[0] = r.nextFloat(3.0f, 22.0f);

        minRadius[0] = r.nextFloat(0.025f, 0.12f);
        maxRadius[0] = r.nextFloat(Math.max(minRadius[0] + 0.04f, 0.13f), 0.48f);
        minHeight[0] = r.nextFloat(0.08f, 0.9f);
        maxHeight[0] = r.nextFloat(Math.max(minHeight[0] + 1.0f, 2.0f), 10.0f);
        heightPower[0] = r.nextFloat(0.7f, 4.5f);
        growthMin[0] = r.nextFloat(0.03f, 0.3f);
        growthMax[0] = r.nextFloat(Math.max(growthMin[0] + 0.2f, 0.35f), 1.8f);
        growthMultiplier[0] = r.nextFloat(0.4f, 2.5f);
        pulseStrength[0] = r.nextFloat(0.0f, 0.55f);

        formationType[0] = r.nextInt(FormationType.values().length);
        surfaceStyle[0] = r.nextInt(SurfaceStyle.values().length);
        sides[0] = r.nextInt(3, 13);
        taper[0] = r.nextFloat(0.35f, 1.05f);
        tipRatio[0] = r.nextFloat(0.07f, 0.52f);
        twistTurns[0] = r.nextFloat(-1.5f, 1.5f);
        tilt[0] = r.nextFloat(0.0f, 0.8f);
        bend[0] = r.nextFloat(0.0f, 0.55f);
        motionStrength[0] = r.nextFloat(0.0f, 0.18f);
        motionSpeed[0] = r.nextFloat(0.15f, 2.2f);
        shardsPerCluster[0] = r.nextInt(1, MAX_SHARDS_PER_CLUSTER + 1);
        shardSpread[0] = r.nextFloat(0.8f, 4.8f);
        shardScale[0] = r.nextFloat(0.28f, 0.92f);
        shardLean[0] = r.nextFloat(0.05f, 1.15f);
        bladeThickness[0] = r.nextFloat(0.08f, 0.55f);
        hopperSteps[0] = r.nextInt(2, 5);
        hopperInset[0] = r.nextFloat(0.10f, 0.34f);
        bipyramidWaist[0] = r.nextFloat(0.30f, 0.70f);
        dendriteBranches[0] = r.nextInt(1, 4);
        dendriteAngle[0] = r.nextFloat(0.25f, 1.15f);
        radialStrength[0] = r.nextFloat(0.25f, 1.5f);
        formationIrregularity[0] = r.nextFloat(0.0f, 0.45f);

        mineralScale[0] = r.nextFloat(0.05f, 0.65f);
        mineralWarp[0] = r.nextFloat(0.0f, 3.5f);
        mineralContrast[0] = r.nextFloat(0.2f, 2.2f);
        thirdColorMix[0] = r.nextFloat(0.0f, 0.75f);
        colorVariation[0] = r.nextFloat(0.0f, 0.35f);
        randomColor(paletteA, r);
        randomColor(paletteB, r);
        randomColor(paletteC, r);

        metallic[0] = r.nextFloat(0.0f, 1.0f);
        roughness[0] = r.nextFloat(0.03f, 0.65f);
        iridescence[0] = r.nextFloat(0.0f, 0.85f);
        surfaceScale[0] = r.nextFloat(0.4f, 3.0f);
        fresnelStrength[0] = r.nextFloat(0.3f, 2.5f);
        emission[0] = r.nextFloat(0.0f, 0.35f);
        bandScale[0] = r.nextFloat(3.0f, 30.0f);
        bandStrength[0] = r.nextFloat(0.0f, 0.25f);
        fogDensity[0] = r.nextFloat(0.003f, 0.04f);
        exposure[0] = r.nextFloat(0.7f, 1.7f);
        clampRanges();
    }

    public void clampRanges() {
        activeCount[0] = Math.max(256, Math.min(MAX_CRYSTALS, activeCount[0]));
        distribution[0] = Math.max(0, Math.min(DISTRIBUTIONS.length - 1, distribution[0]));
        formationType[0] = Math.max(0, Math.min(FormationType.values().length - 1, formationType[0]));
        surfaceStyle[0] = Math.max(0, Math.min(SurfaceStyle.values().length - 1, surfaceStyle[0]));
        selectedMineralPreset[0] = Math.max(0, Math.min(MineralPresetLibrary.NAMES.length - 1, selectedMineralPreset[0]));
        sides[0] = Math.max(3, Math.min(12, sides[0]));
        clusterCount[0] = Math.max(1, Math.min(64, clusterCount[0]));
        shardsPerCluster[0] = Math.max(1, Math.min(MAX_SHARDS_PER_CLUSTER, shardsPerCluster[0]));
        hopperSteps[0] = Math.max(1, Math.min(4, hopperSteps[0]));
        dendriteBranches[0] = Math.max(1, Math.min(3, dendriteBranches[0]));
        bladeThickness[0] = Math.max(0.03f, Math.min(1.0f, bladeThickness[0]));
        hopperInset[0] = Math.max(0.02f, Math.min(0.45f, hopperInset[0]));
        bipyramidWaist[0] = Math.max(0.15f, Math.min(0.85f, bipyramidWaist[0]));
        metallic[0] = Math.max(0.0f, Math.min(1.0f, metallic[0]));
        roughness[0] = Math.max(0.0f, Math.min(1.0f, roughness[0]));
        iridescence[0] = Math.max(0.0f, Math.min(1.0f, iridescence[0]));
        maxRadius[0] = Math.max(minRadius[0], maxRadius[0]);
        maxHeight[0] = Math.max(minHeight[0], maxHeight[0]);
        growthMax[0] = Math.max(growthMin[0], growthMax[0]);
    }

    private static void randomColor(float[] color, ThreadLocalRandom r) {
        color[0] = r.nextFloat(0.0f, 1.0f);
        color[1] = r.nextFloat(0.0f, 1.0f);
        color[2] = r.nextFloat(0.0f, 1.0f);
    }
}
