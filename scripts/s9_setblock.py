#!/usr/bin/env python3
"""
s9_setblock.py - Convert world.setBlock/getBlock/getBlockMetadata to 1.12.2 API

Conversions:
  world.setBlock(x, y, z, block, meta, flags)
    → world.setBlockState(new BlockPos(x, y, z), (block).getStateFromMeta(meta), flags)

  world.getBlockMetadata(x, y, z)
    → world.getBlockState(new BlockPos(x, y, z)).getBlock().getMetaFromState(world.getBlockState(new BlockPos(x, y, z)))

  world.getBlock(x, y, z)     [3-arg form only, not getBlockState]
    → world.getBlockState(new BlockPos(x, y, z)).getBlock()
"""

import os
import re
import sys

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'java')

def extract_args(s, start):
    """Extract comma-separated top-level arguments starting after opening paren.
    Returns list of arg strings and the index after the closing paren."""
    depth = 0
    args = []
    current = []
    i = start
    while i < len(s):
        c = s[i]
        if c in '([{':
            depth += 1
            current.append(c)
        elif c in ')]}':
            if depth == 0:
                args.append(''.join(current).strip())
                return args, i + 1
            depth -= 1
            current.append(c)
        elif c == ',' and depth == 0:
            args.append(''.join(current).strip())
            current = []
        else:
            current.append(c)
        i += 1
    return args, i  # unterminated


def transform_line(line):
    changed = False

    # world.setBlock(x, y, z, block, meta, flags)
    # Match 'world.setBlock(' or 'World.setBlock(' etc.
    pattern_setblock = re.compile(r'(world|this\.world|par1World|worldObj|w)\.setBlock\(')
    # world.getBlockMetadata(x, y, z)
    pattern_getmeta = re.compile(r'(world|this\.world|par1World|worldObj|w)\.getBlockMetadata\(')
    # world.getBlock(x, y, z)  [exactly 3 args]
    pattern_getblock = re.compile(r'(world|this\.world|par1World|worldObj|w)\.getBlock\(')

    result = []
    i = 0
    s = line

    while i < len(s):
        # Try setBlock
        m = pattern_setblock.search(s, i)
        m2 = pattern_getmeta.search(s, i)
        m3 = pattern_getblock.search(s, i)

        # Pick earliest match
        candidates = [(m.start(), m, 'setblock') if m else None,
                      (m2.start(), m2, 'getmeta') if m2 else None,
                      (m3.start(), m3, 'getblock') if m3 else None]
        candidates = [c for c in candidates if c is not None]
        if not candidates:
            result.append(s[i:])
            break

        candidates.sort(key=lambda x: x[0])
        pos, match, kind = candidates[0]

        result.append(s[i:pos])
        prefix = match.group(1)  # e.g. 'world' or 'this.world'

        # Find opening paren
        open_paren = match.end()  # position right after '('
        args, after = extract_args(s, open_paren)

        if kind == 'setblock' and len(args) == 6:
            x, y, z, block, meta, flags = args
            replacement = f'{prefix}.setBlockState(new net.minecraft.util.math.BlockPos({x}, {y}, {z}), ({block}).getStateFromMeta({meta}), {flags})'
            result.append(replacement)
            i = after
            changed = True
        elif kind == 'setblock' and len(args) == 4:
            # world.setBlock(x, y, z, block) - no meta/flags version
            x, y, z, block = args
            replacement = f'{prefix}.setBlockState(new net.minecraft.util.math.BlockPos({x}, {y}, {z}), ({block}).getDefaultState(), 3)'
            result.append(replacement)
            i = after
            changed = True
        elif kind == 'getmeta' and len(args) == 3:
            x, y, z = args
            # Use getMetaFromState
            replacement = f'{prefix}.getBlockState(new net.minecraft.util.math.BlockPos({x}, {y}, {z})).getBlock().getMetaFromState({prefix}.getBlockState(new net.minecraft.util.math.BlockPos({x}, {y}, {z})))'
            result.append(replacement)
            i = after
            changed = True
        elif kind == 'getblock' and len(args) == 3:
            x, y, z = args
            replacement = f'{prefix}.getBlockState(new net.minecraft.util.math.BlockPos({x}, {y}, {z})).getBlock()'
            result.append(replacement)
            i = after
            changed = True
        else:
            # Can't parse cleanly - emit as-is and advance past the match
            result.append(s[pos:match.end()])
            i = match.end()

    return ''.join(result), changed


def process_file(path):
    with open(path, 'r', encoding='utf-8', errors='replace') as f:
        lines = f.readlines()

    new_lines = []
    file_changed = False
    for line in lines:
        new_line, changed = transform_line(line)
        new_lines.append(new_line)
        if changed:
            file_changed = True

    if file_changed:
        with open(path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
    return file_changed


def main():
    changed_count = 0
    file_count = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        for fname in filenames:
            if not fname.endswith('.java'):
                continue
            path = os.path.join(dirpath, fname)
            try:
                if process_file(path):
                    changed_count += 1
                    rel = os.path.relpath(path, ROOT)
                    print(f'  Modified: {rel}')
            except Exception as e:
                print(f'  ERROR in {fname}: {e}')
            file_count += 1

    print(f'\nDone. Modified {changed_count} of {file_count} files.')


if __name__ == '__main__':
    main()
