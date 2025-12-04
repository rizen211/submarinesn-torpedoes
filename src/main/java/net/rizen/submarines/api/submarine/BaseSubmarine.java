package net.rizen.submarines.api.submarine;

import net.minecraft.text.Text;
import net.rizen.submarines.api.submarine.sonar.SonarSystem;
import net.rizen.submarines.api.torpedo.TargetingMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Base class for all submarines. Manages movement, power, weapons, sonar, and inventory.
 *
 * Submarines burn fuel to generate power. Three speed settings are available: silent running for stealth,
 * cruise speed for balanced travel, and flank speed for maximum velocity. Faster speeds consume more
 * power and generate more noise. Players riding in submarines will not drown and cannot take damage while inside.
 *
 * Subclasses define submarine variants with different stats and capabilities.
 */
public abstract class BaseSubmarine extends Entity implements NamedScreenHandlerFactory {
    protected static final TrackedData<Float> HEALTH = DataTracker.registerData(BaseSubmarine.class, TrackedDataHandlerRegistry.FLOAT);
    protected static final TrackedData<Float> SPEED = DataTracker.registerData(BaseSubmarine.class, TrackedDataHandlerRegistry.FLOAT);
    protected static final TrackedData<Integer> TORPEDO_COUNT = DataTracker.registerData(BaseSubmarine.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> TORPEDO_ARMING_TIMER = DataTracker.registerData(BaseSubmarine.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Float> POWER = DataTracker.registerData(BaseSubmarine.class, TrackedDataHandlerRegistry.FLOAT);
    protected static final TrackedData<Integer> MOVEMENT_MODE = DataTracker.registerData(BaseSubmarine.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Integer> TARGETING_MODE = DataTracker.registerData(BaseSubmarine.class, TrackedDataHandlerRegistry.INTEGER);

    protected final SubmarineControls controls;
    protected final SubmarineMovement movement;
    protected final SubmarinePhysics physics;
    protected final SubmarinePower power;
    protected final SubmarineWeaponSystem weaponSystem;
    protected final SubmarineInventory inventory;
    protected final SonarSystem sonarSystem;

    protected float width;
    protected float height;
    protected float length;

    private final TorpedoSpawner torpedoSpawner;
    private final float torpedoFireCost;
    private final Vec3d torpedoSpawnOffset;

    private boolean wasMoving = false;
    private boolean wasLowPower = false;

    /**
     * Creates a new submarine with all stats and components. Sets up movement physics, power system,
     * weapons, inventory, and sonar.
     *
     * @param entityType the Minecraft entity type for this submarine
     * @param world the world this submarine exists in
     * @param maxSpeed maximum forward speed at full throttle
     * @param acceleration rate of speed increase
     * @param deceleration rate of speed decrease
     * @param rotationSpeed turning rate in degrees per tick
     * @param rotationAcceleration rate of rotation speed increase
     * @param rotationDeceleration rate of rotation speed decrease
     * @param verticalSpeedMult multiplier for vertical movement
     * @param backwardSpeedMult multiplier for reverse speed
     * @param maxPower maximum power capacity
     * @param movementConsumption power consumed per tick when moving
     * @param torpedoCooldown ticks between torpedo shots
     * @param torpedoArming ticks before torpedoes can acquire targets
     * @param torpedoSpawner factory for creating torpedoes when fired
     * @param torpedoFireCost power consumed when firing a torpedo
     * @param torpedoSpawnOffset offset from submarine position where torpedo spawns
     * @param width submarine width in blocks
     * @param height submarine height in blocks
     * @param length submarine length in blocks
     */
    public BaseSubmarine(EntityType<? extends BaseSubmarine> entityType, World world,
                         float maxSpeed, float acceleration, float deceleration, float rotationSpeed,
                         float rotationAcceleration, float rotationDeceleration,
                         float verticalSpeedMult, float backwardSpeedMult,
                         float maxPower, float movementConsumption,
                         int torpedoCooldown, int torpedoArming,
                         TorpedoSpawner torpedoSpawner, float torpedoFireCost, Vec3d torpedoSpawnOffset,
                         float width, float height, float length) {
        super(entityType, world);
        this.controls = new SubmarineControls();
        this.movement = new SubmarineMovement(maxSpeed, acceleration, deceleration, rotationSpeed, rotationAcceleration, rotationDeceleration, verticalSpeedMult, backwardSpeedMult);
        this.physics = new SubmarinePhysics();
        this.power = new SubmarinePower(maxPower, movementConsumption);
        this.weaponSystem = new SubmarineWeaponSystem(torpedoCooldown, torpedoArming);
        this.inventory = new SubmarineInventory();
        this.sonarSystem = new SonarSystem();
        this.torpedoSpawner = torpedoSpawner;
        this.torpedoFireCost = torpedoFireCost;
        this.torpedoSpawnOffset = torpedoSpawnOffset;
        this.width = width;
        this.height = height;
        this.length = length;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(HEALTH, 100.0f);
        builder.add(SPEED, 0.0f);
        builder.add(TORPEDO_COUNT, 0);
        builder.add(TORPEDO_ARMING_TIMER, 0);
        builder.add(POWER, 100.0f);
        builder.add(MOVEMENT_MODE, MovementMode.SILENT.ordinal());
        builder.add(TARGETING_MODE, TargetingMode.ALL.ordinal());
    }

    @Override
    public net.minecraft.entity.EntityDimensions getDimensions(net.minecraft.entity.EntityPose pose) {
        return net.minecraft.entity.EntityDimensions.changing(width, height);
    }

    @Override
    protected Box calculateBoundingBox() {
        float maxDimension = Math.max(width, length);
        float halfSize = maxDimension / 2.0f;

        return new Box(
                this.getX() - halfSize, this.getY(), this.getZ() - halfSize,
                this.getX() + halfSize, this.getY() + height, this.getZ() + halfSize
        );
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) {
            if (power.hasPower() && this.isInWaterBlock()) {
                float currentMaxSpeed = getMovementMode().getMaxSpeed();
                movement.update(controls, this, currentMaxSpeed);
            } else {
                movement.stop();
                this.dataTracker.set(SPEED, 0f);
            }

            updatePosition();

            float powerMultiplier = getMovementMode().getPowerMultiplier();
            power.consumePower(controls.isMoving(), powerMultiplier);
            power.tryConsumeFuel(inventory);
            weaponSystem.tick();
            weaponSystem.updateTorpedoCount(inventory);
            sonarSystem.tick();

            this.dataTracker.set(TORPEDO_COUNT, weaponSystem.countTorpedoes(inventory));
            this.dataTracker.set(TORPEDO_ARMING_TIMER, weaponSystem.getTorpedoArmingTimer());
            this.dataTracker.set(POWER, power.getCurrentPower());
            this.dataTracker.set(SPEED, movement.getSignedSpeed());

            breakLilyPads();
            handleLowPowerWarning();
        }

        handleMotorSound();
    }

    private void handleMotorSound() {
        boolean isMoving = controls.isMoving() && power.hasPower() && this.isInWaterBlock();

        if (isMoving && !wasMoving) {
            float volume = getMotorVolume();
            float pitch = getMotorPitch();
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.rizen.submarines.Mod.SUBMARINE_MOTOR,
                    net.minecraft.sound.SoundCategory.NEUTRAL, volume, pitch);
        }

        if (isMoving && this.age % 20 == 0) {
            float volume = getMotorVolume();
            float pitch = getMotorPitch();
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.rizen.submarines.Mod.SUBMARINE_MOTOR,
                    net.minecraft.sound.SoundCategory.NEUTRAL, volume, pitch);
        }

        wasMoving = isMoving;
    }

