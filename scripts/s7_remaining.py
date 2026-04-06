#!/usr/bin/env python3
"""
s7_remaining.py -- Fix remaining MC class renames and dead API stubs.
"""
import re
import sys
from pathlib import Path

IMPORT_RENAMES = [
    # Facing (MC package) -> EnumFacing
    ("import net.minecraft.util.Facing;",
     "import net.minecraft.util.EnumFacing;"),
    # Particle effects
    ("import net.minecraft.client.particle.EffectRenderer;",
     "import net.minecraft.client.particle.ParticleManager;"),
    ("import net.minecraft.client.particle.EntityDiggingFX;",
     "import net.minecraft.client.particle.ParticleDigging; // TODO_PORT: EntityDiggingFX -> ParticleDigging"),
    ("import net.minecraft.client.particle.EntityLavaFX;",
     "import net.minecraft.client.particle.ParticleLava; // TODO_PORT: EntityLavaFX -> ParticleLava"),
    # Pathfinding
    ("import net.minecraft.pathfinding.PathEntity;",
     "import net.minecraft.pathfinding.Path;"),
    # ChunkPos (missing import, class exists)
    ("import net.minecraft.world.ChunkPos;",
     "import net.minecraft.util.math.ChunkPos;"),
    # ForgeRegistries (may be needed in some files)
    ("import net.minecraftforge.registries.ForgeRegistries; // TODO_PORT: GameData replaced by ForgeRegistries",
     "import net.minecraftforge.registries.ForgeRegistries;"),
    # IModelCustom / AdvancedModelLoader (Forge model system changed in 1.12)
    ("import net.minecraftforge.client.model.AdvancedModelLoader;",
     "// TODO_PORT: AdvancedModelLoader removed -- use ModelLoaderRegistry in 1.12\n// import net.minecraftforge.client.model.AdvancedModelLoader;"),
    ("import net.minecraftforge.client.model.IModelCustom;",
     "// TODO_PORT: IModelCustom removed -- use IModel from net.minecraftforge.client.model\n// import net.minecraftforge.client.model.IModelCustom;"),
    # Loot table replacements (stub out)
    ("import net.minecraftforge.common.ChestGenHooks;",
     "// TODO_PORT: ChestGenHooks removed -- use LootTable system\n// import net.minecraftforge.common.ChestGenHooks;"),
    ("import net.minecraft.util.WeightedRandomChestContent;",
     "// TODO_PORT: WeightedRandomChestContent removed -- use LootTable system\n// import net.minecraft.util.WeightedRandomChestContent;"),
    # Fluid containers (capabilities system)
    ("import net.minecraftforge.fluids.FluidContainerRegistry;",
     "// TODO_PORT: FluidContainerRegistry removed -- use IFluidHandlerItem capability\n// import net.minecraftforge.fluids.FluidContainerRegistry;"),
    # EntityInteractEvent -> PlayerInteractEvent
    ("import net.minecraftforge.event.entity.player.PlayerInteractEvent; // TODO_PORT: EntityInteractEvent -> PlayerInteractEvent.EntityInteract",
     "import net.minecraftforge.event.entity.player.PlayerInteractEvent;"),
    # PlayerUseItemEvent -> PlayerInteractEvent (already tagged as TODO)
    ("import net.minecraftforge.event.entity.player.PlayerInteractEvent; // TODO_PORT: PlayerUseItemEvent -> PlayerInteractEvent.RightClickItem",
     "import net.minecraftforge.event.entity.player.PlayerInteractEvent;"),
    # Action enum
    ("import net.minecraftforge.fml.common.eventhandler.Event.Action;",
     "import net.minecraftforge.fml.common.eventhandler.Event.Result;"),
]

BODY_RENAMES = [
    # PathEntity -> Path
    ("PathEntity ",       "Path "),
    ("PathEntity(",       "Path("),
    ("PathEntity>",       "Path>"),
    # EffectRenderer -> ParticleManager
    ("EffectRenderer ",   "ParticleManager "),
    ("EffectRenderer.",   "ParticleManager."),
    ("EffectRenderer,",   "ParticleManager,"),
    ("EffectRenderer)",   "ParticleManager)"),
    # EntityDiggingFX -> ParticleDigging
    ("EntityDiggingFX",   "ParticleDigging"),
    # EntityLavaFX -> ParticleLava
    ("EntityLavaFX",      "ParticleLava"),
    # Action.ALLOW / DENY -> Result.ALLOW / DENY / DEFAULT
    ("Event.Action.",     "Event.Result."),
    ("Action.ALLOW",      "Result.ALLOW"),
    ("Action.DENY",       "Result.DENY"),
    # EntityAIArrowAttack - constructor param count changed in 1.12 (now 4 args not 5)
    # Can't safely auto-fix, just leave (will be a runtime issue, not compile)
]


def _ensure_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text
    last = max(text.rfind("\nimport "), text.rfind("\nimport\t"))
    if last == -1:
        return import_line + "\n" + text
    eol = text.find("\n", last + 1)
    return text[:eol + 1] + import_line + "\n" + text[eol + 1:]


def process_file(path: Path, dry_run: bool) -> int:
    text = path.read_text(encoding="utf-8", errors="replace")
    original = text
    changes = 0

    # Add missing ChunkPos import to files using ChunkPos without import
    if "ChunkPos" in text and "import net.minecraft.util.math.ChunkPos;" not in text:
        text = _ensure_import(text, "import net.minecraft.util.math.ChunkPos;")
        changes += 1

    # Import renames
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

    # Body renames
    for old, new in BODY_RENAMES:
        if old in text:
            count = text.count(old)
            text = text.replace(old, new)
            changes += count

    # Fix serializeInto - not in ForgeRegistries API
    if "ForgeRegistries" in text and ".serializeInto(" in text:
        text = text.replace(
            "ForgeRegistries.BLOCKS.serializeInto(",
            "// TODO_PORT: serializeInto not in ForgeRegistries\n               // ForgeRegistries.BLOCKS.serializeInto("
        )
        text = text.replace(
            "ForgeRegistries.ITEMS.serializeInto(",
            "// TODO_PORT: serializeInto not in ForgeRegistries\n               // ForgeRegistries.ITEMS.serializeInto("
        )
        changes += 1

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
    print(f"s7_remaining: {'DRY RUN -- ' if dry else ''}scanning {src}")
    stats = main(src, dry)
    print(f"\ns7_remaining done: {stats['files']} files, {stats['changes']} changes")
