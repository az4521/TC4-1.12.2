#!/usr/bin/env python3
"""
run_all.py — Run all TC4 1.7.10 → 1.12.2 migration scripts in order.

Usage:
  python scripts/run_all.py              # apply transforms
  python scripts/run_all.py --dry-run    # preview only (no writes)
  python scripts/run_all.py --only s1    # run a single script by name

Scripts are run against src/main/java relative to the project root
(parent of the scripts/ directory).

Order matters:
  s1 first (import renames) so subsequent scripts work on clean package names.
  s2-s5 are independent after s1.
"""
import sys
import time
import importlib.util
from pathlib import Path

SCRIPTS_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPTS_DIR.parent
SRC_ROOT = str(PROJECT_ROOT / "src" / "main" / "java")

SCRIPT_ORDER = [
    ("s1", "s1_imports",    "Import package renames (cpw.mods.fml → net.minecraftforge.fml)"),
    ("s2", "s2_gl11",       "GL11 → GlStateManager"),
    ("s3", "s3_tessellator","Tessellator → BufferBuilder (heuristic — review output)"),
    ("s4", "s4_tesr",       "TESR renderTileEntityAt → render signature"),
    ("s5", "s5_blockpos",   "World int coords → BlockPos (conservative + TODO markers)"),
]


def load_script(module_name: str):
    spec = importlib.util.spec_from_file_location(module_name, SCRIPTS_DIR / f"{module_name}.py")
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


def run_script(key: str, module_name: str, description: str, dry_run: bool) -> dict:
    print(f"\n{'='*60}")
    print(f"  {key}: {description}")
    if dry_run:
        print("  [DRY RUN — no files will be written]")
    print(f"{'='*60}")

    t0 = time.time()
    mod = load_script(module_name)
    stats = mod.main(SRC_ROOT, dry_run)
    elapsed = time.time() - t0

    print(f"\n  → {key} finished in {elapsed:.1f}s: {stats}")
    return stats


def main():
    dry_run = "--dry-run" in sys.argv
    only = None
    for arg in sys.argv[1:]:
        if arg.startswith("--only="):
            only = arg.split("=", 1)[1]
        elif arg == "--only" and sys.argv.index(arg) + 1 < len(sys.argv):
            only = sys.argv[sys.argv.index(arg) + 1]

    print(f"TC4 migration scripts — project root: {PROJECT_ROOT}")
    print(f"Source root: {SRC_ROOT}")
    if dry_run:
        print("MODE: DRY RUN (no files modified)")
    else:
        print("MODE: LIVE (files will be modified — ensure git is clean!)")

    all_stats = {}
    for key, module_name, description in SCRIPT_ORDER:
        if only and only != key:
            continue
        all_stats[key] = run_script(key, module_name, description, dry_run)

    print(f"\n{'='*60}")
    print("  SUMMARY")
    print(f"{'='*60}")
    for key, stats in all_stats.items():
        print(f"  {key}: {stats}")

    # Count total TODO_PORT markers inserted
    print(f"\nTo find all TODO_PORT markers after running:")
    print(f'  grep -r "TODO_PORT:" {SRC_ROOT} | wc -l')
    print(f"\nNext step: update the build system (Phase 0 in the plan), then:")
    print(f"  ./gradlew compileJava 2>&1 | head -80")


if __name__ == "__main__":
    main()
