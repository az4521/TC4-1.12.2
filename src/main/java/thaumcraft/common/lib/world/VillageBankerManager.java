package thaumcraft.common.lib.world;

import net.minecraftforge.fml.common.registry.VillagerRegistry;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import thaumcraft.common.config.ConfigEntities;
import thaumcraft.common.config.ConfigItems;

public class VillageBankerManager implements VillagerRegistry.IVillageCreationHandler /* VillagerRegistry.IVillageTradeHandler removed in 1.12 */ {
   public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
      if (villager.getProfession() == ConfigEntities.entBankerId) {
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 20 + random.nextInt(3), 18), new ItemStack(Items.EMERALD)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 2 + random.nextInt(2), 18), Items.ARROW));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 6 + random.nextInt(3), 18), Item.getItemFromBlock(Blocks.WOOL)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 3 + random.nextInt(2), 18), Items.PAPER));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 7 + random.nextInt(3), 18), Items.BOOK));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 16 + random.nextInt(5), 18), Items.EXPERIENCE_BOTTLE));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 9 + random.nextInt(4), 18), Item.getItemFromBlock(Blocks.GLOWSTONE)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 2 + random.nextInt(2), 18), Items.COAL));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 22 + random.nextInt(3), 18), Items.DIAMOND));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 6 + random.nextInt(3), 18), Items.IRON_INGOT));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 10 + random.nextInt(3), 18), new ItemStack(ConfigItems.itemResource, 1, 2)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 25 + random.nextInt(8), 18), Items.SADDLE));
      }

   }

   public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int i) {
      return new StructureVillagePieces.PieceWeight(ComponentBankerHome.class, 25, MathHelper.getInt(random, i, 1 + i));
   }

   public Class<?> getComponentClass() {
      return ComponentBankerHome.class;
   }

   @Override
   public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, EnumFacing facing, int p5) {
      return (StructureVillagePieces.Village) ComponentBankerHome.buildComponent(startPiece, pieces, random, p1, p2, p3, facing.getIndex(), p5);
   }
}
