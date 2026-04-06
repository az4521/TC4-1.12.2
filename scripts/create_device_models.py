"""Create per-meta item models for multi-subtype blocks."""
import json, os

base = "src/main/resources/assets/thaumcraft/models/item"

def w(name, tex):
    path = os.path.join(base, name + ".json")
    json.dump({"parent":"item/generated","textures":{"layer0":f"thaumcraft:blocks/{tex}"}}, open(path,'w'))
    print(f"  {name} -> {tex}")

print("=== MetalDevice per-meta ===")
# 0=crucible, 1=alembic, 2=lamp, 3=bellows, 5=golem_guard, 7=vis_charger
# 8=node_altar, 9=smeltery, 12=brainbox, 13=charger, 14=vis_relay
metal = {0:"crucible1", 1:"alchemyblock", 2:"lamp_side", 3:"bellows",
         5:"golem_stone_top", 7:"metalbase", 8:"metalbase", 9:"crucible1",
         12:"brainbox", 13:"metalbase", 14:"metalbase"}
for m, tex in metal.items():
    w(f"blockmetaldevice_{m}", tex)

print("\n=== WoodenDevice per-meta ===")
# 0=bellows, 1=valve, 2=warded_glass, 4=alembic, 5=infusion, 6=planks_greatwood
# 7=flux_scrubber, 8=banner
wood = {0:"bellows", 1:"woodplain", 2:"wardedglass", 4:"alchemyblock",
        5:"woodplain", 6:"planks_greatwood", 7:"woodplain", 8:"woodplain"}
for m, tex in wood.items():
    w(f"blockwoodendevice_{m}", tex)

print("\n=== StoneDevice per-meta ===")
# 0=alchemy_furnace, 1=pedestal, 2=infusion_matrix, 5=essentia_holder
# 8=essentia_crystal, 9=research_table, 10=thaumatorium, 11=obelisk_cap
# 12=focal_manip, 13=caster, 14=banner
stone = {0:"al_furnace_side", 1:"pedestal_side", 2:"arcane_stone",
         5:"arcane_stone", 8:"arcane_stone", 9:"arcane_stone",
         10:"arcane_stone", 11:"arcane_stone", 12:"arcane_stone",
         13:"arcane_stone", 14:"arcane_stone"}
for m, tex in stone.items():
    w(f"blockstonedevice_{m}", tex)

print("\n=== Tube per-meta ===")
# 0=normal, 1=valve, 2=centrifuge, 3=filter, 4=restrict, 5=oneway, 6=master, 7=buffer
tubes = {0:"pipe_1", 1:"pipe_valve", 2:"pipe_2", 3:"pipe_filter",
         4:"pipe_restrict", 5:"pipe_oneway", 6:"pipe_1", 7:"pipe_buffer"}
for m, tex in tubes.items():
    w(f"blocktube_{m}", tex)

print("\n=== LootCrate per-meta ===")
for m in range(3):
    w(f"blocklootcrate_{m}", f"crate_side_{m}")

print("\n=== LootUrn per-meta ===")
for m in range(3):
    w(f"blocklooturn_{m}", f"urn_side_{m}")

print("\nDone!")
