package net.rizen.submarines;

import net.minecraft.util.math.BlockPos;
import net.rizen.submarines.api.submarine.SubmarineScreenHandler;
import net.rizen.submarines.block.ManufacturingTableBlock;
import net.rizen.submarines.crafting.DefaultRecipes;
import net.rizen.submarines.entity.TacticalSubmarineEntity;
import net.rizen.submarines.entity.LightweightTorpedoEntity;
import net.rizen.submarines.item.TacticalSubmarineItem;
import net.rizen.submarines.item.LightweightTorpedoItem;
import net.rizen.submarines.item.TransducerItem;
import net.rizen.submarines.item.WarheadItem;
import net.rizen.submarines.item.SteelCasingItem;
import net.rizen.submarines.item.SubmarineRepairToolItem;
import net.rizen.submarines.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("SubmarinesDebug");
	public static final String MOD_ID = "submarines";

	public static final EntityType<TacticalSubmarineEntity> TACTICAL_SUBMARINE_ENTITY = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of(MOD_ID, "tactical_submarine"),
			EntityType.Builder.create((EntityType<TacticalSubmarineEntity> type, World world) -> new TacticalSubmarineEntity(type, world), SpawnGroup.MISC)
					.dimensions(3.0f, 2.75f)
					.maxTrackingRange(64)
					.trackingTickInterval(1)
					.build()
	);

	public static final EntityType<LightweightTorpedoEntity> LIGHTWEIGHT_TORPEDO_ENTITY = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of(MOD_ID, "lightweight_torpedo"),
			EntityType.Builder.<LightweightTorpedoEntity>create((EntityType<LightweightTorpedoEntity> type, World world) -> new LightweightTorpedoEntity(type, world), SpawnGroup.MISC)
					.dimensions(0.5f, 0.5f)
					.maxTrackingRange(128)
					.trackingTickInterval(1)
					.build()
	);

	public static final Item SUBMARINE_ITEM = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "tactical_submarine"),
			new TacticalSubmarineItem(new Item.Settings().maxCount(1))
	);

	public static final Item TORPEDO_ITEM = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "lightweight_torpedo"),
			new LightweightTorpedoItem(new Item.Settings().maxCount(2))
	);

	public static final Item TRANSDUCER_ITEM = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "transducer"),
			new TransducerItem(new Item.Settings().maxCount(16))
	);

	public static final Item WARHEAD_ITEM = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "warhead"),
			new WarheadItem(new Item.Settings().maxCount(16))
	);

	public static final Item STEEL_CASING_ITEM = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "steel_casing"),
			new SteelCasingItem(new Item.Settings().maxCount(64))
	);

	public static final Item SUBMARINE_REPAIR_TOOL = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "submarine_repair_tool"),
			new SubmarineRepairToolItem(new Item.Settings().maxCount(1).maxDamage(200))
	);

	public static final Block MANUFACTURING_TABLE = Registry.register(
			Registries.BLOCK,
			Identifier.of(MOD_ID, "manufacturing_table"),
			new ManufacturingTableBlock(AbstractBlock.Settings.create()
					.strength(3.5f)
					.sounds(BlockSoundGroup.STONE)
					.requiresTool())
	);

	public static final Item MANUFACTURING_TABLE_ITEM = Registry.register(
			Registries.ITEM,
			Identifier.of(MOD_ID, "manufacturing_table"),
			new BlockItem(MANUFACTURING_TABLE, new Item.Settings())
	);

	public static final ScreenHandlerType<SubmarineScreenHandler> SUBMARINE_SCREEN_HANDLER = Registry.register(
			Registries.SCREEN_HANDLER,
			Identifier.of(MOD_ID, "submarine_inventory"),
			new ScreenHandlerType<>(SubmarineScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
	);

	public static final ScreenHandlerType<net.rizen.submarines.screen.ManufacturingTableScreenHandler> MANUFACTURING_TABLE_SCREEN_HANDLER = Registry.register(
			Registries.SCREEN_HANDLER,
			Identifier.of(MOD_ID, "manufacturing_table"),
			new ExtendedScreenHandlerType<>(
				net.rizen.submarines.screen.ManufacturingTableScreenHandler::new,
				BlockPos.PACKET_CODEC
			)
	);

	public static final SoundEvent TORPEDO_UNABLE_TO_FIRE = Registry.register(
			Registries.SOUND_EVENT,
			Identifier.of(MOD_ID, "torpedo_unable_to_fire"),
			SoundEvent.of(Identifier.of(MOD_ID, "torpedo_unable_to_fire"))
	);

	public static final SoundEvent SONAR_PING = Registry.register(
			Registries.SOUND_EVENT,
			Identifier.of(MOD_ID, "sonar_ping"),
			SoundEvent.of(Identifier.of(MOD_ID, "sonar_ping"))
	);

	public static final SoundEvent TORPEDO_FIRE = Registry.register(
			Registries.SOUND_EVENT,
			Identifier.of(MOD_ID, "torpedo_fire"),
			SoundEvent.of(Identifier.of(MOD_ID, "torpedo_fire"))
	);

	public static final SoundEvent SUBMARINE_MOTOR = Registry.register(
			Registries.SOUND_EVENT,
			Identifier.of(MOD_ID, "submarine_motor"),
			SoundEvent.of(Identifier.of(MOD_ID, "submarine_motor"))
	);

	public static final SoundEvent SUBMARINE_REPAIRED = Registry.register(
			Registries.SOUND_EVENT,
			Identifier.of(MOD_ID, "submarine_repaired"),
			SoundEvent.of(Identifier.of(MOD_ID, "submarine_repaired"))
	);

	public static final RegistryKey<ItemGroup> SUBMARINES_GROUP = RegistryKey.of(
			RegistryKeys.ITEM_GROUP,
			Identifier.of(MOD_ID, "submarines")
	);

	public static final ItemGroup SUBMARINES_ITEM_GROUP = Registry.register(
			Registries.ITEM_GROUP,
			SUBMARINES_GROUP,
			FabricItemGroup.builder()
					.icon(() -> new ItemStack(SUBMARINE_ITEM))
					.displayName(Text.translatable("itemGroup.submarines"))
					.entries((context, entries) -> {
						entries.add(SUBMARINE_ITEM);
						entries.add(TORPEDO_ITEM);
						entries.add(SUBMARINE_REPAIR_TOOL);
						entries.add(TRANSDUCER_ITEM);
						entries.add(WARHEAD_ITEM);
						entries.add(STEEL_CASING_ITEM);
						entries.add(MANUFACTURING_TABLE_ITEM);
					})
					.build()
	);

	@Override
	public void onInitialize() {
		NetworkHandler.registerPackets();
		DefaultRecipes.register();
	}
}