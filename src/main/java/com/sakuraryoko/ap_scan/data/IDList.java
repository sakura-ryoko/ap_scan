package com.sakuraryoko.ap_scan.data;

import java.util.HashMap;
import java.util.List;
import com.google.common.collect.ImmutableList;

public class IDList
{
	public static List<String> ENTITY_ID_LIST = ImmutableList.of(
			"minecraft:armor_stand",
			"minecraft:block_display",
			"minecraft:glow_item_frame",
			"minecraft:item",
			"minecraft:item_display",
			"minecraft:item_frame",

			"minecraft:chest_minecart",
			"minecraft:hopper_minecart",

			"minecraft:allay",
			"minecraft:donkey",
			"minecraft:llama",
			"minecraft:mule",
			"minecraft:piglin",
			"minecraft:pillager",
			"minecraft:trader_llama",
			"minecraft:villager",
			"minecraft:wandering_trader",
			"minecraft:player",

			"minecraft:acacia_chest_boat",
			"minecraft:bamboo_chest_raft",
			"minecraft:birch_chest_boat",
			"minecraft:cherry_chest_boat",
			"minecraft:dark_oak_chest_boat",
			"minecraft:jungle_chest_boat",
			"minecraft:mangrove_chest_boat",
			"minecraft:oak_chest_boat",
			"minecraft:pale_oak_chest_boat",
			"minecraft:spruce_chest_boat"
	);

	public static List<String> TILE_ID_LIST = ImmutableList.of(
			"minecraft:jukebox",
			"minecraft:chest",
			"minecraft:shulker_box",
			"minecraft:dispenser",
			"minecraft:dropper",
			"minecraft:hopper",
			"minecraft:barrel",
			"minecraft:trapped_chest",
			"minecraft:decorated_pot",

			"minecraft:crafter",
			"minecraft:furnace",
			"minecraft:smoker",
			"minecraft:blast_furnace"
	);

	public static List<String> RECORD_ID_LIST = ImmutableList.of(
			"minecraft:music_disc_13",
			"minecraft:music_disc_cat",
			"minecraft:music_disc_blocks",
			"minecraft:music_disc_chirp",
			"minecraft:music_disc_creator",
			"minecraft:music_disc_creator_music_box",
			"minecraft:music_disc_far",
			"minecraft:music_disc_lava_chicken",
			"minecraft:music_disc_mall",
			"minecraft:music_disc_mellohi",
			"minecraft:music_disc_stal",
			"minecraft:music_disc_strad",
			"minecraft:music_disc_ward",
			"minecraft:music_disc_11",
			"minecraft:music_disc_wait",
			"minecraft:music_disc_otherside",
			"minecraft:music_disc_relic",
			"minecraft:music_disc_5",
			"minecraft:music_disc_pigstep",
			"minecraft:music_disc_precipice",
			"minecraft:music_disc_tears"
	);

	public static HashMap<String, String> ID_OVERRIDES = new HashMap<>();

	static
	{
		ID_OVERRIDES.put("minecraft:grass", "minecraft:short_grass");
		ID_OVERRIDES.put("minecraft:scute", "minecraft:armadillo_scute");
	}
}
