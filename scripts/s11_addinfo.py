#!/usr/bin/env python3
"""
s11_addinfo.py - Convert addInformation(ItemStack, EntityPlayer, List, boolean)
to addInformation(ItemStack, World, List, ITooltipFlag) in 1.12.2.

Also converts super.addInformation calls to match new signature.
"""
import os, re

ROOT = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'java')

# Pattern for the method declaration
DECL_PAT = re.compile(
    r'(public\s+void\s+addInformation\s*\()'
    r'(ItemStack\s+\w+)\s*,\s*'
    r'(EntityPlayer\s+(\w+))\s*,\s*'
    r'(List\s+(\w+))\s*,\s*'
    r'(boolean\s+(\w+))\s*\)'
)

def replace_decl(m):
    stack_param = m.group(2)
    player_name = m.group(4)
    list_param = m.group(5)
    list_name = m.group(6)
    bool_name = m.group(8)
    return (f'{m.group(1)}{stack_param}, '
            f'@javax.annotation.Nullable net.minecraft.world.World worldIn, '
            f'{list_param}, net.minecraft.client.util.ITooltipFlag flagIn)')

def process_file(path):
    with open(path, 'r', encoding='utf-8', errors='replace') as f:
        content = f.read()

    # Only process files that have the old signature
    if 'addInformation' not in content or 'EntityPlayer' not in content:
        return False

    # Find method declarations and collect player/bool param names
    # so we can update super calls and body references
    new_content = content
    player_names = set()
    bool_names = set()

    for m in DECL_PAT.finditer(content):
        player_names.add(m.group(4))
        bool_names.add(m.group(8))

    if not player_names:
        return False

    # Replace declarations
    new_content = DECL_PAT.sub(replace_decl, new_content)

    # Replace super.addInformation calls
    for bool_name in bool_names:
        for player_name in player_names:
            # super.addInformation(stack, player, list, par4)
            old = f'super.addInformation({{}}, {player_name}, {{}}, {bool_name})'
            # Use regex to handle any first/third arg
            super_pat = re.compile(
                r'super\.addInformation\((\w+),\s*' + re.escape(player_name) +
                r'\s*,\s*(\w+)\s*,\s*' + re.escape(bool_name) + r'\s*\)'
            )
            new_content = super_pat.sub(
                r'super.addInformation(\1, worldIn, \2, flagIn)', new_content
            )

    changed = new_content != content
    if changed:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)
    return changed


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
