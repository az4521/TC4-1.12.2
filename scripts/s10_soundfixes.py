#!/usr/bin/env python3
"""
s10_soundfixes.py - Convert world.playSoundAtEntity and similar old sound API calls to 1.12.2

Old: world.playSoundAtEntity(entity, "thaumcraft:foo", vol, pitch)
New: { SoundEvent _snd = SoundEvent.REGISTRY.getObject(new ResourceLocation("thaumcraft:foo")); if (_snd != null) world.playSound(null, entity.posX, entity.posY, entity.posZ, _snd, SoundCategory.NEUTRAL, vol, pitch); }

Also handles:
  world.playSound(x, y, z, "foo", vol, pitch, false) -> SoundEvent pattern
  player.playSound("foo", vol, pitch) -> SoundEvent pattern
"""

import os, re, sys

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'java')

def extract_args(s, start):
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
    return args, i

def fix_string_sound(sound_str):
    """Normalize a sound string literal. If it's "random.bow" → "minecraft:random.bow" etc."""
    # strip quotes
    inner = sound_str.strip()
    if inner.startswith('"') and inner.endswith('"'):
        inner = inner[1:-1]
        if ':' not in inner:
            # vanilla sound
            inner = 'minecraft:' + inner
        return f'"{inner}"'
    return sound_str

def make_sound_block(world_expr, x_expr, y_expr, z_expr, sound_str, cat, vol, pitch):
    sound_str = fix_string_sound(sound_str)
    return (f'{{ net.minecraft.util.SoundEvent _snd = net.minecraft.util.SoundEvent.REGISTRY.getObject'
            f'(new net.minecraft.util.ResourceLocation({sound_str})); '
            f'if (_snd != null) {world_expr}.playSound(null, {x_expr}, {y_expr}, {z_expr}, _snd, '
            f'net.minecraft.util.SoundCategory.{cat}, {vol}, {pitch}); }}')

def transform_line(line):
    changed = False
    result = []
    s = line
    i = 0

    # Pattern 1: world.playSoundAtEntity(entity, "sound", vol, pitch)
    pat1 = re.compile(r'([\w.]+)\.playSoundAtEntity\(')
    # Pattern 2: world.playSound(x, y, z, "sound", vol, pitch, bool) - old 7-arg form
    pat2 = re.compile(r'([\w.]+)\.playSound\(')

    while i < len(s):
        m1 = pat1.search(s, i)
        m2 = pat2.search(s, i)

        candidates = []
        if m1: candidates.append((m1.start(), m1, 'soundAtEntity'))
        if m2: candidates.append((m2.start(), m2, 'playSound'))
        if not candidates:
            result.append(s[i:])
            break

        candidates.sort(key=lambda x: x[0])
        pos, match, kind = candidates[0]
        result.append(s[i:pos])
        prefix = match.group(1)

        args, after = extract_args(s, match.end())

        if kind == 'soundAtEntity' and len(args) == 4:
            entity, sound, vol, pitch = args
            replacement = make_sound_block(
                prefix,
                f'{entity}.posX', f'{entity}.posY', f'{entity}.posZ',
                sound, 'NEUTRAL', vol, pitch
            )
            result.append(replacement)
            i = after
            changed = True
        elif kind == 'playSound' and len(args) == 7:
            # world.playSound(x, y, z, "sound", vol, pitch, distDelay)
            x, y, z, sound, vol, pitch, delay = args
            # Only convert if sound arg is a string literal
            if sound.strip().startswith('"'):
                replacement = make_sound_block(prefix, x, y, z, sound, 'NEUTRAL', vol, pitch)
                result.append(replacement)
                i = after
                changed = True
            else:
                result.append(s[pos:match.end()])
                i = match.end()
        elif kind == 'playSound' and len(args) == 6:
            # world.playSound(x, y, z, "sound", vol, pitch) - 6-arg old form
            x, y, z, sound, vol, pitch = args
            if sound.strip().startswith('"'):
                replacement = make_sound_block(prefix, x, y, z, sound, 'NEUTRAL', vol, pitch)
                result.append(replacement)
                i = after
                changed = True
            else:
                result.append(s[pos:match.end()])
                i = match.end()
        else:
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
