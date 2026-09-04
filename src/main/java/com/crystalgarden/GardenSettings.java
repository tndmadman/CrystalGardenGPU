package com.crystalgarden;

import java.util.concurrent.ThreadLocalRandom;

public final class GardenSettings {
    public static final int MAX_CRYSTALS = 65_536;
    public static final int MAX_SHARDS_PER_CLUSTER = 5;
    public static final String[] DISTRIBUTIONS = {"Grid", "Radial", "Spiral", "Clusters"};

    // Generation / lifecycle
    public final int[] seed = {1337};
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

    // Geometry / motion
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

    public void randomizeAll() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        randomizeSeed();
        activeCount[0] = r.nextInt(8_000, 48_001);
        distribution[0] = r.nextInt(DISTRIBUTIONS.length);
        spacing[0] = r.nextFloat(0.25f, 0.75f);
        jitter[0] = r.nextFloat(0.15f, 1.0f);
        sparsity[0] = r.nextFloat(0.02f, 0.36f);
        clusterCount[0] = r.nextInt(5, 25);
        clusterRadius[0] = r.nextFloat(1.5f, 6.5f);
        spiralTurns[0] = r.nextFloat(3.0f, 22.0f);

        minRadius[0] = r.nextFloat(0.025f, 0.10f);
        maxRadius[0] = r.nextFloat(Math.max(minRadius[0] + 0.04f, 0.11f), 0.36f);
        minHeight[0] = r.nextFloat(0.08f, 0.8f);
        maxHeight[0] = r.nextFloat(Math.max(minHeight[0] + 1.0f, 2.0f), 10.0f);
        heightPower[0] = r.nextFloat(0.7f, 4.5f);
        growthMin[0] = r.nextFloat(0.03f, 0.3f);
        growthMax[0] = r.nextFloat(Math.max(growthMin[0] + 0.2f, 0.35f), 1.8f);
        growthMultiplier[0] = r.nextFloat(0.4f, 2.5f);
        pulseStrength[0] = r.nextFloat(0.0f, 0.55f);

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
        shardScale[0] = r.nextFloat(0.28f, 0.88f);
        shardLean[0] = r.nextFloat(0.05f, 0.9f);

        mineralScale[0] = r.nextFloat(0.05f, 0.65f);
        mineralWarp[0] = r.nextFloat(0.0f, 3.5f);
        mineralContrast[0] = r.nextFloat(0.2f, 2.2f);
        thirdColorMix[0] = r.nextFloat(0.0f, 0.75f);
        colorVariation[0] = r.nextFloat(0.0f, 0.35f);
        randomColor(paletteA, r);
        randomColor(paletteB, r);
        randomColor(paletteC, r);