    private float getMotorVolume() {
        return switch (getMovementMode()) {
            case SILENT -> 0.3f;
            case CRUISE -> 0.6f;
            case FLANK -> 1.0f;
        };
    }

    private float getMotorPitch() {
        return switch (getMovementMode()) {
            case SILENT -> 0.8f;
            case CRUISE -> 1.0f;
            case FLANK -> 1.2f;
        };
    }

    private void handleLowPowerWarning() {
        float currentPower = power.getCurrentPower();
        float maxPower = power.getMaxPower();
        boolean isLowPower = (currentPower / maxPower) < 0.1f;

        if (isLowPower && !wasLowPower) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.rizen.submarines.Mod.SUBMARINE_LOW_POWER,
                    net.minecraft.sound.SoundCategory.NEUTRAL, 2.0f, 1.0f);
        }

        wasLowPower = isLowPower;
    }

    private void breakLilyPads() {
        Box boundingBox = this.getBoundingBox();
        net.minecraft.util.math.BlockPos minPos = new net.minecraft.util.math.BlockPos(
            (int) Math.floor(boundingBox.minX),
            (int) Math.floor(boundingBox.minY),
            (int) Math.floor(boundingBox.minZ)
        );
        net.minecraft.util.math.BlockPos maxPos = new net.minecraft.util.math.BlockPos(
            (int) Math.ceil(boundingBox.maxX),
            (int) Math.ceil(boundingBox.maxY),
            (int) Math.ceil(boundingBox.maxZ)
        );

        for (net.minecraft.util.math.BlockPos pos : net.minecraft.util.math.BlockPos.iterate(minPos, maxPos)) {
            net.minecraft.block.BlockState state = this.getWorld().getBlockState(pos);
            if (state.isOf(net.minecraft.block.Blocks.LILY_PAD)) {
                this.getWorld().breakBlock(pos, true);
            }
        }
    }

    protected void updatePosition() {
        Vec3d movement = this.movement.calculateMovement(this.getYaw());
        movement = physics.applyWaterPhysics(this, movement);
        movement = physics.constrainToWaterSurface(this.getWorld(), this, movement);

        this.move(net.minecraft.entity.MovementType.SELF, movement);
        this.setPitch(0);
    }

    /**
     * Updates submarine controls based on player input. Controls are reset and input is ignored when
     * the submarine is out of power or not in water.
     *
     * @param forward true if moving forward
     * @param backward true if moving backward
     * @param left true if turning left
     * @param right true if turning right
     * @param up true if ascending
     * @param down true if descending
     */
    public void updateInput(boolean forward, boolean backward, boolean left, boolean right, boolean up, boolean down) {
        if (!power.hasPower() || !this.isInWaterBlock()) {
            controls.reset();
            return;
        }
        controls.updateInput(forward, backward, left, right, up, down);
    }

    protected boolean canFireTorpedo() {
        if (!this.isInWaterBlock()) {
            return false;
        }
        return weaponSystem.canFire();
    }

    /**
     * Fires a torpedo from this submarine. Uses the torpedo spawner configured in the constructor
     * to create and spawn the torpedo. Handles power consumption, inventory management, and
     * sound effects automatically.
     *
     * <p>The default implementation handles all standard torpedo firing logic. Subclasses may override
     * for custom firing behavior.</p>
     *
     * @return true if the torpedo was fired successfully, false otherwise
     */
    public boolean fireTorpedo() {
        if (!this.getWorld().isClient && canFireTorpedo()) {
            if (!power.hasPower() || !power.consumePowerAmount(torpedoFireCost)) {
                playUnableToFireSound();
                return false;
            }

            boolean torpedoFound = weaponSystem.findAndConsumeTorpedo(inventory);
            if (torpedoFound) {
                float yawRad = (float) Math.toRadians(this.getYaw());

                double spawnX = this.getX() - Math.sin(yawRad) * torpedoSpawnOffset.x;
                double spawnY = this.getY() + torpedoSpawnOffset.y;
                double spawnZ = this.getZ() + Math.cos(yawRad) * torpedoSpawnOffset.z;

                net.rizen.submarines.api.torpedo.BaseTorpedo torpedo = torpedoSpawner.create(
                    this.getWorld(), spawnX, spawnY, spawnZ, this.getYaw(), 0
                );

                torpedo.setOwnerSubmarine(this);
                torpedo.setTargetingMode(this.getTargetingMode());
                this.getWorld().spawnEntity(torpedo);

                weaponSystem.setFired();
                weaponSystem.updateTorpedoCount(inventory);

                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.rizen.submarines.Mod.TORPEDO_FIRE,
                    net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);

                return true;
            } else {
                power.setCurrentPower(power.getCurrentPower() + torpedoFireCost);
                playUnableToFireSound();
                return false;
            }
        }

        if (!this.getWorld().isClient && !canFireTorpedo()) {
            playUnableToFireSound();
        }
        return false;
    }

    private void playUnableToFireSound() {
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
            net.rizen.submarines.Mod.TORPEDO_UNABLE_TO_FIRE,
            net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        net.minecraft.item.ItemStack stack = player.getStackInHand(hand);

        if (stack.getItem() instanceof net.rizen.submarines.item.SubmarineRepairToolItem) {
            if (!this.getWorld().isClient) {
                float currentHealth = this.getHealth();

                if (currentHealth >= 100.0f) {
                    return ActionResult.PASS;
                }

                float newHealth = Math.min(100.0f, currentHealth + 10.0f);
                this.setHealth(newHealth);

                stack.damage(20, player, player.getSlotForHand(hand));

                this.getWorld().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    net.rizen.submarines.Mod.SUBMARINE_REPAIRED,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
                );
            }

            return ActionResult.SUCCESS;
        }

        if (!this.getWorld().isClient) {
            if (this.getPassengerList().isEmpty()) {
                player.startRiding(this);
                return ActionResult.SUCCESS;
            } else if (this.getPassengerList().contains(player)) {
                player.openHandledScreen(this);
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.PASS;
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof PlayerEntity player) {
            player.setInvulnerable(true);
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger instanceof PlayerEntity player) {
            player.setInvulnerable(false);
            controls.reset();
        }
    }

    public float getHealth() {
        return this.dataTracker.get(HEALTH);
    }

    public void setHealth(float health) {
        this.dataTracker.set(HEALTH, Math.max(0, Math.min(100, health)));
    }

    public float getSpeed() {
        return this.dataTracker.get(SPEED);
    }

    public int getDepth() {
        return physics.calculateDepth(this.getWorld(), this);
    }

    public int getTorpedoCount() {
        return this.dataTracker.get(TORPEDO_COUNT);
    }

    public int getTorpedoCooldown() {
        return weaponSystem.getTorpedoCooldown();
    }

    public int getTorpedoArmingTimer() {
        return this.dataTracker.get(TORPEDO_ARMING_TIMER);
    }

    public boolean isTorpedoArmed() {
        return weaponSystem.isArmed() && getTorpedoCount() > 0;
    }

    public float getPower() {
        return this.dataTracker.get(POWER);
    }

    public boolean hasPower() {
        return power.hasPower();
    }

    public boolean isInWaterBlock() {
        return physics.isInWater(this);
    }

    public SubmarineInventory getInventory() {
        return inventory;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(HEALTH, nbt.getFloat("Health"));
        this.dataTracker.set(POWER, nbt.getFloat("Power"));
        this.movement.setCurrentForwardSpeed(nbt.getFloat("ForwardSpeed"));
        this.movement.setCurrentVerticalSpeed(nbt.getFloat("VerticalSpeed"));
        this.movement.setCurrentRotationSpeed(nbt.getFloat("RotationSpeed"));
        this.weaponSystem.setTorpedoCooldown(nbt.getInt("TorpedoCooldown"));
        this.weaponSystem.setPreviousTorpedoCount(nbt.getInt("PreviousTorpedoCount"));
        this.dataTracker.set(MOVEMENT_MODE, nbt.getInt("MovementMode"));
        if (nbt.contains("TargetingMode")) {
            this.dataTracker.set(TARGETING_MODE, nbt.getInt("TargetingMode"));
        }
        if (nbt.contains("Inventory")) {
            this.inventory.readNbt(nbt.getCompound("Inventory"), this.getRegistryManager());
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putFloat("Health", this.getHealth());
        nbt.putFloat("Power", this.getPower());
        nbt.putFloat("ForwardSpeed", this.movement.getCurrentForwardSpeed());
        nbt.putFloat("VerticalSpeed", this.movement.getCurrentVerticalSpeed());
        nbt.putFloat("RotationSpeed", this.movement.getCurrentRotationSpeed());
        nbt.putInt("TorpedoCooldown", this.weaponSystem.getTorpedoCooldown());
        nbt.putInt("PreviousTorpedoCount", this.weaponSystem.getPreviousTorpedoCount());
        nbt.putInt("MovementMode", this.getMovementMode().ordinal());
        nbt.putInt("TargetingMode", this.getTargetingMode().ordinal());

        NbtCompound inventoryNbt = new NbtCompound();
        this.inventory.writeNbt(inventoryNbt, this.getRegistryManager());
        nbt.put("Inventory", inventoryNbt);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SubmarineScreenHandler(syncId, playerInventory, this.inventory);
    }

    @Override
    public boolean damage(net.minecraft.entity.damage.DamageSource source, float amount) {
        if (this.isRemoved() || this.getWorld().isClient) {
            return false;
        }

        if (!source.isOf(net.minecraft.entity.damage.DamageTypes.EXPLOSION) &&
                !source.isOf(net.minecraft.entity.damage.DamageTypes.PLAYER_EXPLOSION)) {
            return false;
        }

        float currentHealth = this.getHealth();
        float newHealth = currentHealth - amount;
        this.setHealth(newHealth);

        if (newHealth <= 0) {
            this.destroy();
        }

        return true;
    }

    /**
     * Destroys the submarine completely. Removes all passengers, destroys inventory contents,
     * creates an explosion, and removes the submarine from the world. Called when the submarine
     * health reaches zero.
     */
    public void destroy() {
        if (!this.getWorld().isClient) {
            this.removeAllPassengers();

            this.getWorld().createExplosion(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    4.0f,
                    World.ExplosionSourceType.MOB
            );

            this.discard();
        }
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < 1;
    }

    /**
     * Sends out a sonar ping to detect nearby entities. Consumes power when activated.
     */
    public void performSonarPing() {
        if (!this.getWorld().isClient) {
            if (!power.consumePowerAmount(2.0f)) {
                return;
            }
        }

        sonarSystem.performPing(this.getWorld(), this.getPos(), this.getYaw(), this);

        if (!this.getWorld().isClient) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    net.rizen.submarines.Mod.SONAR_PING,
                    net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);
        }
    }

    public SonarSystem getSonarSystem() {
        return sonarSystem;
    }

    public MovementMode getMovementMode() {
        int ordinal = this.dataTracker.get(MOVEMENT_MODE);
        return MovementMode.values()[ordinal];
    }

    public void setMovementMode(MovementMode mode) {
        this.dataTracker.set(MOVEMENT_MODE, mode.ordinal());
    }

    /**
     * Cycles to the next movement mode: silent to cruise to flank and back to silent.
     * Each mode provides different speed and power consumption characteristics.
     */
    public void cycleMovementMode() {
        setMovementMode(getMovementMode().next());
    }

    public TargetingMode getTargetingMode() {
        int ordinal = this.dataTracker.get(TARGETING_MODE);
        return TargetingMode.values()[ordinal];
    }

    public void setTargetingMode(TargetingMode mode) {
        this.dataTracker.set(TARGETING_MODE, mode.ordinal());
    }

    /**
     * Cycles to the next targeting mode for torpedoes. Changes which entity types torpedoes
     * will lock onto and track.
     */
    public void cycleTargetingMode() {
        setTargetingMode(getTargetingMode().next());
    }

    /**
     * Determines the position where passengers (players) sit when riding this submarine.
     * This method should be overridden by subclasses to position passengers correctly
     * based on the submarine's model and size.
     *
     * @param passenger the entity riding the submarine
     * @return the world position where the passenger should be rendered
     */
    @Override
    public abstract Vec3d getPassengerRidingPos(Entity passenger);

    /**
     * Returns the display name for this submarine type. Should use a translation key
     * that corresponds to an entry in the language files (e.g., lang/en_us.json).
     *
     * @return the translated display name text
     */
    @Override
    public abstract Text getDisplayName();
}