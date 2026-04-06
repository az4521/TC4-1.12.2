package tc4tweak.modules.generateItemHash;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import tc4tweak.modules.FlushableCache;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomItemStacks extends FlushableCache<Set<String>> {
    @Override
    protected Set<String> createCache() {
        // TODO_PORT: GameData.customItemStacks was removed in 1.12 -- no direct equivalent in ForgeRegistries
        return Collections.emptySet();
    }
}
