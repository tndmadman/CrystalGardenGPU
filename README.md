# CrystalGardenGPU

Real-time procedural 3D crystal art generated on the GPU with Java, LWJGL, OpenGL 4.6, GLSL compute shaders, and an in-app Dear ImGui control lab.

## Goal

Build a living mathematical crystal garden where placement, growth, geometry, motion, color, atmosphere, and cluster structure emerge from deterministic GPU-side rules rather than authored 3D models.

## Current procedural lab

The renderer supports up to **65,536 GPU-resident crystal bases**. Each base can render from one to five procedurally generated shards, so a garden can range from a clean field of single crystals to dense leaning multi-shard clusters. There are no authored crystal meshes: the vertex shader synthesizes the geometry from `gl_VertexID` while the compute shader owns generation and growth state in an SSBO.

An in-app **Crystal Garden Lab** panel exposes the generator live. Most visual/material controls apply immediately, while generator controls can automatically regrow the garden when changed.

### Controls

- `Tab` — toggle the settings panel / free-fly camera mode
- `Esc` — release or recapture the mouse; **does not exit the app**
- `W/A/S/D` — free-fly movement
- `Space` / `Left Ctrl` — move up/down
- Mouse — look around in free-fly mode
- `Shift` — boosted movement speed
- `R` — generate a new random seed and regrow
- `G` — regrow the current seed
- `F1` — randomize the procedural settings and seed
- Exit with the **Exit Application** button, the window close button, or `Alt+F4`

### Live settings

The panel currently exposes:

- seed, same-seed regrow, new garden, randomize everything
- live regeneration, automatic timed regeneration, pause growth
- crystal count from 256 to 65,536
- Grid, Radial, Spiral, and Cluster distributions
- spacing, placement jitter, sparsity, cluster count/radius, spiral turns
- minimum/maximum radius and height
- height distribution curve, growth-speed range, live growth multiplier, growth pulse
- polygon sides from 3 to 12
- taper, tip ratio, twist, random tilt, field bend
- animated living sway and sway speed
- 1–5 shards per crystal cluster
- satellite-shard spread, size, and outward lean
- mineral-field scale, domain warp, contrast
- three editable palette colors, third-color mixing, per-crystal color variation
- ambient and directional lighting
- specular strength/sharpness, Fresnel edge glow, emission
- internal crystal banding
- fog density/color/maximum and exposure
- live light direction
- camera FOV, move speed, boost speed, and mouse sensitivity

### Presets

Built-in starting points include:

- Needle Forest
- Amethyst Cathedral
- Alien Reef
- Crystal Storm
- Obsidian Spires

## Stack

- Java 20
- Gradle
- LWJGL 3.4.1
- OpenGL 4.6 core
- GLSL 4.60 compute/vertex/fragment shaders
- JOML
- imgui-java / Dear ImGui

## Run

On Windows, double-click `run.bat`. The launcher uses the existing `java` and `javac` commands from your installed **JDK 20** and caches portable Gradle locally after the first setup. It does not download another JDK.

With Gradle installed manually:

```bash
gradle run
```

A GPU/driver exposing OpenGL 4.6 is required. The primary development target is modern NVIDIA hardware, particularly an RTX 3090-class GPU.

## GPU architecture

```text
Java / GLFW / Dear ImGui
  ├─ window + input
  ├─ free-fly camera
  ├─ live settings / presets
  ├─ regeneration lifecycle
  └─ renderer orchestration
          │
          ▼
OpenGL 4.6
  ├─ Compute shader
  │    ├─ seeded placement
  │    ├─ Grid / Radial / Spiral / Cluster distributions
  │    ├─ warped mineral fields
  │    ├─ palette assignment
  │    └─ animated growth → SSBO
  │
  └─ Instanced renderer
       ├─ 3–12 sided procedural shard geometry
       ├─ twist / tilt / bend / sway
       ├─ multi-shard crystal clusters
       └─ live material / lighting / fog shading
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the underlying state layout and frame flow.

## Next complexity milestones

1. GPU segment-pool branching with configurable n-fold symmetry
2. true Voronoi mineral territories and curl/simplex vector fields
3. crystal competition / spatial collision and growth-resource depletion
4. procedural terrain/substrate driven by the same mineral fields
5. HDR framebuffer, bloom, translucency, absorption, and refraction approximations
6. reaction-diffusion nucleation maps
7. GPU indirect draw/culling and LOD for much larger scenes
8. optional ray-marched/SDF crystal species

## Why OpenGL first?

The project deliberately starts with OpenGL 4.6 compute shaders rather than Vulkan. The procedural work remains GPU-resident while keeping renderer plumbing small enough that development can stay focused on mathematical art and live experimentation. Vulkan remains a future option if explicit scheduling, ray tracing, or scale creates a concrete reason to migrate.
