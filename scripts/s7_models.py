"""
s7_models.py
Generate item model JSON files, block model JSON files, and blockstate JSON files
for the Thaumcraft 4 -> 1.12.2 port.

Re-run safely: existing files are never overwritten (unless --force is passed).
"""

import os
import re
import sys
import json

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

CONFIG_ITEMS  = os.path.join(PROJECT_ROOT, "src", "main", "java",
                             "thaumcraft", "common", "config", "ConfigItems.java")
CONFIG_BLOCKS = os.path.join(PROJECT_ROOT, "src", "main", "java",
                             "thaumcraft", "common", "config", "ConfigBlocks.java")

TEXTURES_ITEMS  = os.path.join(PROJECT_ROOT, "src", "main", "resources",
                                "assets", "thaumcraft", "textures", "items")
TEXTURES_BLOCKS = os.path.join(PROJECT_ROOT, "src", "main", "resources",
                                "assets", "thaumcraft", "textures", "blocks")

MODELS_DIR      = os.path.join(PROJECT_ROOT, "src", "main", "resources",
                                "assets", "thaumcraft", "models")
MODELS_ITEM_DIR = os.path.join(MODELS_DIR, "item")
MODELS_BLOCK_DIR= os.path.join(MODELS_DIR, "block")
BLOCKSTATES_DIR = os.path.join(PROJECT_ROOT, "src", "main", "resources",
                                "assets", "thaumcraft", "blockstates")

FORCE = "--force" in sys.argv

# ---------------------------------------------------------------------------
# Manual overrides: registry name -> texture name (no extension)
# These win over all heuristics.
# ---------------------------------------------------------------------------
ITEM_OVERRIDES: dict[str, str] = {
    # Wand parts
    "WandCasting":               "wand_rod_greatwood",   # placeholder – first available rod
    "WandCap":                   "wand_cap_iron",
    "WandRod":                   "wand_rod_greatwood",
    # Misc items
    "ItemManaBean":              "mana_bean",
    "ItemResource":              "thaumiumingot",         # generic resource sheet
    "ItemThaumometer":           "thaumiumingot",         # no dedicated texture
    "ItemGoggles":               "gogglesrevealing",
    # Thaumium armour
    "ItemHelmetThaumium":        "thaumiumhelm",
    "ItemChestplateThaumium":    "thaumiumchest",
    "ItemLeggingsThaumium":      "thaumiumlegs",
    "ItemBootsThaumium":         "thaumiumboots",
    # Thaumium tools
    "ItemShovelThaumium":        "thaumiumshovel",
    "ItemPickThaumium":          "thaumiumpick",
    "ItemAxeThaumium":           "thaumiumaxe",
    "ItemSwordThaumium":         "thaumiumsword",
    "ItemHoeThaumium":           "thaumiumhoe",
    # Nuggets (generic)
    "ItemNugget":                "nuggetthaumium",
    # Boots of the Traveller
    "BootsTraveller":            "bootstraveler",
    # Food
    "TripleMeatTreat":           "tripletreat",
    # Elemental tools
    "ItemSwordElemental":        "elementalsword",
    "ItemShovelElemental":       "elementalshovel",
    "ItemPickaxeElemental":      "elementalpick",
    "ItemAxeElemental":          "elementalaxe",
    "ItemHoeElemental":          "elementalhoe",
    # Robes
    "ItemChestplateRobe":        "clothchest",
    "ItemLeggingsRobe":          "clothlegs",
    "ItemBootsRobe":             "clothboots",
    # Mirror
    "HandMirror":                "mirrorhand",
    # Jar items (use item version of the jar)
    "BlockJarFilledItem":        "phial",
    "BlockJarNodeItem":          "phial",
    # Golem items
    "TrunkSpawner":              "ob_placer",
    "ItemGolemPlacer":           "ob_placer",
    "ItemGolemCore":             "golem_core_empty",
    "ItemGolemUpgrade":          "golem_upgrade_empty",
    "GolemBell":                 "ironbell",
    "ItemGolemDecoration":       "golemdecoarmor",
    # Bow
    "ItemBowBone":               "bonebow",
    # Arrows
    "PrimalArrow":               "el_arrow_fire",
    # Runic baubles
    "ItemAmuletRunic":           "runic_amulet",
    "ItemRingRunic":             "runic_ring",
    "ItemGirdleRunic":           "runic_girdle",
    "ItemAmuletVis":             "vis_amulet",
    "ItemGirdleHover":           "hovergirdle",
    # Misc
    "ItemSpawnerEgg":            "blank",
    "ItemBathSalts":             "bath_salts",
    "ItemBucketDeath":           "bucket_death",
    "ItemBucketPure":            "bucket_pure",
    # Fortress armour
    "ItemHelmetFortress":        "thaumiumfortresshelm",
    "ItemChestplateFortress":    "thaumiumfortresschest",
    "ItemLeggingsFortress":      "thaumiumfortresslegs",
    # Eldritch items
    "ItemEldritchObject":        "eldritch_object",
    # Void armour
    "ItemHelmetVoid":            "voidhelm",
    "ItemChestplateVoid":        "voidchest",
    "ItemLeggingsVoid":          "voidlegs",
    "ItemBootsVoid":             "voidboots",
    # Void tools
    "ItemShovelVoid":            "voidshovel",
    "ItemPickVoid":              "voidpick",
    "ItemAxeVoid":               "voidaxe",
    "ItemSwordVoid":             "voidsword",
    "ItemHoeVoid":               "voidhoe",
    # Void fortress
    "ItemHelmetVoidFortress":    "voidhelm",
    "ItemChestplateVoidFortress":"voidchest",
    "ItemLeggingsVoidFortress":  "voidlegs",
    # Taint
    "ItemBottleTaint":           "bottle_taint",
    # Cultist robes
    "ItemHelmetCultistRobe":     "cultistrobehelm",
    "ItemChestplateCultistRobe": "cultistrobechest",
    "ItemLeggingsCultistRobe":   "cultistrobelegs",
    "ItemBootsCultist":          "cultistboots",
    # Cultist plate
    "ItemHelmetCultistPlate":    "cultistplatehelm",
    "ItemChestplateCultistPlate":"cultistplatechest",
    "ItemLeggingsCultistPlate":  "cultistplatelegs",
    # Cultist leader plate
    "ItemHelmetCultistLeaderPlate":    "cultistplateleaderhelm",
    "ItemChestplateCultistLeaderPlate":"cultistplateleaderchest",
    "ItemLeggingsCultistLeaderPlate":  "cultistplateleaderlegs",
    # Crimson
    "ItemSwordCrimson":          "crimson_blade",
    # Compass
    "ItemCompassStone":          "sinister_stone",
    # Crusher
    "ItemPrimalCrusher":         "primal_crusher",
}

