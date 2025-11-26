# Submarines'n Torpedoes - Developer API

This document explains how to extend the Submarines mod with custom submarines, torpedoes, and manufacturing recipes. The API is designed to provide controlled extension points while keeping core systems (physics, guidance, power management) internal.

---

## Table of Contents

1. [What Can Be Extended](#what-can-be-extended)
2. [Creating Custom Submarines](#creating-custom-submarines)
3. [Creating Custom Torpedoes](#creating-custom-torpedoes)
4. [Custom Entity Renderers](#custom-entity-renderers)
5. [Manufacturing Recipes](#manufacturing-recipes)
6. [What NOT to Extend](#what-not-to-extend)

---

## What Can Be Extended

The following extension points are provided and supported:

- **Custom Submarines** - Create submarine variants with different stats and capabilities
- **Custom Torpedoes** - Create torpedo variants with different speeds, ranges, and damage
- **Custom Renderers** - Add OBJ models and textures for custom entities
- **Manufacturing Recipes** - Define custom crafting recipes for the Manufacturing Table

All other systems (power management, physics simulation, guidance algorithms, sonar) are internal implementation details and should not be extended or overridden.

---

## Creating Custom Submarines

### Basic Implementation

Custom submarines extend `BaseSubmarine` and provide their stat configuration through the constructor:

```java
package com.example.modid.entity;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.text.Text;

public class HeavySubmarineEntity extends BaseSubmarine {
    // Define all stats as constants for clarity
    private static final float MAX_SPEED = 0.12f;
    private static final float ACCELERATION = 0.004f;
    private static final float DECELERATION = 0.002f;
    private static final float ROTATION_SPEED = 1.5f;
    private static final float VERTICAL_SPEED_MULT = 0.3f;
    private static final float BACKWARD_SPEED_MULT = 0.5f;

    private static final float MAX_POWER = 150.0f;
    private static final float POWER_CONSUMPTION = 0.015f;

    private static final int TORPEDO_COOLDOWN = 60;
    private static final int TORPEDO_ARMING = 80;
    private static final float TORPEDO_FIRE_COST = 3.0f;
    private static final Vec3d TORPEDO_SPAWN_OFFSET = new Vec3d(5.0, 0, 0);

    private static final float WIDTH = 3.0f;
    private static final float HEIGHT = 3.0f;
    private static final float LENGTH = 8.0f;

    public HeavySubmarineEntity(EntityType<? extends HeavySubmarineEntity> entityType, World world) {
        super(entityType, world,
            MAX_SPEED, ACCELERATION, DECELERATION, ROTATION_SPEED,
            VERTICAL_SPEED_MULT, BACKWARD_SPEED_MULT,
            MAX_POWER, POWER_CONSUMPTION,
            TORPEDO_COOLDOWN, TORPEDO_ARMING,
            HeavyTorpedoEntity::new, TORPEDO_FIRE_COST, TORPEDO_SPAWN_OFFSET,
            WIDTH, HEIGHT, LENGTH
        );
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("entity.modid.heavy_submarine");
    }
}
```

### Constructor Parameters Explained

| Parameter | Type | Description |
|-----------|------|-------------|
| `maxSpeed` | float | Maximum forward speed |
| `acceleration` | float | Rate of speed increase |
| `deceleration` | float | Rate of speed decrease |
| `rotationSpeed` | float | Turning speed in degrees per tick |
| `verticalSpeedMult` | float | Multiplier for vertical movement |
| `backwardSpeedMult` | float | Multiplier for reverse speed |
| `maxPower` | float | Maximum power capacity |
| `movementConsumption` | float | Power consumed per tick while moving |
| `torpedoCooldown` | int | Ticks between torpedo shots |
| `torpedoArming` | int | Ticks before torpedo can track targets |
| `torpedoSpawner` | TorpedoSpawner | Factory for creating torpedoes |
| `torpedoFireCost` | float | Power consumed when firing a torpedo |
| `torpedoSpawnOffset` | Vec3d | Offset from submarine position where torpedo spawns |
| `width` | float | Submarine width in blocks |
| `height` | float | Submarine height in blocks |
| `length` | float | Submarine length in blocks |

### Torpedo Configuration

The submarine automatically handles torpedo firing through the `TorpedoSpawner` functional interface passed to the constructor:

```java
// Simple method reference to torpedo constructor
HeavyTorpedoEntity::new
```

The spawner is called automatically when the submarine fires. All power consumption, inventory management, sound effects, and targeting setup are handled internally.

### Torpedo Spawn Offset

The `torpedoSpawnOffset` parameter controls where torpedoes appear relative to the submarine:

```java
// Spawn 5 blocks forward from submarine center
new Vec3d(5.0, 0, 0)

// Spawn 3 blocks forward, 1 block down
new Vec3d(3.0, -1.0, 0)
```

The offset's X component represents forward/backward distance (positive = forward), Y is vertical offset, and Z is not used (rotation is applied automatically based on submarine yaw).

### Passenger Positioning

The `getPassengerRidingPos()` method controls where players sit in the submarine. The method calculates the position based on the submarine's rotation and configured offsets.

### Entity Registration

Register the submarine entity in the mod initializer:

```java
public static final EntityType<HeavySubmarineEntity> HEAVY_SUBMARINE_ENTITY = Registry.register(
    Registries.ENTITY_TYPE,
    Identifier.of("modid", "heavy_submarine"),
    EntityType.Builder.create(
        (EntityType<HeavySubmarineEntity> type, World world) -> new HeavySubmarineEntity(type, world),
        SpawnGroup.MISC
    )
    .dimensions(3.0f, 3.0f)  // Should match WIDTH and HEIGHT
    .maxTrackingRange(64)
    .trackingTickInterval(1)
    .build()
);
```

---

## Creating Custom Torpedoes

### Basic Implementation

Custom torpedoes extend `BaseTorpedo` and are significantly simpler than submarines:

```java
package com.example.modid.entity;

import net.rizen.submarines.api.torpedo.BaseTorpedo;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class HeavyTorpedoEntity extends BaseTorpedo {
    private static final float MAX_SPEED = 2.0f;  // blocks per tick
    private static final int MAX_RANGE = 150;      // blocks
    private static final float EXPLOSION_POWER = 6.0f;
    private static final float DIRECT_DAMAGE = 75.0f;

    private static final float WIDTH = 0.6f;
    private static final float HEIGHT = 0.6f;
    private static final float LENGTH = 2.5f;

    public HeavyTorpedoEntity(EntityType<? extends HeavyTorpedoEntity> entityType, World world) {
        super(entityType, world, MAX_SPEED, MAX_RANGE, EXPLOSION_POWER,
              DIRECT_DAMAGE, WIDTH, HEIGHT, LENGTH);
    }

    // Convenience constructor for spawning
    public HeavyTorpedoEntity(World world, double x, double y, double z, float yaw, float pitch) {
        this(MyMod.HEAVY_TORPEDO_ENTITY, world);
        initialize(x, y, z, yaw, pitch);
    }
}
```

### Constructor Parameters Explained

| Parameter | Type | Description |
|-----------|------|-------------|
| `maxSpeed` | float | Travel speed in blocks per tick |
| `maxRange` | int | Maximum travel distance in blocks |
| `explosionPower` | float | Explosion size |
| `directDamage` | float | Additional damage to the directly hit entity |
| `width` | float | Torpedo width in blocks |
| `height` | float | Torpedo height in blocks |
| `length` | float | Torpedo length in blocks |

### Built-in Features

All torpedoes automatically include:

- **Homing guidance** - Acquires and tracks targets using cone-based detection
- **Arming delay** - 10 tick delay before homing activates (configurable per submarine)
- **Targeting modes** - Inherits from firing submarine (ALL, PLAYERS, SUBMARINES, ENTITIES)
- **Water physics** - Automatically explodes when leaving water
- **Collision detection** - Explodes on impact with blocks or entities
- **Particle effects** - Bubble trail while traveling

No additional implementation is required for these features.

### Entity Registration

```java
public static final EntityType<HeavyTorpedoEntity> HEAVY_TORPEDO_ENTITY = Registry.register(
    Registries.ENTITY_TYPE,
    Identifier.of("modid", "heavy_torpedo"),
    EntityType.Builder.<HeavyTorpedoEntity>create(
        (EntityType<HeavyTorpedoEntity> type, World world) -> new HeavyTorpedoEntity(type, world),
        SpawnGroup.MISC
    )
    .dimensions(0.6f, 0.6f)  // Should match WIDTH and HEIGHT
    .maxTrackingRange(128)
    .trackingTickInterval(1)
    .build()
);
```

---

## Custom Entity Renderers

### Using BaseOBJEntityRenderer

The mod provides `BaseOBJEntityRenderer` for rendering entities with OBJ models:

```java
package com.example.modid.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.rizen.submarines.api.client.render.BaseOBJEntityRenderer;
import net.rizen.submarines.api.submarine.BaseSubmarine;

public class HeavySubmarineRenderer extends BaseOBJEntityRenderer<BaseSubmarine> {
    private static final Identifier MODEL = Identifier.of("modid", "heavy_submarine.obj");
    private static final Identifier TEXTURE = Identifier.of("modid", "textures/entity/heavy_submarine.png");

    public HeavySubmarineRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, MODEL, TEXTURE);
    }
}
```

### File Locations

Place OBJ models and textures in the resources directory:

```
src/main/resources/
└── assets/
    └── modid/
        ├── models/
        │   ├── heavy_submarine.obj
        │   └── heavy_torpedo.obj
        └── textures/
            └── entity/
                ├── heavy_submarine.png
                └── heavy_torpedo.png
```

### Renderer Registration

Register renderers in the client initializer:

```java
@Override
public void onInitializeClient() {
    EntityRendererRegistry.register(MyMod.HEAVY_SUBMARINE_ENTITY, HeavySubmarineRenderer::new);
    EntityRendererRegistry.register(MyMod.HEAVY_TORPEDO_ENTITY, HeavyTorpedoRenderer::new);
}
```

### OBJ Model Requirements

- Models must use **triangulated faces** (no quads or n-gons)
- Export with vertex normals and UV coordinates
- Model should face **forward on the Z+ axis** in modeling software
- The renderer handles rotation automatically based on entity yaw

### Advanced Rendering

`BaseOBJEntityRenderer` provides protected methods for customization:
- `getColor()` - Apply color tinting to the model
- `applyTransformations()` - Apply additional transformations (scale, rotation, translation)
- `getRenderLayer()` - Change the render layer
- `getOverlay()` - Modify overlay effects

---

## Manufacturing Recipes

### Creating Recipes

Manufacturing recipes are defined using a builder pattern:

```java
package com.example.modid.crafting;

import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.rizen.submarines.api.crafting.ManufacturingRecipe;
import net.rizen.submarines.api.crafting.ManufacturingRecipeRegistry;

public class MyRecipes {
    public static void register() {
        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of("modid", "heavy_submarine"))
                .ingredient(MyMod.ADVANCED_TRANSDUCER, 1)
                .ingredient(MyMod.STEEL_CASING, 48)
                .ingredient(Items.CHEST, 2)
                .ingredient(Items.LAVA_BUCKET, 2)
                .result(MyMod.HEAVY_SUBMARINE_ITEM)
                .category(ManufacturingRecipe.CATEGORY_SUBMARINES)
                .displayName("Heavy Submarine")
                .build()
        );

        ManufacturingRecipeRegistry.register(
            ManufacturingRecipe.builder(Identifier.of("modid", "heavy_torpedo"))
                .ingredient(MyMod.ADVANCED_WARHEAD, 1)
                .ingredient(MyMod.STEEL_CASING, 4)
                .ingredient(Items.REDSTONE, 8)
                .result(MyMod.HEAVY_TORPEDO_ITEM)
                .category(ManufacturingRecipe.CATEGORY_WEAPONS)
                .displayName("Heavy Torpedo")
                .build()
        );
    }
}
```

### Recipe Categories

Three categories are available for organizing recipes in the GUI:

- `ManufacturingRecipe.CATEGORY_SUBMARINES` - Submarine crafting
- `ManufacturingRecipe.CATEGORY_WEAPONS` - Torpedoes and weapons
- `ManufacturingRecipe.CATEGORY_COMPONENTS` - Parts and materials

### Registering Recipes

Call the registration method in the mod initializer:

```java
@Override
public void onInitialize() {
    MyRecipes.register();
}
```

### Recipe Builder Methods

| Method | Description |
|--------|-------------|
| `ingredient(Item, int)` | Add required ingredient with count |
| `result(Item)` | Set the result item (crafts 1) |
| `result(ItemStack)` | Set result with count or NBT data |
| `category(String)` | Set category (use CATEGORY_* constants) |
| `displayName(String)` | Set display name in GUI |
| `build()` | Build the recipe (validates all required fields) |

---

## What NOT to Extend

The following classes are internal implementation details and should **not** be extended or modified:

### Submarine Internals

- `SubmarineMovement` - Movement physics and acceleration
- `SubmarinePhysics` - Water physics and collision
- `SubmarinePower` - Power management and fuel consumption
- `SubmarineControls` - Input handling
- `SubmarineWeaponSystem` - Weapon cooldowns and firing
- `SubmarineInventory` - Inventory management
- `SonarSystem` - Sonar detection and rendering

### Torpedo Internals

- `TorpedoPhysics` - Homing guidance and target acquisition
- `TorpedoCollision` - Collision detection and explosions

### Why These Are Internal

These systems contain complex algorithms including:

- Rodriguez rotation formulas for torpedo guidance
- Sophisticated physics simulation
- Network synchronization logic
- Client-server state management

Modifying these systems will likely break functionality and cause incompatibilities. The API surface (`BaseSubmarine`, `BaseTorpedo`) provides all necessary extension points through composition.

### Accessing Internal State

Protected fields in `BaseSubmarine` provide safe access to internal systems:

```java
protected final SubmarineControls controls;
protected final SubmarineMovement movement;
protected final SubmarinePhysics physics;
protected final SubmarinePower power;
protected final SubmarineWeaponSystem weaponSystem;
protected final SubmarineInventory inventory;
protected final SonarSystem sonarSystem;
```

These are provided for **read-only use** in subclass methods (like `fireTorpedo()`). Do not attempt to override or replace these instances.

---

## Complete Example

Here's a complete example mod adding a new submarine:

### Entity Class

```java
// src/main/java/com/example/deepsea/entity/DeepSeaSubmarineEntity.java
package com.example.deepsea.entity;

import net.rizen.submarines.api.submarine.BaseSubmarine;
import com.example.deepsea.DeepSeaMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DeepSeaSubmarineEntity extends BaseSubmarine {
    private static final float TORPEDO_FIRE_COST = 2.5f;
    private static final Vec3d TORPEDO_SPAWN_OFFSET = new Vec3d(4.0, 0, 0);

    public DeepSeaSubmarineEntity(EntityType<? extends DeepSeaSubmarineEntity> entityType, World world) {
        super(entityType, world,
            0.18f, 0.006f, 0.004f, 2.5f, 0.5f, 0.65f,
            120.0f, 0.012f,
            50, 70,
            DeepSeaTorpedoEntity::new, TORPEDO_FIRE_COST, TORPEDO_SPAWN_OFFSET,
            2.8f, 2.8f, 7.0f
        );
    }

    @Override
    public Vec3d getPassengerRidingPos(Entity passenger) {
        return new Vec3d(this.getX(), this.getY() + 4.0, this.getZ());
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("entity.deepsea.deep_sea_submarine");
    }
}
```

### Torpedo Class

```java
// src/main/java/com/example/deepsea/entity/DeepSeaTorpedoEntity.java
package com.example.deepsea.entity;

import net.rizen.submarines.api.torpedo.BaseTorpedo;
import com.example.deepsea.DeepSeaMod;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public class DeepSeaTorpedoEntity extends BaseTorpedo {
    public DeepSeaTorpedoEntity(EntityType<? extends DeepSeaTorpedoEntity> entityType, World world) {
        super(entityType, world, 2.5f, 140, 5.0f, 60.0f, 0.55f, 0.55f, 2.2f);
    }

    public DeepSeaTorpedoEntity(World world, double x, double y, double z, float yaw, float pitch) {
        this(DeepSeaMod.DEEP_SEA_TORPEDO_ENTITY, world);
        initialize(x, y, z, yaw, pitch);
    }
}
```

### Renderer Classes

```java
// src/main/java/com/example/deepsea/client/render/DeepSeaSubmarineRenderer.java
package com.example.deepsea.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.rizen.submarines.api.client.render.BaseOBJEntityRenderer;
import net.rizen.submarines.api.submarine.BaseSubmarine;

public class DeepSeaSubmarineRenderer extends BaseOBJEntityRenderer<BaseSubmarine> {
    public DeepSeaSubmarineRenderer(EntityRendererFactory.Context ctx) {
        super(ctx,
            Identifier.of("deepsea", "deep_sea_submarine.obj"),
            Identifier.of("deepsea", "textures/entity/deep_sea_submarine.png")
        );
    }
}
```

### Mod Initializer

```java
// src/main/java/com/example/deepsea/DeepSeaMod.java
package com.example.deepsea;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DeepSeaMod implements ModInitializer {
    public static final String MOD_ID = "deepsea";

    public static final EntityType<DeepSeaSubmarineEntity> DEEP_SEA_SUBMARINE_ENTITY =
        Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "deep_sea_submarine"),
            EntityType.Builder.create(
                (EntityType<DeepSeaSubmarineEntity> type, World world) ->
                    new DeepSeaSubmarineEntity(type, world),
                SpawnGroup.MISC
            )
            .dimensions(2.8f, 2.8f)
            .maxTrackingRange(64)
            .trackingTickInterval(1)
            .build()
        );

    public static final EntityType<DeepSeaTorpedoEntity> DEEP_SEA_TORPEDO_ENTITY =
        Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "deep_sea_torpedo"),
            EntityType.Builder.<DeepSeaTorpedoEntity>create(
                (EntityType<DeepSeaTorpedoEntity> type, World world) ->
                    new DeepSeaTorpedoEntity(type, world),
                SpawnGroup.MISC
            )
            .dimensions(0.55f, 0.55f)
            .maxTrackingRange(128)
            .trackingTickInterval(1)
            .build()
        );

    public static final Item SUBMARINE_ITEM = Registry.register(
        Registries.ITEM,
        Identifier.of(MOD_ID, "deep_sea_submarine"),
        new DeepSeaSubmarineItem(new Item.Settings().maxCount(1))
    );

    @Override
    public void onInitialize() {
        DeepSeaRecipes.register();
    }
}
```

### Client Initializer

```java
// src/main/java/com/example/deepsea/client/DeepSeaClient.java
package com.example.deepsea.client;

import com.example.deepsea.DeepSeaMod;
import com.example.deepsea.client.render.DeepSeaSubmarineRenderer;
import com.example.deepsea.client.render.DeepSeaTorpedoRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DeepSeaClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(DeepSeaMod.DEEP_SEA_SUBMARINE_ENTITY,
            DeepSeaSubmarineRenderer::new);
        EntityRendererRegistry.register(DeepSeaMod.DEEP_SEA_TORPEDO_ENTITY,
            DeepSeaTorpedoRenderer::new);
    }
}
```

---

## Summary

The Submarines mod API provides focused extension points for creating gameplay variety while protecting core systems. When creating custom content:

✅ **DO** extend `BaseSubmarine` and `BaseTorpedo` with different stats
✅ **DO** use `TorpedoSpawner` (method references) to configure torpedo types
✅ **DO** use `BaseOBJEntityRenderer` for custom models
✅ **DO** create `ManufacturingRecipe` entries for custom items
✅ **DO** read protected fields in `BaseSubmarine` when needed

❌ **DON'T** extend or modify internal system classes
❌ **DON'T** attempt to override physics or guidance algorithms
❌ **DON'T** replace component instances (`power`, `weaponSystem`, etc.)
❌ **DON'T** override `fireTorpedo()` unless custom behavior is required

The API is designed to enable creative submarine variants without exposing implementation complexity. All complex systems (physics, guidance, power management, torpedo firing) work automatically once stats are configured.
