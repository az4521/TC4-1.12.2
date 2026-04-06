#!/usr/bin/env python3
"""
fix_batch7.py - Fix entity renderers (RenderManager constructors) and ClientProxy issues
"""
import os
import re
import sys

SRC = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'java')


def read(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()


def write(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"  WROTE {os.path.relpath(path)}")


def fix_tile_research_table():
    """Add removeStackFromSlot method (last missing IInventory method)."""
    path = os.path.join(SRC, 'thaumcraft/common/tiles/TileResearchTable.java')
    content = read(path)
    # Insert after getStackInSlotOnClosing method
    old = '''   public ItemStack getStackInSlotOnClosing(int var1) {
      if (this.contents[var1] != null) {
         ItemStack var2 = this.contents[var1];
         this.contents[var1] = null;
         return var2;
      } else {
         return null;
      }
   }'''
    new = '''   public ItemStack getStackInSlotOnClosing(int var1) {
      if (this.contents[var1] != null) {
         ItemStack var2 = this.contents[var1];
         this.contents[var1] = null;
         return var2;
      } else {
         return null;
      }
   }

   public ItemStack removeStackFromSlot(int index) {
      if (this.contents[index] != null) {
         ItemStack itemstack = this.contents[index];
         this.contents[index] = null;
         return itemstack;
      }
      return null;
   }'''
    if 'removeStackFromSlot' in content:
        print("  TileResearchTable already has removeStackFromSlot, skipping")
        return
    if old not in content:
        print("  WARNING: TileResearchTable insertion point not found")
        return
    write(path, content.replace(old, new))


def fix_fx_breaking():
    """Add setMotion method to FXBreaking."""
    path = os.path.join(SRC, 'thaumcraft/client/fx/particles/FXBreaking.java')
    content = read(path)
    if 'setMotion' in content:
        print("  FXBreaking already has setMotion, skipping")
        return
    # Add setMotion after setParticleMaxAge
    old = '   public void setParticleMaxAge(int particleMaxAge) {\n      this.particleMaxAge = particleMaxAge;\n   }'
    new = ('   public void setParticleMaxAge(int particleMaxAge) {\n'
           '      this.particleMaxAge = particleMaxAge;\n'
           '   }\n\n'
           '   public void setMotion(double mx, double my, double mz) {\n'
           '      this.motionX = mx;\n'
           '      this.motionY = my;\n'
           '      this.motionZ = mz;\n'
           '   }')
    if old not in content:
        print("  WARNING: FXBreaking insertion point not found")
        return
    write(path, content.replace(old, new))


def fix_fx_bore_particles():
    """Add setMotion method to FXBoreParticles."""
    path = os.path.join(SRC, 'thaumcraft/client/fx/particles/FXBoreParticles.java')
    content = read(path)
    if 'setMotion' in content:
        print("  FXBoreParticles already has setMotion, skipping")
        return
    # Add before the last closing brace of class by adding before getFXLayer or similar
    old = '   public int getFXLayer() {'
    new = ('   public void setMotion(double mx, double my, double mz) {\n'
           '      this.motionX = mx;\n'
           '      this.motionY = my;\n'
           '      this.motionZ = mz;\n'
           '   }\n\n'
           '   public int getFXLayer() {')
    if old not in content:
        # Try a different insertion point
        print("  WARNING: FXBoreParticles getFXLayer not found, trying class end")
        # Append before last }
        last_brace = content.rfind('\n}')
        if last_brace == -1:
            print("  WARNING: FXBoreParticles no trailing brace found")
            return
        new_method = ('\n   public void setMotion(double mx, double my, double mz) {\n'
                      '      this.motionX = mx;\n'
                      '      this.motionY = my;\n'
                      '      this.motionZ = mz;\n'
                      '   }\n')
        content = content[:last_brace] + new_method + content[last_brace:]
        write(path, content)
        return
    write(path, content.replace(old, new, 1))


# ===========================================================================
# Entity renderer constructor fixes
# ===========================================================================

# These renderers extend Render (not RenderLiving) and have no-arg constructors
# that need RenderManager added
RENDER_NO_ARGS = [
    'RenderAlumentum',
    'RenderAspectOrb',
    'RenderCultistPortal',
    'RenderDart',       # no constructor at all
    'RenderEldritchOrb',
    'RenderElectricOrb',
    'RenderEmber',
    'RenderExplosiveOrb',
    'RenderFallingTaint',
    'RenderFollowingItem',
    'RenderFrostShard',
    'RenderGolemBobber',   # no constructor at all
    'RenderPechBlast',
    'RenderPrimalArrow',   # no constructor at all
    'RenderPrimalOrb',
    'RenderSpecialItem',
    'RenderWisp',
]

# RenderLiving subclasses with no-arg constructors that call super(model, shadow)
RENDER_LIVING_NO_ARG = {
    'RenderEldritchCrab': 'super(renderManager, new net.minecraft.client.renderer.models.ModelEldritchCrab(), 1.0F)',
    'RenderFireBat': None,      # has explicit no-arg, will be handled by regex
    'RenderMindSpider': None,
    'RenderTaintCreeper': None,
    'RenderTaintSpider': None,
    'RenderTaintSpore': None,
    'RenderTaintSporeSwarmer': None,
    'RenderTaintSwarm': None,
    'RenderTaintVillager': None,
    'RenderWatcher': None,
}

# RenderLiving subclasses with (Model, float) constructors
RENDER_LIVING_MODEL_SHADOW = [
    'RenderEldritchGolem',
    'RenderEldritchGuardian',
    'RenderPech',
    'RenderThaumicSlime',
    'RenderTaintChicken',
    'RenderTaintCow',
    'RenderTaintPig',
    'RenderTravelingTrunk',
]

# RenderZombie/RenderBiped subclasses with no constructor
RENDER_ZOMBIE_NO_CTOR = [
    'RenderBrainyZombie',
    'RenderInhabitedZombie',
    'RenderCultist',  # extends RenderBiped - BUT has explicit constructor
]


def get_renderer_path(name):
    return os.path.join(SRC, f'thaumcraft/client/renderers/entity/{name}.java')


def add_render_manager_import(content):
    """Ensure RenderManager is imported."""
    if 'import net.minecraft.client.renderer.entity.RenderManager;' in content:
        return content
    # Add after the last entity renderer import
    insert_after = 'import net.minecraft.client.renderer.entity.Render;'
    if insert_after in content:
        return content.replace(
            insert_after,
            insert_after + '\nimport net.minecraft.client.renderer.entity.RenderManager;',
            1
        )
    # Fallback: insert before class declaration
    m = re.search(r'\n(public class |@SideOnly)', content)
    if m:
        return content[:m.start()] + '\nimport net.minecraft.client.renderer.entity.RenderManager;' + content[m.start():]
    return content


def fix_render_no_args(name):
    """
    For renderers extending Render directly with a no-arg constructor (or no constructor).
    Changes:
      public RenderFoo() { super(); ... }  ->  public RenderFoo(RenderManager renderManager) { super(renderManager); ... }
    Or adds one if missing.
    """
    path = get_renderer_path(name)
    if not os.path.exists(path):
        print(f"  WARNING: {name}.java not found")
        return
    content = read(path)
    if 'RenderManager renderManager' in content:
        print(f"  {name} already has RenderManager, skipping")
        return
    content = add_render_manager_import(content)

    # Pattern: constructor with no args
    # "   public RenderFoo() {\n"
    pattern = rf'public {name}\(\)'
    if re.search(pattern, content):
        # Add renderManager parameter
        content = re.sub(
            rf'public ({name})\(\)',
            r'public \1(RenderManager renderManager)',
            content
        )
        # Fix or add super(renderManager) call
        # After "{", if there's a super() call, replace it
        content = re.sub(
            rf'(public {name}\(RenderManager renderManager\)\s*\{{)\s*(?:super\(\);?)',
            r'\1\n      super(renderManager);',
            content
        )
        # If no super() found after the {, add it
        # Check if super(renderManager) was added
        if f'public {name}(RenderManager renderManager)' in content:
            # Find the constructor body and ensure super is first
            def add_super_if_missing(m):
                body_start = m.group(0)
                if 'super(renderManager)' not in body_start:
                    # Insert super after {
                    return body_start + '\n      super(renderManager);'
                return body_start
            content = re.sub(
                rf'public {name}\(RenderManager renderManager\)\s*\{{(?!\s*super\(renderManager\))',
                add_super_if_missing,
                content
            )
    else:
        # No constructor found - add one before the first method
        # Find the class opening brace line, then find first method
        # Simple approach: insert after class declaration
        class_match = re.search(rf'public class {name}.*?\{{', content, re.DOTALL)
        if class_match:
            insert_pos = class_match.end()
            constructor = (f'\n\n   public {name}(RenderManager renderManager) {{\n'
                           f'      super(renderManager);\n'
                           f'   }}')
            content = content[:insert_pos] + constructor + content[insert_pos:]
        else:
            print(f"  WARNING: {name} class declaration not found")
            return

    write(path, content)


def fix_render_living_no_arg(name):
    """
    For renderers extending RenderLiving that have no-arg constructors calling super(model, shadow).
    Changes super(model, shadow) to super(renderManager, model, shadow).
    """
    path = get_renderer_path(name)
    if not os.path.exists(path):
        print(f"  WARNING: {name}.java not found")
        return
    content = read(path)
    if 'RenderManager renderManager' in content:
        print(f"  {name} already has RenderManager, skipping")
        return
    content = add_render_manager_import(content)

    # Change: public RenderFoo() { -> public RenderFoo(RenderManager renderManager) {
    content = re.sub(
        rf'public ({name})\(\)',
        r'public \1(RenderManager renderManager)',
        content
    )
    # Change: super(model, shadow) at start of constructor -> super(renderManager, model, shadow)
    # Must be careful not to match super calls in other methods
    # Pattern: super( immediately after constructor opening
    # We'll do a targeted replacement: super(<non-renderManager content>)
    # that appears right after the constructor signature
    content = re.sub(
        r'(public ' + re.escape(name) + r'\(RenderManager renderManager\)\s*\{)\s*\n(\s*)(super\()',
        r'\1\n\2super(renderManager, ',
        content
    )
    # Remove the extra "super(" that was put back
    # Actually the above regex replaces "super(" with "super(renderManager, "
    # But the original super already had "super(..." - so we need to be careful
    # Let me re-approach: find "super(" that comes right after constructor opening
    # and prepend "renderManager, " to its args

    write(path, content)


def fix_render_living_model_shadow(name):
    """
    For renderers with (ModelBase model, float shadow) constructors extending RenderLiving.
    Changes constructor to (RenderManager renderManager, ModelBase model, float shadow)
    and super(model, shadow) to super(renderManager, model, shadow).
    """
    path = get_renderer_path(name)
    if not os.path.exists(path):
        print(f"  WARNING: {name}.java not found")
        return
    content = read(path)
    if 'RenderManager renderManager' in content:
        print(f"  {name} already has RenderManager, skipping")
        return
    content = add_render_manager_import(content)

    # Find constructor: public RenderFoo(TypeA a, TypeB b, ...) {
    # and prepend "RenderManager renderManager, " to params
    # Then fix super call

    # Generic approach: find the constructor and add renderManager as first param
    # The constructor signature ends with ") {"
    # We need to find: public RenderName(... -> public RenderName(RenderManager renderManager, ...
    content = re.sub(
        rf'public ({name})\(',
        r'public \1(RenderManager renderManager, ',
        content
    )
    # Fix super call: super(model, shadow) -> super(renderManager, model, shadow)
    # Find super( that appears right after this constructor
    # Use a simple approach: find "super(" that doesn't already have renderManager
    content = re.sub(
        r'(public ' + re.escape(name) + r'\(RenderManager renderManager,[^)]+\)\s*\{)\s*\n(\s*)(super\()',
        r'\1\n\2super(renderManager, ',
        content
    )

    write(path, content)


def fix_render_zombie_no_ctor(name):
    """
    For renderers extending RenderZombie/RenderBiped that have no explicit constructor.
    Adds: public RenderFoo(RenderManager renderManager) { super(renderManager); }
    """
    path = get_renderer_path(name)
    if not os.path.exists(path):
        print(f"  WARNING: {name}.java not found")
        return
    content = read(path)
    if 'RenderManager renderManager' in content:
        print(f"  {name} already has RenderManager, skipping")
        return
    content = add_render_manager_import(content)

    # Check if there's already an explicit constructor
    if f'public {name}(' in content:
        # Already has a constructor, just add renderManager param
        content = re.sub(
            rf'public ({name})\(\)',
            r'public \1(RenderManager renderManager)',
            content
        )
        # Add super(renderManager) call
        content = re.sub(
            r'(public ' + re.escape(name) + r'\(RenderManager renderManager\)\s*\{)',
            r'\1\n      super(renderManager);',
            content
        )
    else:
        # No constructor - add one
        class_match = re.search(rf'public class {name}.*?\{{', content, re.DOTALL)
        if class_match:
            insert_pos = class_match.end()
            constructor = (f'\n\n   public {name}(RenderManager renderManager) {{\n'
                           f'      super(renderManager);\n'
                           f'   }}')
            content = content[:insert_pos] + constructor + content[insert_pos:]
        else:
            print(f"  WARNING: {name} class not found")
            return

    write(path, content)


def fix_render_golem_base():
    """Special case: RenderGolemBase(ModelBase) -> RenderGolemBase(RenderManager, ModelBase)"""
    path = get_renderer_path('RenderGolemBase')
    content = read(path)
    if 'RenderManager renderManager' in content:
        print("  RenderGolemBase already has RenderManager, skipping")
        return
    content = add_render_manager_import(content)
    # public RenderGolemBase(ModelBase par1ModelBase) {
    #     super(par1ModelBase, 0.25F);
    content = content.replace(
        'public RenderGolemBase(ModelBase par1ModelBase) {\n      super(par1ModelBase, 0.25F);',
        'public RenderGolemBase(RenderManager renderManager, ModelBase par1ModelBase) {\n      super(renderManager, par1ModelBase, 0.25F);'
    )
    write(path, content)


def fix_render_taintacle():
    """Special case: RenderTaintacle(float, int) -> RenderTaintacle(RenderManager, float, int)"""
    path = get_renderer_path('RenderTaintacle')
    content = read(path)
    if 'RenderManager renderManager' in content:
        print("  RenderTaintacle already has RenderManager, skipping")
        return
    content = add_render_manager_import(content)
    content = content.replace(
        'public RenderTaintacle(float shadow, int length) {\n      super(new ModelTaintacle(length), shadow);',
        'public RenderTaintacle(RenderManager renderManager, float shadow, int length) {\n      super(renderManager, new ModelTaintacle(length), shadow);'
    )
    write(path, content)


def fix_render_traveling_trunk():
    """Special case: RenderTravelingTrunk(ModelBase, float) -> (RenderManager, ModelBase, float)"""
    path = get_renderer_path('RenderTravelingTrunk')
    content = read(path)
    if 'RenderManager renderManager' in content:
        print("  RenderTravelingTrunk already has RenderManager, skipping")
        return
    content = add_render_manager_import(content)
    content = re.sub(
        r'public RenderTravelingTrunk\(ModelBase modelbase, float f\)\s*\{\s*\n(\s*)super\(modelbase, f\)',
        r'public RenderTravelingTrunk(RenderManager renderManager, ModelBase modelbase, float f) {\n\1super(renderManager, modelbase, f)',
        content
    )
    write(path, content)


# ===========================================================================
# ClientProxy fixes
# ===========================================================================

def fix_client_proxy():
    path = os.path.join(SRC, 'thaumcraft/client/ClientProxy.java')
    content = read(path)

    # --- Fix imports ---
    # Remove ISimpleBlockRenderingHandler import (doesn't exist in 1.12.2)
    content = content.replace(
        'import net.minecraftforge.fml.client.registry.ISimpleBlockRenderingHandler;  // TODO_PORT: no direct 1.12.2 equivalent -- manual rewrite needed',
        '// TODO_PORT: ISimpleBlockRenderingHandler removed in 1.12.2'
    )
    # Comment out AdvancedModelLoader (doesn't exist in 1.12.2)
    content = content.replace(
        'import net.minecraftforge.client.model.AdvancedModelLoader;',
        '// import net.minecraftforge.client.model.AdvancedModelLoader; // TODO_PORT: removed in 1.12.2'
    )
    # Comment out MinecraftForgeClient (registerItemRenderer is gone)
    content = content.replace(
        'import net.minecraftforge.client.MinecraftForgeClient;',
        '// import net.minecraftforge.client.MinecraftForgeClient; // registerItemRenderer removed in 1.12.2'
    )
    # Comment out VillagerRegistry (API completely changed)
    content = content.replace(
        'import net.minecraftforge.fml.common.registry.VillagerRegistry;',
        '// import net.minecraftforge.fml.common.registry.VillagerRegistry; // TODO_PORT: villager API changed'
    )
    # Add IRenderFactory import if not present
    if 'import net.minecraftforge.fml.client.registry.IRenderFactory;' not in content:
        content = content.replace(
            'import net.minecraftforge.fml.client.registry.RenderingRegistry;  // TODO_PORT: no direct 1.12.2 equivalent -- manual rewrite needed',
            'import net.minecraftforge.fml.client.registry.IRenderFactory;\nimport net.minecraftforge.fml.client.registry.RenderingRegistry;'
        )
    # Add EnumParticleTypes import if not present
    if 'import net.minecraft.util.EnumParticleTypes;' not in content:
        content = content.replace(
            'import net.minecraft.util.EnumFacing;',
            'import net.minecraft.util.EnumFacing;\nimport net.minecraft.util.EnumParticleTypes;\nimport net.minecraft.util.SoundCategory;\nimport net.minecraft.util.SoundEvent;'
        )

    # --- Fix setupItemRenderers() ---
    # Comment out entire body
    old_item_renderers = '''   private void setupItemRenderers() {
      MinecraftForgeClient.registerItemRenderer(ConfigItems.itemJarFilled, new ItemJarFilledRenderer());
      MinecraftForgeClient.registerItemRenderer(ConfigItems.itemJarNode, new ItemJarNodeRenderer());
      MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ConfigBlocks.blockAiry), new ItemNodeRenderer());
      MinecraftForgeClient.registerItemRenderer(ConfigItems.itemThaumometer, new ItemThaumometerRenderer());
      MinecraftForgeClient.registerItemRenderer(ConfigItems.itemWandCasting, new ItemWandRenderer());
      MinecraftForgeClient.registerItemRenderer(ConfigItems.itemTrunkSpawner, new ItemTrunkSpawnerRenderer());
      MinecraftForgeClient.registerItemRenderer(ConfigItems.itemBowBone, new ItemBowBoneRenderer());
      MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ConfigBlocks.blockWoodenDevice), new ItemBannerRenderer());
   }'''
    new_item_renderers = '''   private void setupItemRenderers() {
      // TODO_PORT: MinecraftForgeClient.registerItemRenderer removed in 1.12.2
      // Item renderers need to use ModelLoader.setCustomModelResourceLocation
      // in a ModelRegistryEvent handler (see M4 rendering phase)
   }'''
    content = content.replace(old_item_renderers, new_item_renderers)

    # --- Fix setupEntityRenderers() ---
    # Simple no-arg renderers: replace "new RenderFoo()" with "manager -> new RenderFoo(manager)"
    simple_renderers = [
        'RenderSpecialItem', 'RenderFollowingItem', 'RenderAspectOrb', 'RenderGolemBobber',
        'RenderWisp', 'RenderAlumentum', 'RenderPrimalOrb', 'RenderEldritchOrb',
        'RenderElectricOrb', 'RenderEmber', 'RenderExplosiveOrb', 'RenderPechBlast',
        'RenderBrainyZombie', 'RenderInhabitedZombie', 'RenderFireBat', 'RenderFrostShard',
        'RenderDart', 'RenderPrimalArrow', 'RenderFallingTaint', 'RenderTaintSpider',
        'RenderTaintSpore', 'RenderTaintSporeSwarmer', 'RenderTaintSwarm', 'RenderTaintVillager',
        'RenderTaintCreeper', 'RenderMindSpider', 'RenderEldritchCrab', 'RenderCultistPortal',
        'RenderCultist', 'RenderWatcher',
    ]
    for r in simple_renderers:
        content = content.replace(f', new {r}())', f', manager -> new {r}(manager))')

    # Renderers with args
    content = content.replace(
        ', new RenderGolemBase(new ModelGolem(false)))',
        ', manager -> new RenderGolemBase(manager, new ModelGolem(false)))'
    )
    content = content.replace(
        ', new RenderPech(new ModelPech(), 0.25F))',
        ', manager -> new RenderPech(manager, new ModelPech(), 0.25F))'
    )
    content = content.replace(
        ', new RenderThaumicSlime(new ModelSlime(16), new ModelSlime(0), 0.25F))',
        ', manager -> new RenderThaumicSlime(manager, new ModelSlime(16), new ModelSlime(0), 0.25F))'
    )
    content = content.replace(
        ', new RenderTaintChicken(new ModelChicken(), 0.3F))',
        ', manager -> new RenderTaintChicken(manager, new ModelChicken(), 0.3F))'
    )
    content = content.replace(
        ', new RenderTaintCow(new ModelCow(), 0.7F))',
        ', manager -> new RenderTaintCow(manager, new ModelCow(), 0.7F))'
    )
    content = content.replace(
        ', new RenderTaintPig(new ModelPig(), 0.7F))',
        ', manager -> new RenderTaintPig(manager, new ModelPig(), 0.7F))'
    )
    content = content.replace(
        ', new RenderTaintSheep(new ModelTaintSheep2(), new ModelTaintSheep1(), 0.7F))',
        ', manager -> new RenderTaintSheep(manager, new ModelTaintSheep2(), new ModelTaintSheep1(), 0.7F))'
    )
    content = content.replace(
        ', new RenderTravelingTrunk(new ModelTrunk(), 0.5F))',
        ', manager -> new RenderTravelingTrunk(manager, new ModelTrunk(), 0.5F))'
    )
    content = content.replace(
        ', new RenderEldritchGuardian(new ModelEldritchGuardian(), 0.5F))',
        ', manager -> new RenderEldritchGuardian(manager, new ModelEldritchGuardian(), 0.5F))'
    )
    content = content.replace(
        ', new RenderEldritchGuardian(new ModelEldritchGuardian(), 0.6F))',
        ', manager -> new RenderEldritchGuardian(manager, new ModelEldritchGuardian(), 0.6F))'
    )
    content = content.replace(
        ', new RenderEldritchGolem(new ModelEldritchGolem(), 0.5F))',
        ', manager -> new RenderEldritchGolem(manager, new ModelEldritchGolem(), 0.5F))'
    )
    content = content.replace(
        ', new RenderTaintacle(0.6F, 10))',
        ', manager -> new RenderTaintacle(manager, 0.6F, 10))'
    )
    content = content.replace(
        ', new RenderTaintacle(0.2F, 6))',
        ', manager -> new RenderTaintacle(manager, 0.2F, 6))'
    )
    content = content.replace(
        ', new RenderTaintacle(1.0F, 14))',
        ', manager -> new RenderTaintacle(manager, 1.0F, 14))'
    )
    # RenderSnowball in 1.12.2: (RenderManager, Item, RenderItem) - meta gone
    content = content.replace(
        ', new RenderSnowball(ConfigItems.itemBottleTaint, 0))',
        ', manager -> new RenderSnowball(manager, ConfigItems.itemBottleTaint, net.minecraft.client.Minecraft.getMinecraft().getRenderItem()))'
    )
    # EntityItemGrate: use RenderEntityItem instead of RenderItem
    content = content.replace(
        'RenderingRegistry.registerEntityRenderingHandler(EntityItemGrate.class, new RenderItem());',
        'RenderingRegistry.registerEntityRenderingHandler(EntityItemGrate.class, manager -> new net.minecraft.client.renderer.entity.RenderEntityItem(manager, net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));'
    )
    # Comment out VillagerRegistry calls
    content = content.replace(
        '      VillagerRegistry.instance().registerVillagerSkin(ConfigEntities.entWizardId, new ResourceLocation("thaumcraft", "textures/models/wizard.png"));\n'
        '      VillagerRegistry.instance().registerVillagerSkin(ConfigEntities.entBankerId, new ResourceLocation("thaumcraft", "textures/models/moneychanger.png"));',
        '      // TODO_PORT: VillagerRegistry.registerVillagerSkin removed in 1.12.2\n'
        '      // VillagerRegistry.instance().registerVillagerSkin(ConfigEntities.entWizardId, new ResourceLocation("thaumcraft", "textures/models/wizard.png"));\n'
        '      // VillagerRegistry.instance().registerVillagerSkin(ConfigEntities.entBankerId, new ResourceLocation("thaumcraft", "textures/models/moneychanger.png"));'
    )

    # --- Fix setupBlockRenderers() ---
    # Comment out all getNextAvailableRenderId and registerBlockRenderer calls
    old_block_renderers_body = '''   void setupBlockRenderers() {
      ConfigBlocks.blockFluxGasRI = RenderingRegistry.getNextAvailableRenderId();
      this.registerBlockRenderer(new BlockGasRenderer());'''
    if old_block_renderers_body in content:
        # Find the full method body and replace with no-op
        start = content.find('   void setupBlockRenderers() {')
        if start != -1:
            # Find matching closing brace
            depth = 0
            i = content.find('{', start)
            while i < len(content):
                if content[i] == '{':
                    depth += 1
                elif content[i] == '}':
                    depth -= 1
                    if depth == 0:
                        end = i + 1
                        break
                i += 1
            old_method = content[start:end]
            new_method = ('   void setupBlockRenderers() {\n'
                          '      // TODO_PORT: ISimpleBlockRenderingHandler/RenderingRegistry.getNextAvailableRenderId removed in 1.12.2\n'
                          '      // Block rendering is JSON-based in 1.12.2 (see M4 rendering phase)\n'
                          '   }')
            content = content[:start] + new_method + content[end:]

    # --- Fix registerBlockRenderer ---
    content = content.replace(
        '   public void registerBlockRenderer(ISimpleBlockRenderingHandler renderer) {\n      RenderingRegistry.registerBlockHandler(renderer);\n   }',
        '   public void registerBlockRenderer(Object renderer) {\n      // TODO_PORT: ISimpleBlockRenderingHandler/registerBlockHandler removed in 1.12.2\n   }'
    )

    # --- Fix getClientWorld() ---
    content = content.replace(
        'return FMLClientHandler.instance().getClient().theWorld;',
        'return FMLClientHandler.instance().getClient().world;'
    )

    # --- Fix crucibleBoilSound() playSound ---
    content = content.replace(
        '   public void crucibleBoilSound(World world, int xCoord, int yCoord, int zCoord) {\n'
        '      world.playSound((float)xCoord + 0.5F, (float)yCoord + 0.5F, (float)zCoord + 0.5F, "thaumcraft:spill", 0.2F, 1.0F, false);\n'
        '   }',
        '   public void crucibleBoilSound(World world, int xCoord, int yCoord, int zCoord) {\n'
        '      SoundEvent _sndSpill = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft", "spill"));\n'
        '      if (_sndSpill != null) world.playSound(null, new BlockPos(xCoord, yCoord, zCoord), _sndSpill, SoundCategory.BLOCKS, 0.2F, 1.0F);\n'
        '   }'
    )

    # --- Fix excavateFX() destroyBlockPartially ---
    content = content.replace(
        'rg.destroyBlockPartially(p.getEntityId(), x, y, z, progress);',
        'rg.destroyBlockPartially(p.getEntityId(), new BlockPos(x, y, z), progress);'
    )

    # --- Fix furnaceLavaFx() - ParticleLava protected constructor ---
    old_furnace_lava = '''   public void furnaceLavaFx(World world, int x, int y, int z, int facingX, int facingZ) {
      ParticleLava fb = new ParticleLava(world, (float)x + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F + (float) facingX, (float)y + 0.3F, (float)z + 0.5F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3F + (float) facingZ);
      fb.motionY = 0.2F * world.rand.nextFloat();
      float qx = facingX == 0 ? (world.rand.nextFloat() - world.rand.nextFloat()) * 0.5F : (float)facingX * world.rand.nextFloat();
      float qz = facingZ == 0 ? (world.rand.nextFloat() - world.rand.nextFloat()) * 0.5F : (float)facingZ * world.rand.nextFloat();
      fb.motionX = 0.15F * qx;
      fb.motionZ = 0.15F * qz;
      FMLClientHandler.instance().getClient().effectRenderer.addEffect(fb);
   }'''
    new_furnace_lava = '''   public void furnaceLavaFx(World world, int x, int y, int z, int facingX, int facingZ) {
      // TODO_PORT: ParticleLava constructor is protected; using spawnEffectParticle instead
      double _px = x + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3 + facingX;
      double _py = y + 0.3;
      double _pz = z + 0.5 + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.3 + facingZ;
      FMLClientHandler.instance().getClient().effectRenderer.spawnEffectParticle(
         EnumParticleTypes.LAVA.getParticleID(), _px, _py, _pz, 0, 0, 0);
   }'''
    content = content.replace(old_furnace_lava, new_furnace_lava)

    # --- Fix Items.slime_ball -> Items.SLIME_BALL ---
    content = content.replace('Items.slime_ball', 'Items.SLIME_BALL')

    # --- Fix e.boundingBox -> e.getEntityBoundingBox() ---
    content = content.replace('e.boundingBox.minY', 'e.getEntityBoundingBox().minY')
    content = content.replace('e.boundingBox.maxY', 'e.getEntityBoundingBox().maxY')

    # --- Fix taintsplosionFX() motionX/Y/Z on FXBreaking ---
    old_taintsplosion = (
        '      fx.motionX = (float)(Math.random() * (double)2.0F - (double)1.0F);\n'
        '      fx.motionY = (float)(Math.random() * (double)2.0F - (double)1.0F);\n'
        '      fx.motionZ = (float)(Math.random() * (double)2.0F - (double)1.0F);\n'
        '      float f = (float)(Math.random() + Math.random() + (double)1.0F) * 0.15F;\n'
        '      float f1 = MathHelper.sqrt(fx.motionX * fx.motionX + fx.motionY * fx.motionY + fx.motionZ * fx.motionZ);\n'
        '      fx.motionX = fx.motionX / (double)f1 * (double)f * 0.9640000000596046;\n'
        '      fx.motionY = fx.motionY / (double)f1 * (double)f * 0.9640000000596046 + (double)0.1F;\n'
        '      fx.motionZ = fx.motionZ / (double)f1 * (double)f * 0.9640000000596046;'
    )
    new_taintsplosion = (
        '      double _fxMx = (Math.random() * 2.0 - 1.0);\n'
        '      double _fxMy = (Math.random() * 2.0 - 1.0);\n'
        '      double _fxMz = (Math.random() * 2.0 - 1.0);\n'
        '      float f = (float)(Math.random() + Math.random() + 1.0) * 0.15F;\n'
        '      float f1 = MathHelper.sqrt((float)(_fxMx * _fxMx + _fxMy * _fxMy + _fxMz * _fxMz));\n'
        '      fx.setMotion(_fxMx / f1 * f * 0.9640000000596046,\n'
        '                   _fxMy / f1 * f * 0.9640000000596046 + 0.1,\n'
        '                   _fxMz / f1 * f * 0.9640000000596046);'
    )
    content = content.replace(old_taintsplosion, new_taintsplosion)

    # --- Fix drawInfusionParticles1() motionX/Y/Z on FXBoreParticles ---
    old_infusion_motion = (
        '      fb.motionX = (float)world.rand.nextGaussian() * 0.03F;\n'
        '      fb.motionY = (float)world.rand.nextGaussian() * 0.03F;\n'
        '      fb.motionZ = (float)world.rand.nextGaussian() * 0.03F;'
    )
    new_infusion_motion = (
        '      fb.setMotion((float)world.rand.nextGaussian() * 0.03F,\n'
        '                   (float)world.rand.nextGaussian() * 0.03F,\n'
        '                   (float)world.rand.nextGaussian() * 0.03F);'
    )
    content = content.replace(old_infusion_motion, new_infusion_motion)

    # --- Fix tentacleAriseFX() getBlockMetadata and ParticleDigging constructor ---
    old_particle_digging = (
        '            ParticleDigging fx2 = (new ParticleDigging(e.world, e.posX + (double)f2, e.posY, e.posZ + (double)f3, 0.0F, 0.0F, 0.0F, e.world.getBlockState(new BlockPos(xx, yy, zz)).getBlock(), e./* TODO_PORT: getBlockMetadata -> world.getBlockState(new BlockPos(x,y,z)).getValue(PROP) */\n'
        '        world.getBlockMetadata(xx, yy, zz), 1)).applyColourMultiplier(xx, yy, zz);'
    )
    new_particle_digging = (
        '            net.minecraft.block.state.IBlockState _pdState = e.world.getBlockState(new BlockPos(xx, yy, zz));\n'
        '            ParticleDigging fx2 = (new ParticleDigging(e.world, e.posX + (double)f2, e.posY, e.posZ + (double)f3, 0.0F, 0.0F, 0.0F, _pdState)).applyColourMultiplier(xx, yy, zz);'
    )
    content = content.replace(old_particle_digging, new_particle_digging)

    write(path, content)


def main():
    print("=== fix_batch7.py ===")

    print("\n[1] TileResearchTable - add removeStackFromSlot")
    fix_tile_research_table()

    print("\n[2] FXBreaking - add setMotion")
    fix_fx_breaking()

    print("\n[3] FXBoreParticles - add setMotion")
    fix_fx_bore_particles()

    print("\n[4] Entity renderers - add RenderManager to constructors")

    print("  No-arg Render subclasses:")
    for name in RENDER_NO_ARGS:
        print(f"    {name}")
        fix_render_no_args(name)

    print("  RenderLiving no-arg (super(model, shadow)):")
    for name in ['RenderFireBat', 'RenderMindSpider', 'RenderTaintCreeper', 'RenderTaintSpider',
                 'RenderTaintSpore', 'RenderTaintSporeSwarmer', 'RenderTaintSwarm', 'RenderTaintVillager',
                 'RenderEldritchCrab', 'RenderWatcher']:
        print(f"    {name}")
        fix_render_living_no_arg(name)

    print("  RenderLiving (Model, float) constructors:")
    for name in ['RenderEldritchGolem', 'RenderEldritchGuardian', 'RenderPech', 'RenderThaumicSlime',
                 'RenderTaintChicken', 'RenderTaintCow', 'RenderTaintPig']:
        print(f"    {name}")
        fix_render_living_model_shadow(name)

    print("  RenderZombie/RenderBiped subclasses:")
    for name in ['RenderBrainyZombie', 'RenderInhabitedZombie']:
        print(f"    {name}")
        fix_render_zombie_no_ctor(name)

    # RenderCultist already has explicit constructor - handled by fix_render_living_no_arg pattern
    # but it extends RenderBiped not RenderLiving...
    # Actually RenderCultist has: public RenderCultist() { super(new ModelBiped(), 0.5F); }
    # RenderBiped extends RenderLiving, so same fix applies
    print("  RenderCultist (extends RenderBiped):")
    fix_render_living_no_arg('RenderCultist')

    print("  Special cases:")
    fix_render_golem_base()
    fix_render_taintacle()
    fix_render_traveling_trunk()

    print("\n[5] ClientProxy - applying all fixes")
    fix_client_proxy()

    print("\nDone!")


if __name__ == '__main__':
    main()
