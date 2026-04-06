#!/usr/bin/env python3
"""
s8_register_models.py
Outputs Java source code for a ModelRegistryEvent handler that registers all
Thaumcraft items with ModelLoader.setCustomModelResourceLocation.

Usage:
    python scripts/s8_register_models.py

Paste the printed output into ClientProxy.java as a @SubscribeEvent method.
"""

# Each entry is:
#   (field_expr, registry_name, list_of_meta_or_None, parent)
# parent: "item/generated" or "item/handheld" (only relevant for JSON, not the registration line)
#
# For single-meta items, metas = [0]
# For multi-meta items, metas = [list of valid meta values]

ITEMS = [
    # --- foci (single) ---
    ("ConfigItems.itemWandCasting",    "WandCasting",             [0]),
    ("ConfigItems.itemFocusPouch",     "FocusPouch",              [0]),
    ("ConfigItems.itemFocusFire",      "FocusFire",               [0]),
    ("ConfigItems.itemFocusShock",     "FocusShock",              [0]),
    ("ConfigItems.itemFocusHellbat",   "FocusHellbat",            [0]),
    ("ConfigItems.itemFocusFrost",     "FocusFrost",              [0]),
    ("ConfigItems.itemFocusTrade",     "FocusTrade",              [0]),
    ("ConfigItems.itemFocusExcavation","FocusExcavation",         [0]),
    ("ConfigItems.itemFocusPortableHole","FocusPortableHole",     [0]),
    ("ConfigItems.itemFocusPech",      "FocusPech",               [0]),
    ("ConfigItems.itemFocusWarding",   "FocusWarding",            [0]),
    ("ConfigItems.itemFocusPrimal",    "FocusPrimal",             [0]),

    # --- ItemEssence (2 meta) ---
    ("ConfigItems.itemEssence",        "ItemEssence",             [0, 1]),

    # --- ItemManaBean (single) ---
    ("ConfigItems.itemManaBean",       "ItemManaBean",            [0]),

    # --- ItemWispEssence (single) ---
    ("ConfigItems.itemWispEssence",    "ItemWispEssence",         [0]),

    # --- ItemResource (meta 0-18) ---
    ("ConfigItems.itemResource",       "ItemResource",
     [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]),

    # --- ItemShard (meta 0-6) ---
    ("ConfigItems.itemShard",          "ItemShard",               [0, 1, 2, 3, 4, 5, 6]),

    # --- ItemResearchNotes (practical metas: 0, 24, 42, 64) ---
    ("ConfigItems.itemResearchNotes",  "ItemResearchNotes",       [0, 24, 42, 64]),

    # --- ItemInkwell (single) ---
    ("ConfigItems.itemInkwell",        "ItemInkwell",             [0]),

    # --- ItemThaumonomicon (meta 0 and 42) ---
    ("ConfigItems.itemThaumonomicon",  "ItemThaumonomicon",       [0, 42]),

    # --- ItemThaumometer (single) ---
    ("ConfigItems.itemThaumometer",    "ItemThaumometer",         [0]),

    # --- ItemGoggles (single) ---
    ("ConfigItems.itemGoggles",        "ItemGoggles",             [0]),

    # --- thaumium armor (all single) ---
    ("ConfigItems.itemHelmetThaumium",    "ItemHelmetThaumium",     [0]),
    ("ConfigItems.itemChestThaumium",     "ItemChestplateThaumium", [0]),
    ("ConfigItems.itemLegsThaumium",      "ItemLeggingsThaumium",   [0]),
    ("ConfigItems.itemBootsThaumium",     "ItemBootsThaumium",      [0]),

    # --- thaumium tools (all single) ---
    ("ConfigItems.itemShovelThaumium",    "ItemShovelThaumium",     [0]),
    ("ConfigItems.itemPickThaumium",      "ItemPickThaumium",       [0]),
    ("ConfigItems.itemAxeThaumium",       "ItemAxeThaumium",        [0]),
    ("ConfigItems.itemSwordThaumium",     "ItemSwordThaumium",      [0]),
    ("ConfigItems.itemHoeThaumium",       "ItemHoeThaumium",        [0]),

    # --- ItemArcaneDoor (single, it's an ItemBlock) ---
    ("ConfigItems.itemArcaneDoor",        "ItemArcaneDoor",         [0]),

    # --- ItemNugget (multi: 0-7 partial, 16-21, 31) ---
    ("ConfigItems.itemNugget",            "ItemNugget",
     [0, 1, 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 20, 21, 31]),

    # --- BootsTraveller (single) ---
    ("ConfigItems.itemBootsTraveller",    "BootsTraveller",         [0]),

    # --- edible nuggets (single each) ---
    ("ConfigItems.itemNuggetChicken",     "ItemNuggetChicken",      [0]),
    ("ConfigItems.itemNuggetBeef",        "ItemNuggetBeef",         [0]),
    ("ConfigItems.itemNuggetPork",        "ItemNuggetPork",         [0]),
    ("ConfigItems.itemNuggetFish",        "ItemNuggetFish",         [0]),
    ("ConfigItems.itemTripleMeatTreat",   "TripleMeatTreat",        [0]),

    # --- elemental tools (single each) ---
    ("ConfigItems.itemSwordElemental",    "ItemSwordElemental",     [0]),
    ("ConfigItems.itemShovelElemental",   "ItemShovelElemental",    [0]),
    ("ConfigItems.itemPickElemental",     "ItemPickaxeElemental",   [0]),
    ("ConfigItems.itemAxeElemental",      "ItemAxeElemental",       [0]),
    ("ConfigItems.itemHoeElemental",      "ItemHoeElemental",       [0]),

    # --- robe armor (single each) ---
    ("ConfigItems.itemChestRobe",         "ItemChestplateRobe",     [0]),
    ("ConfigItems.itemLegsRobe",          "ItemLeggingsRobe",       [0]),
    ("ConfigItems.itemBootsRobe",         "ItemBootsRobe",          [0]),

    # --- ArcaneDoorKey (multi: 0, 1) ---
    ("ConfigItems.itemKey",               "ArcaneDoorKey",          [0, 1]),

    # --- HandMirror (single) ---
    ("ConfigItems.itemHandMirror",        "HandMirror",             [0]),

    # --- HoverHarness (single) ---
    ("ConfigItems.itemHoverHarness",      "HoverHarness",           [0]),

    # --- jar items (single each) ---
    ("ConfigItems.itemJarFilled",         "BlockJarFilledItem",     [0]),
    ("ConfigItems.itemJarNode",           "BlockJarNodeItem",       [0]),

    # --- TrunkSpawner (single) ---
    ("ConfigItems.itemTrunkSpawner",      "TrunkSpawner",           [0]),

    # --- golem items ---
    ("ConfigItems.itemGolemPlacer",       "ItemGolemPlacer",        [0, 1, 2, 3, 4, 5, 6, 7]),
    ("ConfigItems.itemGolemCore",         "ItemGolemCore",
     [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 100]),
    ("ConfigItems.itemGolemUpgrade",      "ItemGolemUpgrade",       [0, 1, 2, 3, 4, 5]),
    ("ConfigItems.itemGolemBell",         "GolemBell",              [0]),
    ("ConfigItems.itemGolemDecoration",   "ItemGolemDecoration",    [0, 1, 2, 3, 4, 5, 6, 7]),

    # --- ItemBowBone (single) ---
    ("ConfigItems.itemBowBone",           "ItemBowBone",            [0]),

    # --- PrimalArrow (multi: 0-5) ---
    ("ConfigItems.itemPrimalArrow",       "PrimalArrow",            [0, 1, 2, 3, 4, 5]),

    # --- ItemResonator (single) ---
    ("ConfigItems.itemResonator",         "ItemResonator",          [0]),

    # --- baubles ---
    ("ConfigItems.itemBaubleBlanks",      "ItemBaubleBlanks",       [0, 1, 2, 3, 4, 5, 6, 7, 8]),
    ("ConfigItems.itemAmuletRunic",       "ItemAmuletRunic",        [0, 1]),
    ("ConfigItems.itemRingRunic",         "ItemRingRunic",          [0, 1, 2, 3]),
    ("ConfigItems.itemGirdleRunic",       "ItemGirdleRunic",        [0, 1]),
    ("ConfigItems.itemAmuletVis",         "ItemAmuletVis",          [0, 1]),
    ("ConfigItems.itemGirdleHover",       "ItemGirdleHover",        [0]),

    # --- ItemSpawnerEgg (single) ---
    ("ConfigItems.itemSpawnerEgg",        "ItemSpawnerEgg",         [0]),

    # --- ItemZombieBrain (single) ---
    ("ConfigItems.itemZombieBrain",       "ItemZombieBrain",        [0]),

    # --- ItemBathSalts (single) ---
    ("ConfigItems.itemBathSalts",         "ItemBathSalts",          [0]),

    # --- ItemCrystalEssence (single) ---
    ("ConfigItems.itemCrystalEssence",    "ItemCrystalEssence",     [0]),

    # --- buckets (single each) ---
    ("ConfigItems.itemBucketDeath",       "ItemBucketDeath",        [0]),
    ("ConfigItems.itemBucketPure",        "ItemBucketPure",         [0]),

    # --- fortress armor (single each) ---
    ("ConfigItems.itemHelmetFortress",    "ItemHelmetFortress",     [0]),
    ("ConfigItems.itemChestFortress",     "ItemChestplateFortress", [0]),
    ("ConfigItems.itemLegsFortress",      "ItemLeggingsFortress",   [0]),

    # --- ItemEldritchObject (multi: 0-4) ---
    ("ConfigItems.itemEldritchObject",    "ItemEldritchObject",     [0, 1, 2, 3, 4]),

    # --- void armor (single each) ---
    ("ConfigItems.itemHelmetVoid",        "ItemHelmetVoid",         [0]),
    ("ConfigItems.itemChestVoid",         "ItemChestplateVoid",     [0]),
    ("ConfigItems.itemLegsVoid",          "ItemLeggingsVoid",       [0]),
    ("ConfigItems.itemBootsVoid",         "ItemBootsVoid",          [0]),

    # --- void tools (single each) ---
    ("ConfigItems.itemShovelVoid",        "ItemShovelVoid",         [0]),
    ("ConfigItems.itemPickVoid",          "ItemPickVoid",           [0]),
    ("ConfigItems.itemAxeVoid",           "ItemAxeVoid",            [0]),
    ("ConfigItems.itemSwordVoid",         "ItemSwordVoid",          [0]),
    ("ConfigItems.itemHoeVoid",           "ItemHoeVoid",            [0]),

    # --- void robe (single each) ---
    ("ConfigItems.itemHelmetVoidRobe",    "ItemHelmetVoidFortress",      [0]),
    ("ConfigItems.itemChestVoidRobe",     "ItemChestplateVoidFortress",  [0]),
    ("ConfigItems.itemLegsVoidRobe",      "ItemLeggingsVoidFortress",    [0]),

    # --- sanity / soap / bottle (single each) ---
    ("ConfigItems.itemSanitySoap",        "ItemSanitySoap",         [0]),
    ("ConfigItems.itemSanityChecker",     "ItemSanityChecker",      [0]),
    ("ConfigItems.itemBottleTaint",       "ItemBottleTaint",        [0]),

    # --- cultist armor (single each) ---
    ("ConfigItems.itemHelmetCultistRobe",  "ItemHelmetCultistRobe",         [0]),
    ("ConfigItems.itemChestCultistRobe",   "ItemChestplateCultistRobe",     [0]),
    ("ConfigItems.itemLegsCultistRobe",    "ItemLeggingsCultistRobe",       [0]),
    ("ConfigItems.itemBootsCultist",       "ItemBootsCultist",              [0]),
    ("ConfigItems.itemHelmetCultistPlate", "ItemHelmetCultistPlate",        [0]),
    ("ConfigItems.itemChestCultistPlate",  "ItemChestplateCultistPlate",    [0]),
    ("ConfigItems.itemLegsCultistPlate",   "ItemLeggingsCultistPlate",      [0]),
    ("ConfigItems.itemHelmetCultistLeaderPlate","ItemHelmetCultistLeaderPlate",   [0]),
    ("ConfigItems.itemChestCultistLeaderPlate", "ItemChestplateCultistLeaderPlate",[0]),
    ("ConfigItems.itemLegsCultistLeaderPlate",  "ItemLeggingsCultistLeaderPlate", [0]),

    # --- ItemSwordCrimson (single) ---
    ("ConfigItems.itemSwordCrimson",      "ItemSwordCrimson",       [0]),

    # --- ItemLootBag (multi: 0-2) ---
    ("ConfigItems.itemLootbag",           "ItemLootBag",            [0, 1, 2]),

    # --- ItemCompassStone (single + dummy meta 1 for sinister_active; only 0 in creative tab) ---
    ("ConfigItems.itemCompassStone",      "ItemCompassStone",       [0, 1]),

    # --- ItemPrimalCrusher (single) ---
    ("ConfigItems.itemPrimalCrusher",     "ItemPrimalCrusher",      [0]),

    # --- WandCap (multi: 0-8) ---
    ("ConfigItems.itemWandCap",           "WandCap",                [0, 1, 2, 3, 4, 5, 6, 7, 8]),

    # --- WandRod (wands 0-7, staves 50-57, primal staff 100) ---
    ("ConfigItems.itemWandRod",           "WandRod",
     [0, 1, 2, 3, 4, 5, 6, 7, 50, 51, 52, 53, 54, 55, 56, 57, 100]),
]


def main():
    lines = []
    lines.append("    @SubscribeEvent")
    lines.append("    @SideOnly(Side.CLIENT)")
    lines.append("    public void registerModels(ModelRegistryEvent event) {")

    for field, reg_name, metas in ITEMS:
        if len(metas) == 1 and metas[0] == 0:
            # single, clean line
            lines.append(
                f'        ModelLoader.setCustomModelResourceLocation({field}, 0, '
                f'new ModelResourceLocation("thaumcraft:{reg_name}", "inventory"));'
            )
        else:
            # multiple metas: emit one line per meta
            for m in metas:
                lines.append(
                    f'        ModelLoader.setCustomModelResourceLocation({field}, {m}, '
                    f'new ModelResourceLocation("thaumcraft:{reg_name}_{m}", "inventory"));'
                )

    lines.append("    }")

    print("\n".join(lines))


if __name__ == "__main__":
    main()
