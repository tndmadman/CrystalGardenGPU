# Architecture

## Design rule

Keep procedural state on the GPU whenever practical. Java owns orchestration and live settings; GLSL owns the large parallel workloads and procedural geometry.

## Frame

1. GLFW polls input.
2. Dear ImGui edits `GardenSettings` live.
3. Java updates the free-fly camera.
4. `crystal.comp` executes one invocation per crystal base.
5. A shader-storage memory barrier makes the new crystal state visible to rendering.
6. One `glDrawArraysInstanced` call renders every active base and all configured shards.
7. `crystal.vert` selects a formation family and synthesizes geometry from `gl_VertexID`.
8. `crystal.frag` applies mineral surface structure, material response, lighting, exposure, and fog.

## Crystal SSBO layout

Each crystal base occupies three `vec4` values (48 bytes, std430):

```text
positionHeight.xyz = world-space base position
positionHeight.w   = current height

params.x = radius
params.y = growth speed
params.z = initialized marker
params.w = target height

color.rgb = mineral color
color.a   = reserved
```

The SSBO is allocated for up to 65,536 bases, which is about 3 MiB of persistent crystal state.

## Procedural geometry

There is intentionally no crystal mesh VBO. `crystal.vert` reserves **144 vertex slots per shard** and emits only the triangles required by the active formation; unused slots collapse to degenerate triangles.

Current formation families:

- prismatic
- needle
- blade
- cube
- hopper / terraced
- scalenohedron / dogtooth
- bipyramid
- radial starburst
- bladed fan
- dendritic branching

Each crystal base can render from **1 to 8 shards**. Starburst and fan modes reinterpret those shards as radial/fanned rays, while the dendrite geometry uses its vertex budget for a trunk plus lateral branch segments.

The theoretical maximum draw configuration is deliberately much larger than the tuned mineral presets. Complex formations use lower base counts so the RTX 3090-class target can spend more vertex work on each mineral habit.

## Mineral preset layer

`MineralPresetLibrary` maps twenty mineral-inspired formations onto the same underlying GPU controls. A preset configures:

- formation family
- surface family
- population/distribution
- radius/height/growth ranges
- cluster shard count and spread
- geometry-specific controls
- mineral palettes and variation
- metallic/roughness/iridescence response
- lighting/fog/exposure defaults

The user can freely change every underlying setting after loading a preset.

## Surface model

`crystal.frag` currently supports six procedural surface families:

- smooth / glassy
- striated
- stepped / terraced
- metallic
- banded / color-zoned
- iridescent

These are stylized real-time approximations, not physically complete mineral optics. Metallic and roughness parameters modify diffuse/specular balance; iridescence uses a view/position-dependent spectral palette approximation.

## Compute generation

`crystal.comp` remains responsible for:

- deterministic seeded placement
- Grid / Radial / Spiral / Cluster distributions
- warped mineral fields
- per-base radius and target height
- growth speed and animated growth pulse
- palette mixing and per-crystal color variation

Changing generator-affecting settings can clear and regrow the SSBO immediately. Rendering-only formation/material settings can change live without rebuilding GPU state.

## Future growth model

The current dendrite formation is procedurally branch-like geometry inside one shard budget. The next major architecture step is a true GPU segment pool where growth tips can recursively create new segments:

```text
Seed
 ├─ trunk segment
 │   ├─ child segment
 │   │   └─ grandchild segment
 │   └─ child segment
 └─ symmetry copy
```

Future compute passes can handle:

- active growth tips
- recursive branching probability
- n-fold radial symmetry
- collision/competition
- mineral-field influence
- curl-noise direction
- resource depletion

## Why compute + raster first

This gives GPU-resident simulation and procedural geometry without committing the project to a complex Vulkan renderer. SDF ray marching or ray tracing can be added for hero crystals later, while conventional rasterization remains efficient for large interactive gardens.
