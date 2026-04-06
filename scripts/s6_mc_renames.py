#!/usr/bin/env python3
"""
s6_mc_renames.py -- MC class renames from 1.7.10 to 1.12.2.

Handles both import-line renames and call-site renames for classes that
were simply moved or renamed (not architecturally changed).
"""
import re
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Import-line renames: old full import -> new full import
# Applied as exact string replacements on lines starting with "import ".
# ---------------------------------------------------------------------------
IMPORT_RENAMES = [
    # Translation
    ("import net.minecraft.util.StatCollector;",
     "import net.minecraft.util.text.translation.I18n;"),
    ("import net.minecraft.util.EnumChatFormatting;",
     "import net.minecraft.util.text.TextFormatting;"),
    ("import net.minecraft.util.ChatComponentText;",
     "import net.minecraft.util.text.TextComponentString;"),
    ("import net.minecraft.util.ChatComponentTranslation;",
     "import net.minecraft.util.text.TextComponentTranslation;"),
    # Math / geometry
    ("import net.minecraft.util.MathHelper;",
     "import net.minecraft.util.math.MathHelper;"),
    ("import net.minecraft.util.AxisAlignedBB;",
     "import net.minecraft.util.math.AxisAlignedBB;"),
    ("import net.minecraft.util.MovingObjectPosition;",
     "import net.minecraft.util.math.RayTraceResult;"),
    ("import net.minecraft.util.Vec3;",
     "import net.minecraft.util.math.Vec3d;"),
    # Positions / coords
    ("import net.minecraft.util.ChunkCoordinates;",
     "import net.minecraft.util.math.BlockPos;"),
    ("import net.minecraft.world.ChunkCoordIntPair;",
     "import net.minecraft.util.math.ChunkPos;"),
    ("import net.minecraft.world.ChunkPosition;",
     "import net.minecraft.util.math.BlockPos;"),
    # Directions
    ("import net.minecraftforge.common.util.ForgeDirection;",
     "import net.minecraft.util.EnumFacing;"),
    ("import net.minecraftforge.common.util.Facing;",
     "import net.minecraft.util.EnumFacing;"),
    # Biomes
    ("import net.minecraft.world.biome.BiomeGenBase;",
     "import net.minecraft.world.biome.Biome;"),
    # Particles
    ("import net.minecraft.client.particle.EntityFX;",
     "import net.minecraft.client.particle.Particle;"),
    # Chat / network
    ("import net.minecraft.network.play.server.S35PacketUpdateTileEntity;",
     "import net.minecraft.network.play.server.SPacketUpdateTileEntity;"),
    # Rendering
    ("import net.minecraft.client.renderer.entity.RenderItem;",
     "import net.minecraft.client.renderer.RenderItem;"),
    ("import net.minecraft.client.renderer.RenderBlocks;",
     "// TODO_PORT: RenderBlocks removed in 1.8 -- replace with IBakedModel or JSON models\n// import net.minecraft.client.renderer.RenderBlocks;"),
    # Textures (IIcon system replaced by TextureAtlasSprite + JSON models)
    ("import net.minecraft.util.IIcon;",
     "import net.minecraft.client.renderer.texture.TextureAtlasSprite; // TODO_PORT: IIcon replaced by TextureAtlasSprite + JSON model system"),
    ("import net.minecraft.client.renderer.texture.IIconRegister;",
     "// TODO_PORT: IIconRegister removed -- textures registered via JSON models/resource packs\n// import net.minecraft.client.renderer.texture.IIconRegister;"),
    ("import cpw.mods.fml.common.registry.IIconRegister;",
     "// TODO_PORT: IIconRegister removed -- textures registered via JSON models/resource packs\n// import cpw.mods.fml.common.registry.IIconRegister;"),
    # Fluids
    ("import net.minecraftforge.fluids.IFluidHandler;",
     "import net.minecraftforge.fluids.capability.IFluidHandler;"),
    # Server management
    ("import net.minecraft.server.management.ItemInWorldManager;",
     "import net.minecraft.server.management.PlayerInteractionManager;"),
    # Registry
    ("import net.minecraftforge.fml.common.registry.GameData;",
     "import net.minecraftforge.registries.ForgeRegistries; // TODO_PORT: GameData replaced by ForgeRegistries"),
    # Events
    ("import net.minecraftforge.event.entity.player.PlayerUseItemEvent;",
     "import net.minecraftforge.event.entity.player.PlayerInteractEvent; // TODO_PORT: PlayerUseItemEvent -> PlayerInteractEvent.RightClickItem"),
    ("import cpw.mods.fml.common.gameevent.InputEvent.EntityInteractEvent;",
     "import net.minecraftforge.event.entity.player.PlayerInteractEvent; // TODO_PORT: EntityInteractEvent -> PlayerInteractEvent.EntityInteract"),
    # Action enum (FML -> Forge)
    ("import net.minecraftforge.fml.common.eventhandler.Event.Result;",
     "import net.minecraftforge.fml.common.eventhandler.Event.Result;"),  # unchanged
]