BLOCK_OVERRIDES: dict[str, str] = {
    "blockCustomOre":           "amberore",
    "blockCustomPlant":         "shimmerleaf",
    "blockMagicalLog":          "greatwoodside",
    "blockMagicalLeaves":       "greatwoodleaves",
    "blockArcaneFurnace":       "furnace0",
    "blockMetalDevice":         "metalbase",
    "blockAlchemyFurnace":      "al_furnace_side",
    "blockTable":               "tablequill",
    "blockChestHungry":         "crate_side_0",
    "blockJar":                 "jar_side",
    "blockArcaneDoor":          "adoorbot",
    "blockWoodenDevice":        "woodplain",
    "blockLifter":              "lifterside",
    "blockAiry":                "blank",
    "blockCosmeticOpaque":      "arcane_stone",
    "blockCosmeticSolid":       "obsidiantile",
    "blockMirror":              "mirrorframe",
    "blockTaint":               "taint_crust",
    "blockTaintFibres":         "taint_fibres",
    "blockStoneDevice":         "arcane_stone",
    "blockManaPod":             "manapod_stem_0",
    "blockTube":                "pipe_1",
    "blockWarded":              "wardedstone",
    "blockMagicBox":            "brainbox",
    "blockEldritch":            "obsidiantile",
    "blockPortalEldritch":      "animatedglow",
    "blockPortalNothing":       "blank",
    "blockStairsArcaneStone":   "arcane_stone",
    "blockStairsGreatwood":     "planks_greatwood",
    "blockStairsSilverwood":    "planks_silverwood",
    "blockStairsEldritch":      "obsidiantile",
    "blockCosmeticSlabWood":    "planks_greatwood",
    "blockCosmeticSlabStone":   "arcane_stone",
    "blockCosmeticDoubleSlabWood":  "planks_greatwood",
    "blockCosmeticDoubleSlabStone": "arcane_stone",
    "blockLootUrn":             "urn_side_0",
    "blockLootCrate":           "crate_side_0",
    "blockHole":                "blank",
}

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
REGISTRY_RE = re.compile(r'setRegistryName\s*\(\s*"thaumcraft"\s*,\s*"([^"]+)"\s*\)')

