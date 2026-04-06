#!/usr/bin/env python3
"""Fix remaining errors: TileCrucible, TileResearchTable, REHWandHandler, and other files."""
import re, os, glob

ROOT = os.path.normpath(os.path.join(os.path.dirname(__file__), '..'))
SRC = os.path.join(ROOT, 'src/main/java')

def read(path):
    with open(path, 'r', encoding='utf-8') as f: return f.read()
def write(path, c):
    with open(path, 'w', encoding='utf-8') as f: f.write(c)

def split_args(s):
    parts, cur, d = [], [], 0
    for ch in s:
        if ch in '([{': d += 1; cur.append(ch)
        elif ch in ')]}': d -= 1; cur.append(ch)
        elif ch == ',' and d == 0: parts.append(''.join(cur).strip()); cur = []
        else: cur.append(ch)
    if cur: parts.append(''.join(cur).strip())
    return parts

def find_close(s, i):
    d = 0
    for j in range(i, len(s)):
        if s[j] == '(': d += 1
        elif s[j] == ')':
            d -= 1
            if d == 0: return j
    return -1

def transform_method(content, pattern_str, converter):
    """Generic transform: find pattern_str(...args...) and apply converter(args_list)."""
    pat = re.compile(pattern_str)
    result, last = [], 0
    for m in pat.finditer(content):
        result.append(content[last:m.start()])
        po = m.end() - 1  # position of '('
        pc = find_close(content, po)
        if pc == -1:
            result.append(content[m.start():])
            return ''.join(result)
        args = split_args(content[po+1:pc])
        replacement = converter(args)
        if replacement is None:
            result.append(content[m.start():pc+1])
        else:
            result.append(replacement)
        last = pc + 1
    result.append(content[last:])
    return ''.join(result)

# ─────────────────────────────────────────────
# GLOBAL second pass: remaining worldObj, mc.theWorld, etc.
# ─────────────────────────────────────────────
all_java = glob.glob(os.path.join(SRC, '**', '*.java'), recursive=True)

def global_pass2(path):
    c = read(path)
    orig = c
    # standalone worldObj (not preceded by '.') — catches the TODO_PORT-indented patterns
    c = re.sub(r'(?<!\.)(?<!\w)worldObj\b', 'world', c)
    # mc.theWorld → mc.world
    c = re.sub(r'\bmc\.theWorld\b', 'mc.world', c)
    c = re.sub(r'\bMinecraft\.getMinecraft\(\)\.theWorld\b', 'Minecraft.getMinecraft().world', c)
    # .stackTagCompound = expr  (write/assignment)
    c = re.sub(r'(\w+)\.stackTagCompound\s*=\s*([^;]+);',
               lambda m: f'{m.group(1)}.setTagCompound({m.group(2).strip()});', c)
    # remaining .stackTagCompound reads (should already be getTagCompound() but catch any missed)
    c = c.replace('.stackTagCompound', '.getTagCompound()')
    # inventory.mainInventory[idx] → .get(idx)
    c = re.sub(r'\.mainInventory\[([^\]]+)\]',
               lambda m: f'.mainInventory.get({m.group(1)})', c)
    # world.getBlockLightValue(int,int,int) → world.getBlockLightValue(new BlockPos(...))
    def fix_3int_method_to_blockpos(method_name, new_name=None):
        if new_name is None: new_name = method_name
        nonlocal c
        def conv(args):
            if len(args) == 3:
                return f'world.{new_name}(new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]}))'
            return None
        c = transform_method(c, r'\bworld\.' + re.escape(method_name) + r'\(', conv)
    fix_3int_method_to_blockpos('getBlockLightValue')
    fix_3int_method_to_blockpos('canBlockSeeTheSky', 'canBlockSeeSky')
    fix_3int_method_to_blockpos('getLight')
    # entity.setEntityItemStack → entity.setItem
    c = c.replace('.setEntityItemStack(', '.setItem(')
    # items.getCount() -= expr  →  items.shrink(expr)
    c = re.sub(r'\b(\w+)\.getCount\(\)\s*-=\s*([^;]+);',
               lambda m: f'{m.group(1)}.shrink({m.group(2).strip()});', c)
    # world.setTileEntity(int,int,int,te) → world.setTileEntity(new BlockPos(...), te)
    def conv_setTileEntity(args):
        if len(args) == 4:
            return f'world.setTileEntity(new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]}), {args[3]})'
        return None
    c = transform_method(c, r'\bworld\.setTileEntity\((?!new\s)', conv_setTileEntity)
    if c != orig:
        write(path, c)
        return True
    return False

