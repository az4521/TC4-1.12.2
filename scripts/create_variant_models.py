"""Create blockstate variants and models for all multi-variant Thaumcraft blocks."""
import json, os

base = "src/main/resources/assets/thaumcraft"

def write_json(path, data):
    fp = os.path.join(base, path)
    os.makedirs(os.path.dirname(fp), exist_ok=True)
    with open(fp, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"  {path}")

print("=== BlockCosmeticOpaque (3 variants) ===")
variants = {
    "variant=amber":      {"model": "thaumcraft:blockcosmeticopaque_amber"},
    "variant=amberbrick":  {"model": "thaumcraft:blockcosmeticopaque_amberbrick"},
    "variant=wardedglass": {"model": "thaumcraft:blockcosmeticopaque_wardedglass"},
}
write_json("blockstates/blockcosmeticopaque.json", {"variants": variants})
write_json("models/block/blockcosmeticopaque_amber.json", {"parent":"block/cube_all","textures":{"all":"thaumcraft:blocks/amberblock"}})
write_json("models/block/blockcosmeticopaque_amberbrick.json", {"parent":"block/cube_all","textures":{"all":"thaumcraft:blocks/amberbrick"}})
write_json("models/block/blockcosmeticopaque_wardedglass.json", {"parent":"block/cube_all","textures":{"all":"thaumcraft:blocks/wardedglass"}})

print("=== BlockCosmeticSolid (14 variants) ===")
solid_variants = {
    0: ("obsidiantotem", "obsidiantotem1"),
    1: ("obsidiantile", "obsidiantile"),
    2: ("pavingtravel", "paving_stone_travel"),
    3: ("pavingwarding", "paving_stone_warding"),
    4: ("thaumiumblock", "thaumiumblock"),
    5: ("tallowblock", "tallowblock"),
    6: ("arcanestone", "arcane_stone"),
    7: ("arcanestonebrick", "arcane_stone"),  # same texture as 6 in original
    8: ("obsidiantotemcharged", "obsidiantotembase"),
    9: ("golemfetter", "golem_stone_side"),
    11: ("ancientstone", "obsidiantile"),  # placeholder
    12: ("ancientrock", "obsidiantile"),  # placeholder
    14: ("crustedstone", "crust"),
    15: ("pedestalstone", "pedestal_side"),
}
sv = {}
for meta, (name, tex) in solid_variants.items():
    sv[f"variant={name}"] = {"model": f"thaumcraft:blockcosmeticsolid_{name}"}
    write_json(f"models/block/blockcosmeticsolid_{name}.json", {"parent":"block/cube_all","textures":{"all":f"thaumcraft:blocks/{tex}"}})
write_json("blockstates/blockcosmeticsolid.json", {"variants": sv})

print("=== BlockCustomOre (8 variants) ===")
ore_variants = {
    0: ("cinnabar", "cinnibar"),
    1: ("infusedair", "infusedorestone"),
    2: ("infusedfire", "infusedorestone"),
    3: ("infusedwater", "infusedorestone"),
    4: ("infusedearth", "infusedorestone"),
    5: ("infusedorder", "infusedorestone"),
    6: ("infusedentropy", "infusedorestone"),
    7: ("amber", "amberore"),
}
ov = {}
for meta, (name, tex) in ore_variants.items():
    ov[f"variant={name}"] = {"model": f"thaumcraft:blockcustomore_{name}"}
    write_json(f"models/block/blockcustomore_{name}.json", {"parent":"block/cube_all","textures":{"all":f"thaumcraft:blocks/{tex}"}})
write_json("blockstates/blockcustomore.json", {"variants": ov})

print("=== BlockMagicalLeaves (2 variants) ===")
lv = {
    "variant=greatwood": {"model": "thaumcraft:blockmagicalleaves_greatwood"},
    "variant=silverwood": {"model": "thaumcraft:blockmagicalleaves_silverwood"},
}
write_json("blockstates/blockmagicalleaves.json", {"variants": lv})
write_json("models/block/blockmagicalleaves_greatwood.json", {"parent":"block/leaves","textures":{"all":"thaumcraft:blocks/greatwoodleaves"}})
write_json("models/block/blockmagicalleaves_silverwood.json", {"parent":"block/leaves","textures":{"all":"thaumcraft:blocks/silverwoodleaves"}})

print("=== BlockCustomPlant (6 variants) ===")
pv = {
    "variant=shimmerleaf":    {"model": "thaumcraft:blockcustomplant_shimmerleaf"},
    "variant=cinderpearl":    {"model": "thaumcraft:blockcustomplant_cinderpearl"},
    "variant=manashroom":     {"model": "thaumcraft:blockcustomplant_manashroom"},
    "variant=greatwoodsap":   {"model": "thaumcraft:blockcustomplant_greatwoodsap"},
    "variant=etherealbloom":  {"model": "thaumcraft:blockcustomplant_etherealbloom"},
    "variant=silverwoodsap":  {"model": "thaumcraft:blockcustomplant_silverwoodsap"},
}
write_json("blockstates/blockcustomplant.json", {"variants": pv})
plants = [("shimmerleaf","shimmerleaf"),("cinderpearl","cinderpearl"),("manashroom","manashroom"),
          ("greatwoodsap","greatwoodsapling"),("etherealbloom","purifier_seed"),("silverwoodsap","silverwoodsapling")]
for name, tex in plants:
    write_json(f"models/block/blockcustomplant_{name}.json", {"parent":"block/cross","textures":{"cross":f"thaumcraft:blocks/{tex}"}})

print("\nDone!")