# ---------------------------------------------------------------------------
# Body-level renames: old token/call -> new token/call
# Applied as exact string replacements anywhere in the file body.
# Order matters: longer/more-specific before shorter.
# ---------------------------------------------------------------------------
BODY_RENAMES = [
    # StatCollector -> I18n
    ("StatCollector.translateToLocalFormatted(", "I18n.translateToLocalFormatted("),
    ("StatCollector.translateToLocal(",          "I18n.translateToLocal("),
    ("StatCollector.",                           "I18n."),
    # EnumChatFormatting -> TextFormatting (only class name, values are the same)
    ("EnumChatFormatting.",  "TextFormatting."),
    # Chat components
    ("new ChatComponentText(",         "new TextComponentString("),
    ("new ChatComponentTranslation(",  "new TextComponentTranslation("),
    ("ChatComponentText(",             "TextComponentString("),
    ("ChatComponentTranslation(",      "TextComponentTranslation("),
    # MovingObjectPosition -> RayTraceResult
    ("MovingObjectPosition.",          "RayTraceResult."),
    ("MovingObjectPosition ",          "RayTraceResult "),
    ("(MovingObjectPosition)",         "(RayTraceResult)"),
    ("MovingObjectPosition>",          "RayTraceResult>"),
    # Packet rename
    ("S35PacketUpdateTileEntity",      "SPacketUpdateTileEntity"),
    # Server management
    ("ItemInWorldManager",             "PlayerInteractionManager"),
    # Registry GameData -> ForgeRegistries
    ("GameData.getBlockRegistry()",    "ForgeRegistries.BLOCKS"),
    ("GameData.getItemRegistry()",     "ForgeRegistries.ITEMS"),
    # ChunkCoordIntPair -> ChunkPos
    ("ChunkCoordIntPair(",             "ChunkPos("),
    ("ChunkCoordIntPair ",             "ChunkPos "),
    ("ChunkCoordIntPair>",             "ChunkPos>"),
    # ChunkCoordinates / ChunkPosition -> BlockPos
    ("new ChunkCoordinates(",          "new BlockPos("),
    ("ChunkCoordinates ",              "BlockPos "),
    ("ChunkCoordinates(",              "BlockPos("),
    ("ChunkCoordinates>",              "ChunkPos>"),
    ("new ChunkPosition(",             "new BlockPos("),
    ("ChunkPosition ",                 "BlockPos "),
    # ForgeDirection -> EnumFacing
    ("ForgeDirection.NORTH",   "EnumFacing.NORTH"),
    ("ForgeDirection.SOUTH",   "EnumFacing.SOUTH"),
    ("ForgeDirection.EAST",    "EnumFacing.EAST"),
    ("ForgeDirection.WEST",    "EnumFacing.WEST"),
    ("ForgeDirection.UP",      "EnumFacing.UP"),
    ("ForgeDirection.DOWN",    "EnumFacing.DOWN"),
    ("ForgeDirection.UNKNOWN", "EnumFacing.UP /* TODO_PORT: ForgeDirection.UNKNOWN has no EnumFacing equivalent */"),
    ("ForgeDirection.",        "EnumFacing."),
    ("ForgeDirection ",        "EnumFacing "),
    ("Facing.",                "EnumFacing."),
    # BiomeGenBase -> Biome
    ("BiomeGenBase ",   "Biome "),
    ("BiomeGenBase(",   "Biome("),
    ("BiomeGenBase>",   "Biome>"),
    ("BiomeGenBase[",   "Biome["),
    # EntityFX -> Particle
    ("extends EntityFX",  "extends Particle"),
    ("EntityFX ",         "Particle "),
    ("EntityFX(",         "Particle("),
    ("EntityFX>",         "Particle>"),
    # Vec3 -> Vec3d
    ("Vec3.createVectorHelper(",  "new Vec3d("),
    ("Vec3 ",                     "Vec3d "),
    ("Vec3(",                     "Vec3d("),
    ("Vec3>",                     "Vec3d>"),
    # IIcon -> TextureAtlasSprite (type only; usages are more complex)
    ("IIcon ",  "TextureAtlasSprite "),
    ("IIcon>",  "TextureAtlasSprite>"),
    ("IIcon[",  "TextureAtlasSprite["),
    # IIconRegister method (registerIcon was the main usage)
    (".registerIcon(",  ".registerSprite( /* TODO_PORT: registerIcon removed, use ModelLoader */"),
    # RenderItem field reference
    ("RenderItem ",  "RenderItem "),  # no-op, just ensures import is correct
]


def process_file(path: Path, dry_run: bool) -> int:
    text = path.read_text(encoding="utf-8", errors="replace")
    original = text
    changes = 0

    lines = text.splitlines(keepends=True)
    new_lines = []
    for line in lines:
        if line.lstrip().startswith("import "):
            for old, new in IMPORT_RENAMES:
                if old in line:
                    line = line.replace(old, new)
                    changes += 1
                    break
        new_lines.append(line)
    text = "".join(new_lines)

    for old, new in BODY_RENAMES:
        if old in text:
            count = text.count(old)
            text = text.replace(old, new)
            changes += count

    if changes and not dry_run and text != original:
        path.write_text(text, encoding="utf-8")

    return changes


def main(src_root: str, dry_run: bool = False) -> dict:
    root = Path(src_root)
    total_files = total_changes = 0
    for java in sorted(root.rglob("*.java")):
        n = process_file(java, dry_run)
        if n:
            total_files += 1
            total_changes += n
            print(f"  {'[DRY]' if dry_run else '[OK] '} {java.relative_to(root)}  ({n})")
    return {"files": total_files, "changes": total_changes}


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    src = next((a for a in sys.argv[1:] if not a.startswith("--")),
               str(Path(__file__).parent.parent / "src" / "main" / "java"))
    print(f"s6_mc_renames: {'DRY RUN -- ' if dry else ''}scanning {src}")
    stats = main(src, dry)
    print(f"\ns6_mc_renames done: {stats['files']} files, {stats['changes']} changes")