        fresnelStrength[0] = r.nextFloat(0.3f, 2.5f);
        emission[0] = r.nextFloat(0.0f, 0.35f);
        bandScale[0] = r.nextFloat(3.0f, 30.0f);
        bandStrength[0] = r.nextFloat(0.0f, 0.25f);
        fogDensity[0] = r.nextFloat(0.003f, 0.04f);
        exposure[0] = r.nextFloat(0.7f, 1.7f);
    }

    public void applyPreset(int preset) {
        switch (preset) {
            case 0 -> { // Needle Forest
                distribution[0] = 0;
                activeCount[0] = 30_000;
                sparsity[0] = 0.08f;
                minRadius[0] = 0.025f;
                maxRadius[0] = 0.09f;
                minHeight[0] = 1.2f;
                maxHeight[0] = 8.5f;
                heightPower[0] = 1.8f;
                sides[0] = 5;
                taper[0] = 0.62f;
                tipRatio[0] = 0.27f;
                tilt[0] = 0.08f;
                bend[0] = 0.03f;
                shardsPerCluster[0] = 2;
                shardSpread[0] = 1.4f;
                shardScale[0] = 0.42f;
                shardLean[0] = 0.16f;
                setColor(paletteA, 0.02f, 0.38f, 0.90f);
                setColor(paletteB, 0.12f, 0.78f, 1.00f);
                setColor(paletteC, 0.68f, 0.12f, 0.95f);
            }
            case 1 -> { // Amethyst Cathedral
                distribution[0] = 3;
                activeCount[0] = 18_000;
                clusterCount[0] = 10;
                clusterRadius[0] = 3.2f;
                sparsity[0] = 0.18f;
                minRadius[0] = 0.07f;
                maxRadius[0] = 0.28f;
                minHeight[0] = 0.5f;
                maxHeight[0] = 7.2f;
                sides[0] = 6;
                taper[0] = 0.78f;
                tipRatio[0] = 0.24f;
                twistTurns[0] = 0.05f;
                shardsPerCluster[0] = 4;
                shardSpread[0] = 2.6f;
                shardScale[0] = 0.68f;
                shardLean[0] = 0.52f;
                setColor(paletteA, 0.18f, 0.05f, 0.40f);
                setColor(paletteB, 0.72f, 0.18f, 1.00f);
                setColor(paletteC, 0.96f, 0.52f, 1.00f);
                emission[0] = 0.10f;
                fresnelStrength[0] = 1.65f;
            }
            case 2 -> { // Alien Reef
                distribution[0] = 1;
                activeCount[0] = 22_000;
                sparsity[0] = 0.24f;
                minHeight[0] = 0.25f;
                maxHeight[0] = 5.5f;
                sides[0] = 7;
                twistTurns[0] = 0.55f;
                tilt[0] = 0.45f;
                bend[0] = 0.42f;
                motionStrength[0] = 0.10f;
                motionSpeed[0] = 0.7f;
                shardsPerCluster[0] = 5;
                shardSpread[0] = 3.5f;
                shardScale[0] = 0.72f;
                shardLean[0] = 0.85f;
                mineralWarp[0] = 2.4f;
                setColor(paletteA, 0.00f, 0.95f, 0.52f);
                setColor(paletteB, 0.08f, 0.38f, 1.00f);
                setColor(paletteC, 1.00f, 0.12f, 0.64f);
                emission[0] = 0.18f;
            }
            case 3 -> { // Crystal Storm
                distribution[0] = 2;
                activeCount[0] = 28_000;
                spiralTurns[0] = 18.0f;
                sparsity[0] = 0.08f;
                minRadius[0] = 0.035f;
                maxRadius[0] = 0.15f;
                minHeight[0] = 0.35f;
                maxHeight[0] = 6.8f;
                sides[0] = 4;
                twistTurns[0] = 1.15f;
                tilt[0] = 0.72f;
                bend[0] = 0.30f;
                motionStrength[0] = 0.16f;
                motionSpeed[0] = 1.4f;
                shardsPerCluster[0] = 3;
                shardSpread[0] = 2.0f;
                shardScale[0] = 0.50f;
                shardLean[0] = 0.70f;
                setColor(paletteA, 0.02f, 0.55f, 1.00f);
                setColor(paletteB, 0.82f, 0.08f, 1.00f);
                setColor(paletteC, 0.08f, 1.00f, 0.82f);
            }
            case 4 -> { // Obsidian Spires
                distribution[0] = 3;
                activeCount[0] = 12_000;
                clusterCount[0] = 8;
                clusterRadius[0] = 4.5f;
                minRadius[0] = 0.09f;
                maxRadius[0] = 0.34f;
                minHeight[0] = 0.8f;
                maxHeight[0] = 9.0f;
                sides[0] = 8;
                taper[0] = 0.72f;
                tipRatio[0] = 0.35f;
                shardsPerCluster[0] = 3;
                shardSpread[0] = 2.8f;
                shardScale[0] = 0.58f;
                shardLean[0] = 0.32f;
                setColor(paletteA, 0.008f, 0.012f, 0.025f);
                setColor(paletteB, 0.05f, 0.07f, 0.13f);
                setColor(paletteC, 0.30f, 0.04f, 0.46f);
                ambient[0] = 0.06f;
                fresnelStrength[0] = 2.1f;
                specularStrength[0] = 1.8f;
            }
            default -> throw new IllegalArgumentException("Unknown preset: " + preset);
        }
        randomizeSeed();
    }

    public void clampRanges() {
        activeCount[0] = Math.max(256, Math.min(MAX_CRYSTALS, activeCount[0]));
        sides[0] = Math.max(3, Math.min(12, sides[0]));
        clusterCount[0] = Math.max(1, Math.min(64, clusterCount[0]));
        shardsPerCluster[0] = Math.max(1, Math.min(MAX_SHARDS_PER_CLUSTER, shardsPerCluster[0]));
        maxRadius[0] = Math.max(minRadius[0], maxRadius[0]);
        maxHeight[0] = Math.max(minHeight[0], maxHeight[0]);
        growthMax[0] = Math.max(growthMin[0], growthMax[0]);
    }

    private static void randomColor(float[] color, ThreadLocalRandom r) {
        color[0] = r.nextFloat(0.0f, 1.0f);
        color[1] = r.nextFloat(0.0f, 1.0f);
        color[2] = r.nextFloat(0.0f, 1.0f);
    }

    private static void setColor(float[] color, float r, float g, float b) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
    }
}
