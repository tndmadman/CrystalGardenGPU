package com.crystalgarden;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
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
    private boolean cursorCaptured;
    private boolean firstMouseEvent = true;
    private boolean controlsVisible = true;
    private boolean imguiInitialized;
    private double lastMouseX;
    private double lastMouseY;
    private double lastAutoRegenerate;

    private GLFWErrorCallback errorCallback;
    private Camera camera;
    private CrystalGarden garden;
    private GardenSettings settings;
    private GardenControlPanel controlPanel;
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGl3 imGuiGl3;

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

        window = glfwCreateWindow(framebufferWidth, framebufferHeight, "CrystalGardenGPU - Procedural Lab", NULL, NULL);
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

        settings = new GardenSettings();
        settings.applyMineralPreset(0);
        camera = new Camera();
        garden = new CrystalGarden();
        controlPanel = new GardenControlPanel();

        installCallbacks();
        initImGui();

        // Start with the control panel usable. Tab switches into free-fly camera mode.
        captureCursor(false);
        glfwShowWindow(window);
        lastAutoRegenerate = glfwGetTime();
    }

    private void initImGui() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        ImGui.styleColorsDark();

        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 460 core");
        imguiInitialized = true;
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
            camera.addLookDelta(dx, dy, settings.mouseSensitivity[0]);
        });

        glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> {
            if (action != GLFW_PRESS) {
                return;
            }

            if (key == GLFW_KEY_ESCAPE) {
                // Escape never quits. It only releases or recaptures the mouse.
                if (cursorCaptured) {
                    captureCursor(false);
                } else {
                    controlsVisible = false;
                    captureCursor(true);
                }
            } else if (key == GLFW_KEY_TAB) {
                controlsVisible = !controlsVisible;
                captureCursor(!controlsVisible);
            } else if (key == GLFW_KEY_R) {
                settings.randomizeSeed();
                garden.reset();
                lastAutoRegenerate = glfwGetTime();
            } else if (key == GLFW_KEY_G) {
                garden.reset();
                lastAutoRegenerate = glfwGetTime();
            } else if (key == GLFW_KEY_F1) {
                settings.randomizeAll();
                garden.reset();
                lastAutoRegenerate = glfwGetTime();
            }
        });
    }

    private void captureCursor(boolean capture) {
        cursorCaptured = capture;
        firstMouseEvent = true;
        glfwSetInputMode(window, GLFW_CURSOR, capture ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        if (glfwRawMouseMotionSupported()) {
            glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, capture ? GLFW_TRUE : GLFW_FALSE);
        }
    }

    private void loop() {
        double previous = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float delta = (float) Math.min(0.1, now - previous);
            previous = now;

            glfwPollEvents();

            imGuiGlfw.newFrame();
            imGuiGl3.newFrame();
            ImGui.newFrame();

            if (controlsVisible) {
                GardenControlPanel.Actions actions = controlPanel.draw(settings);
                if (actions.regenerate()) {
                    garden.reset();
                    lastAutoRegenerate = now;
                }
                if (actions.exitRequested()) {
                    glfwSetWindowShouldClose(window, true);
                }
            }

            if (settings.autoRegenerate[0]) {
                double interval = Math.max(1.0, settings.autoRegenerateSeconds[0]);
                if (now - lastAutoRegenerate >= interval) {
                    settings.randomizeSeed();
                    garden.reset();
                    lastAutoRegenerate = now;
                }
            } else {
                lastAutoRegenerate = now;
            }

            if (cursorCaptured && !controlsVisible) {
                camera.updateMovement(
                        window,
                        delta,
                        settings.cameraSpeed[0],
                        settings.cameraBoost[0]
                );
            }

            glClearColor(
                    settings.fogColor[0],
                    settings.fogColor[1],
                    settings.fogColor[2],
                    1.0f
            );
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            garden.update(settings, delta, (float) now);
            garden.render(settings, camera, (float) framebufferWidth / framebufferHeight, (float) now);

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            glfwSwapBuffers(window);
        }
    }

    private void cleanup() {
        if (garden != null) {
            garden.close();
            garden = null;
        }

        if (imguiInitialized) {
            if (imGuiGl3 != null) {
                imGuiGl3.shutdown();
                imGuiGl3 = null;
            }
            if (imGuiGlfw != null) {
                imGuiGlfw.shutdown();
                imGuiGlfw = null;
            }
            ImGui.destroyContext();
            imguiInitialized = false;
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
