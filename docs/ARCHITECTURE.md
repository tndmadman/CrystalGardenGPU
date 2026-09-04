# Architecture

## Design rule

Keep procedural state on the GPU whenever practical. Java owns orchestration; GLSL owns the large parallel workloads.

## Frame

1. GLFW polls input.
2. Java updates the free-fly camera.
3. `crystal.comp` executes one invocation per crystal.
4. A shader-storage memory barrier makes the new crystal state visible to rendering.
5. One `glDrawArraysInstanced` call renders every crystal.
6. `crystal.vert` synthesizes 54 vertices per instance from `gl_VertexID`.
7. `crystal.frag` shades the facets.

## Crystal SSBO layout

Each crystal occupies three `vec4` values (48 bytes, std430):

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

At 16,384 crystals this is only 786,432 bytes of persistent crystal state.

## Geometry

There is intentionally no VBO containing a crystal mesh. For each instance the vertex shader emits:

- 6 tapered side quads = 36 vertices
- 6 triangular tip facets = 18 vertices
- Total = 54 vertices/crystal

The initial 16,384-crystal garden therefore asks the GPU to process 884,736 procedural vertices per frame while issuing a single instanced draw call.

## Near-term growth model

The prototype currently grows independent crystals toward deterministic target heights. The next growth system should turn an individual crystal into a cluster graph:

```text
Seed
 ├─ trunk segment
 │   ├─ child segment
 │   └─ child segment
 └─ symmetry copy
```

A future SSBO can store growth nodes/segments, with compute passes handling:

- active growth tips
- branching probability
- n-fold radial symmetry
- collision/competition
- mineral-field influence
- curl-noise direction

## Why compute + raster first

This gives us GPU-resident simulation and procedural geometry without committing the project to a complex Vulkan renderer. SDF ray marching can be added for special crystal species later, while conventional rasterization remains efficient for very large gardens.
