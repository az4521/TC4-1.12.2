#!/usr/bin/env python3
"""Batch 6: Fix remaining compile errors."""
import re
import os

def read(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def write(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

def patch(path, old, new, label=""):
    content = read(path)
    if old not in content:
        print(f"  MISS [{label or old[:50]}]: {path}")
        return False
    content = content.replace(old, new)
    write(path, content)
    print(f"  OK [{label or old[:50]}]: {path}")
    return True

# ============================================================
# VisNetHandler.java: dimensionId -> getDimension(), xCoord/yCoord/zCoord on TileEntity
# ============================================================
print("=== VisNetHandler.java ===")
vis = r"src\main\java\thaumcraft\api\visnet\VisNetHandler.java"
content = read(vis)
content = content.replace('world.provider.dimensionId', 'world.provider.getDimension()')
content = content.replace('node.xCoord', 'node.getPos().getX()')
content = content.replace('node.yCoord', 'node.getPos().getY()')
content = content.replace('node.zCoord', 'node.getPos().getZ()')
# Also n.xCoord/zCoord
content = content.replace('n.xCoord', 'n.getPos().getX()')
content = content.replace('n.zCoord', 'n.getPos().getZ()')
# isChunkLoaded(World, int, int) -> still exists in 1.12.2 with different signature
# World.isChunkLoaded(BlockPos) or (int, int, boolean) - use BlockPos form
content = content.replace(
    'isChunkLoaded(n.getWorldObj(), n.getPos().getX(), n.getPos().getZ())',
    'n.getWorldObj().isChunkLoaded(n.getPos())'
)
write(vis, content)
print(f"  Written: {vis}")

# ============================================================
# ServerTickEventsFML.java: loc.chunkXPos -> loc.x, loc.chunkZPos -> loc.z
# ============================================================
print("\n=== ServerTickEventsFML.java ===")
stef = r"src\main\java\thaumcraft\common\lib\events\ServerTickEventsFML.java"
content = read(stef)
content = content.replace('loc.chunkXPos', 'loc.x')
content = content.replace('loc.chunkZPos', 'loc.z')
write(stef, content)
print(f"  Written: {stef}")

# ============================================================
# TileCrucible.java: getMaterial() -> IBlockState.getMaterial(), isAirBlock 3-int, setBlock
# ============================================================
print("\n=== TileCrucible.java ===")
crucible = r"src\main\java\thaumcraft\common\tiles\TileCrucible.java"
content = read(crucible)

# Line 85: getBlock().getMaterial() -> getBlockState.getMaterial()
content = content.replace(
    'this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getBlock().getMaterial()',
    'this.world.getBlockState(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() - 1, this.getPos().getZ())).getMaterial()'
)

# isAirBlock(3-int) occurrences in spill()
content = content.replace(
    'this.world.isAirBlock(new BlockPos(this.getPos()).getX(), this.getPos().getY() + 1, this.getPos().getZ())',
    'this.world.isAirBlock(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()))'
)
content = content.replace(
    'this.world.isAirBlock(new BlockPos(this.getPos()).getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z)',
    'this.world.isAirBlock(new net.minecraft.util.math.BlockPos(this.getPos().getX() + x, this.getPos().getY() + y, this.getPos().getZ() + z))'
)

# setBlock TODO_PORT -> setBlockState with getDefaultState()
# These blocks (blockFluxGas, blockFluxGoo) use metadata for "level" which is block state property
# For now use getDefaultState() as placeholder — correct block state port is needed later
def fix_setblock_metadata(content, from_block, to_block, flags, meta_expr='0'):
    """Replace world.setBlock(x,y,z, block, meta, flags) with setBlockState"""
    # We need to replace the TODO_PORT comment + world.setBlock
    # The pattern wraps across two physical lines due to the TODO comment
    pass

# Handle the setBlock calls specifically - they all have the TODO_PORT comment format
# Pattern: this./* TODO_PORT: setBlock ... */\n        world.setBlock(x, y, z, block, meta, flags)
setblock_pattern = re.compile(
    r'this\./\* TODO_PORT: setBlock -> world\.setBlockState[^*]*\*/\s*\n\s*world\.setBlock\(([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^)]+)\)',
    re.DOTALL
)

def setblock_replacer(m):
    x, y, z = m.group(1).strip(), m.group(2).strip(), m.group(3).strip()
    block = m.group(4).strip()
    meta = m.group(5).strip()
    # flags = m.group(6).strip()
    # We'll use getDefaultState() as the block state base
    # If meta == 0, just use getDefaultState()
    # If meta is an expression (md+1, etc), we need getStateFromMeta
    if meta == '0':
        state = f'{block}.getDefaultState()'
    else:
        state = f'{block}.getStateFromMeta({meta})'
    return f'world.setBlockState(new net.minecraft.util.math.BlockPos({x}, {y}, {z}), {state}, 3)'

new_content = setblock_pattern.sub(setblock_replacer, content)
if new_content != content:
    content = new_content
    print(f"  OK: setBlock TODO_PORT replaced")
else:
    print(f"  MISS: setBlock TODO_PORT pattern in TileCrucible")

write(crucible, content)
print(f"  Written: {crucible}")

# ============================================================
# NodeAspectGenerators.java: getBiomeGenForCoords, biomeID, getBlock, getMaterial, isFoliage
# ============================================================
print("\n=== NodeAspectGenerators.java ===")
nag = r"src\main\java\thaumcraft\api\expands\worldgen\node\consts\NodeAspectGenerators.java"
content = read(nag)

# getBiomeGenForCoords(x, z) -> getBiome(new BlockPos(x, 0, z))
content = re.sub(
    r'world\.getBiomeGenForCoords\((\w+),\s*(\w+)\)',
    r'world.getBiome(new net.minecraft.util.math.BlockPos(\1, 0, \2))',
    content
)

# bg.biomeID -> net.minecraft.world.biome.Biome.getIdForBiome(bg)
content = content.replace('bg.biomeID', 'net.minecraft.world.biome.Biome.getIdForBiome(bg)')
# biomeTaint.biomeID
content = content.replace('biomeTaint.biomeID', 'net.minecraft.world.biome.Biome.getIdForBiome(biomeTaint)')

# world.getBlock(x+xx, y+yy, z+zz) -> world.getBlockState(new BlockPos(...)).getBlock()
content = re.sub(
    r'world\.getBlock\((\w+)\s*\+\s*(\w+),\s*(\w+)\s*\+\s*(\w+),\s*(\w+)\s*\+\s*(\w+)\)',
    r'world.getBlockState(new net.minecraft.util.math.BlockPos(\1+\2, \3+\4, \5+\6)).getBlock()',
    content
)

# bi.getMaterial() -> world.getBlockState(new BlockPos(x+xx,y+yy,z+zz)).getMaterial()
# These are inside the loop where the Block bi was just fetched - need IBlockState
# Replace bi.getMaterial() == Material.WATER/LAVA with IBlockState-based check
# Since we converted getBlock to getBlockState().getBlock(), we should store the state
# Better: replace the entire block-fetching section with IBlockState approach
old_block_loop = '''                            Block bi = world.getBlockState(new net.minecraft.util.math.BlockPos(x+xx, y+yy, z+zz)).getBlock();
                                if (bi.getMaterial() == Material.WATER) {
                                    ++water;
                                } else if (bi.getMaterial() == Material.LAVA) {
                                    ++lava;
                                } else if (bi == Blocks.STONE) {
                                    ++stone;
                                }

                                if (bi.isFoliage(world, x + xx, y + yy, z + zz)) {
                                    ++foliage;
                                }'''
new_block_loop = '''                            net.minecraft.block.state.IBlockState _bst = world.getBlockState(new net.minecraft.util.math.BlockPos(x+xx, y+yy, z+zz));
                                Block bi = _bst.getBlock();
                                if (_bst.getMaterial() == Material.WATER) {
                                    ++water;
                                } else if (_bst.getMaterial() == Material.LAVA) {
                                    ++lava;
                                } else if (bi == Blocks.STONE) {
                                    ++stone;
                                }

                                if (bi.isFoliage(world, new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz))) {
                                    ++foliage;
                                }'''
if old_block_loop in content:
    content = content.replace(old_block_loop, new_block_loop)
    print(f"  OK: block loop replaced")
else:
    # Try with just getMaterial fix
    content = content.replace('bi.getMaterial() == Material.WATER', '_bst.getMaterial() == Material.WATER')
    content = content.replace('bi.getMaterial() == Material.LAVA', '_bst.getMaterial() == Material.LAVA')
    content = re.sub(
        r'bi\.isFoliage\(world,\s*x\s*\+\s*xx,\s*y\s*\+\s*yy,\s*z\s*\+\s*zz\)',
        'bi.isFoliage(world, new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz))',
        content
    )
    print(f"  PARTIAL: block loop (pattern not found, applied partial fixes)")

write(nag, content)
print(f"  Written: {nag}")

# ============================================================
# NodeTypePickers.java: getBiomeGenForCoords, biomeID
# ============================================================
print("\n=== NodeTypePickers.java ===")
ntp = r"src\main\java\thaumcraft\api\expands\worldgen\node\consts\NodeTypePickers.java"
content = read(ntp)
content = re.sub(
    r'world\.getBiomeGenForCoords\((\w+),\s*(\w+)\)',
    r'world.getBiome(new net.minecraft.util.math.BlockPos(\1, 0, \2))',
    content
)
content = content.replace('bg.biomeID', 'net.minecraft.world.biome.Biome.getIdForBiome(bg)')
content = content.replace('biomeTaint.biomeID', 'net.minecraft.world.biome.Biome.getIdForBiome(biomeTaint)')
write(ntp, content)
print(f"  Written: {ntp}")

# ============================================================
# TileHoleSyncPacket.java: message.origin.getX() -> message.origin.getPos().getX()
# ============================================================
print("\n=== TileHoleSyncPacket.java ===")
thsp = r"src\main\java\tc4tweak\network\TileHoleSyncPacket.java"
content = read(thsp)
content = content.replace('message.origin.getX()', 'message.origin.getPos().getX()')
content = content.replace('message.origin.getY()', 'message.origin.getPos().getY()')
content = content.replace('message.origin.getZ()', 'message.origin.getPos().getZ()')
write(thsp, content)
print(f"  Written: {thsp}")

# ============================================================
# IndexedCodec.java: FMLProxyPacket(buffer.copy(), ...) -> FMLProxyPacket(new PacketBuffer(buffer.copy()), ...)
# ============================================================
print("\n=== IndexedCodec.java ===")
ic = r"src\main\java\tc4tweak\network\IndexedCodec.java"
content = read(ic)
content = content.replace(
    'FMLProxyPacket proxy = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());',
    'FMLProxyPacket proxy = new FMLProxyPacket(new net.minecraft.network.PacketBuffer(buffer.copy()), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());'
)
write(ic, content)
print(f"  Written: {ic}")

# ============================================================
# ItemFocusPouch.java: EnumRarity.rare -> EnumRarity.RARE
# ============================================================
print("\n=== ItemFocusPouch.java ===")
ifp = r"src\main\java\thaumcraft\common\items\wands\ItemFocusPouch.java"
content = read(ifp)
content = content.replace('EnumRarity.rare', 'EnumRarity.RARE')
write(ifp, content)
print(f"  Written: {ifp}")

# ============================================================
# PotionVisExhaust.java + PotionFluxTaint.java: Potion constructor
# ============================================================
print("\n=== Potion classes ===")
for f in [
    r"src\main\java\thaumcraft\api\potions\PotionVisExhaust.java",
    r"src\main\java\thaumcraft\api\potions\PotionFluxTaint.java",
]:
    content = read(f)
    # Change constructor: (int par1, boolean par2, int par3) -> (boolean par2, int par3)
    # and super call: super(par1, par2, par3) -> super(par2, par3)
    content = re.sub(
        r'public (PotionVisExhaust|PotionFluxTaint)\(int par1, boolean par2, int par3\)',
        r'public \1(boolean par2, int par3)',
        content
    )
    content = content.replace('super(par1,par2,par3);', 'super(par2,par3);')
    content = content.replace('super(par1, par2, par3);', 'super(par2, par3);')
    # setEffectiveness doesn't exist in 1.12.2 - remove it
    content = re.sub(r'\s*instance\.setEffectiveness\([^)]+\);', '', content)
    write(f, content)
    print(f"  Written: {f}")

# ============================================================
# BonusTagForItemListeners.java: getAttributeModifiers needs 2 args in 1.12.2 Forge
# ============================================================
print("\n=== BonusTagForItemListeners.java ===")
btfl = r"src\main\java\thaumcraft\api\expands\aspects\item\consts\BonusTagForItemListeners.java"
content = read(btfl)
# Fix: item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND) -> item.getAttributeModifiers(slot, itemstack)
content = content.replace(
    'item.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND)',
    'item.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, itemstack)'
)
write(btfl, content)
print(f"  Written: {btfl}")

# ============================================================
# TileResearchTable.java: fix remaining issues
# ============================================================
print("\n=== TileResearchTable.java ===")
trt = r"src\main\java\thaumcraft\common\tiles\TileResearchTable.java"
content = read(trt)

# isUseableByPlayer -> isUsableByPlayer (IInventory renamed it in 1.12.2)
content = content.replace('public boolean isUseableByPlayer(', 'public boolean isUsableByPlayer(')

# Remove super.update() since TileThaumcraft doesn't have update()
# Add implements ITickable to TileResearchTable
content = content.replace(
    'public class TileResearchTable extends TileThaumcraft implements IInventory {',
    'public class TileResearchTable extends TileThaumcraft implements IInventory, net.minecraft.util.ITickable {'
)
content = content.replace('      super.update();\n', '')

# Fix getBlockLightValue - direct string replacement (avoid regex with nested parens)
content = content.replace(
    'this.world.getBlockLightValue(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()))',
    'this.world.getLightFor(net.minecraft.world.EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()))'
)

# canBlockSeeSky -> isSkyLightMax or canSeeSky - in 1.12.2 it's canBlockSeeSky(BlockPos)
# It should already be converted by prior scripts, but check
content = content.replace(
    'this.world.canBlockSeeSky(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()))',
    'this.world.canSeeSky(new net.minecraft.util.math.BlockPos(this.getPos().getX(), this.getPos().getY() + 1, this.getPos().getZ()))'
)

write(trt, content)
print(f"  Written: {trt}")

print("\nDone.")
