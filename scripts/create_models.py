import json, os

base = "src/main/resources/assets/thaumcraft"

def w(path, data):
    fp = os.path.join(base, path)
    with open(fp, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"  {path}")

print("Creating block models...")

# BLOCKJAR: body [3,0,3]-[13,12,13] + rim [5,12,5]-[11,14,11]
w("models/block/blockjar.json", {
    "ambientocclusion": False,
    "textures": {"side":"thaumcraft:blocks/jar_side","top":"thaumcraft:blocks/jar_top","bottom":"thaumcraft:blocks/jar_bottom","particle":"thaumcraft:blocks/jar_side"},
    "elements": [
        {"from":[3,0,3],"to":[13,12,13],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#bottom"},"up":{"texture":"#top"}}},
        {"from":[5,12,5],"to":[11,14,11],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"up":{"texture":"#top"}}}
    ]
})

# BLOCKLOOTURN: base [3,0,3]-[13,1,13] + body [2,1,2]-[14,13,14] + rim [4,13,4]-[12,16,12]
w("models/block/blocklooturn.json", {
    "textures": {"side":"thaumcraft:blocks/urn_side_0","top":"thaumcraft:blocks/urn_top","particle":"thaumcraft:blocks/urn_side_0"},
    "elements": [
        {"from":[3,0,3],"to":[13,1,13],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#top","cullface":"down"}}},
        {"from":[2,1,2],"to":[14,13,14],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#top"},"up":{"texture":"#top"}}},
        {"from":[4,13,4],"to":[12,16,12],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"up":{"texture":"#top","cullface":"up"}}}
    ]
})

# BLOCKLOOTCRATE: body [1,0,1]-[15,14,15]
w("models/block/blocklootcrate.json", {
    "textures": {"side":"thaumcraft:blocks/crate_side_0","top":"thaumcraft:blocks/crate_top","particle":"thaumcraft:blocks/crate_side_0"},
    "elements": [
        {"from":[1,0,1],"to":[15,14,15],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#top","cullface":"down"},"up":{"texture":"#top"}}}
    ]
})

# BLOCKTUBE: center [7,0,7]-[9,16,9] + core [6,4,6]-[10,10,10]
w("models/block/blocktube.json", {
    "ambientocclusion": False,
    "textures": {"tube":"thaumcraft:blocks/pipe_1","core":"thaumcraft:blocks/pipe_3","particle":"thaumcraft:blocks/pipe_1"},
    "elements": [
        {"from":[7,0,7],"to":[9,16,9],"faces":{"north":{"texture":"#tube"},"south":{"texture":"#tube"},"west":{"texture":"#tube"},"east":{"texture":"#tube"},"down":{"texture":"#tube","cullface":"down"},"up":{"texture":"#tube","cullface":"up"}}},
        {"from":[6,4,6],"to":[10,10,10],"faces":{"north":{"texture":"#core"},"south":{"texture":"#core"},"west":{"texture":"#core"},"east":{"texture":"#core"},"down":{"texture":"#core"},"up":{"texture":"#core"}}}
    ]
})

# BLOCKESSENTIARESERVOIR: body [2,2,2]-[14,14,14]
w("models/block/blockessentiareservoir.json", {
    "textures": {"all":"thaumcraft:blocks/essentiareservoir","particle":"thaumcraft:blocks/essentiareservoir"},
    "elements": [
        {"from":[2,2,2],"to":[14,14,14],"faces":{"north":{"texture":"#all"},"south":{"texture":"#all"},"west":{"texture":"#all"},"east":{"texture":"#all"},"down":{"texture":"#all"},"up":{"texture":"#all"}}}
    ]
})

# BLOCKMIRROR: flat pane [3,1,7]-[13,13,9]
w("models/block/blockmirror.json", {
    "ambientocclusion": False,
    "textures": {"frame":"thaumcraft:blocks/mirrorframe","pane":"thaumcraft:blocks/mirrorpane","particle":"thaumcraft:blocks/mirrorframe"},
    "elements": [
        {"from":[3,1,7],"to":[13,13,9],"faces":{"north":{"texture":"#pane"},"south":{"texture":"#pane"},"west":{"texture":"#frame"},"east":{"texture":"#frame"},"down":{"texture":"#frame"},"up":{"texture":"#frame"}}}
    ]
})

# BLOCKLIFTER: full cube with distinct top/side
w("models/block/blocklifter.json", {"parent":"block/cube_bottom_top","textures":{"side":"thaumcraft:blocks/lifterside","top":"thaumcraft:blocks/liftertop","bottom":"thaumcraft:blocks/liftertop"}})

# BLOCKMAGICBOX: small box [3,3,3]-[13,13,13]
w("models/block/blockmagicbox.json", {
    "textures": {"all":"thaumcraft:blocks/brainbox","particle":"thaumcraft:blocks/brainbox"},
    "elements": [
        {"from":[3,3,3],"to":[13,13,13],"faces":{"north":{"texture":"#all"},"south":{"texture":"#all"},"west":{"texture":"#all"},"east":{"texture":"#all"},"down":{"texture":"#all"},"up":{"texture":"#all"}}}
    ]
})

# BLOCKTABLE: table top + 4 legs
w("models/block/blocktable.json", {
    "textures": {"top":"thaumcraft:blocks/tablequill","side":"thaumcraft:blocks/woodplain","particle":"thaumcraft:blocks/woodplain"},
    "elements": [
        {"from":[0,12,0],"to":[16,16,16],"faces":{"north":{"texture":"#side","cullface":"north"},"south":{"texture":"#side","cullface":"south"},"west":{"texture":"#side","cullface":"west"},"east":{"texture":"#side","cullface":"east"},"down":{"texture":"#top"},"up":{"texture":"#top","cullface":"up"}}},
        {"from":[1,0,1],"to":[3,12,3],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#side","cullface":"down"}}},
        {"from":[13,0,1],"to":[15,12,3],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#side","cullface":"down"}}},
        {"from":[1,0,13],"to":[3,12,15],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#side","cullface":"down"}}},
        {"from":[13,0,13],"to":[15,12,15],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#side","cullface":"down"}}}
    ]
})

# BLOCKCHESTHUNGRY: chest box [1,0,1]-[15,14,15]
w("models/block/blockchesthungry.json", {
    "textures": {"side":"thaumcraft:blocks/crate_side_0","top":"thaumcraft:blocks/crate_top","particle":"thaumcraft:blocks/crate_side_0"},
    "elements": [
        {"from":[1,0,1],"to":[15,14,15],"faces":{"north":{"texture":"#side"},"south":{"texture":"#side"},"west":{"texture":"#side"},"east":{"texture":"#side"},"down":{"texture":"#top","cullface":"down"},"up":{"texture":"#top"}}}
    ]
})

# Now update item models to parent from block models (3D in inventory)
print("\nUpdating item models to use 3D block models...")

for name in ["blockjar","blocklooturn","blocklootcrate","blocktube","blockessentiareservoir",
             "blockmirror","blocklifter","blockmagicbox","blocktable","blockchesthungry",
             "blockalchemyfurnace"]:
    w(f"models/item/{name}.json", {"parent": f"thaumcraft:block/{name}"})

print("\nDone!")
