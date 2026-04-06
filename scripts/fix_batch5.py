#!/usr/bin/env python3
"""Batch 5: Fix remaining compile errors across multiple files."""
import re
import os

SRC = r"src\main\java"

def read(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def write(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

def patch(path, old, new):
    content = read(path)
    if old not in content:
        print(f"  MISS: {path}: pattern not found: {old[:60]!r}")
        return False
    content = content.replace(old, new, 1)
    write(path, content)
    print(f"  OK: {path}")
    return True

def patch_all(path, old, new):
    content = read(path)
    if old not in content:
        print(f"  MISS: {path}: pattern not found: {old[:60]!r}")
        return False
    count = content.count(old)
    content = content.replace(old, new)
    write(path, content)
    print(f"  OK ({count}x): {path}")
    return True

# ============================================================
# IArchitect implementors: int side -> EnumFacing side
# ============================================================

ARCHITECT_FILES = [
    r"src\main\java\thaumcraft\common\items\wands\ItemWandCasting.java",
    r"src\main\java\thaumcraft\common\items\equipment\ItemElementalShovel.java",
    r"src\main\java\thaumcraft\common\items\wands\foci\ItemFocusWarding.java",
    r"src\main\java\thaumcraft\common\items\wands\foci\ItemFocusTrade.java",
]

print("=== IArchitect: int side -> EnumFacing side ===")
for f in ARCHITECT_FILES:
    content = read(f)
    changed = False
    # getArchitectBlocks(... int side ...) -> EnumFacing side
    if 'getArchitectBlocks(' in content and 'int side' in content:
        content = content.replace(
            'ArrayList getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, int side, EntityPlayer player)',
            'ArrayList getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, net.minecraft.util.EnumFacing side, EntityPlayer player)',
            1
        )
        content = content.replace(
            'ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack focusstack, World world, int x, int y, int z, int side, EntityPlayer player)',
            'ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack focusstack, World world, int x, int y, int z, net.minecraft.util.EnumFacing side, EntityPlayer player)',
            1
        )
        content = content.replace(
            'ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, int side, EntityPlayer player)',
            'ArrayList<BlockCoordinates> getArchitectBlocks(ItemStack stack, World world, int x, int y, int z, net.minecraft.util.EnumFacing side, EntityPlayer player)',
            1
        )
        changed = True
    # showAxis(... int side ...) -> EnumFacing side
    if 'showAxis(' in content and 'int side' in content:
        content = content.replace(
            'public boolean showAxis(ItemStack stack, World world, EntityPlayer player, int side, IArchitect.EnumAxis axis)',
            'public boolean showAxis(ItemStack stack, World world, EntityPlayer player, net.minecraft.util.EnumFacing side, IArchitect.EnumAxis axis)',
            1
        )
        changed = True
    if changed:
        write(f, content)
        print(f"  OK: {f}")
    else:
        print(f"  SKIP (no changes): {f}")

# ============================================================
# ItemFocusWarding and ItemFocusTrade: mop.blockX/Y/Z -> getBlockPos()
# ============================================================
print("\n=== ItemFocusWarding / ItemFocusTrade: mop.blockX -> getBlockPos() ===")
for f in [
    r"src\main\java\thaumcraft\common\items\wands\foci\ItemFocusWarding.java",
    r"src\main\java\thaumcraft\common\items\wands\foci\ItemFocusTrade.java",
]:
    content = read(f)
    changed = False
    # mop.blockX -> mop.getBlockPos().getX() etc.
    for coord, method in [('blockX', 'getX'), ('blockY', 'getY'), ('blockZ', 'getZ')]:
        old = f'mop.{coord}'
        new = f'mop.getBlockPos().{method}()'
        if old in content:
            content = content.replace(old, new)
            changed = True
    if changed:
        write(f, content)
        print(f"  OK: {f}")

# ============================================================
# IndexedCodec.java: fix broken PacketBuffer line
# ============================================================
print("\n=== IndexedCodec.java: fix PacketBuffer ===")
codec = r"src\main\java\tc4tweak\network\IndexedCodec.java"
content = read(codec)
old_line = '        msg.toBytes(new io.netty.buffer.Unpooled.wrappedBuffer(buffer) instanceof net.minecraft.network.PacketBuffer ? (net.minecraft.network.PacketBuffer)buffer : new net.minecraft.network.PacketBuffer(buffer));'
new_line = '        msg.toBytes(new net.minecraft.network.PacketBuffer(buffer));'
if old_line in content:
    content = content.replace(old_line, new_line)
    write(codec, content)
    print(f"  OK: {codec}")
else:
    print(f"  MISS: {codec}")

# ============================================================
# ItemFocusPouch.java: fix onItemRightClick signature + loadItemStackFromNBT
# ============================================================
print("\n=== ItemFocusPouch.java: fix signatures ===")
pouch = r"src\main\java\thaumcraft\common\items\wands\ItemFocusPouch.java"
content = read(pouch)

# Fix onItemRightClick signature and super call
old_method = '   public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {\n      if (!par2World.isRemote) {\n         par3EntityPlayer.openGui(Thaumcraft.instance, 5, par2World, MathHelper.floor(par3EntityPlayer.posX), MathHelper.floor(par3EntityPlayer.posY), MathHelper.floor(par3EntityPlayer.posZ));\n      }\n\n      return super.onItemRightClick(par3EntityPlayer, net.minecraft.util.EnumHand.MAIN_HAND, net.minecraft.util.EnumHand.MAIN_HAND);\n   }'
new_method = '   public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer par3EntityPlayer, net.minecraft.util.EnumHand hand) {\n      if (!par2World.isRemote) {\n         par3EntityPlayer.openGui(Thaumcraft.instance, 5, par2World, net.minecraft.util.math.MathHelper.floor(par3EntityPlayer.posX), net.minecraft.util.math.MathHelper.floor(par3EntityPlayer.posY), net.minecraft.util.math.MathHelper.floor(par3EntityPlayer.posZ));\n      }\n\n      return new net.minecraft.util.ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, par3EntityPlayer.getHeldItem(hand));\n   }'
if old_method in content:
    content = content.replace(old_method, new_method)
    print(f"  OK: onItemRightClick fixed")
else:
    print(f"  MISS: onItemRightClick pattern")

# Fix loadItemStackFromNBT
content = content.replace('ItemStack.loadItemStackFromNBT(var4)', 'new ItemStack(var4)')
write(pouch, content)
print(f"  OK: {pouch}")

# ============================================================
# BonusTagForItemListeners.java: enchantment API migration
# ============================================================
print("\n=== BonusTagForItemListeners.java: enchantment API ===")
bonus = r"src\main\java\thaumcraft\api\expands\aspects\item\consts\BonusTagForItemListeners.java"
content = read(bonus)

# Add Enchantments import
if 'import net.minecraft.init.Enchantments;' not in content:
    content = content.replace(
        'import net.minecraft.enchantment.Enchantment;',
        'import net.minecraft.enchantment.Enchantment;\nimport net.minecraft.init.Enchantments;'
    )

# Fix func_150931_i() - ItemSword attack damage via attributes
old_sword = '            if (item instanceof ItemSword && ((ItemSword) item).func_150931_i() + 1.0F > 0.0F) {\n                currentAspects.merge(Aspect.WEAPON, (int) (((ItemSword) item).func_150931_i() + 1.0F));\n            }'
new_sword = '            if (item instanceof ItemSword) {\n                com.google.common.collect.Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> attrs = item.getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND);\n                java.util.Collection<net.minecraft.entity.ai.attributes.AttributeModifier> mods = attrs.get(net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE.getName());\n                if (!mods.isEmpty()) {\n                    double dmg = mods.iterator().next().getAmount();\n                    if (dmg + 1.0 > 0.0) {\n                        currentAspects.merge(Aspect.WEAPON, (int)(dmg + 1.0));\n                    }\n                }\n            }'
if old_sword in content:
    content = content.replace(old_sword, new_sword)
    print(f"  OK: sword fix")
else:
    print(f"  MISS: sword pattern")

# Fix func_92110_g - ItemEnchantedBook.getEnchantments
content = content.replace(
    '((ItemEnchantedBook) item).func_92110_g(itemstack)',
    'ItemEnchantedBook.getEnchantments(itemstack)'
)

# Fix enchantment comparisons: add Enchantment.getEnchantmentByID lookup after eid/lvl reads
old_loop = '                    for (int var3 = 0; var3 < ench.tagCount(); ++var3) {\n                        short eid = ench.getCompoundTagAt(var3).getShort("id");\n                        short lvl = ench.getCompoundTagAt(var3).getShort("lvl");\n                        if (eid == Enchantment.aquaAffinity.effectId) {'
new_loop = '                    for (int var3 = 0; var3 < ench.tagCount(); ++var3) {\n                        short eid = ench.getCompoundTagAt(var3).getShort("id");\n                        short lvl = ench.getCompoundTagAt(var3).getShort("lvl");\n                        Enchantment enchObj = Enchantment.getEnchantmentByID(eid & 0xFFFF);\n                        if (enchObj == Enchantments.AQUA_AFFINITY) {'
if old_loop in content:
    content = content.replace(old_loop, new_loop)
    print(f"  OK: loop fix")
else:
    print(f"  MISS: loop pattern")

# Replace all enchantment comparisons
ENCHANTMENT_MAP = [
    ('eid == Enchantment.baneOfArthropods.effectId',    'enchObj == Enchantments.BANE_OF_ARTHROPODS'),
    ('eid == Enchantment.blastProtection.effectId',     'enchObj == Enchantments.BLAST_PROTECTION'),
    ('eid == Enchantment.efficiency.effectId',          'enchObj == Enchantments.EFFICIENCY'),
    ('eid == Enchantment.featherFalling.effectId',      'enchObj == Enchantments.FEATHER_FALLING'),
    ('eid == Enchantment.fireAspect.effectId',          'enchObj == Enchantments.FIRE_ASPECT'),
    ('eid == Enchantment.fireProtection.effectId',      'enchObj == Enchantments.FIRE_PROTECTION'),
    ('eid == Enchantment.flame.effectId',               'enchObj == Enchantments.FLAME'),
    ('eid == Enchantment.fortune.effectId',             'enchObj == Enchantments.FORTUNE'),
    ('eid == Enchantment.infinity.effectId',            'enchObj == Enchantments.INFINITY'),
    ('eid == Enchantment.knockback.effectId',           'enchObj == Enchantments.KNOCKBACK'),
    ('eid == Enchantment.looting.effectId',             'enchObj == Enchantments.LOOTING'),
    ('eid == Enchantment.power.effectId',               'enchObj == Enchantments.POWER'),
    ('eid == Enchantment.projectileProtection.effectId','enchObj == Enchantments.PROJECTILE_PROTECTION'),
    ('eid == Enchantment.protection.effectId',          'enchObj == Enchantments.PROTECTION'),
    ('eid == Enchantment.punch.effectId',               'enchObj == Enchantments.PUNCH'),
    ('eid == Enchantment.respiration.effectId',         'enchObj == Enchantments.RESPIRATION'),
    ('eid == Enchantment.sharpness.effectId',           'enchObj == Enchantments.SHARPNESS'),
    ('eid == Enchantment.silkTouch.effectId',           'enchObj == Enchantments.SILK_TOUCH'),
    ('eid == Enchantment.thorns.effectId',              'enchObj == Enchantments.THORNS'),
    ('eid == Enchantment.smite.effectId',               'enchObj == Enchantments.SMITE'),
    ('eid == Enchantment.unbreaking.effectId',          'enchObj == Enchantments.UNBREAKING'),
    ('eid == Enchantment.field_151370_z.effectId',      'enchObj == Enchantments.LUCK_OF_THE_SEA'),
    ('eid == Enchantment.field_151369_A.effectId',      'enchObj == Enchantments.LURE'),
    ('eid == Config.enchHaste.effectId',                'enchObj == Config.enchHaste'),
    ('eid == Config.enchRepair.effectId',               'enchObj == Config.enchRepair'),
]
for old, new in ENCHANTMENT_MAP:
    if old in content:
        content = content.replace(old, new)
        print(f"  OK: {old[:40]}")
    else:
        print(f"  MISS: {old[:40]}")

write(bonus, content)
print(f"  Written: {bonus}")

# ============================================================
# TileResearchTable.java: multiple fixes
# ============================================================
print("\n=== TileResearchTable.java ===")
tile = r"src\main\java\thaumcraft\common\tiles\TileResearchTable.java"
content = read(tile)

# Add missing imports
imports_to_add = []
if 'import net.minecraft.init.SoundEvents;' not in content:
    imports_to_add.append('import net.minecraft.init.SoundEvents;')
if 'import net.minecraft.util.SoundCategory;' not in content:
    imports_to_add.append('import net.minecraft.util.SoundCategory;')
if 'import net.minecraft.world.EnumSkyBlock;' not in content:
    imports_to_add.append('import net.minecraft.world.EnumSkyBlock;')
if 'import net.minecraft.block.state.IBlockState;' not in content:
    imports_to_add.append('import net.minecraft.block.state.IBlockState;')

if imports_to_add:
    content = content.replace(
        'import net.minecraft.util.math.BlockPos;',
        'import net.minecraft.util.math.BlockPos;\n' + '\n'.join(imports_to_add)
    )

# updateEntity -> update
content = content.replace('public void updateEntity() {', 'public void update() {')
content = content.replace('super.updateEntity();', 'super.update();')

# canUpdate: remove super.canUpdate() call
content = content.replace(
    '   public boolean canUpdate() {\n       return super.canUpdate();\n   }',
    '   public boolean canUpdate() {\n       return true;\n   }'
)

# loadItemStackFromNBT -> new ItemStack
content = content.replace('ItemStack.loadItemStackFromNBT(var4)', 'new ItemStack(var4)')

# playSoundAtEntity -> playSound
content = content.replace(
    'this.world.playSoundAtEntity(player, "random.orb", 0.2F, 0.9F + player.world.rand.nextFloat() * 0.2F)',
    'player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, 0.9F + player.world.rand.nextFloat() * 0.2F)'
)

# world.getBlockLightValue(BlockPos) -> world.getLightFor(EnumSkyBlock.BLOCK, pos)
content = re.sub(
    r'this\.world\.getBlockLightValue\(new net\.minecraft\.util\.math\.BlockPos\(([^)]+)\)\)',
    r'this.world.getLightFor(EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(\1))',
    content
)

# Block fetch with 3-int -> IBlockState based (the TODO_PORT block in recalculateBonus)
old_block_fetch = '''                  Block bi = this.world.getBlock(x + this.getPos().getX(), y + this.getPos().getY(), z + this.getPos().getZ());
                  int md = this./* TODO_PORT: getBlockMetadata -> world.getBlockState(new BlockPos(x,y,z)).getValue(PROP) */
        world.getBlockMetadata(x + this.getPos().getX(), y + this.getPos().getY(), z + this.getPos().getZ());
                  Material bm = bi.getMaterial();'''
new_block_fetch = '''                  IBlockState _bs = this.world.getBlockState(new net.minecraft.util.math.BlockPos(x + this.getPos().getX(), y + this.getPos().getY(), z + this.getPos().getZ()));
                  Block bi = _bs.getBlock();
                  int md = bi.getMetaFromState(_bs);
                  Material bm = _bs.getMaterial();'''
if old_block_fetch in content:
    content = content.replace(old_block_fetch, new_block_fetch)
    print(f"  OK: block fetch")
else:
    print(f"  MISS: block fetch pattern")

# stackSize fixes
content = content.replace('this.contents[var1].stackSize <= var2', 'this.contents[var1].getCount() <= var2')
content = content.replace('if (this.contents[var1].stackSize == 0)', 'if (this.contents[var1].isEmpty())')
content = content.replace('var2.stackSize > this.getInventoryStackLimit()', 'var2.getCount() > this.getInventoryStackLimit()')
content = content.replace('var2.stackSize = this.getInventoryStackLimit()', 'var2.setCount(this.getInventoryStackLimit())')
content = content.replace('++this.contents[1].stackSize', 'this.contents[1].grow(1)')

# AxisAlignedBB.getBoundingBox -> new AxisAlignedBB
content = content.replace(
    'return AxisAlignedBB.getBoundingBox(',
    'return new AxisAlignedBB('
)

# playSound(x, y, z, String, float, float, boolean) -> new API
content = content.replace(
    'this.world.playSound(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), "thaumcraft:learn", 1.0F, 1.0F, false)',
    'this.world.playSound(null, this.getPos(), net.minecraft.util.SoundEvent.REGISTRY.getObject(new net.minecraft.util.ResourceLocation("thaumcraft", "learn")), SoundCategory.BLOCKS, 1.0F, 1.0F)'
)

# Items.paper -> Items.PAPER, Items.dye -> Items.DYE
content = content.replace('Items.paper', 'Items.PAPER')
content = content.replace('Items.dye', 'Items.DYE')

# openInventory/closeInventory -> add EntityPlayer param
content = content.replace(
    '   public void openInventory() {\n   }',
    '   public void openInventory(EntityPlayer player) {\n   }'
)
content = content.replace(
    '   public void closeInventory() {\n   }',
    '   public void closeInventory(EntityPlayer player) {\n   }'
)

# Add missing IInventory methods: getFieldCount, getField, setField, isEmpty
if 'getFieldCount' not in content:
    # Insert before the closing brace of the class (before last })
    content = content.rstrip()
    if content.endswith('}'):
        content = content[:-1].rstrip() + '\n\n   public boolean isEmpty() { for (ItemStack s : contents) { if (s != null && !s.isEmpty()) return false; } return true; }\n   public int getField(int id) { return 0; }\n   public void setField(int id, int value) {}\n   public int getFieldCount() { return 0; }\n}'
        print(f"  OK: added getFieldCount/getField/setField/isEmpty")
    else:
        print(f"  WARN: couldn't add IInventory methods")

write(tile, content)
print(f"  Written: {tile}")

# ============================================================
# REHWandHandler.java: multiple fixes
# ============================================================
print("\n=== REHWandHandler.java ===")
reh = r"src\main\java\thaumcraft\client\lib\REHWandHandler.java"
content = read(reh)

# Add ITooltipFlag import
if 'import net.minecraft.item.ITooltipFlag;' not in content:
    content = content.replace(
        'import net.minecraft.item.Item;',
        'import net.minecraft.item.ITooltipFlag;\nimport net.minecraft.item.Item;'
    )

# ungrabMouse -> ungrabMouseCursor
content = content.replace('mc.mouseHelper.ungrabMouse()', 'mc.mouseHelper.ungrabMouseCursor()')

# getCurrentEquippedItem -> getHeldItemMainhand
content = content.replace('getCurrentEquippedItem()', 'getHeldItemMainhand()')

# renderViewEntity private -> getRenderViewEntity()
content = content.replace(
    'Minecraft.getMinecraft().renderViewEntity',
    'Minecraft.getMinecraft().getRenderViewEntity()'
)

# TextureMap.locationBlocksTexture -> TextureMap.LOCATION_BLOCKS_TEXTURE
content = content.replace('TextureMap.locationBlocksTexture', 'TextureMap.LOCATION_BLOCKS_TEXTURE')

# ITooltipFlag qualified name - already fixed with import; fix the qualified usage
content = content.replace(
    'net.minecraft.item.ITooltipFlag.TooltipFlags.ADVANCED',
    'ITooltipFlag.TooltipFlags.ADVANCED'
)
content = content.replace(
    'net.minecraft.item.ITooltipFlag.TooltipFlags.NORMAL',
    'ITooltipFlag.TooltipFlags.NORMAL'
)

write(reh, content)
print(f"  Written: {reh}")

print("\nDone.")
