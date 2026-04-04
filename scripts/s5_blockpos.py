#!/usr/bin/env python3
"""
s5_blockpos.py — Migrate world coordinate method calls from int x,y,z to BlockPos.

CONSERVATIVE: Applies only patterns where x, y, z are simple identifiers (variable
names or field accesses like context.x). Anything more complex (arithmetic, casts,
method calls as arguments) is marked with // TODO_PORT: for manual review.

Adds: import net.minecraft.util.math.BlockPos;
to files that gain new BlockPos() calls.

After this script, the caller side is updated. Note that the method *signatures*
inside block/tile classes also need updating (int x, int y, int z → BlockPos pos) —
those require manual work per class.
"""
import re
import sys
from pathlib import Path

BLOCKPOS_IMPORT = "import net.minecraft.util.math.BlockPos;"

# ---------------------------------------------------------------------------
# Helper: match a "simple" coordinate argument — a variable name, field access
# like "foo.x", or an array element like "coords[0]".
# We intentionally exclude arithmetic (+, -, *, /) to stay conservative.
# ---------------------------------------------------------------------------
_SIMPLE_ARG = r'(?:[a-zA-Z_]\w*(?:\.\w+|\[\d+\])*)'

def _args3(sep=r'\s*,\s*') -> str:
    """Regex group for three simple args."""
    return rf'({_SIMPLE_ARG}){sep}({_SIMPLE_ARG}){sep}({_SIMPLE_ARG})'

def _args_complex(n: int) -> str:
    """Regex for n args that may be complex (anything without closing paren)."""
    inner = r'[^()]+'
    return inner

# ---------------------------------------------------------------------------
# SAFE replacements: methods that take ONLY (x, y, z) as args
# ---------------------------------------------------------------------------

# world.getBlock(x, y, z)  →  world.getBlockState(new BlockPos(x, y, z)).getBlock()
_GET_BLOCK = re.compile(
    rf'(\w+)\.getBlock\(\s*{_args3()}\s*\)'
)
# world.getTileEntity(x, y, z)  →  world.getTileEntity(new BlockPos(x, y, z))
_GET_TE = re.compile(
    rf'(\w+)\.getTileEntity\(\s*{_args3()}\s*\)'
)
# world.isAirBlock(x, y, z)  →  world.isAirBlock(new BlockPos(x, y, z))
_IS_AIR = re.compile(
    rf'(\w+)\.isAirBlock\(\s*{_args3()}\s*\)'
)
# world.setBlockToAir(x, y, z)  →  world.setBlockToAir(new BlockPos(x, y, z))
_SET_AIR = re.compile(
    rf'(\w+)\.setBlockToAir\(\s*{_args3()}\s*\)'
)
# world.markBlockForUpdate(x, y, z)
_MARK_UPDATE = re.compile(
    rf'(\w+)\.markBlockForUpdate\(\s*{_args3()}\s*\)'
)
# world.getBlockLightValue(x, y, z)
_GET_LIGHT = re.compile(
    rf'(\w+)\.getBlockLightValue\(\s*{_args3()}\s*\)'
)
# world.canBlockSeeTheSky(x, y, z)
_CAN_SEE_SKY = re.compile(
    rf'(\w+)\.canBlockSeeTheSky\(\s*{_args3()}\s*\)'
)
# world.getBiomeGenForCoords(x, z) — only 2 args, different signature
# Skip to avoid over-matching.

SAFE_REPLACEMENTS = [
    (_GET_BLOCK,   lambda m: f"{m.group(1)}.getBlockState(new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)})).getBlock()"),
    (_GET_TE,      lambda m: f"{m.group(1)}.getTileEntity(new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)}))"),
    (_IS_AIR,      lambda m: f"{m.group(1)}.isAirBlock(new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)}))"),
    (_SET_AIR,     lambda m: f"{m.group(1)}.setBlockToAir(new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)}))"),
    (_MARK_UPDATE, lambda m: (
        f"{m.group(1)}.markBlockRangeForRenderUpdate("
        f"new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)}), "
        f"new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)}))"
    )),
    (_GET_LIGHT,   lambda m: f"{m.group(1)}.getBlockLightValue(new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)}))"),
    (_CAN_SEE_SKY, lambda m: f"{m.group(1)}.canBlockSeeTheSky(new BlockPos({m.group(2)}, {m.group(3)}, {m.group(4)}))"),
]

