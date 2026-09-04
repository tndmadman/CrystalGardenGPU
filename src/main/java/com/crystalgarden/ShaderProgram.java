package com.crystalgarden;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL46.*;

public final class ShaderProgram implements AutoCloseable {
    private final int programId;

    private ShaderProgram(int programId) {
        this.programId = programId;
    }

    public static ShaderProgram graphics(String vertexResource, String fragmentResource) {
        int vertex = compile(GL_VERTEX_SHADER, load(vertexResource), vertexResource);
        int fragment = compile(GL_FRAGMENT_SHADER, load(fragmentResource), fragmentResource);
        int program = link(vertex, fragment);
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        return new ShaderProgram(program);
    }

    public static ShaderProgram compute(String computeResource) {
        int compute = compile(GL_COMPUTE_SHADER, load(computeResource), computeResource);
        int program = link(compute);
        glDeleteShader(compute);
        return new ShaderProgram(program);
    }

    public void use() {
        glUseProgram(programId);
    }

    public void setFloat(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        if (location >= 0) {
            glUniform1f(location, value);
        }
    }

    public void setInt(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        if (location >= 0) {
            glUniform1i(location, value);
        }
    }

    public void setUnsignedInt(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        if (location >= 0) {
            glUniform1ui(location, value);
        }
    }

    public void setVector3(String name, Vector3f value) {
        setVector3(name, value.x, value.y, value.z);
    }

    public void setVector3(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programId, name);
        if (location >= 0) {
            glUniform3f(location, x, y, z);
        }
    }

    public void setVector3(String name, float[] value) {
        if (value == null || value.length < 3) {
            throw new IllegalArgumentException("vec3 uniform requires at least three values");
        }
        setVector3(name, value[0], value[1], value[2]);
    }

    public void setMatrix4(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        if (location < 0) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    @Override
    public void close() {
        glDeleteProgram(programId);
    }

    private static int compile(int type, String source, String label) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException("Shader compile failed for " + label + ":\n" + log);
        }
        return shader;
    }

    private static int link(int... shaders) {
        int program = glCreateProgram();
        for (int shader : shaders) {
            glAttachShader(program, shader);
        }
        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            glDeleteProgram(program);
            throw new IllegalStateException("Shader link failed:\n" + log);
        }
        for (int shader : shaders) {
            glDetachShader(program, shader);
        }
        return program;
    }

    private static String load(String resource) {
        try (InputStream input = ShaderProgram.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing shader resource: " + resource);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read shader resource: " + resource, e);
        }
    }
}