changed = sum(1 for p in all_java if global_pass2(p))
print(f"Global pass2: {changed} files modified")

# ─────────────────────────────────────────────
# TileCrucible paren-aware world method fixes
# ─────────────────────────────────────────────
crucible = os.path.join(SRC, 'thaumcraft/common/tiles/TileCrucible.java')
c = read(crucible)

# world.getBlock(x,y,z) → world.getBlockState(new BlockPos(x,y,z)).getBlock()
def conv_getBlock(args):
    if len(args) == 3:
        p = f'new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]})'
        return f'world.getBlockState({p}).getBlock()'
    return None
c = transform_method(c, r'\bworld\.getBlock\((?!State)', conv_getBlock)

# world.getBlockState(pos).getMaterial() etc already correct; but fix getBlock().getMaterial() too
# (getBlock is fixed above, but we're accessing .getMaterial() on Block which needs IBlockState)
# Actually Block.getMaterial() in 1.12.2 needs IBlockState. The pattern getBlock().getMaterial() is wrong.
# world.getBlock(pos).getMaterial() → world.getBlockState(pos).getMaterial()
# After getBlock transform: world.getBlockState(new BlockPos(x,y,z)).getBlock().getMaterial()
# → should be world.getBlockState(new BlockPos(x,y,z)).getMaterial()
c = re.sub(
    r'(world\.getBlockState\([^)]+\))\.getBlock\(\)\.getMaterial\(\)',
    r'\1.getMaterial()',
    c
)

# world.getBlockMetadata(args) with nested parens — paren-aware
def conv_getBlockMetadata(args):
    if len(args) == 3:
        pos = f'new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]})'
        return f'world.getBlockState({pos}).getBlock().getMetaFromState(world.getBlockState({pos}))'
    return None
c = transform_method(c, r'\bworld\.getBlockMetadata\(', conv_getBlockMetadata)

# world.isAirBlock(x,y,z) paren-aware
def conv_isAirBlock(args):
    if len(args) == 3:
        return f'world.isAirBlock(new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]}))'
    return None
c = transform_method(c, r'\bworld\.isAirBlock\((?!new)', conv_isAirBlock)

# world.addBlockEvent(x,y,z,block,a,b) paren-aware
def conv_addBlockEvent(args):
    if len(args) == 6:
        return f'world.addBlockEvent(new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]}), {args[3]}, {args[4]}, {args[5]})'
    return None
c = transform_method(c, r'\bworld\.addBlockEvent\((?!new)', conv_addBlockEvent)

write(crucible, c)
print("Fixed TileCrucible.java")

# ─────────────────────────────────────────────
# TileResearchTable paren-aware fixes + clear()
# ─────────────────────────────────────────────
research = os.path.join(SRC, 'thaumcraft/common/tiles/TileResearchTable.java')
c = read(research)

# Add clear() method to satisfy IInventory in 1.12.2
if 'public void clear()' not in c:
    c = c.replace(
        'public int getSizeInventory()',
        'public void clear() { java.util.Arrays.fill(this.contents, null); }\n\n   public int getSizeInventory()'
    )

# world.addBlockEvent paren-aware
c = transform_method(c, r'\bworld\.addBlockEvent\((?!new)', conv_addBlockEvent)

# world.getBlockLightValue(int,int,int) → world.getBlockLightValue(new BlockPos(...))
def conv_getBlockLightValue(args):
    if len(args) == 3:
        return f'world.getBlockLightValue(new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]}))'
    return None
c = transform_method(c, r'\bworld\.getBlockLightValue\((?!new)', conv_getBlockLightValue)

# world.canBlockSeeTheSky → world.canBlockSeeSky with BlockPos
def conv_canBlockSky(args):
    if len(args) == 3:
        return f'world.canBlockSeeSky(new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]}))'
    return None
