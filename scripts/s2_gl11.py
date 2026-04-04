#!/usr/bin/env python3
"""
s2_gl11.py — Replace direct GL11 calls with GlStateManager equivalents.

Safe for all listed patterns. GL11 calls that aren't in the mapping are
left as-is (they still compile in 1.12.2 but bypass Forge's state tracker).

Adds:
  import net.minecraft.client.renderer.GlStateManager;
to any file it modifies (skips if already present).
"""
import re
import sys
from pathlib import Path

# ---------------------------------------------------------------------------
# Simple method renames: GL11.glFoo( → GlStateManager.foo(
# Applied as exact prefix string replacements (not regex) for safety.
# ---------------------------------------------------------------------------
SIMPLE_RENAMES = [
    ("GL11.glPushMatrix()",     "GlStateManager.pushMatrix()"),
    ("GL11.glPopMatrix()",      "GlStateManager.popMatrix()"),
    ("GL11.glTranslatef(",      "GlStateManager.translate("),
    ("GL11.glTranslated(",      "GlStateManager.translate("),
    ("GL11.glRotatef(",         "GlStateManager.rotate("),
    ("GL11.glRotated(",         "GlStateManager.rotate("),
    ("GL11.glScalef(",          "GlStateManager.scale("),
    ("GL11.glScaled(",          "GlStateManager.scale("),
    ("GL11.glColor4f(",         "GlStateManager.color("),
    ("GL11.glColor3f(",         "GlStateManager.color("),
    ("GL11.glDepthMask(",       "GlStateManager.depthMask("),
    ("GL11.glBlendFunc(",       "GlStateManager.blendFunc("),
    ("GL11.glAlphaFunc(",       "GlStateManager.alphaFunc("),
    ("GL11.glClearColor(",      "GlStateManager.clearColor("),
    ("GL11.glClear(",           "GlStateManager.clear("),
    ("GL11.glShadeModel(",      "GlStateManager.shadeModel("),
    ("GL11.glColorMask(",       "GlStateManager.colorMask("),
    ("GL11.glBindTexture(GL11.GL_TEXTURE_2D, ", "GlStateManager.bindTexture("),
    ("GL11.glBindTexture(",     "GlStateManager.bindTexture("),  # fallback
]

# ---------------------------------------------------------------------------
# glEnable / glDisable → specific GlStateManager methods.
# Keys are the argument as it appears in source (symbolic or numeric literal).
# ---------------------------------------------------------------------------
ENABLE_MAP = {
    # symbolic
    "GL11.GL_BLEND":          ("GlStateManager.enableBlend()",          "GlStateManager.disableBlend()"),
    "GL11.GL_DEPTH_TEST":     ("GlStateManager.enableDepth()",          "GlStateManager.disableDepth()"),
    "GL11.GL_LIGHTING":       ("GlStateManager.enableLighting()",       "GlStateManager.disableLighting()"),
    "GL11.GL_CULL_FACE":      ("GlStateManager.enableCull()",           "GlStateManager.disableCull()"),
    "GL11.GL_TEXTURE_2D":     ("GlStateManager.enableTexture2D()",      "GlStateManager.disableTexture2D()"),
    "GL11.GL_ALPHA_TEST":     ("GlStateManager.enableAlpha()",          "GlStateManager.disableAlpha()"),
    "GL11.GL_FOG":            ("GlStateManager.enableFog()",            "GlStateManager.disableFog()"),
    "GL11.GL_COLOR_MATERIAL": ("GlStateManager.enableColorMaterial()",  "GlStateManager.disableColorMaterial()"),
    "GL12.GL_RESCALE_NORMAL": ("GlStateManager.enableRescaleNormal()",  "GlStateManager.disableRescaleNormal()"),
    # numeric decimal literals (as found in this codebase)
    "3042":  ("GlStateManager.enableBlend()",          "GlStateManager.disableBlend()"),      # GL_BLEND     0x0BE2
    "2929":  ("GlStateManager.enableDepth()",          "GlStateManager.disableDepth()"),      # GL_DEPTH_TEST 0x0B71
    "2896":  ("GlStateManager.enableLighting()",       "GlStateManager.disableLighting()"),   # GL_LIGHTING  0x0B50
    "2884":  ("GlStateManager.enableCull()",           "GlStateManager.disableCull()"),       # GL_CULL_FACE 0x0B44
    "3553":  ("GlStateManager.enableTexture2D()",      "GlStateManager.disableTexture2D()"),  # GL_TEXTURE_2D 0x0DE1
    "3008":  ("GlStateManager.enableAlpha()",          "GlStateManager.disableAlpha()"),      # GL_ALPHA_TEST 0x0BC0
    "2912":  ("GlStateManager.enableFog()",            "GlStateManager.disableFog()"),        # GL_FOG       0x0B60
    "32826": ("GlStateManager.enableRescaleNormal()",  "GlStateManager.disableRescaleNormal()"),  # GL_RESCALE_NORMAL 0x803A
    "2903":  ("GlStateManager.enableColorMaterial()",  "GlStateManager.disableColorMaterial()"),  # GL_COLOR_MATERIAL 0x0B57
    "2881":  ("GlStateManager.enableLighting()",       "GlStateManager.disableLighting()"),   # GL_LIGHTING (alt value seen in source)
}

