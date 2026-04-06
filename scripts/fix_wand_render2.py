#!/usr/bin/env python3
"""Fix remaining errors in WandManager.java and RenderEventHandler.java"""
import re, os

ROOT = os.path.join(os.path.dirname(__file__), '..')

# ============================================================
# WandManager.java
# ============================================================
wand_path = os.path.join(ROOT, 'src/main/java/thaumcraft/common/items/wands/WandManager.java')
with open(wand_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. world.playSoundEffect -> world.playSound(null, ...)
content = content.replace(
    'world.playSoundEffect((double) x + (double) 0.5F, (double) y + (double) 0.5F, (double) z + (double) 0.5F, "thaumcraft:wand", 1.0F, 1.0F)',
    'world.playSound(null, (double) x + 0.5, (double) y + 0.5, (double) z + 0.5, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "wand")), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F)'
)
content = content.replace(
    'w.playSoundAtEntity(player, "thaumcraft:cameraticks", 0.3F, 1.0F)',
    'player.world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "cameraticks")), net.minecraft.util.SoundCategory.PLAYERS, 0.3F, 1.0F)'
)
content = content.replace(
    'w.playSoundAtEntity(player, "thaumcraft:cameraticks", 0.3F, 0.9F)',
    'player.world.playSound(null, player.posX, player.posY, player.posZ, net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "cameraticks")), net.minecraft.util.SoundCategory.PLAYERS, 0.3F, 0.9F)'
)

# 2. world.setBlock -> world.setBlockState (specific occurrences with meta)
setblock_fixes = [
    ('world.setBlock(x, y, z, ConfigBlocks.blockMetalDevice, 0, 3)',
     'world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice.getStateFromMeta(0), 3)'),
    ('world.setBlock(x + xx, y - yy + 2, z + zz, ConfigBlocks.blockStoneDevice, 4, 3)',
     'world.setBlockState(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockStoneDevice.getStateFromMeta(4), 3)'),
    ('world.setBlock(x + xx, y - yy + 2, z + zz, ConfigBlocks.blockStoneDevice, 3, 3)',
     'world.setBlockState(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockStoneDevice.getStateFromMeta(3), 3)'),
    ('world.setBlock(x, y, z, ConfigBlocks.blockMetalDevice, 10, 0)',
     'world.setBlockState(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice.getStateFromMeta(10), 0)'),
    ('world.setBlock(x, y + 1, z, ConfigBlocks.blockMetalDevice, 11, 0)',
     'world.setBlockState(new BlockPos(x, y + 1, z), ConfigBlocks.blockMetalDevice.getStateFromMeta(11), 0)'),
    ('world.setBlock(x + xx, y - yy + 2, z + zz, ConfigBlocks.blockJar, 2, 3)',
     'world.setBlockState(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockJar.getStateFromMeta(2), 3)'),
    ('world.setBlock(x + xx, y + yy, z + zz, ConfigBlocks.blockArcaneFurnace, md, 0)',
     'world.setBlockState(new BlockPos(x + xx, y + yy, z + zz), ConfigBlocks.blockArcaneFurnace.getStateFromMeta(md), 0)'),
    # No-meta form (default state)
    ('world.setBlock(x, y + 1, z, ConfigBlocks.blockEldritchPortal)',
     'world.setBlockState(new BlockPos(x, y + 1, z), ConfigBlocks.blockEldritchPortal.getDefaultState())'),
]
for old, new in setblock_fixes:
    content = content.replace(old, new)

# 3. world.addBlockEvent(x, y, z, block, a, b) -> world.addBlockEvent(new BlockPos(x,y,z), block, a, b)
addevent_fixes = [
    ('world.addBlockEvent(x, y, z, ConfigBlocks.blockMetalDevice, 1, 1)',
     'world.addBlockEvent(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice, 1, 1)'),
    ('world.addBlockEvent(x + xx, y - yy + 2, z + zz, ConfigBlocks.blockStoneDevice, 1, 0)',
     'world.addBlockEvent(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockStoneDevice, 1, 0)'),
    ('world.addBlockEvent(x + xx, y - yy + 2, z + zz, ConfigBlocks.blockJar, 9, 0)',
     'world.addBlockEvent(new BlockPos(x + xx, y - yy + 2, z + zz), ConfigBlocks.blockJar, 9, 0)'),
    ('world.addBlockEvent(x + xx, y + yy, z + zz, ConfigBlocks.blockArcaneFurnace, 1, 4)',
     'world.addBlockEvent(new BlockPos(x + xx, y + yy, z + zz), ConfigBlocks.blockArcaneFurnace, 1, 4)'),
]
for old, new in addevent_fixes:
    content = content.replace(old, new)

# 4. world.getTileEntity(int, int, int) -> world.getTileEntity(new BlockPos(...))
# All cases have simple (no-comma-within-arg) expressions
def fix_getTileEntity(m):
    args = m.group(1)
    parts = [p.strip() for p in args.split(',')]
    if len(parts) == 3:
        return f'world.getTileEntity(new BlockPos({parts[0]}, {parts[1]}, {parts[2]}))'
    return m.group(0)

content = re.sub(
    r'world\.getTileEntity\((?!new\s+BlockPos)([^)]+)\)',
    fix_getTileEntity,
    content
)

# 5. world.removeTileEntity(int, int, int)
content = re.sub(
    r'world\.removeTileEntity\((?!new\s+BlockPos)([^)]+)\)',
    lambda m: f'world.removeTileEntity(new BlockPos({m.group(1)}))',
    content
)

# 6. world.setBlockToAir(int, int, int) - 3-arg form only
content = re.sub(
    r'world\.setBlockToAir\((?!new\s+BlockPos)([^)]+)\)',
    lambda m: f'world.setBlockToAir(new BlockPos({m.group(1)}))',
    content
)

# 7. world.markBlockForUpdate(x, y, z) -> world.notifyBlockUpdate(new BlockPos(...), state, state, 3)
def fix_markBlockForUpdate(m):
    args = m.group(1)
    pos = f'new BlockPos({args})'
    return f'world.notifyBlockUpdate({pos}, world.getBlockState({pos}), world.getBlockState({pos}), 3)'
content = re.sub(r'world\.markBlockForUpdate\(([^)]+)\)', fix_markBlockForUpdate, content)

# 8. world.notifyBlockChange(x, y, z, block) -> world.notifyNeighborsOfStateChange(new BlockPos(x,y,z), block, true)
#    These are 4-arg forms; block arg can be a getBlock() call
content = content.replace(
    'world.notifyBlockChange(x, y, z, ConfigBlocks.blockMetalDevice)',
    'world.notifyNeighborsOfStateChange(new BlockPos(x, y, z), ConfigBlocks.blockMetalDevice, true)'
)
content = content.replace(
    'world.notifyBlockChange(x, y + 1, z, ConfigBlocks.blockMetalDevice)',
    'world.notifyNeighborsOfStateChange(new BlockPos(x, y + 1, z), ConfigBlocks.blockMetalDevice, true)'
)

# 9. world.notifyBlocksOfNeighborChange(...)
content = content.replace(
    'world.notifyBlocksOfNeighborChange(x, y, z, world.getBlockState(new BlockPos(x, y, z)).getBlock())',
    'world.notifyNeighborsOfStateChange(new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)).getBlock(), true)'
)

# 10. world.getBlockMetadata(x, y, z) -> use getMetaFromState
def fix_getBlockMetadata(m):
    args = m.group(1)
    pos = f'new net.minecraft.util.math.BlockPos({args})'
    return f'world.getBlockState({pos}).getBlock().getMetaFromState(world.getBlockState({pos}))'
content = re.sub(r'world\.getBlockMetadata\(([^)]+)\)', fix_getBlockMetadata, content)

# 11. world.provider.dimensionId -> world.provider.getDimension()
content = content.replace('world.provider.dimensionId', 'world.provider.getDimension()')

# 12. player.inventory.mainInventory[idx] -> player.inventory.mainInventory.get(idx)
content = re.sub(
    r'player\.inventory\.mainInventory\[([^\]]+)\]',
    lambda m: f'player.inventory.mainInventory.get({m.group(1)})',
    content
)

# 13. stack.stackTagCompound -> stack.getTagCompound() (any variable name)
content = re.sub(r'(\w+)\.stackTagCompound', lambda m: f'{m.group(1)}.getTagCompound()', content)

# 14. world.spawnEntityInWorld -> world.spawnEntity
content = content.replace('world.spawnEntityInWorld(', 'world.spawnEntity(')

# 15. EnumEnumFacing.getOrientation(side) -> net.minecraft.util.EnumFacing.byIndex(side)
content = content.replace('EnumEnumFacing.getOrientation(side)', 'net.minecraft.util.EnumFacing.byIndex(side)')

# 16. entityLiving.worldObj -> entityLiving.world
content = content.replace('entityLiving.worldObj', 'entityLiving.world')

with open(wand_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed WandManager.java")

# ============================================================
# RenderEventHandler.java
# ============================================================
render_path = os.path.join(ROOT, 'src/main/java/thaumcraft/client/lib/RenderEventHandler.java')
with open(render_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. isAirBlock(int, int, int) -> isAirBlock(new BlockPos(...))
content = re.sub(
    r'\.isAirBlock\((?!new\s+(?:net\.minecraft\.util\.math\.)?BlockPos)([^)]+)\)',
    lambda m: f'.isAirBlock(new net.minecraft.util.math.BlockPos({m.group(1)}))',
    content
)

# 2. bi.getItem(player.world, x+xx, y+yy, z+zz)
#    In 1.12.2: Block.getItem(World, BlockPos, IBlockState) -> ItemStack
#    Usage was: Item.getIdFromItem(bi.getItem(...)) - need .getItem() on the ItemStack
content = content.replace(
    'bi.getItem(player.world, x + xx, y + yy, z + zz)',
    'bi.getItem(player.world, new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz), player.world.getBlockState(new net.minecraft.util.math.BlockPos(x + xx, y + yy, z + zz))).getItem()'
)

# 3. stackTagCompound -> getTagCompound()
content = re.sub(r'(\w+)\.stackTagCompound', lambda m: f'{m.group(1)}.getTagCompound()', content)

# 4. EnumFacing.byIndex(x).offsetX/Y/Z -> getXOffset/YOffset/ZOffset()
#    Handle both bare and fully-qualified forms
for prefix in ['net\\.minecraft\\.util\\.EnumFacing', 'EnumFacing']:
    content = re.sub(rf'({prefix}\.byIndex\([^)]+\))\.offsetX', r'\1.getXOffset()', content)
    content = re.sub(rf'({prefix}\.byIndex\([^)]+\))\.offsetY', r'\1.getYOffset()', content)
    content = re.sub(rf'({prefix}\.byIndex\([^)]+\))\.offsetZ', r'\1.getZOffset()', content)

# 5. cc.posX/Y/Z -> cc.getX()/getY()/getZ() (cc is BlockPos)
content = re.sub(r'\bcc\.posX\b', 'cc.getX()', content)
content = re.sub(r'\bcc\.posY\b', 'cc.getY()', content)
content = re.sub(r'\bcc\.posZ\b', 'cc.getZ()', content)

# 6a. tessellator.setColorRGBA_F in drawMarkerLine: remove the call, fix vertex colors
content = content.replace(
    '         tessellator.setColorRGBA_F(r, g, b, f2a * (1.0F - f4));\n         float f13',
    '         float f13'
)
content = content.replace(
    '.tex(f13, f10).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color',
    '.tex(f13, f10).color(r, g, b, f2a * (1.0F - f4))'
)
content = content.replace(
    '.tex(f13, f9).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color',
    '.tex(f13, f9).color(r, g, b, f2a * (1.0F - f4))'
)

# 6b. Vignette vertex colors: GlStateManager.color(b,b,b,1) already sets color;
#     for BufferBuilder we put it per-vertex.
content = content.replace(
    '.tex(0.0F, 1.0F).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color',
    '.tex(0.0F, 1.0F).color(b, b, b, 1.0F)'
)
content = content.replace(
    '.tex(1.0F, 1.0F).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color',
    '.tex(1.0F, 1.0F).color(b, b, b, 1.0F)'
)
content = content.replace(
    '.tex(1.0F, 0.0F).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color',
    '.tex(1.0F, 0.0F).color(b, b, b, 1.0F)'
)
content = content.replace(
    '.tex(0.0F, 0.0F).color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color',
    '.tex(0.0F, 0.0F).color(b, b, b, 1.0F)'
)

# 7. event.entity -> event.getEntity() (skip import lines to avoid corruption)
lines = content.split('\n')
new_lines = []
for line in lines:
    if not line.strip().startswith('import '):
        line = re.sub(r'\bevent\.entity\b', 'event.getEntity()', line)
    new_lines.append(line)
content = '\n'.join(new_lines)

with open(render_path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed RenderEventHandler.java")
