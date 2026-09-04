package com.crystalgarden;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Application {
    private long window;
    private int framebufferWidth = 1600;
    private int framebufferHeight = 900;
    private boolean cursorCaptured = true;
    private boolean firstMouseEvent = true;
    private double lastMouseX;
    private double lastMouseY;

    private GLFWErrorCallback errorCallback;
    private Camera camera;
    private CrystalGarden garden;

    public void run() {
        try {
            init();
            loop();
        } finally {
            cleanup();
        }
    }

    private void init() {
        errorCallback = GLFWErrorCallback.createPrint(System.err);
        errorCallback.set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        window = glfwCreateWindow(framebufferWidth, framebufferHeight, "CrystalGardenGPU", NULL, NULL);
        if (window == NULL) {
            throw new IllegalStateException("Failed to create the OpenGL 4.6 window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GL.createCapabilities();

        if (!GL.getCapabilities().OpenGL46) {
            throw new IllegalStateException("OpenGL 4.6 is required by this prototype");
        }

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glClearColor(0.004f, 0.006f, 0.012f, 1.0f);

        camera = new Camera();
        garden = new CrystalGarden();
        installCallbacks();
        captureCursor(true);

        glfwShowWindow(window);
    }

    private void installCallbacks() {
        glfwSetFramebufferSizeCallback(window, (handle, width, height) -> {
            framebufferWidth = Math.max(1, width);
            framebufferHeight = Math.max(1, height);
            glViewport(0, 0, framebufferWidth, framebufferHeight);
        });

        glfwSetCursorPosCallback(window, (handle, x, y) -> {
            if (!cursorCaptured) {
                return;
            }
            if (firstMouseEvent) {
                lastMouseX = x;
                lastMouseY = y;
                firstMouseEvent = false;
                return;
            }

            double dx = x - lastMouseX;
            double dy = y - lastMouseY;
            lastMouseX = x;
            lastMouseY = y;
            camera.addLookDelta(dx, dy);
        });

        glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {
            if (action != GLFW_PRESS) {
                return;
            }

            if (key == GLFW_KEY_ESCAPE) {
                if (cursorCaptured) {
                    captureCursor(false);
                } else {
                    glfwSetWindowShouldClose(window, true);
                }
            } else if (key == GLFW_KEY_R) {
                garden.reset();
            }
        });
    }

    private void captureCursor(boolean capture) {
        cursorCaptured = capture;
        firstMouseEvent = true;
        glfwSetInputMode(window, GLFW_CURSOR, capture ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        if (capture && glfwRawMouseMotionSupported()) {
            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }
    }

    private void loop() {
        double previous = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float delta = (float) Math.min(0.1, now - previous);
            previous = now;

            glfwPollEvents();
            camera.updateMovement(window, delta);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            garden.update(delta, (float) now);
            garden.render(camera, (float) framebufferWidth / framebufferHeight);

            glfwSwapBuffers(window);
        }
    }

    private void cleanup() {
        if (garden != null) {
            garden.close();
            garden = null;
        }
        if (window != NULL) {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
            window = NULL;
        }
        glfwTerminate();
        if (errorCallback != null) {
            errorCallback.free();
            errorCallback = null;
        }
    }
}