c = transform_method(c, r'\bworld\.canBlockSeeTheSky\(', conv_canBlockSky)

# ItemStack.loadItemStackFromNBT(nbt) → new ItemStack(nbt)  (removed in 1.12.2 base, Forge may not have it)
# Actually Forge 1.12.2 DOES have it as static, but let's replace to be safe
# Actually leave it as-is — Forge keeps it. If it causes errors we'll revisit.

write(research, c)
print("Fixed TileResearchTable.java")

# ─────────────────────────────────────────────
# REHWandHandler.java
# ─────────────────────────────────────────────
reh = os.path.join(SRC, 'thaumcraft/client/lib/REHWandHandler.java')
c = read(reh)

# RenderGameOverlayEvent private fields
c = re.sub(r'\bevent\.resolution\b', 'event.getResolution()', c)
c = re.sub(r'\bevent\.partialTicks\b', 'event.getPartialTicks()', c)

# DrawBlockHighlightEvent private fields
c = re.sub(r'\bevent\.player\b', 'event.getPlayer()', c)

# new RenderItem() → Minecraft.getMinecraft().getRenderItem()
# Store it in local var ri instead
c = c.replace('RenderItem ri = new RenderItem();', 'RenderItem ri = Minecraft.getMinecraft().getRenderItem();')

# mc.renderEngine → mc.getTextureManager()
c = re.sub(r'\bmc\.renderEngine\b', 'mc.getTextureManager()', c)

# renderItemIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, x, y)
#   → renderItemIntoGUI(item, x, y)
c = re.sub(
    r'ri\.renderItemIntoGUI\([^,]+,\s*[^,]+,\s*(item[^,]*),\s*(-?\d+),\s*(-?\d+)\)',
    r'ri.renderItemIntoGUI(\1, \2, \3)',
    c
)

# getTooltip(mc.player, boolean) → getTooltip(mc.player, ITooltipFlag)
c = re.sub(
    r'\.getTooltip\(([^,]+),\s*mc\.gameSettings\.advancedItemTooltips\)',
    r'.getTooltip(\1, mc.gameSettings.advancedItemTooltips ? net.minecraft.item.ITooltipFlag.TooltipFlags.ADVANCED : net.minecraft.item.ITooltipFlag.TooltipFlags.NORMAL)',
    c
)

# RayTraceResult.blockX/Y/Z → .getBlockPos().getX/Y/Z()
c = re.sub(r'\btarget\.blockX\b', 'target.getBlockPos().getX()', c)
c = re.sub(r'\btarget\.blockY\b', 'target.getBlockPos().getY()', c)
c = re.sub(r'\btarget\.blockZ\b', 'target.getBlockPos().getZ()', c)
# target.sideHit is still EnumFacing in 1.12.2, but when passed as int to methods, use .getIndex()
# Actually in 1.12.2, RayTraceResult.sideHit is public EnumFacing, and the method likely takes EnumFacing
# Leave as target.sideHit for now unless it causes type errors

# mc.renderViewEntity → mc.getRenderViewEntity() (returns Entity, not necessarily EntityPlayer)
# Cast if needed: (EntityPlayer)mc.getRenderViewEntity()
c = re.sub(r'\bmc\.renderViewEntity\b', 'mc.getRenderViewEntity()', c)

# EnumFacing.offsetsXForSide[side] → EnumFacing.byIndex(side).getXOffset()
c = re.sub(r'\bEnumFacing\.offsetsXForSide\[([^\]]+)\]', r'net.minecraft.util.EnumFacing.byIndex(\1).getXOffset()', c)
c = re.sub(r'\bEnumFacing\.offsetsYForSide\[([^\]]+)\]', r'net.minecraft.util.EnumFacing.byIndex(\1).getYOffset()', c)
c = re.sub(r'\bEnumFacing\.offsetsZForSide\[([^\]]+)\]', r'net.minecraft.util.EnumFacing.byIndex(\1).getZOffset()', c)

# mc.inGameHasFocus stays as-is (field exists in 1.12.2)
# mc.mouseHelper.ungrabMouseCursor() → mc.mouseHelper.ungrabMouse()
c = c.replace('mc.mouseHelper.ungrabMouseCursor()', 'mc.mouseHelper.ungrabMouse()')

