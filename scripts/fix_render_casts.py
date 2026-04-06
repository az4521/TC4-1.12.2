#!/usr/bin/env python3
"""Add explicit IRenderFactory casts to entity renderer registrations in ClientProxy."""
import re

path = 'c:/Users/az4521/Documents/programming/TC4_1.12.2/src/main/java/thaumcraft/client/ClientProxy.java'

with open(path, 'r') as f:
    content = f.read()

def add_cast(m):
    entity = m.group(1)
    rest = m.group(2)
    return f'registerEntityRenderingHandler({entity}.class, (IRenderFactory<{entity}>) {rest}'

content2 = re.sub(
    r'registerEntityRenderingHandler\((\w+)\.class, (manager ->)',
    add_cast,
    content
)

changed = content != content2
if changed:
    with open(path, 'w') as f:
        f.write(content2)
    print("Done - casts added")
else:
    print("No changes needed (already cast or pattern not found)")
