#!/usr/bin/env python3
"""
s3_tessellator.py — Migrate Tessellator API from 1.7.10 to 1.12.2 (BufferBuilder).

HEURISTIC — handles common patterns observed in the TC4 codebase.
Marks complex cases with // TODO_PORT: for manual review.

Key API changes:
  1.7.10: Tessellator.instance  (static field)
  1.12.2: Tessellator.getInstance()  (static method) + getBuffer()

  1.7.10: tess.startDrawingQuads()
  1.12.2: buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

  1.7.10: tess.addVertexWithUV(x,y,z, u,v)
  1.12.2: buf.pos(x,y,z).tex(u,v).endVertex()  [needs color in vertex for POSITION_TEX_COLOR]

Review ALL files modified by this script.
"""
import re
import sys
from pathlib import Path

BUFBUILDER_IMPORT  = "import net.minecraft.client.renderer.BufferBuilder;"
VERTEXFMT_IMPORT   = "import net.minecraft.client.renderer.vertex.DefaultVertexFormats;"
TESS_IMPORT_PAT    = re.compile(r'^import\s+net\.minecraft\.client\.renderer\.Tessellator\s*;\s*$', re.MULTILINE)

# ---------------------------------------------------------------------------
# Pattern 1: Tessellator.instance → Tessellator.getInstance() + getBuffer()
#
# Before:  Tessellator tess = Tessellator.instance;
# After:   Tessellator tess = Tessellator.getInstance();
#          BufferBuilder buf = tess.getBuffer();   (variable name derived from tess var)
#
# We capture the variable name to use in the getBuffer() line.
# ---------------------------------------------------------------------------
_TESS_INSTANCE_RE = re.compile(
    r'([ \t]*)Tessellator\s+(\w+)\s*=\s*Tessellator\.instance\s*;'
)

def _replace_tess_instance(m: re.Match) -> str:
    indent = m.group(1)
    var = m.group(2)
    # Derive buffer var name: 'tessellator' → 'buffer', 't' → 'buf', others → varBuf
    buf_var = {"tessellator": "buffer", "tess": "buf", "t": "buf"}.get(var, var + "Buf")
    return (f"{indent}Tessellator {var} = Tessellator.getInstance();\n"
            f"{indent}BufferBuilder {buf_var} = {var}.getBuffer();")

# ---------------------------------------------------------------------------
# Pattern 2: startDrawingQuads() → buf.begin(GL11.GL_QUADS, ...)
#
# We use POSITION_TEX_COLOR as the safe default (works for most TC4 renders
# that mix UV + color). The script marks the generated line with a comment
# so the reviewer can verify the format is correct for that draw block.
# ---------------------------------------------------------------------------
_START_QUADS_RE = re.compile(r'(\w+)\.startDrawingQuads\(\s*\)\s*;')
_START_DRAWING_RE = re.compile(r'(\w+)\.startDrawing\(\s*(\d+)\s*\)\s*;')

_GL_MODE_MAP = {
    "0":  "GL11.GL_POINTS",
    "1":  "GL11.GL_LINES",
    "2":  "GL11.GL_LINE_LOOP",
    "3":  "GL11.GL_LINE_STRIP",
    "4":  "GL11.GL_TRIANGLES",
    "5":  "GL11.GL_TRIANGLE_STRIP",
    "6":  "GL11.GL_TRIANGLE_FAN",
    "7":  "GL11.GL_QUADS",
}

def _tess_var_to_buf(var: str) -> str:
    return {"tessellator": "buffer", "tess": "buf", "t": "buf"}.get(var, var + "Buf")

def _replace_start_drawing(text: str) -> tuple[str, int]:
    changes = 0

    def sub_quads(m):
        nonlocal changes
        tvar = m.group(1)
        buf = _tess_var_to_buf(tvar)
        changes += 1
        return (f"{buf}.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR); "
                f"// TODO_PORT: verify vertex format")

    def sub_drawing(m):
        nonlocal changes
        tvar = m.group(1)
        mode_int = m.group(2)
        buf = _tess_var_to_buf(tvar)
        gl_mode = _GL_MODE_MAP.get(mode_int, f"/* GL mode {mode_int} */")
        changes += 1
        return (f"{buf}.begin({gl_mode}, DefaultVertexFormats.POSITION_TEX_COLOR); "
                f"// TODO_PORT: verify vertex format")

    text = _START_QUADS_RE.sub(sub_quads, text)
    text = _START_DRAWING_RE.sub(sub_drawing, text)
    return text, changes

# ---------------------------------------------------------------------------
# Pattern 3: addVertexWithUV(x, y, z, u, v) → buf.pos(x,y,z).tex(u,v).endVertex()
#
# Color must now be per-vertex for POSITION_TEX_COLOR; if setColorRGBA_F was
# called before this block the script adds a TODO comment.
# ---------------------------------------------------------------------------
_ADD_UV_RE = re.compile(
    r'(\w+)\.addVertexWithUV\('
    r'\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,'  # x, y, z
    r'\s*([^,]+?)\s*,\s*([^)]+?)\s*\)\s*;'              # u, v
)
_ADD_VERTEX_RE = re.compile(
    r'(\w+)\.addVertex\('
    r'\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^)]+?)\s*\)\s*;'  # x, y, z
)

