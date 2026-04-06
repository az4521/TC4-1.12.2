#!/usr/bin/env python3
"""Comprehensive batch fix: WandManager, RenderEventHandler, TileCrucible, and global renames."""
import re, os, glob

ROOT = os.path.normpath(os.path.join(os.path.dirname(__file__), '..'))
SRC = os.path.join(ROOT, 'src/main/java')

# ──────────────────────────────────────────────────
# Helpers
# ──────────────────────────────────────────────────

def read(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def write(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

def split_args(s):
    """Split s by commas at paren depth 0."""
    parts, current, depth = [], [], 0
    for ch in s:
        if ch in '([{': depth += 1; current.append(ch)
        elif ch in ')]}': depth -= 1; current.append(ch)
        elif ch == ',' and depth == 0:
            parts.append(''.join(current).strip()); current = []
        else:
            current.append(ch)
    if current:
        parts.append(''.join(current).strip())
    return parts

def find_close_paren(s, open_pos):
    """Given s[open_pos] == '(', find the matching ')'."""
    depth = 0
    for i in range(open_pos, len(s)):
        if s[i] == '(': depth += 1
        elif s[i] == ')':
            depth -= 1
            if depth == 0: return i
    return -1

def transform_world_setblock(content, host='world'):
    """world.setBlock(x,y,z,block[,meta,flags]) -> world.setBlockState(new BlockPos(x,y,z), ...)"""
    pattern = re.compile(r'\b' + re.escape(host) + r'\.setBlock\(')
    result, last = [], 0
    for m in pattern.finditer(content):
        result.append(content[last:m.start()])
        paren_open = m.end() - 1
        paren_close = find_close_paren(content, paren_open)
        if paren_close == -1:
            result.append(content[m.start():])
            return ''.join(result)
        args = split_args(content[paren_open + 1:paren_close])
        if len(args) == 4:
            x, y, z, block = args
            result.append(f'{host}.setBlockState(new BlockPos({x}, {y}, {z}), {block}.getDefaultState())')
        elif len(args) == 6:
            x, y, z, block, meta, flags = args
            result.append(f'{host}.setBlockState(new BlockPos({x}, {y}, {z}), {block}.getStateFromMeta({meta}), {flags})')
        else:
            result.append(content[m.start():paren_close + 1])
        last = paren_close + 1
    result.append(content[last:])
    return ''.join(result)

def transform_play_sound_effect(content):
    """world.playSoundEffect(x,y,z,'ns:name',vol,pitch) -> world.playSound(null,x,y,z,SoundEvent,SoundCategory.BLOCKS,vol,pitch)"""
    pattern = re.compile(r'\bworld\.playSoundEffect\(')
    result, last = [], 0
    for m in pattern.finditer(content):
        result.append(content[last:m.start()])
        paren_open = m.end() - 1
        paren_close = find_close_paren(content, paren_open)
        if paren_close == -1:
            result.append(content[m.start():])
            return ''.join(result)
        args = split_args(content[paren_open + 1:paren_close])
        if len(args) == 6:
            x, y, z, sound_str, vol, pitch = args
            # sound_str is like '"thaumcraft:wand"' or '"random.pop"'
            sound_raw = sound_str.strip('"')
            if ':' in sound_raw:
                ns, name = sound_raw.split(':', 1)
                res_loc = f'new net.minecraft.util.ResourceLocation("{ns}", "{name}")'
            else:
                res_loc = f'new net.minecraft.util.ResourceLocation("{sound_raw}")'
            result.append(
                f'world.playSound(null, {x}, {y}, {z}, '
                f'net.minecraft.util.SoundEvent.REGISTRY.getObject({res_loc}), '
                f'net.minecraft.util.SoundCategory.BLOCKS, {vol}, {pitch})'
            )
        else:
            result.append(content[m.start():paren_close + 1])
        last = paren_close + 1
    result.append(content[last:])
    return ''.join(result)


# ──────────────────────────────────────────────────
# GLOBAL fixes across all Java files
# ──────────────────────────────────────────────────

all_java = glob.glob(os.path.join(SRC, '**', '*.java'), recursive=True)

BLOCKS_RENAME = {}  # built dynamically: lowercase → UPPERCASE

def process_global(path):
    c = read(path)
    orig = c

    # 1. worldObj → world (Entity / TileEntity field rename in 1.12.2)
    c = re.sub(r'\.worldObj\b', '.world', c)

    # 2. TileEntity xCoord/yCoord/zCoord with 'this.' prefix
    c = re.sub(r'\bthis\.xCoord\b', 'this.getPos().getX()', c)
    c = re.sub(r'\bthis\.yCoord\b', 'this.getPos().getY()', c)
    c = re.sub(r'\bthis\.zCoord\b', 'this.getPos().getZ()', c)

    # 3. Blocks.lowercase_name → Blocks.UPPERCASE_NAME
    def upgrade_blocks(m):
        field = m.group(1)
        return 'Blocks.' + field.upper()
    c = re.sub(r'\bBlocks\.([a-z][a-z0-9_]*)\b', upgrade_blocks, c)

    # 4. Material.lowercase_or_camelCase → Material.SCREAMING_SNAKE
    def to_screaming(name):
        s = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', name)
        return s.upper()
    def upgrade_material(m):
        field = m.group(1)
        return 'Material.' + to_screaming(field)
    # Only match lowercase-starting Material fields (skip already-uppercase ones)
    c = re.sub(r'\bMaterial\.([a-z][a-zA-Z0-9]*)\b', upgrade_material, c)

    # 5. ItemStack.stackSize → getCount/setCount (careful: only simple reads here,
    #    assignments handled separately)
    # 5a. stack.stackSize direct read in conditions: no assignment, just reference
    #     This is tricky — skip for now, do targeted fixes per-file

    if c != orig:
        write(path, c)
        return True
    return False

changed = 0
for p in all_java:
    if process_global(p):
        changed += 1
print(f"Global pass: modified {changed} files")

# ──────────────────────────────────────────────────
# WandManager.java targeted fixes
# ──────────────────────────────────────────────────

wand_path = os.path.join(SRC, 'thaumcraft/common/items/wands/WandManager.java')
c = read(wand_path)

# General world.setBlock transformer (catches the AlchemyFurnace ones and any remaining)
c = transform_world_setblock(c, 'world')

# General world.playSoundEffect transformer (catches the (x+a), (y+b), (z+c) form)
c = transform_play_sound_effect(c)

write(wand_path, c)
print("Fixed WandManager.java")

# ──────────────────────────────────────────────────
# RenderEventHandler.java targeted fixes
# ──────────────────────────────────────────────────

render_path = os.path.join(SRC, 'thaumcraft/client/lib/RenderEventHandler.java')
c = read(render_path)

# Fix the mangled isAirBlock call at the goggles check line (line 187 in source)
# Script turned: isAirBlock(target.getBlockPos().getX(), target.getBlockPos().getY() + 1, target.getBlockPos().getZ())
# into: isAirBlock(new net.minecraft.util.math.BlockPos(target.getBlockPos()).getX(), ...)
# Correct form should be: isAirBlock(new BlockPos(target.getBlockPos().getX(), target.getBlockPos().getY() + 1, target.getBlockPos().getZ()))
c = c.replace(
    'isAirBlock(new net.minecraft.util.math.BlockPos(target.getBlockPos()).getX(), target.getBlockPos().getY() + 1, target.getBlockPos().getZ())',
    'isAirBlock(new net.minecraft.util.math.BlockPos(target.getBlockPos().getX(), target.getBlockPos().getY() + 1, target.getBlockPos().getZ()))'
)

# Fix stackTagCompound missed by the (\w+). pattern (because getCurrentItem() ends with ')')
# Use a simpler suffix-only replacement
c = c.replace('.stackTagCompound', '.getTagCompound()')

# Fix cc.getX/Y/Z() → cc.posX/Y/Z in drawMarkerLine where cc is Entity (not BlockPos)
# The method signature is: drawMarkerLine(double x, double y, double z, int side, float partialTicks, int color, Entity cc)
# The corrupted lines look like:
#   double ePX = cc.prevPosX + (cc.getX() - cc.prevPosX) * ...
c = c.replace(
    'double ePX = cc.prevPosX + (cc.getX() - cc.prevPosX)',
    'double ePX = cc.prevPosX + (cc.posX - cc.prevPosX)'
)
c = c.replace(
    'double ePY = cc.prevPosY + (cc.getY() - cc.prevPosY)',
    'double ePY = cc.prevPosY + (cc.posY - cc.prevPosY)'
)
c = c.replace(
    'double ePZ = cc.prevPosZ + (cc.getZ() - cc.prevPosZ)',
    'double ePZ = cc.prevPosZ + (cc.posZ - cc.prevPosZ)'
)

write(render_path, c)
print("Fixed RenderEventHandler.java")

# ──────────────────────────────────────────────────
# TileCrucible.java targeted fixes
# ──────────────────────────────────────────────────

crucible_path = os.path.join(SRC, 'thaumcraft/common/tiles/TileCrucible.java')
c = read(crucible_path)

# After global pass: worldObj → world, this.xCoord → this.getPos().getX(), etc.
# Now do TileCrucible-specific fixes:

# 1. world.getBlock(this.getPos().getX(), ...) (3-int form) → world.getBlockState(new BlockPos(...)).getBlock()
#    Also handle worldObj.getBlock (already renamed to world.getBlock)
def fix_getBlock_3int(content):
    pattern = re.compile(r'\bworld\.getBlock\((?!new\s+BlockPos)([^)]+)\)')
    def replace(m):
        args = m.group(1)
        parts = split_args(args)
        if len(parts) == 3:
            return f'world.getBlockState(new BlockPos({parts[0]}, {parts[1]}, {parts[2]})).getBlock()'
        return m.group(0)
    return pattern.sub(replace, content)

c = fix_getBlock_3int(c)

# 2. world.getBlockMetadata(int, int, int) → getMetaFromState
def fix_getBlockMetadata(content):
    def replace(m):
        args = m.group(1)
        pos = f'new net.minecraft.util.math.BlockPos({args})'
        return f'world.getBlockState({pos}).getBlock().getMetaFromState(world.getBlockState({pos}))'
    return re.sub(r'world\.getBlockMetadata\(([^)]+)\)', replace, content)

c = fix_getBlockMetadata(c)

# 3. world.setBlock(...) - generic transformer
c = transform_world_setblock(c, 'world')

# 4. world.isAirBlock(int, int, int) → world.isAirBlock(new BlockPos(...))
c = re.sub(
    r'\bworld\.isAirBlock\((?!new\s+(?:net\.minecraft\.util\.math\.)?BlockPos)([^)]+)\)',
    lambda m: f'world.isAirBlock(new BlockPos({m.group(1)}))',
    c
)

# 5. world.addBlockEvent(int, int, int, block, int, int) → world.addBlockEvent(new BlockPos(...), block, int, int)
def fix_addBlockEvent(content):
    pattern = re.compile(r'\bworld\.addBlockEvent\((?!new\s+BlockPos)([^)]+)\)')
    def replace(m):
        args = split_args(m.group(1))
        if len(args) == 6:
            return f'world.addBlockEvent(new BlockPos({args[0]}, {args[1]}, {args[2]}), {args[3]}, {args[4]}, {args[5]})'
        return m.group(0)
    return pattern.sub(replace, content)
c = fix_addBlockEvent(c)

# 6. world.markBlockRangeForRenderUpdate already uses BlockPos — should be fine
#    But let's fix markBlockForUpdate(int,int,int) if any remain
c = re.sub(r'\bworld\.markBlockForUpdate\((?!new\s+BlockPos)([^)]+)\)',
           lambda m: f'world.notifyBlockUpdate(new BlockPos({m.group(1)}), world.getBlockState(new BlockPos({m.group(1)})), world.getBlockState(new BlockPos({m.group(1)})), 3)',
           c)

# 7. world.playSoundAtEntity(entity, "sound", vol, pitch) → entity.playSound(SoundEvent, vol, pitch)
#    or world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvent, category, vol, pitch)
def fix_playSoundAtEntity(content):
    pattern = re.compile(r'\bworld\.playSoundAtEntity\(([^)]+)\)')
    def replace(m):
        args = split_args(m.group(1))
        if len(args) == 4:
            entity, sound_str, vol, pitch = args
            sound_raw = sound_str.strip('"')
            if ':' in sound_raw:
                ns, name = sound_raw.split(':', 1)
                res_loc = f'new net.minecraft.util.ResourceLocation("{ns}", "{name}")'
            else:
                res_loc = f'new net.minecraft.util.ResourceLocation("{sound_raw}")'
            sound_ev = f'net.minecraft.util.SoundEvent.REGISTRY.getObject({res_loc})'
            return (f'world.playSound(null, {entity}.posX, {entity}.posY, {entity}.posZ, '
                    f'{sound_ev}, net.minecraft.util.SoundCategory.NEUTRAL, {vol}, {pitch})')
        return m.group(0)
    return pattern.sub(replace, content)
c = fix_playSoundAtEntity(c)

# 8. world.spawnEntityInWorld → world.spawnEntity
c = c.replace('world.spawnEntityInWorld(', 'world.spawnEntity(')

# 9. entity.getEntityItem() → entity.getItem() (EntityItem in 1.12.2)
c = c.replace('entity.getEntityItem()', 'entity.getItem()')

# 10. ItemStack.stackSize → getCount/setCount
#     stackSize direct read
c = re.sub(r'\b(\w+)\.stackSize\b(?!\s*[=])', lambda m: f'{m.group(1)}.getCount()', c)
#     stackSize = expr  → setCount(expr)
c = re.sub(r'\b(\w+)\.stackSize\s*=\s*([^;]+);', lambda m: f'{m.group(1)}.setCount({m.group(2).strip()});', c)
#     stackSize -= expr → shrink(expr)  (after the above substitutions, no -= remains for stackSize)
c = re.sub(r'\b(\w+)\.stackSize\s*-=\s*([^;]+);', lambda m: f'{m.group(1)}.shrink({m.group(2).strip()});', c)

# 11. EnumEnumFacing.getOrientation(a) → EnumFacing.byIndex(a)
c = c.replace('EnumEnumFacing.getOrientation(', 'EnumFacing.byIndex(')

# 12. dir.offsetX/Z → dir.getXOffset()/getZOffset() (EnumFacing facing fields → methods)
c = re.sub(r'\bdir\.offsetX\b', 'dir.getXOffset()', c)
c = re.sub(r'\bdir\.offsetY\b', 'dir.getYOffset()', c)
c = re.sub(r'\bdir\.offsetZ\b', 'dir.getZOffset()', c)

# 13. AxisAlignedBB.getBoundingBox(x1,y1,z1,x2,y2,z2) → new AxisAlignedBB(x1,y1,z1,x2,y2,z2)
c = c.replace('AxisAlignedBB.getBoundingBox(', 'new AxisAlignedBB(')

# 14. FluidRegistry.WATER.getID() / fluid.getID() — these APIs don't exist in 1.12.2
#     Fluid equality is now done by reference: resource.getFluid() == FluidRegistry.WATER
c = c.replace('resource.getFluid() != FluidRegistry.WATER.getID()',
              'resource.getFluid() != FluidRegistry.WATER')
c = c.replace('resource.getFluidID() != FluidRegistry.WATER.getID()',
              'resource.getFluid() != FluidRegistry.WATER')
c = c.replace('fluid.getID() == FluidRegistry.WATER.getID()',
              'fluid == FluidRegistry.WATER')
c = c.replace('fluid != null && fluid.getID() == FluidRegistry.WATER.getID()',
              'fluid == FluidRegistry.WATER')
# Also fix the fill method condition
c = c.replace(
    'resource != null && resource.getFluidID() != FluidRegistry.WATER.getID()',
    'resource == null || resource.getFluid() != FluidRegistry.WATER'
)
# Catch remaining getFluidID() / getID() usages
c = re.sub(r'resource\.getFluidID\(\)', 'resource.getFluid().getID()', c)

# 15. IFluidHandler interface - add missing getTankProperties() and fix method signatures
#     The class has fill(EnumFacing, ...) / drain(EnumFacing, ...) — not matching the 1.12.2 IFluidHandler.
#     Strategy: add the correct @Override methods, and rename the old ones to keep them as non-interface helpers.

# Remove EnumFacing from fill signature to satisfy interface
c = c.replace(
    'public int fill(EnumFacing from, FluidStack resource, boolean doFill)',
    '@Override\n   public int fill(FluidStack resource, boolean doFill)'
)
c = c.replace(
    'public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)',
    '@Override\n   public FluidStack drain(FluidStack resource, boolean doDrain)'
)
c = c.replace(
    'public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)',
    '@Override\n   public FluidStack drain(int maxDrain, boolean doDrain)'
)

# Add getTankProperties() before the fill method, and add IFluidTankProperties import
# First add import
if 'IFluidTankProperties' not in c:
    c = c.replace(
        'import net.minecraftforge.fluids.capability.IFluidHandler;',
        'import net.minecraftforge.fluids.capability.IFluidHandler;\nimport net.minecraftforge.fluids.capability.IFluidTankProperties;'
    )

# Insert getTankProperties() before the @Override fill line
tank_props_method = '''
   @Override
   public IFluidTankProperties[] getTankProperties() {
      return tank.getTankProperties();
   }

   '''
c = c.replace(
    '@Override\n   public int fill(FluidStack resource, boolean doFill)',
    tank_props_method + '@Override\n   public int fill(FluidStack resource, boolean doFill)'
)

# Fix the fill method body — it used to reference 'from' param for markBlockRangeForRenderUpdate
# The worldObj.markBlockRangeForRenderUpdate was already present and uses BlockPos, should be OK
# But 'from' is no longer a parameter — check if it's used in the body (it wasn't in the original)

# 16. FluidTankInfo[] → IFluidTankProperties[] (for the old getTankInfo method)
c = c.replace('FluidTankInfo[]', 'IFluidTankProperties[]')
c = c.replace('this.tank.getInfo()', 'this.tank.getTankProperties()[0]')
# Remove the unused FluidTankInfo import if present
c = c.replace('import net.minecraftforge.fluids.FluidTankInfo;\n', '')

# 17. resource.amount (FluidStack field is public in 1.12.2, should be fine — leave as is)

# 18. Remove canFill/canDrain methods if they use old Fluid.getID() API
# (they're not interface methods in 1.12.2 IFluidHandler, but they do reference old APIs)
# The canFill one has: fluid != null && fluid.getID() == FluidRegistry.WATER.getID()
# We already fixed that above to: fluid == FluidRegistry.WATER

write(crucible_path, c)
print("Fixed TileCrucible.java")
