"""
Deobfuscate all .java files under src/main/java using MCP mappings.
Run from the project root: python scripts/deobfuscate.py
"""
import csv
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).parent.parent
JAVA_ROOT = ROOT / "src" / "main" / "java"
MAPPINGS = ROOT / "scripts" / "mappings"

def load_csv(path):
    """Return list of (searge_name, deob_name) from a mappings CSV."""
    pairs = []
    with open(path, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            searge = row.get('searge') or row.get('param')
            name = row.get('name')
            if searge and name and searge != name:
                pairs.append((searge, name))
    return pairs

# Build replacement map: searge → deob, sorted longest-first to avoid
# partial replacements (e.g. func_12345_a before func_12345_ab)
replacements = {}
for csv_file in ['fields.csv', 'methods.csv', 'params.csv']:
    for searge, name in load_csv(MAPPINGS / csv_file):
        replacements[searge] = name

# Sort by length descending so longer names replace before shorter prefixes
sorted_pairs = sorted(replacements.items(), key=lambda x: len(x[0]), reverse=True)

# Build a single combined regex for efficiency: match any searge name as a whole word
# These names are distinctive (field_NNN_x, func_NNN_x, p_NNN_N_) so word boundaries work.
pattern = re.compile(
    r'\b(' + '|'.join(re.escape(s) for s, _ in sorted_pairs) + r')\b'
)
lookup = dict(sorted_pairs)

files_changed = 0
replacements_made = 0

for java_file in JAVA_ROOT.rglob('*.java'):
    text = java_file.read_text(encoding='utf-8')
    new_text, count = pattern.subn(lambda m: lookup[m.group(0)], text)
    if count:
        java_file.write_text(new_text, encoding='utf-8')
        print(f"  {java_file.relative_to(ROOT)} — {count} replacement(s)")
        files_changed += 1
        replacements_made += count

print(f"\nDone: {replacements_made} replacements across {files_changed} files.")
