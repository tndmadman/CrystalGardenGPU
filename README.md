# CrystalGardenGPU

Real-time procedural 3D mineral/crystal art generated on the GPU with Java, LWJGL, OpenGL 4.6, GLSL compute shaders, and an in-app Dear ImGui control lab.

## Goal

Build a living mathematical mineral garden where placement, growth, geometry, surface structure, color, atmosphere, and cluster habit emerge from deterministic GPU-side rules rather than authored 3D models.

## Current procedural lab

The renderer supports up to **65,536 GPU-resident crystal bases**. Each base can render from one to eight procedurally generated shards. There are no authored crystal meshes: the vertex shader synthesizes every formation from `gl_VertexID`, while the compute shader owns generation and growth state in an SSBO.

The app starts in **Quartz Cathedral** and exposes a live **Crystal Garden Lab** panel. Mineral presets set realistic-inspired crystal habits, cluster behavior, palette, surface style, and material response, while every underlying parameter remains editable afterward.

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

## Twenty mineral formations

1. Quartz Cathedral
2. Amethyst Geode
3. Citrine Spires
4. Smoky Quartz Field
5. Emerald Pocket
6. Aquamarine Columns
7. Tourmaline Grove
8. Ruby Corundum Cluster
9. Sapphire Corundum Cluster
10. Fluorite Cubes
11. Galena Blocks
12. Pyrite Citadel
13. Bismuth Hopper Towers
14. Selenite Blades
15. Kyanite Fans
16. Stibnite Needle Spray
17. Aragonite Starburst
18. Calcite Dogtooth Cluster
19. Sulfur Bipyramids
20. Native Copper Dendrites

These are not only palette swaps. The GPU renderer now has ten formation families:

- prismatic
- needle
- blade
- cubic
- hopper / terraced
- scalenohedron / dogtooth
- bipyramid
- radial starburst
- bladed fan
- dendritic branching

It also has six surface families: smooth/glassy, striated, stepped/terraced, metallic, banded/zoned, and iridescent.

## Live settings

The panel exposes:

- mineral preset selection and descriptions
- seed, same-seed regrow, new garden, randomize everything
- live regeneration, automatic timed regeneration, pause growth
- crystal count from 256 to 65,536
- Grid, Radial, Spiral, and Cluster distributions
- spacing, placement jitter, sparsity, cluster count/radius, spiral turns
- minimum/maximum radius and height
- height distribution curve, growth-speed range, multiplier, growth pulse
- formation geometry selection
- polygon sides from 3 to 12
- taper, tip ratio, twist, random tilt, field bend, animated sway
- 1–8 shards per crystal base, satellite spread/size/lean
- blade thickness
- 1–4 hopper steps and hopper inset
- bipyramid/scalenohedron waist position
- dendrite branch levels and branch angle
- starburst/fan radial force
- formation irregularity
- mineral-field scale, domain warp, contrast
- three editable palette colors and per-crystal color variation
- surface structure, metallic response, roughness, iridescence, pattern scale
- ambient/directional lighting, specular response, Fresnel edge glow, emission
- internal crystal banding
- fog density/color/maximum and exposure
- camera FOV, move speed, boost speed, mouse sensitivity

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
  ├─ live settings + 20 mineral presets
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
       ├─ 10 procedural geometry families
       ├─ 1–8 shards per base
       ├─ twist / tilt / bend / sway
       ├─ metallic / rough / iridescent surface models
       └─ live lighting / fog / exposure
```

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the underlying state layout and frame flow.

## Next complexity milestones

1. true recursive GPU segment-pool branching with configurable n-fold symmetry
2. Voronoi mineral territories and curl/simplex vector fields
3. crystal competition / spatial collision and growth-resource depletion
4. procedural terrain/substrate driven by the same mineral fields
5. HDR framebuffer, bloom, translucency, absorption, and refraction approximations
6. reaction-diffusion nucleation maps
7. GPU indirect draw/culling and LOD for much larger scenes
8. optional ray-marched/SDF hero crystals

## Why OpenGL first?

The project deliberately starts with OpenGL 4.6 compute shaders rather than Vulkan. The procedural work remains GPU-resident while keeping renderer plumbing small enough that development can stay focused on mathematical art and live experimentation. Vulkan remains a future option if explicit scheduling, ray tracing, or scale creates a concrete reason to migrate.
