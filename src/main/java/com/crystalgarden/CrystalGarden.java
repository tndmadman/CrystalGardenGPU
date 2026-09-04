package com.crystalgarden;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46.*;

public final class CrystalGarden implements AutoCloseable {
    public static final int CRYSTAL_COUNT = 16_384;
    private static final int CRYSTAL_STRIDE_BYTES = 48;
    private static final int VERTICES_PER_CRYSTAL = 54;

    private final ShaderProgram computeShader;
    private final ShaderProgram renderShader;
    private final int ssbo;
    private final int vao;

    public CrystalGarden() {
        computeShader = ShaderProgram.compute("/shaders/crystal.comp");
        renderShader = ShaderProgram.graphics("/shaders/crystal.vert", "/shaders/crystal.frag");

        ssbo = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, ssbo);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) CRYSTAL_COUNT * CRYSTAL_STRIDE_BYTES, GL_DYNAMIC_COPY);
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

    public void update(float deltaSeconds, float timeSeconds) {
        computeShader.use();
        computeShader.setFloat("uDelta", deltaSeconds);
        computeShader.setFloat("uTime", timeSeconds);
        computeShader.setUnsignedInt("uCount", CRYSTAL_COUNT);

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);
        int groups = (CRYSTAL_COUNT + 255) / 256;
        glDispatchCompute(groups, 1, 1);
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT | GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT);
    }

    public void render(Camera camera, float aspectRatio) {
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(65.0),
                aspectRatio,
                0.05f,
                250.0f
        );
        Matrix4f viewProjection = projection.mul(camera.viewMatrix(), new Matrix4f());

        renderShader.use();
        renderShader.setMatrix4("uViewProjection", viewProjection);
        renderShader.setVector3("uCameraPos", camera.position());

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, ssbo);
        glBindVertexArray(vao);
        glDrawArraysInstanced(GL_TRIANGLES, 0, VERTICES_PER_CRYSTAL, CRYSTAL_COUNT);
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