HANDHELD_KEYWORDS = {
    "tool", "sword", "pick", "axe", "shovel", "hoe", "wand", "rod", "cap",
    "bow", "arrow", "spade", "staff", "blade", "knife", "dagger",
    "hammer", "focus", "crusher", "resonator", "key",
}


def extract_registry_names(java_file: str) -> list[str]:
    with open(java_file, encoding="utf-8") as f:
        text = f.read()
    return REGISTRY_RE.findall(text)


def load_textures(directory: str) -> list[str]:
    """Return sorted list of texture names (no extension, no .mcmeta)."""
    if not os.path.isdir(directory):
        return []
    return sorted(
        os.path.splitext(fn)[0]
        for fn in os.listdir(directory)
        if fn.lower().endswith(".png") and not fn.lower().endswith(".png.mcmeta")
    )


def camel_to_tokens(name: str) -> list[str]:
    """Split CamelCase/PascalCase into lowercase tokens. 'ItemBowBone' -> ['item','bow','bone']"""
    tokens = re.findall(r'[A-Z]?[a-z]+|[A-Z]+(?=[A-Z]|$)', name)
    return [t.lower() for t in tokens]


def tex_tokens(tex: str) -> list[str]:
    """Split underscore-separated texture name into tokens. 'wand_cap_iron' -> ['wand','cap','iron']"""
    return [t.lower() for t in tex.split("_") if t]


def token_overlap(name_tokens: list[str], tex_tok: list[str]) -> int:
    """Count how many tokens from tex appear in name_tokens."""
    name_set = set(name_tokens)
    return sum(1 for t in tex_tok if t in name_set)


def best_texture_match(name: str, textures: list[str]) -> str | None:
    """
    Try to find the best texture for a registry NAME from textures list.
    Returns texture name (no extension) or None if nothing found.
    """
    low = name.lower()
    tex_set = set(textures)

    # 1. Exact match (case-insensitive)
    if low in tex_set:
        return low

    # 2. Strip "Item"/"item"/"block"/"Block" prefix once
    for prefix in ("item", "block"):
        if low.startswith(prefix):
            stripped = low[len(prefix):]
            if stripped in tex_set:
                return stripped

    # 3. Concatenated name contains texture or vice versa (simple substring)
    #    Prefer longer textures (more specific) and filter out very short ones.
    candidates_sub = []
    for tex in textures:
        if len(tex) < 4:
            continue
        if tex in low or low in tex:
            candidates_sub.append(tex)
    if len(candidates_sub) == 1:
        return candidates_sub[0]
    if len(candidates_sub) > 1:
        candidates_sub.sort(key=len, reverse=True)
        return candidates_sub[0]

    # 4. Token-overlap scoring
    name_tokens = camel_to_tokens(name)
    scored = []
    for tex in textures:
        ttok = tex_tokens(tex)
        score = token_overlap(name_tokens, ttok)
        if score > 0:
            scored.append((score, len(tex), tex))
    if scored:
        scored.sort(key=lambda x: (x[0], x[1]), reverse=True)
        best_score = scored[0][0]
        # Only accept if at least 2 tokens match OR all tex tokens match
        best_tex = scored[0][2]
        best_ttok = tex_tokens(best_tex)
        if best_score >= 2 or (best_score == len(best_ttok) and best_score >= 1):
            return best_tex

    return None


def is_handheld(name: str) -> bool:
    low = name.lower()
    return any(kw in low for kw in HANDHELD_KEYWORDS)


