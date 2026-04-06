package thaumcraft.api.expands.worldgen.node.consts;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import thaumcraft.api.expands.worldgen.node.PickNodeCoordinateContext;
import thaumcraft.api.expands.worldgen.node.listeners.PickNodeCoordinatesListener;
import thaumcraft.common.lib.utils.Utils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class NodeCoordinatesPickers {
    public static final PickNodeCoordinateContext[] EMPTY_CONTEXTS = new PickNodeCoordinateContext[0];
    public static final PickNodeCoordinatesListener defaultPicker = new PickNodeCoordinatesListener(0) {
        @Override
        @ParametersAreNonnullByDefault
        public PickNodeCoordinateContext[] pickNodeCoordinates(World world, Random random, int chunkX, int chunkZ, boolean auraGen, boolean newGen) {
            int x = chunkX * 16 + random.nextInt(16);
            int z = chunkZ * 16 + random.nextInt(16);
            int selectedY = Utils.getFirstUncoveredY(world, x, z);
            if (selectedY < 2) {
                selectedY = world.provider.getAverageGroundLevel() + random.nextInt(64) - 32 + Utils.getFirstUncoveredY(world, x, z);
            }

            if (selectedY < 2) {
                selectedY = 32 + random.nextInt(64);
            }

            if (world.isAirBlock(new net.minecraft.util.math.BlockPos(x, selectedY + 1, z))) {
                ++selectedY;
            }

            int p = random.nextInt(4);
            net.minecraft.util.math.BlockPos _bPos = new net.minecraft.util.math.BlockPos(x, selectedY + p, z);
            Block b = world.getBlockState(_bPos).getBlock();
            if (world.isAirBlock(_bPos) || b.isReplaceable(world, _bPos)) {
                selectedY += p;
            }
            if (selectedY > world.getActualHeight()) {
                return EMPTY_CONTEXTS;
            }
            return new PickNodeCoordinateContext[]{new PickNodeCoordinateContext(x, selectedY, z, false, false, false)};
        }
    };
}
