package thaumcraft.api.expands.worldgen.node.consts;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thaumcraft.api.expands.worldgen.node.CreateNodeContext;
import thaumcraft.api.expands.worldgen.node.listeners.CreateNodeListener;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.tiles.TileNode;
import net.minecraft.util.math.BlockPos;

public class CreateNodeListeners {
    public static CreateNodeListener DEFAULT_NODE_CREATOR = new CreateNodeListener(0) {
        @Override
        public boolean onCreateNode(World world, CreateNodeContext context) {
            if (world.isAirBlock(new BlockPos(context.x, context.y, context.z))) {
                world.setBlockState(new BlockPos(context.x, context.y, context.z), ConfigBlocks.blockAiry.getDefaultState(), 3);
            }

            TileEntity te = world.getTileEntity(new BlockPos(context.x, context.y, context.z));
            if (te instanceof TileNode) {
                TileNode tileNode = (TileNode) te;
                tileNode.setNodeType(context.nodeType);
                tileNode.setNodeModifier(context.nodeModifier);
                tileNode.setAspects(context.aspects);
            }

            { BlockPos _np = new BlockPos(context.x, context.y, context.z); net.minecraft.block.state.IBlockState _bs = world.getBlockState(_np); world.notifyBlockUpdate(_np, _bs, _bs, 3); }
            return false;
        }
    };
}