# Pattern: GL11.glEnable(ARG) or GL11.glDisable(ARG)
# We match the whole call including trailing ) and optional ;
_ENABLE_RE  = re.compile(r'GL11\.glEnable\s*\(\s*([^)]+?)\s*\)\s*;')
_DISABLE_RE = re.compile(r'GL11\.glDisable\s*\(\s*([^)]+?)\s*\)\s*;')

GLSTATEMANAGER_IMPORT = "import net.minecraft.client.renderer.GlStateManager;"
GL11_IMPORT_RE = re.compile(r'^import\s+org\.lwjgl\.opengl\.GL11\s*;\s*$', re.MULTILINE)
GL12_IMPORT_RE = re.compile(r'^import\s+org\.lwjgl\.opengl\.GL12\s*;\s*$', re.MULTILINE)


def _replace_enable_disable(text: str) -> tuple[str, int]:
    changes = 0

    def replace_enable(m):
        nonlocal changes
        arg = m.group(1).strip()
        if arg in ENABLE_MAP:
            changes += 1
            return ENABLE_MAP[arg][0] + ";"
        return m.group(0)  # leave unchanged

    def replace_disable(m):
        nonlocal changes
        arg = m.group(1).strip()
        if arg in ENABLE_MAP:
            changes += 1
            return ENABLE_MAP[arg][1] + ";"
        return m.group(0)  # leave unchanged

    text = _ENABLE_RE.sub(replace_enable, text)
    text = _DISABLE_RE.sub(replace_disable, text)
    return text, changes


def _apply_simple_renames(text: str) -> tuple[str, int]:
    changes = 0
    for old, new in SIMPLE_RENAMES:
        count = text.count(old)
        if count:
            text = text.replace(old, new)
            changes += count
    return text, changes


def _ensure_import(text: str, import_line: str) -> str:
    if import_line in text:
        return text
    # Insert after last existing import block
    last_import = max(text.rfind("\nimport "), text.rfind("\nimport\t"))
    if last_import == -1:
        # No imports at all — insert after package declaration
        pkg = text.find("\npackage ")
        if pkg != -1:
            eol = text.find("\n", pkg + 1)
            return text[:eol + 1] + "\n" + import_line + "\n" + text[eol + 1:]
        return import_line + "\n" + text
    eol = text.find("\n", last_import + 1)
    return text[:eol + 1] + import_line + "\n" + text[eol + 1:]


def _check_gl11_still_used(text: str) -> bool:
    """Return True if any GL11.gl* call remains after our transforms."""
    return bool(re.search(r'\bGL11\.gl', text))


def process_file(path: Path, dry_run: bool) -> tuple[int, int]:
    """Return (changes, gl11_import_removed)."""
    text = path.read_text(encoding="utf-8", errors="replace")
    original = text

    text, simple_changes = _apply_simple_renames(text)
    text, enable_changes = _replace_enable_disable(text)
    total_changes = simple_changes + enable_changes

    if total_changes == 0:
        return 0, 0

    # Add GlStateManager import if not present
    text = _ensure_import(text, GLSTATEMANAGER_IMPORT)

    # Remove GL11 import if no GL11.gl* calls remain
    gl11_removed = 0
    if not _check_gl11_still_used(text):
        new_text = GL11_IMPORT_RE.sub("", text)
        if new_text != text:
            text = new_text
            gl11_removed = 1

    if not dry_run and text != original:
        path.write_text(text, encoding="utf-8")

    return total_changes, gl11_removed


def main(src_root: str, dry_run: bool = False) -> dict:
    root = Path(src_root)
    total_files = total_changes = total_removed = 0
    for java in sorted(root.rglob("*.java")):
        changes, removed = process_file(java, dry_run)
        if changes:
            total_files += 1
            total_changes += changes
            total_removed += removed
            rel = java.relative_to(root)
            print(f"  {'[DRY]' if dry_run else '[OK] '} {rel}  ({changes} replacements{', GL11 import removed' if removed else ''})")
    return {"files": total_files, "changes": total_changes, "imports_removed": total_removed}


if __name__ == "__main__":
    dry = "--dry-run" in sys.argv
    src = next((a for a in sys.argv[1:] if not a.startswith("--")),
               str(Path(__file__).parent.parent / "src" / "main" / "java"))
    print(f"s2_gl11: {'DRY RUN — ' if dry else ''}scanning {src}")
    stats = main(src, dry)
    print(f"\ns2_gl11 done: {stats['files']} files, {stats['changes']} replacements, "
          f"{stats['imports_removed']} GL11 imports removed")