def write_json(path: str, data: dict, force: bool = False) -> bool:
    """Write JSON file. Returns True if written, False if already existed (and not forced)."""
    if os.path.exists(path) and not force:
        return False
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    return True


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    item_names  = extract_registry_names(CONFIG_ITEMS)
    block_names = extract_registry_names(CONFIG_BLOCKS)

    item_textures  = load_textures(TEXTURES_ITEMS)
    block_textures = load_textures(TEXTURES_BLOCKS)

    print(f"Found {len(item_names)} items, {len(block_names)} blocks")
    print(f"Found {len(item_textures)} item textures, {len(block_textures)} block textures")
    print()

    # -----------------------------------------------------------------------
    # Pre-scan existing models directory
    # -----------------------------------------------------------------------
    existing_item_models  = set()
    existing_block_models = set()
    existing_blockstates  = set()

    if os.path.isdir(MODELS_ITEM_DIR):
        existing_item_models = {
            os.path.splitext(fn)[0]
            for fn in os.listdir(MODELS_ITEM_DIR)
            if fn.endswith(".json")
        }
    if os.path.isdir(MODELS_BLOCK_DIR):
        existing_block_models = {
            os.path.splitext(fn)[0]
            for fn in os.listdir(MODELS_BLOCK_DIR)
            if fn.endswith(".json")
        }
    if os.path.isdir(BLOCKSTATES_DIR):
        existing_blockstates = {
            os.path.splitext(fn)[0]
            for fn in os.listdir(BLOCKSTATES_DIR)
            if fn.endswith(".json")
        }

    print(f"Pre-existing item models:   {len(existing_item_models)}")
    print(f"Pre-existing block models:  {len(existing_block_models)}")
    print(f"Pre-existing blockstates:   {len(existing_blockstates)}")
    print()

    # -----------------------------------------------------------------------
    # Process items
    # -----------------------------------------------------------------------
    matched_items  = []
    fallback_items = []
    skipped_items  = []

    for name in item_names:
        out_path = os.path.join(MODELS_ITEM_DIR, f"{name}.json")

        if name in existing_item_models and not FORCE:
            skipped_items.append(name)
            continue

        if name in ITEM_OVERRIDES:
            tex = ITEM_OVERRIDES[name]
            matched_items.append((name, tex, "override"))
        else:
            tex = best_texture_match(name, item_textures)
            if tex is None:
                tex = "blank"
                fallback_items.append(name)
            else:
                matched_items.append((name, tex, "heuristic"))

        parent = "item/handheld" if is_handheld(name) else "item/generated"
        data = {
            "parent": parent,
            "textures": {
                "layer0": f"thaumcraft:items/{tex}"
            }
        }
        write_json(out_path, data, force=FORCE)

    # -----------------------------------------------------------------------
    # Process blocks
    # -----------------------------------------------------------------------
    matched_blocks  = []
    fallback_blocks = []
    skipped_bstates = []
    skipped_bmodels = []

    for name in block_names:
        bs_path    = os.path.join(BLOCKSTATES_DIR,  f"{name}.json")
        model_path = os.path.join(MODELS_BLOCK_DIR, f"{name}.json")

        # --- blockstate ---
        if name in existing_blockstates and not FORCE:
            skipped_bstates.append(name)
        else:
            bs_data = {
                "variants": {
                    "normal": {"model": f"thaumcraft:{name}"}
                }
            }
            write_json(bs_path, bs_data, force=FORCE)

        # --- block model ---
        if name in existing_block_models and not FORCE:
            skipped_bmodels.append(name)
            continue

        if name in BLOCK_OVERRIDES:
            tex = BLOCK_OVERRIDES[name]
            matched_blocks.append((name, tex, "override"))
        else:
            tex = best_texture_match(name, block_textures)
            if tex is None:
                tex = "blank"
                fallback_blocks.append(name)
            else:
                matched_blocks.append((name, tex, "heuristic"))

        model_data = {
            "parent": "block/cube_all",
            "textures": {
                "all": f"thaumcraft:blocks/{tex}"
            }
        }
        write_json(model_path, model_data, force=FORCE)

    # -----------------------------------------------------------------------
    # Summary
    # -----------------------------------------------------------------------
    print("=" * 60)
    print("ITEM MODELS")
    print("=" * 60)
    print(f"  Written with texture match : {len(matched_items)}")
    print(f"  Written with 'blank' fallback: {len(fallback_items)}")
    print(f"  Skipped (already existed)  : {len(skipped_items)}")
    print()

    if matched_items:
        print("  Matched items:")
        for item_name, tex, how in matched_items:
            print(f"    {item_name:45s} -> {tex}  [{how}]")
    print()

    if fallback_items:
        print("  Fallback (blank) items:")
        for item_name in fallback_items:
            print(f"    {item_name}")
    print()

    print("=" * 60)
    print("BLOCK MODELS / BLOCKSTATES")
    print("=" * 60)
    print(f"  Block models with texture match : {len(matched_blocks)}")
    print(f"  Block models with 'blank' fallback: {len(fallback_blocks)}")
    print(f"  Block models skipped (existed)  : {len(skipped_bmodels)}")
    print(f"  Blockstates skipped (existed)   : {len(skipped_bstates)}")
    print()

    if matched_blocks:
        print("  Matched blocks:")
        for block_name, tex, how in matched_blocks:
            print(f"    {block_name:45s} -> {tex}  [{how}]")
    print()

    if fallback_blocks:
        print("  Fallback (blank) blocks:")
        for block_name in fallback_blocks:
            print(f"    {block_name}")
    print()

    print("Done.")


if __name__ == "__main__":
    main()
