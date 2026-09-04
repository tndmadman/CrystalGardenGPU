package com.crystalgarden;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public final class Camera {
    private final Vector3f position = new Vector3f(0.0f, 5.0f, 16.0f);
    private float yaw = (float) Math.toRadians(-90.0);
    private float pitch = (float) Math.toRadians(-16.0);

    public void updateMovement(long window, float deltaSeconds, float normalSpeed, float boostSpeed) {
        float speed = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS ? boostSpeed : normalSpeed;
        float distance = speed * deltaSeconds;

        Vector3f forward = forward();
        Vector3f right = new Vector3f(forward).cross(0.0f, 1.0f, 0.0f).normalize();

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            position.fma(distance, forward);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            position.fma(-distance, forward);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            position.fma(distance, right);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            position.fma(-distance, right);
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            position.y += distance;
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            position.y -= distance;
        }
    }

    public void addLookDelta(double deltaX, double deltaY, float sensitivity) {
        yaw += (float) deltaX * sensitivity;
        pitch -= (float) deltaY * sensitivity;
        float limit = (float) Math.toRadians(89.0);
        pitch = Math.max(-limit, Math.min(limit, pitch));
    }

    public Matrix4f viewMatrix() {
        Vector3f target = new Vector3f(position).add(forward());
        return new Matrix4f().lookAt(position, target, new Vector3f(0.0f, 1.0f, 0.0f));
    }

    public Vector3f position() {
        return position;
    }

    private Vector3f forward() {
        float cosPitch = (float) Math.cos(pitch);
        return new Vector3f(
                (float) Math.cos(yaw) * cosPitch,
                (float) Math.sin(pitch),
                (float) Math.sin(yaw) * cosPitch
        ).normalize();
    }
}
