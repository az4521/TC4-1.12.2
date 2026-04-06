"""Add VARIANT blockstate properties to multi-variant blocks."""
import re

def add_variant_enum(filepath, enum_name, variants, default_variant):
    """Add a VARIANT property enum to a block class."""
    with open(filepath, 'r') as f:
        content = f.read()

    if f'PropertyEnum<{enum_name}>' in content:
        print(f"  SKIP (already has property): {filepath}")
        return

    # Build enum code
    enum_values = ", ".join([f'{v.upper()}({i}, "{v}")' for i, v in enumerate(variants)])

    variant_code = f'''
   // --- Variant property for 1.12.2 blockstate system ---
   public static final net.minecraft.block.properties.PropertyEnum<{enum_name}> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", {enum_name}.class);

   public enum {enum_name} implements net.minecraft.util.IStringSerializable {{
      {enum_values};
      private final int meta;
      private final String name;
      {enum_name}(int meta, String name) {{ this.meta = meta; this.name = name; }}
      public int getMeta() {{ return meta; }}
      @Override public String getName() {{ return name; }}
      public static {enum_name} byMeta(int m) {{
         for ({enum_name} v : values()) if (v.meta == m) return v;
         return {default_variant.upper()};
      }}
   }}

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {{
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }}

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {{
      return this.getDefaultState().withProperty(VARIANT, {enum_name}.byMeta(meta));
   }}

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {{
      return state.getValue(VARIANT).getMeta();
   }}
   // --- End variant property ---
'''

    # Find the class body opening and insert after the first {
    # Look for the class declaration line
    class_pattern = r'(public class \w+[^{]*\{)'
    match = re.search(class_pattern, content)
    if match:
        insert_pos = match.end()
        content = content[:insert_pos] + variant_code + content[insert_pos:]
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"  ADDED: {filepath}")
    else:
        print(f"  ERROR: Could not find class declaration in {filepath}")

# BlockCosmeticOpaque
add_variant_enum(
    "src/main/java/thaumcraft/common/blocks/BlockCosmeticOpaque.java",
    "OpaqueVariant",
    ["amber", "amberbrick", "wardedglass"],
    "amber"
)

# BlockCosmeticSolid - uses non-contiguous metas
filepath = "src/main/java/thaumcraft/common/blocks/BlockCosmeticSolid.java"
with open(filepath, 'r') as f:
    content = f.read()
if 'PropertyEnum<SolidVariant>' not in content:
    solid_variants = [
        (0, "obsidiantotem"), (1, "obsidiantile"), (2, "pavingtravel"), (3, "pavingwarding"),
        (4, "thaumiumblock"), (5, "tallowblock"), (6, "arcanestone"), (7, "arcanestonebrick"),
        (8, "obsidiantotemcharged"), (9, "golemfetter"), (11, "ancientstone"), (12, "ancientrock"),
        (14, "crustedstone"), (15, "pedestalstone"),
    ]
    enum_values = ", ".join([f'{name.upper()}({meta}, "{name}")' for meta, name in solid_variants])

    variant_code = f'''
   public static final net.minecraft.block.properties.PropertyEnum<SolidVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", SolidVariant.class);

   public enum SolidVariant implements net.minecraft.util.IStringSerializable {{
      {enum_values};
      private final int meta;
      private final String name;
      SolidVariant(int meta, String name) {{ this.meta = meta; this.name = name; }}
      public int getMeta() {{ return meta; }}
      @Override public String getName() {{ return name; }}
      public static SolidVariant byMeta(int m) {{
         for (SolidVariant v : values()) if (v.meta == m) return v;
         return OBSIDIANTOTEM;
      }}
   }}

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {{
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }}

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {{
      return this.getDefaultState().withProperty(VARIANT, SolidVariant.byMeta(meta));
   }}

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {{
      return state.getValue(VARIANT).getMeta();
   }}
'''
    match = re.search(r'(public class \w+[^{]*\{)', content)
    if match:
        content = content[:match.end()] + variant_code + content[match.end():]
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"  ADDED: {filepath}")

# BlockCustomOre
add_variant_enum(
    "src/main/java/thaumcraft/common/blocks/BlockCustomOre.java",
    "OreVariant",
    ["cinnabar", "infusedair", "infusedfire", "infusedwater", "infusedearth", "infusedorder", "infusedentropy", "amber"],
    "cinnabar"
)

# BlockMagicalLeaves - uses bitflags (bit 0 = type, bit 2 = check, bit 3 = decay)
filepath = "src/main/java/thaumcraft/common/blocks/BlockMagicalLeaves.java"
with open(filepath, 'r') as f:
    content = f.read()
if 'PropertyEnum<LeafVariant>' not in content:
    variant_code = '''
   public static final net.minecraft.block.properties.PropertyEnum<LeafVariant> VARIANT =
         net.minecraft.block.properties.PropertyEnum.create("variant", LeafVariant.class);

   public enum LeafVariant implements net.minecraft.util.IStringSerializable {
      GREATWOOD(0, "greatwood"), SILVERWOOD(1, "silverwood");
      private final int meta;
      private final String name;
      LeafVariant(int meta, String name) { this.meta = meta; this.name = name; }
      public int getMeta() { return meta; }
      @Override public String getName() { return name; }
   }

   @Override
   protected net.minecraft.block.state.BlockStateContainer createBlockState() {
      return new net.minecraft.block.state.BlockStateContainer(this, VARIANT);
   }

   @Override
   public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, (meta & 1) == 0 ? LeafVariant.GREATWOOD : LeafVariant.SILVERWOOD);
   }

   @Override
   public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
      return state.getValue(VARIANT).getMeta();
   }
'''
    match = re.search(r'(public class \w+[^{]*\{)', content)
    if match:
        content = content[:match.end()] + variant_code + content[match.end():]
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"  ADDED: {filepath}")

# BlockCustomPlant
add_variant_enum(
    "src/main/java/thaumcraft/common/blocks/BlockCustomPlant.java",
    "PlantVariant",
    ["shimmerleaf", "cinderpearl", "manashroom", "greatwoodsap", "etherealbloom", "silverwoodsap"],
    "shimmerleaf"
)

print("\nDone!")
