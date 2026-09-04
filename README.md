# CrystalGardenGPU

Real-time procedural 3D crystal art generated on the GPU with Java, LWJGL, OpenGL 4.6, and GLSL compute shaders.

## Goal

Build a living mathematical crystal garden where crystal placement, growth, shape, and color emerge from deterministic GPU-side rules rather than authored 3D models.

## Current prototype

The first milestone uses a compute shader to initialize and grow thousands of crystal instances in an SSBO. A vertex shader procedurally creates hexagonal crystal prisms from `gl_VertexID`, so there is no crystal mesh asset to load. Java handles the window, camera, input, timing, and dispatch.

### Controls

- `W/A/S/D` — move
- `Space` / `Left Ctrl` — move up/down
- Mouse — look
- `Shift` — faster movement
- `Esc` — release/capture mouse; press again while released to exit
- `R` — reset/reseed the garden

## Stack

- Java 21
- Gradle
- LWJGL 3.3.6
- OpenGL 4.6 core
- GLSL 4.60 compute/vertex/fragment shaders
- JOML for camera matrices

## Run

```bash
./gradlew run
```

On Windows:

```powershell
.\gradlew.bat run
```

A GPU/driver exposing OpenGL 4.6 is required. The project is targeted primarily at modern NVIDIA hardware; the original development target is an RTX 3090.

## Architecture

```text
Java / GLFW
  ├─ window + input
  ├─ free-fly camera
  ├─ timing
  └─ renderer orchestration
          │
          ▼
OpenGL 4.6
  ├─ Compute shader
  │    └─ crystal generation + growth → SSBO
  │
  └─ Instanced renderer
       ├─ vertex shader generates crystal prism geometry
       └─ fragment shader handles lighting / glow
```

## Roadmap

1. GPU crystal growth prototype
2. Branching crystal clusters and multiple symmetry families
3. Voronoi territory / mineral fields
4. Curl/simplex-noise growth direction
5. Reaction-diffusion nucleation map
6. Better transparent/refractive crystal material
7. HDR + bloom + fog
8. L-system crystal trees
9. GPU indirect draw / culling for very large gardens
10. Optional SDF ray-marched crystal species

## Why OpenGL first?

The project deliberately starts with OpenGL 4.6 compute shaders rather than Vulkan. The procedural work remains GPU-resident while keeping renderer plumbing small enough that development effort can stay focused on the mathematical art. Vulkan remains a future option if explicit scheduling, ray tracing, or scale creates a real reason to migrate.