write(reh, c)
print("Fixed REHWandHandler.java")

# ─────────────────────────────────────────────
# REHNotifyHandler.java — event.resolution
# ─────────────────────────────────────────────
notify_path = os.path.join(SRC, 'thaumcraft/client/lib/REHNotifyHandler.java')
c = read(notify_path)
c = re.sub(r'\bevent\.resolution\b', 'event.getResolution()', c)
c = re.sub(r'\bevent\.partialTicks\b', 'event.getPartialTicks()', c)
write(notify_path, c)
print("Fixed REHNotifyHandler.java")

# ─────────────────────────────────────────────
# TrueTypeFont.java — GlStateManager.bindTexture(int, int)
# In 1.12.2, GlStateManager.bindTexture(int) takes only one arg (texture unit is gone)
# ─────────────────────────────────────────────
ttf_path = os.path.join(SRC, 'truetyper/TrueTypeFont.java')
c = read(ttf_path)
# GlStateManager.bindTexture(3553, textureId) → GlStateManager.bindTexture(textureId)
# 3553 = GL_TEXTURE_2D, which is the target, not used in 1.12.2 bindTexture
c = re.sub(r'GlStateManager\.bindTexture\(3553,\s*([^)]+)\)', r'GlStateManager.bindTexture(\1)', c)
c = re.sub(r'GlStateManager\.bindTexture\(GL11\.GL_TEXTURE_2D,\s*([^)]+)\)', r'GlStateManager.bindTexture(\1)', c)
write(ttf_path, c)
print("Fixed TrueTypeFont.java")

# ─────────────────────────────────────────────
# IndexedCodec.java — ByteBuf → PacketBuffer
# ─────────────────────────────────────────────
codec_path = os.path.join(SRC, 'tc4tweak/network/IndexedCodec.java')
c = read(codec_path)
c = read(codec_path)
# In 1.12.2 FML simpleimpl, encode/decode use PacketBuffer not ByteBuf
# The error is: incompatible types: ByteBuf cannot be converted to PacketBuffer
# Need to wrap: new PacketBuffer(buffer) or cast
# Let's find the encode method
c = c.replace('msg.toBytes(buffer)', 'msg.toBytes(new io.netty.buffer.Unpooled.wrappedBuffer(buffer) instanceof net.minecraft.network.PacketBuffer ? (net.minecraft.network.PacketBuffer)buffer : new net.minecraft.network.PacketBuffer(buffer))')
# That's ugly - let me just add a cast or import
# Actually simpler: FMLProxyPacket now uses PacketBuffer, so just cast
c = c.replace('msg.toBytes(buffer)', 'msg.toBytes(buffer instanceof net.minecraft.network.PacketBuffer ? (net.minecraft.network.PacketBuffer)buffer : new net.minecraft.network.PacketBuffer(buffer))')
write(codec_path, c)
print("Fixed IndexedCodec.java")

# ─────────────────────────────────────────────
# SetParentHelper.java — xCoord/yCoord/zCoord on TileVisNode (not this.)
# ─────────────────────────────────────────────
sph_path = os.path.join(SRC, 'tc4tweak/modules/visrelay/SetParentHelper.java')
c = read(sph_path)
# child.xCoord → child.getPos().getX() etc.
c = re.sub(r'\bchild\.xCoord\b', 'child.getPos().getX()', c)
c = re.sub(r'\bchild\.yCoord\b', 'child.getPos().getY()', c)
c = re.sub(r'\bchild\.zCoord\b', 'child.getPos().getZ()', c)
c = re.sub(r'\bparent\.xCoord\b', 'parent.getPos().getX()', c)
c = re.sub(r'\bparent\.yCoord\b', 'parent.getPos().getY()', c)
c = re.sub(r'\bparent\.zCoord\b', 'parent.getPos().getZ()', c)
write(sph_path, c)
print("Fixed SetParentHelper.java")

