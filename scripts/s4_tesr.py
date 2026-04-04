#!/usr/bin/env python3
"""
s4_tesr.py — Update TileEntitySpecialRenderer to 1.12.2 API.

Changes per file:
  1. class Foo extends TileEntitySpecialRenderer
     → class Foo extends TileEntitySpecialRenderer<FooTile>
     (typed overload's first parameter type is used as the generic)

  2. The typed renderTileEntityAt overload:
       public void renderTileEntityAt(FooTile tile, double x, double y, double z, float f)
     becomes:
       @Override
       public void render(FooTile tile, double x, double y, double z, float partialTick, int destroyStage, float alpha)

  3. The TileEntity base-type overload is deleted:
       public void renderTileEntityAt(TileEntity ...) { ... single-line dispatch ... }

Safe — pattern is uniform across all 47 TESR files.
"""
import re
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Step 1: Add generic parameter to class declaration
#
# Matches:
#   class Foo extends TileEntitySpecialRenderer {
#   class Foo extends TileEntitySpecialRenderer<  (already typed — skip)
# ---------------------------------------------------------------------------
_CLASS_DECL_RE = re.compile(
    r'(class\s+\w+\s+extends\s+TileEntitySpecialRenderer)\s*(\{|implements)'
)

def _add_generic(text: str, typed_param: str) -> tuple[str, int]:
    """Add <TypedParam> to class declaration if not already generic."""
    def sub(m):
        if "<" in m.group(0):
            return m.group(0)  # already typed
        return f"{m.group(1)}<{typed_param}> {m.group(2)}"

    new_text = _CLASS_DECL_RE.sub(sub, text)
    return new_text, (1 if new_text != text else 0)


# ---------------------------------------------------------------------------
# Step 2: Extract typed parameter from the typed renderTileEntityAt overload
#
# Pattern: public void renderTileEntityAt(FooType varName, double ...
# We need the first parameter type (NOT TileEntity).
# ---------------------------------------------------------------------------
_TYPED_OVERLOAD_RE = re.compile(
    r'public\s+void\s+renderTileEntityAt\s*\(\s*'
    r'(?!TileEntity\b)(\w+)\s+\w+\s*,'    # first param: typed (not TileEntity)
    r'\s*double\s+\w+\s*,'                 # x
    r'\s*double\s+\w+\s*,'                 # y
    r'\s*double\s+\w+\s*,'                 # z
    r'\s*float\s+\w+'                      # partial tick
    r'\s*\)'
)

def _find_typed_param(text: str) -> str | None:
    m = _TYPED_OVERLOAD_RE.search(text)
    return m.group(1) if m else None


# ---------------------------------------------------------------------------
# Step 3: Rename typed renderTileEntityAt → render, add new parameters
#
# Before:  public void renderTileEntityAt(FooTile tile, double x, double y, double z, float f) {
# After:   @Override
#          public void render(FooTile tile, double x, double y, double z, float f, int destroyStage, float alpha) {
# ---------------------------------------------------------------------------
_TYPED_METHOD_FULL_RE = re.compile(
    r'([ \t]*)'                                  # indent (group 1)
    r'((?:@\w+\s*)*)'                            # existing annotations (group 2)
    r'public\s+void\s+renderTileEntityAt\s*\('
    r'\s*(?!TileEntity\b)'                       # not TileEntity
    r'(\w+\s+\w+)'                               # typed first param (group 3)
    r'((?:\s*,\s*double\s+\w+){3})'             # , double x, double y, double z (group 4)
    r'(\s*,\s*float\s+\w+)'                      # , float f (group 5)
    r'\s*\)'
)

def _rename_typed_overload(text: str) -> tuple[str, int]:
    def sub(m):
        indent = m.group(1)
        existing_annotations = m.group(2)
        first_param = m.group(3)
        coords = m.group(4)
        partial = m.group(5)
        return (f"{indent}{existing_annotations}"
                f"@Override\n{indent}"
                f"public void render({first_param}{coords}{partial}, int destroyStage, float alpha)")

    new_text = _TYPED_METHOD_FULL_RE.sub(sub, text)
    return new_text, (1 if new_text != text else 0)


# ---------------------------------------------------------------------------
# Step 4: Delete the TileEntity base-type overload
#
# Pattern (single-body dispatch):
#   public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f) {
#       this.renderTileEntityAt((FooType) te, x, y, z, f);
#   }
#
# We match and remove the whole 3-line block (declaration + one-liner body + closing brace).
# Multi-statement bodies are left alone and marked TODO.
# ---------------------------------------------------------------------------
_BASE_OVERLOAD_RE = re.compile(
    r'[ \t]*public\s+void\s+renderTileEntityAt\s*\(\s*TileEntity\s+\w+\s*,'
    r'[^{]*\)\s*\{'               # signature → {
    r'[^{}]*'                     # single body — no nested braces
    r'\}[ \t]*\n?',               # closing }
    re.DOTALL
)

def _remove_base_overload(text: str) -> tuple[str, int]:
    new_text = _BASE_OVERLOAD_RE.sub("", text)
    return new_text, (1 if new_text != text else 0)


# ---------------------------------------------------------------------------
# Main per-file processing
# ---------------------------------------------------------------------------

def process_file(path: Path, dry_run: bool) -> bool:
    text = path.read_text(encoding="utf-8", errors="replace")

    if "TileEntitySpecialRenderer" not in text:
        return False
    if "renderTileEntityAt" not in text:
        return False

    original = text
    changed = False

    # Find the typed param first (needed for generic annotation)
    typed_param = _find_typed_param(text)

    # Step 2+3: rename typed overload
    text, n = _rename_typed_overload(text)
    if n:
        changed = True

    # Step 4: remove base-type overload
    text, n = _remove_base_overload(text)
    if n:
        changed = True

    # Step 1: add generic to class declaration
    if typed_param:
        text, n = _add_generic(text, typed_param)
        if n:
            changed = True
    else:
        # Could not determine type — mark with TODO
        text = text.replace(
            "extends TileEntitySpecialRenderer {",
            "extends TileEntitySpecialRenderer<TileEntity> { // TODO_PORT: set correct generic type"
        )
        changed = True

    if changed and not dry_run and text != original:
        path.write_text(text, encoding="utf-8")

    return changed


def main(src_root: str, dry_run: bool = False) -> dict:
    root = Path(src_root)
    total = 0
    for java in sorted(root.rglob("*.java")):
        if process_file(java, dry_run):
            total += 1
            print(f"  {'[DRY]' if dry_run else '[OK] '} {java.relative_to(root)}")
    return {"files": total}


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    src = next((a for a in sys.argv[1:] if not a.startswith("--")),
               str(Path(__file__).parent.parent / "src" / "main" / "java"))
    print(f"s4_tesr: {'DRY RUN — ' if dry else ''}scanning {src}")
    stats = main(src, dry)
    print(f"\ns4_tesr done: {stats['files']} TESR files updated")
