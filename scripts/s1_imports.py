#!/usr/bin/env python3
"""
s1_imports.py — Import package renames for MC 1.7.10 → 1.12.2 port.

Renames cpw.mods.fml.* → net.minecraftforge.fml.* and a handful of
MC class renames. 100% mechanical; safe to run without manual review.

Adds // TODO_PORT: comments on imports that have no direct equivalent.
"""
import os
import re
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Mapping: old import string → new import string
# Applied in order (specific before wildcard catch-all).
# ---------------------------------------------------------------------------
IMPORT_RENAMES = [
    # --- @SideOnly / Side (321 + 315 files) ---
    ("cpw.mods.fml.relauncher.Side",                            "net.minecraftforge.fml.relauncher.Side"),
    ("cpw.mods.fml.relauncher.SideOnly",                        "net.minecraftforge.fml.relauncher.SideOnly"),
    # --- Networking ---
    ("cpw.mods.fml.common.network.simpleimpl.IMessage",         "net.minecraftforge.fml.common.network.simpleimpl.IMessage"),
    ("cpw.mods.fml.common.network.simpleimpl.IMessageHandler",  "net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler"),
    ("cpw.mods.fml.common.network.simpleimpl.MessageContext",   "net.minecraftforge.fml.common.network.simpleimpl.MessageContext"),
    ("cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper", "net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper"),
    ("cpw.mods.fml.common.network.NetworkRegistry",             "net.minecraftforge.fml.common.network.NetworkRegistry"),
    ("cpw.mods.fml.common.network.ByteBufUtils",                "net.minecraftforge.fml.common.network.ByteBufUtils"),
    ("cpw.mods.fml.common.network.FMLNetworkEvent",             "net.minecraftforge.fml.common.network.FMLNetworkEvent"),
    # --- FML client ---
    ("cpw.mods.fml.client.FMLClientHandler",                    "net.minecraftforge.fml.client.FMLClientHandler"),
    # --- Events ---
    ("cpw.mods.fml.common.eventhandler.SubscribeEvent",         "net.minecraftforge.fml.common.eventhandler.SubscribeEvent"),
    ("cpw.mods.fml.common.gameevent.TickEvent",                 "net.minecraftforge.fml.common.gameevent.TickEvent"),
    # --- Registry / common ---
    ("cpw.mods.fml.common.FMLCommonHandler",                    "net.minecraftforge.fml.common.FMLCommonHandler"),
    ("cpw.mods.fml.relauncher.ReflectionHelper",                "net.minecraftforge.fml.relauncher.ReflectionHelper"),
    ("cpw.mods.fml.common.ObfuscationReflectionHelper",         "net.minecraftforge.fml.common.ObfuscationReflectionHelper"),
    ("cpw.mods.fml.common.registry.GameRegistry",               "net.minecraftforge.fml.common.registry.GameRegistry"),
    ("cpw.mods.fml.common.registry.VillagerRegistry",           "net.minecraftforge.fml.common.registry.VillagerRegistry"),
    ("cpw.mods.fml.common.registry.IEntityAdditionalSpawnData", "net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData"),
    # --- Catch-all: any remaining cpw.mods.fml prefix ---
    ("cpw.mods.fml.",                                           "net.minecraftforge.fml."),
    # --- MC class renames ---
    ("net.minecraft.world.biome.BiomeGenBase",                  "net.minecraft.world.biome.Biome"),
    ("net.minecraft.potion.Potion;",                            "net.minecraft.potion.Potion;"),  # stays same, listed for awareness
]

# Imports that have no direct equivalent — add TODO comment instead of renaming.
TODO_IMPORTS = [
    "cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler",  # removed in 1.12
    "cpw.mods.fml.client.registry.RenderingRegistry",             # restructured in 1.12
]


def process_file(path: Path, dry_run: bool) -> tuple[int, int]:
    """Return (renames_applied, todos_added)."""
    text = path.read_text(encoding="utf-8", errors="replace")
    lines = text.splitlines(keepends=True)
    new_lines = []
    renames = 0
    todos = 0

    for line in lines:
        stripped = line.strip()
        if not stripped.startswith("import "):
            new_lines.append(line)
            continue

        # Check TODO imports first
        todo_hit = False
        for todo_pat in TODO_IMPORTS:
            if todo_pat in line and "// TODO_PORT:" not in line:
                new_lines.append(line.rstrip("\n") + "  // TODO_PORT: no direct 1.12.2 equivalent — manual rewrite needed\n")
                todos += 1
                todo_hit = True
                break
        if todo_hit:
            continue

        # Apply renames
        new_line = line
        for old, new in IMPORT_RENAMES:
            if old in new_line:
                new_line = new_line.replace(old, new)
                renames += 1
                break  # one rename per import line is enough

        new_lines.append(new_line)

    if renames or todos:
        new_text = "".join(new_lines)
        if not dry_run:
            path.write_text(new_text, encoding="utf-8")

    return renames, todos


def main(src_root: str, dry_run: bool = False) -> dict:
    root = Path(src_root)
    if not root.exists():
        print(f"ERROR: source root not found: {root}")
        sys.exit(1)

    total_files = total_renames = total_todos = 0
    for java in sorted(root.rglob("*.java")):
        renames, todos = process_file(java, dry_run)
        if renames or todos:
            total_files += 1
            total_renames += renames
            total_todos += todos
            rel = java.relative_to(root)
            print(f"  {'[DRY]' if dry_run else '[OK] '} {rel}  ({renames} renames, {todos} TODOs)")

    return {"files": total_files, "renames": total_renames, "todos": total_todos}


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    src = next((a for a in sys.argv[1:] if not a.startswith("--")),
               str(Path(__file__).parent.parent / "src" / "main" / "java"))
    print(f"s1_imports: {'DRY RUN — ' if dry else ''}scanning {src}")
    stats = main(src, dry)
    print(f"\ns1_imports done: {stats['files']} files, {stats['renames']} renames, {stats['todos']} TODOs added")