# ---------------------------------------------------------------------------
# TODO markers: methods that need manual rewrite (too complex to auto-transform)
# We match the whole statement line and prepend a comment.
# ---------------------------------------------------------------------------
_TODO_PATTERNS = [
    # setBlock(x, y, z, block, meta, notify) — needs IBlockState
    re.compile(r'(\w+\.setBlock\s*\([^;]+;)', re.DOTALL),
    # setBlockMetadataWithNotify(x, y, z, meta, flags) — needs IBlockState.withProperty()
    re.compile(r'(\w+\.setBlockMetadataWithNotify\s*\([^;]+;)', re.DOTALL),
    # getBlockMetadata(x, y, z) — needs getBlockState().getValue()
    re.compile(r'(\w+\.getBlockMetadata\s*\([^;]+;)', re.DOTALL),
    # setBlockWithNotify — alias for setBlock
    re.compile(r'(\w+\.setBlockWithNotify\s*\([^;]+;)', re.DOTALL),
]
_TODO_MESSAGES = [
    "TODO_PORT: setBlock → world.setBlockState(new BlockPos(x,y,z), state, flags) — needs IBlockState",
    "TODO_PORT: setBlockMetadataWithNotify → setBlockState with state.withProperty(PROP, val)",
    "TODO_PORT: getBlockMetadata → world.getBlockState(new BlockPos(x,y,z)).getValue(PROP)",
    "TODO_PORT: setBlockWithNotify → world.setBlockState(new BlockPos(x,y,z), state, flags)",
]


def _mark_todo(text: str, pat: re.Pattern, msg: str) -> tuple[str, int]:
    count = 0

    def sub(m):
        nonlocal count
        original = m.group(0)
        if "TODO_PORT:" in original:
            return original
        count += 1
        return f"/* {msg} */\n        {original}"

    return pat.sub(sub, text), count


def _ensure_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text
    last_import = max(text.rfind("\nimport "), text.rfind("\nimport\t"))
    if last_import == -1:
        pkg = text.find("\npackage ")
        if pkg != -1:
            eol = text.find("\n", pkg + 1)
            return text[:eol + 1] + "\n" + import_line + "\n" + text[eol + 1:]
        return import_line + "\n" + text
    eol = text.find("\n", last_import + 1)
    return text[:eol + 1] + import_line + "\n" + text[eol + 1:]


def process_file(path: Path, dry_run: bool) -> tuple[int, int]:
    """Return (safe_changes, todos_added)."""
    text = path.read_text(encoding="utf-8", errors="replace")

    # Quick skip: no world method calls of interest
    if not any(kw in text for kw in ("getBlock(", "getTileEntity(", "isAirBlock(",
                                      "setBlockToAir(", "markBlockForUpdate(",
                                      "setBlock(", "setBlockMetadataWithNotify(",
                                      "getBlockMetadata(")):
        return 0, 0

    original = text
    safe_changes = todos = 0
    added_blockpos = False

    # Safe replacements
    for pat, repl in SAFE_REPLACEMENTS:
        def sub(m, r=repl):
            return r(m)
        new_text, n = pat.subn(sub, text)
        if n:
            safe_changes += n
            if not added_blockpos:
                added_blockpos = True
            text = new_text

    # TODO markers
    for pat, msg in zip(_TODO_PATTERNS, _TODO_MESSAGES):
        text, n = _mark_todo(text, pat, msg)
        todos += n

    if safe_changes == 0 and todos == 0:
        return 0, 0

    if added_blockpos:
        text = _ensure_import(text, BLOCKPOS_IMPORT)

    if not dry_run and text != original:
        path.write_text(text, encoding="utf-8")

    return safe_changes, todos


def main(src_root: str, dry_run: bool = False) -> dict:
    root = Path(src_root)
    total_files = total_safe = total_todos = 0
    for java in sorted(root.rglob("*.java")):
        safe, todos = process_file(java, dry_run)
        if safe or todos:
            total_files += 1
            total_safe += safe
            total_todos += todos
            print(f"  {'[DRY]' if dry_run else '[OK] '} {java.relative_to(root)}"
                  f"  ({safe} safe, {todos} TODOs)")
    return {"files": total_files, "safe": total_safe, "todos": total_todos}


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    src = next((a for a in sys.argv[1:] if not a.startswith("--")),
               str(Path(__file__).parent.parent / "src" / "main" / "java"))
    print(f"s5_blockpos: {'DRY RUN — ' if dry else ''}scanning {src}")
    stats = main(src, dry)
    print(f"\ns5_blockpos done: {stats['files']} files, "
          f"{stats['safe']} safe replacements, {stats['todos']} TODO markers added")
