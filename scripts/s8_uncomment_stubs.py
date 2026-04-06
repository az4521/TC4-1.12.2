#!/usr/bin/env python3
"""
s8_uncomment_stubs.py -- Re-enable commented-out imports for shim classes.
Only uncomments if the class name is actually used in the file body.
"""
import sys
from pathlib import Path

# Map: commented import line -> (class name to check in body, plain import line)
STUBS = [
    (
        "// import net.minecraftforge.client.model.AdvancedModelLoader;",
        "AdvancedModelLoader",
        "import net.minecraftforge.client.model.AdvancedModelLoader;"
    ),
    (
        "// import net.minecraftforge.client.model.IModelCustom;",
        "IModelCustom",
        "import net.minecraftforge.client.model.IModelCustom;"
    ),
    (
        "// import net.minecraft.client.renderer.texture.IIconRegister;",
        "IIconRegister",
        "import net.minecraft.client.renderer.texture.IIconRegister;"
    ),
    (
        "// import net.minecraftforge.common.ChestGenHooks;",
        "ChestGenHooks",
        "import net.minecraftforge.common.ChestGenHooks;"
    ),
    (
        "// import net.minecraft.util.WeightedRandomChestContent;",
        "WeightedRandomChestContent",
        "import net.minecraft.util.WeightedRandomChestContent;"
    ),
]


def process_file(path: Path, dry_run: bool) -> int:
    text = path.read_text(encoding="utf-8", errors="replace")
    original = text
    changes = 0

    for commented, class_name, plain_import in STUBS:
        if commented not in text:
            continue
        # Only uncomment if the class is referenced in the body (not just in comments/imports)
        # Check for class name used outside of import lines
        lines = text.splitlines()
        body_uses = False
        for line in lines:
            stripped = line.strip()
            if stripped.startswith("import ") or stripped.startswith("//"):
                continue
            if class_name in line:
                body_uses = True
                break

        if body_uses:
            # Also remove the TODO_PORT comment line before it if present
            text = text.replace(
                "// TODO_PORT: AdvancedModelLoader removed -- use ModelLoaderRegistry in 1.12\n" + commented,
                plain_import
            )
            text = text.replace(
                "// TODO_PORT: IModelCustom removed -- use IModel from net.minecraftforge.client.model\n" + commented,
                plain_import
            )
            text = text.replace(
                "// TODO_PORT: IIconRegister removed -- textures registered via JSON models/resource packs\n" + commented,
                plain_import
            )
            # Also handle standalone commented import without TODO comment
            if commented in text:
                text = text.replace(commented, plain_import)
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
    print(f"s8_uncomment_stubs: {'DRY RUN -- ' if dry else ''}scanning {src}")
    stats = main(src, dry)
    print(f"\ns8_uncomment_stubs done: {stats['files']} files, {stats['changes']} changes")