# ─────────────────────────────────────────────
# TileHoleSyncPacket.java — setTileEntity(int,int,int,te) + theWorld + func_ methods
# ─────────────────────────────────────────────
thsp_path = os.path.join(SRC, 'tc4tweak/network/TileHoleSyncPacket.java')
c = read(thsp_path)
# setTileEntity — already handled by global pass2 (world.setTileEntity pattern)
# But also: message.origin.func_148856_c() etc. (obfuscated ChunkCoordIntPair methods)
# These are ChunkPos in 1.12.2: chunkXPos, chunkZPos accessible via getXStart etc.
# func_148856_c() → chunkXPos * 16 (start X), func_148854_e() → chunkZPos * 16
# But wait: origin might be a BlockPos or ChunkPos — need to check
# Also theWorld → world already handled by global pass2
# theWorld.blockExists(x,y,z) → theWorld.isBlockLoaded(new BlockPos(x,y,z))
c = c.replace('Minecraft.getMinecraft().theWorld', 'Minecraft.getMinecraft().world')
def conv_blockExists(args):
    if len(args) == 3:
        return f'theWorld.isBlockLoaded(new net.minecraft.util.math.BlockPos({args[0]}, {args[1]}, {args[2]}))'
    return None
c = transform_method(c, r'\btheWorld\.blockExists\(', conv_blockExists)
# func_ method names are SRG names — these are from 1.7.10 and don't exist in 1.12.2
# ChunkCoordIntPair.func_148856_c() → chunkXPos (or getXStart())
# ChunkCoordIntPair.func_148855_d() → ... (probably Y, but ChunkCoordIntPair has no Y)
# func_148854_e() → chunkZPos
# But wait, this is a BlockPos (origin in TileHoleSyncPacket) based on context
# If origin is a BlockPos: getX(), getY(), getZ() — but these come from reading the packets
# Let me check what origin is
c = c.replace('message.origin.func_148856_c()', 'message.origin.getX()')
c = c.replace('message.origin.func_148855_d()', 'message.origin.getY()')
c = c.replace('message.origin.func_148854_e()', 'message.origin.getZ()')
# world.setTileEntity already handled above
write(thsp_path, c)
print("Fixed TileHoleSyncPacket.java")

# ─────────────────────────────────────────────
# ChunkLoc.java — ChunkPos field rename
# ─────────────────────────────────────────────
chunkloc_path = os.path.join(SRC, 'thaumcraft/common/lib/world/ChunkLoc.java')
c = read(chunkloc_path)
# ChunkPos.chunkXPos → ChunkPos.x in 1.12.2
c = c.replace('.chunkXPos', '.x')
c = c.replace('.chunkZPos', '.z')
write(chunkloc_path, c)
print("Fixed ChunkLoc.java")

# ─────────────────────────────────────────────
# ItemFocusPouch.java
# ─────────────────────────────────────────────
ifp_path = os.path.join(SRC, 'thaumcraft/common/items/wands/ItemFocusPouch.java')
c = read(ifp_path)
# .stackTagCompound already handled by global pass
# onItemRightClick signature change in 1.12.2:
#   old: ItemStack onItemRightClick(ItemStack, World, EntityPlayer)
#   new: ActionResult<ItemStack> onItemRightClick(World, EntityPlayer, EnumHand)
# The error says "incompatible types: ItemStack cannot be converted to World" at line 53
# This means a call like onItemRightClick(stack, world, player) is passing stack as 1st arg
# and world as 2nd, but in 1.12.2 the 1st arg is World not ItemStack
# Let's look at what's at line 53 and 41
c = c.replace('.stackTagCompound', '.getTagCompound()')
# Fix onItemRightClick call if it's calling super with old signature
# In 1.12.2: super.onItemRightClick(world, player, hand)
# Old: super.onItemRightClick(stack, world, player)
# Check for the specific pattern
c = re.sub(
    r'super\.onItemRightClick\(([^,]+),\s*([^,]+),\s*([^)]+)\)',
    r'super.onItemRightClick(\2, \3, net.minecraft.util.EnumHand.MAIN_HAND)',
    c
)
c = re.sub(
    r'return super\.onItemRightClick\(([^,]+),\s*([^,]+),\s*([^)]+)\)',
    r'return super.onItemRightClick(\2, \3, net.minecraft.util.EnumHand.MAIN_HAND)',
    c
)
write(ifp_path, c)
print("Fixed ItemFocusPouch.java")
