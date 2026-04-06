"""Fix per-meta blockstate variants and models for multi-variant blocks."""
import json, os

base = "src/main/resources/assets/thaumcraft"

def write_json(path, data):
    fp = os.path.join(base, path)
    with open(fp, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"  {path}")

def cube_all(tex):
    return {"parent":"block/cube_all","textures":{"all":f"thaumcraft:blocks/{tex}"}}

def cube_bt(side, top):
    return {"parent":"block/cube_bottom_top","textures":{"side":f"thaumcraft:blocks/{side}","top":f"thaumcraft:blocks/{top}","bottom":f"thaumcraft:blocks/{top}"}}

print("=== BlockTaint (3 variants) ===")
taint_variants = {0: ("taintcrust", "taint_crust"), 1: ("taintsoil", "taint_soil"), 2: ("taintflesh", "fleshblock")}
tv = {}
for m, (name, tex) in taint_variants.items():
    tv[f"meta={m}"] = {"model": f"thaumcraft:blocktaint_{name}"}
    write_json(f"models/block/blocktaint_{name}.json", cube_all(tex))
for i in range(3, 16):
    tv[f"meta={i}"] = {"model": "thaumcraft:blocktaint_taintcrust"}
write_json("blockstates/blocktaint.json", {"variants": tv})

# Item models
for m, (name, tex) in taint_variants.items():
    write_json(f"models/item/blocktaint_{m}.json", {"parent":"item/generated","textures":{"layer0":f"thaumcraft:blocks/{tex}"}})

print("\n=== BlockTaintFibres (5 variants) ===")
fibres = {0: ("fibres", "taint_fibres"), 1: ("grass1", "taintgrass1"), 2: ("grass2", "taintgrass2"),
          3: ("stalk1", "taint_spore_stalk_1"), 4: ("stalk2", "taint_spore_stalk_2")}
fv = {}
for m, (name, tex) in fibres.items():
    fv[f"meta={m}"] = {"model": f"thaumcraft:blocktaintfibres_{name}"}
    write_json(f"models/block/blocktaintfibres_{name}.json", {"parent":"block/cross","textures":{"cross":f"thaumcraft:blocks/{tex}"}})
for i in range(5, 16):
    fv[f"meta={i}"] = {"model": "thaumcraft:blocktaintfibres_fibres"}
write_json("blockstates/blocktaintfibres.json", {"variants": fv})

print("\n=== BlockEldritch (per-meta models) ===")
eld = {
    0: ("altar", "obsidiantile"),
    1: ("obelisk", "obsidiantile"),
    2: ("unused", "obsidiantile"),
    3: ("cap", "obsidiantile"),
    4: ("stone", "es_i_1"),
    5: ("resource", "es_i_2"),
    6: ("deco1", "deco_1"),
    7: ("deco2", "deco_2"),
    8: ("lock", "deco_3"),
    9: ("spawner", "crust"),
    10: ("trap", "es_5"),
}
ev = {}
for m, (name, tex) in eld.items():
    ev[f"meta={m}"] = {"model": f"thaumcraft:blockeldritch_{name}"}
    write_json(f"models/block/blockeldritch_{name}.json", cube_all(tex))
    write_json(f"models/item/blockeldritch_{m}.json", {"parent":"item/generated","textures":{"layer0":f"thaumcraft:blocks/{tex}"}})
for i in range(11, 16):
    ev[f"meta={i}"] = {"model": "thaumcraft:blockeldritch_altar"}
write_json("blockstates/blockeldritch.json", {"variants": ev})

print("\n=== BlockStoneDevice (per-meta) ===")
sd = {
    0: ("furnace", "al_furnace_front_off"),
    1: ("pedestal", "pedestal_side"),
    2: ("matrix", "arcane_stone"),
    3: ("pillar", "arcane_stone"),
    4: ("pillar_top", "arcane_stone"),
    5: ("wandpedestal", "wandpedestal_side"),
    8: ("wandpedestal_focus", "wandpedestal_focus_side"),
    9: ("stabilizer", "arcane_stone"),
    10: ("stabilizer2", "arcane_stone"),
    11: ("converter", "arcane_stone"),
    12: ("spa", "spa_side"),
    13: ("focal", "arcane_stone"),
    14: ("scrubber", "arcane_stone"),
}
sv = {}
for m, (name, tex) in sd.items():
    sv[f"meta={m}"] = {"model": f"thaumcraft:blockstonedevice_{name}"}
    write_json(f"models/block/blockstonedevice_{name}.json", cube_all(tex))
for i in range(16):
    if f"meta={i}" not in sv:
        sv[f"meta={i}"] = {"model": "thaumcraft:blockstonedevice_furnace"}
write_json("blockstates/blockstonedevice.json", {"variants": sv})

# StoneDevice item models
for m, (name, tex) in sd.items():
    write_json(f"models/item/blockstonedevice_{m}.json", {"parent":"item/generated","textures":{"layer0":f"thaumcraft:blocks/{tex}"}})

print("\nDone!")
