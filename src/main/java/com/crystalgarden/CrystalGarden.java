package com.crystalgarden;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46.*;

public final class CrystalGarden implements AutoCloseable {
    private static final int CRYSTAL_STRIDE_BYTES = 48;
    private static final int VERTICES_PER_SHARD = 144;

    private final ShaderProgram computeShader;
    private final ShaderProgram renderShader;
    private final int ssbo;
    private final int vao;

    public CrystalGarden() {
        computeShader = ShaderProgram.compute("/shaders/crystal.comp");
        renderShader = ShaderProgram.graphics("/shaders/crystal.vert", "/shaders/crystal.frag");

        ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(
                GL_SHADER_STORAGE_BUFFER,
                (long) GardenSettings.MAX_CRYSTALS * CRYSTAL_STRIDE_BYTES,
                GL_DYNAMIC_COPY
        );
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);

        vao = glGenVertexArrays();
        reset();
    }

    public void reset() {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer zero = stack.ints(0);
            glClearBufferData(
                    GL_SHADER_STORAGE_BUFFER,
                    GL_R32UI,
                    GL_RED_INTEGER,
                    GL_UNSIGNED_INT,
                    zero
            );
        }
        glMemoryBarrier(GL_BUFFER_UPDATE_BARRIER_BIT | GL_SHADER_STORAGE_BARRIER_BIT);
    }

    public void update(GardenSettings s, float deltaSeconds, float timeSeconds) {
        computeShader.use();
        computeShader.setFloat("uDelta", s.pauseGrowth[0] ? 0.0f : deltaSeconds);
        computeShader.setFloat("uTime", timeSeconds);
        computeShader.setUnsignedInt("uCount", s.activeCount[0]);
        computeShader.setUnsignedInt("uSeed", s.seed[0]);
        computeShader.setInt("uDistribution", s.distribution[0]);
        computeShader.setFloat("uSpacing", s.spacing[0]);
        computeShader.setFloat("uJitter", s.jitter[0]);
        computeShader.setFloat("uSparsity", s.sparsity[0]);
        computeShader.setInt("uClusterCount", s.clusterCount[0]);
        computeShader.setFloat("uClusterRadius", s.clusterRadius[0]);
        computeShader.setFloat("uSpiralTurns", s.spiralTurns[0]);

        computeShader.setFloat("uMinRadius", s.minRadius[0]);
        computeShader.setFloat("uMaxRadius", s.maxRadius[0]);
        computeShader.setFloat("uMinHeight", s.minHeight[0]);
        computeShader.setFloat("uMaxHeight", s.maxHeight[0]);
        computeShader.setFloat("uHeightPower", s.heightPower[0]);
        computeShader.setFloat("uGrowthMin", s.growthMin[0]);
        computeShader.setFloat("uGrowthMax", s.growthMax[0]);
        computeShader.setFloat("uGrowthMultiplier", s.growthMultiplier[0]);
        computeShader.setFloat("uPulseStrength", s.pulseStrength[0]);

        computeShader.setFloat("uMineralScale", s.mineralScale[0]);
        computeShader.setFloat("uMineralWarp", s.mineralWarp[0]);
        computeShader.setFloat("uMineralContrast", s.mineralContrast[0]);
        computeShader.setVector3("uPaletteA", s.paletteA);
        computeShader.setVector3("uPaletteB", s.paletteB);
        computeShader.setVector3("uPaletteC", s.paletteC);
        computeShader.setFloat("uThirdColorMix", s.thirdColorMix[0]);
        computeShader.setFloat("uColorVariation", s.colorVariation[0]);

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);
        int groups = (s.activeCount[0] + 255) / 256;
        glDispatchCompute(groups, 1, 1);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
    }

    public void render(GardenSettings s, Camera camera, float aspectRatio, float timeSeconds) {
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(s.fov[0]),
                aspectRatio,
                0.05f,
                400.0f
        );
        Matrix4f viewProjection = projection.mul(camera.viewMatrix(), new Matrix4f());

        renderShader.use();
        renderShader.setMatrix4("uViewProjection", viewProjection);
        renderShader.setVector3("uCameraPos", camera.position());
        renderShader.setFloat("uTime", timeSeconds);
        renderShader.setUnsignedInt("uSeed", s.seed[0]);

        renderShader.setInt("uFormationType", s.formationType[0]);
        renderShader.setInt("uSurfaceStyle", s.surfaceStyle[0]);
        renderShader.setInt("uSides", s.sides[0]);
        renderShader.setFloat("uTaper", s.taper[0]);
        renderShader.setFloat("uTipRatio", s.tipRatio[0]);
        renderShader.setFloat("uTwistTurns", s.twistTurns[0]);
        renderShader.setFloat("uTilt", s.tilt[0]);
        renderShader.setFloat("uBend", s.bend[0]);
        renderShader.setFloat("uMotionStrength", s.motionStrength[0]);
        renderShader.setFloat("uMotionSpeed", s.motionSpeed[0]);
        renderShader.setInt("uShardCount", s.shardsPerCluster[0]);
        renderShader.setFloat("uShardSpread", s.shardSpread[0]);
        renderShader.setFloat("uShardScale", s.shardScale[0]);
        renderShader.setFloat("uShardLean", s.shardLean[0]);
        renderShader.setFloat("uBladeThickness", s.bladeThickness[0]);
        renderShader.setInt("uHopperSteps", s.hopperSteps[0]);
        renderShader.setFloat("uHopperInset", s.hopperInset[0]);
        renderShader.setFloat("uBipyramidWaist", s.bipyramidWaist[0]);
        renderShader.setInt("uDendriteBranches", s.dendriteBranches[0]);
        renderShader.setFloat("uDendriteAngle", s.dendriteAngle[0]);
        renderShader.setFloat("uRadialStrength", s.radialStrength[0]);
        renderShader.setFloat("uFormationIrregularity", s.formationIrregularity[0]);

        renderShader.setFloat("uAmbient", s.ambient[0]);
        renderShader.setFloat("uLightIntensity", s.lightIntensity[0]);
        renderShader.setFloat("uSpecularStrength", s.specularStrength[0]);
        renderShader.setFloat("uSpecularPower", s.specularPower[0]);
        renderShader.setFloat("uFresnelStrength", s.fresnelStrength[0]);
        renderShader.setFloat("uEmission", s.emission[0]);
        renderShader.setFloat("uBandScale", s.bandScale[0]);
        renderShader.setFloat("uBandStrength", s.bandStrength[0]);
        renderShader.setFloat("uMetallic", s.metallic[0]);
        renderShader.setFloat("uRoughness", s.roughness[0]);
        renderShader.setFloat("uIridescence", s.iridescence[0]);
        renderShader.setFloat("uSurfaceScale", s.surfaceScale[0]);
        renderShader.setFloat("uFogDensity", s.fogDensity[0]);
        renderShader.setFloat("uFogMax", s.fogMax[0]);
        renderShader.setFloat("uExposure", s.exposure[0]);
        renderShader.setVector3("uFogColor", s.fogColor);
        renderShader.setVector3("uLightDirection", s.lightDirection);

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);
        glBindVertexArray(vao);
        int verticesPerCluster = VERTICES_PER_SHARD * s.shardsPerCluster[0];
        glDrawArraysInstanced(GL_TRIANGLES, 0, verticesPerCluster, s.activeCount[0]);
        glBindVertexArray(0);
    }

    @Override
    public void close() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(ssbo);
        renderShader.close();
        computeShader.close();
    }
}