def _replace_vertex_calls(text: str) -> tuple[str, int]:
    changes = 0

    def sub_uv(m):
        nonlocal changes
        tvar, x, y, z, u, v = m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6)
        buf = _tess_var_to_buf(tvar)
        changes += 1
        # For POSITION_TEX_COLOR we need color; insert TODO placeholder
        return (f"{buf}.pos({x}, {y}, {z}).tex({u}, {v})"
                f".color(1.0f, 1.0f, 1.0f, 1.0f) // TODO_PORT: set actual color\n"
                f"        .endVertex();")

    def sub_vertex(m):
        nonlocal changes
        tvar, x, y, z = m.group(1), m.group(2), m.group(3), m.group(4)
        buf = _tess_var_to_buf(tvar)
        changes += 1
        return f"{buf}.pos({x}, {y}, {z}).endVertex();"

    text = _ADD_UV_RE.sub(sub_uv, text)
    text = _ADD_VERTEX_RE.sub(sub_vertex, text)
    return text, changes

# ---------------------------------------------------------------------------
# Pattern 4: Color-setting calls — add TODO since color now goes per-vertex
# ---------------------------------------------------------------------------
_COLOR_CALLS = [
    re.compile(r'(\w+)\.setColorRGBA_F\s*\(([^)]*)\)\s*;'),
    re.compile(r'(\w+)\.setColorRGBA_I\s*\(([^)]*)\)\s*;'),
    re.compile(r'(\w+)\.setColorOpaque_I\s*\(([^)]*)\)\s*;'),
    re.compile(r'(\w+)\.setBrightness\s*\(([^)]*)\)\s*;'),
    re.compile(r'(\w+)\.setNormal\s*\(([^)]*)\)\s*;'),
]
_COLOR_REPLACE = [
    "// TODO_PORT: color now per-vertex — use buf.pos(...).tex(...).color(r,g,b,a).endVertex()",
    "// TODO_PORT: unpack int color into r,g,b,a and use buf...color() per vertex",
    "// TODO_PORT: unpack int color (opaque) into r,g,b and use buf...color() per vertex",
    "// TODO_PORT: lightmap — buf.pos(...).tex(...).lightmap(b>>16, b&0xFFFF).endVertex()",
    "// TODO_PORT: normal — use buf...normal(nx,ny,nz).endVertex() on the vertex call",
]

def _mark_color_calls(text: str) -> tuple[str, int]:
    changes = 0
    for pat, replacement in zip(_COLOR_CALLS, _COLOR_REPLACE):
        def sub(m, r=replacement):
            return r
        new_text, n = pat.subn(sub, text)
        changes += n
        text = new_text
    return text, changes

# ---------------------------------------------------------------------------
# Pattern 5: tess.draw() stays as-is (Tessellator.draw() wraps the buffer)
# But we rename variable references for consistency.
# Actually: Tessellator.draw() still works in 1.12.2 — no change needed.
# ---------------------------------------------------------------------------

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


def process_file(path: Path, dry_run: bool) -> int:
    text = path.read_text(encoding="utf-8", errors="replace")

    # Quick check: does this file use Tessellator?
    if "Tessellator" not in text:
        return 0

    original = text
    total = 0

    # Step 1: instance → getInstance() + getBuffer()
    new_text = _TESS_INSTANCE_RE.sub(_replace_tess_instance, text)
    total += text.count("Tessellator.instance")  # approximate
    text = new_text

    # Step 2: startDrawing variants
    text, n = _replace_start_drawing(text)
    total += n

    # Step 3: vertex calls
    text, n = _replace_vertex_calls(text)
    total += n

    # Step 4: color/brightness calls → TODO markers
    text, n = _mark_color_calls(text)
    total += n

    if total == 0:
        return 0

    # Add imports
    text = _ensure_import(text, BUFBUILDER_IMPORT)
    text = _ensure_import(text, VERTEXFMT_IMPORT)

    if not dry_run and text != original:
        path.write_text(text, encoding="utf-8")

    return total


def main(src_root: str, dry_run: bool = False) -> dict:
    root = Path(src_root)
    total_files = total_changes = 0
    for java in sorted(root.rglob("*.java")):
        n = process_file(java, dry_run)
        if n:
            total_files += 1
            total_changes += n
            print(f"  {'[DRY]' if dry_run else '[OK] '} {java.relative_to(root)}  ({n} transforms)")
    return {"files": total_files, "changes": total_changes}


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    src = next((a for a in sys.argv[1:] if not a.startswith("--")),
               str(Path(__file__).parent.parent / "src" / "main" / "java"))
    print(f"s3_tessellator: {'DRY RUN — ' if dry else ''}scanning {src}")
    print("  NOTE: This script is heuristic. Review all modified files.")
    stats = main(src, dry)
    print(f"\ns3_tessellator done: {stats['files']} files, {stats['changes']} transforms applied")
