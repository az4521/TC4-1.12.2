package thaumcraft.common.lib.world;

import net.minecraftforge.fml.common.registry.VillagerRegistry;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import thaumcraft.common.config.ConfigEntities;
import thaumcraft.common.config.ConfigItems;

public class VillageWizardManager implements VillagerRegistry.IVillageCreationHandler /* VillagerRegistry.IVillageTradeHandler removed in 1.12 */ {
   public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
      if (villager.getProfession() == ConfigEntities.entWizardId) {
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 20 + random.nextInt(3), 18), new ItemStack(Items.EMERALD)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD), new ItemStack(ConfigItems.itemResource, 1, 9)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 4 + random.nextInt(3), 3), new ItemStack(Items.EMERALD)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD), new ItemStack(ConfigItems.itemResource, 1, 0)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemResource, 4 + random.nextInt(3), 6), new ItemStack(Items.EMERALD)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD), new ItemStack(ConfigItems.itemResource, 1, 1)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemNuggetChicken, 24 + random.nextInt(8), 0), new ItemStack(Items.EMERALD)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.BOOK, 4 + random.nextInt(3), 0), new ItemStack(ConfigItems.itemResource, 1, 9)));
         recipeList.add(new MerchantRecipe(new ItemStack(ConfigItems.itemNuggetBeef, 24 + random.nextInt(8), 0), new ItemStack(Items.EMERALD)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD), new ItemStack(ConfigItems.itemShard, 2 + random.nextInt(2), random.nextInt(6))));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD), new ItemStack(ConfigItems.itemManaBean, 1 + random.nextInt(2), 0)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 5 + random.nextInt(3)), new ItemStack(ConfigItems.itemBathSalts, 1, 0)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 5 + random.nextInt(3)), new ItemStack(ConfigItems.itemRingRunic, 1, 0)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 5 + random.nextInt(3)), new ItemStack(ConfigItems.itemAmuletVis, 1, 0)));
         recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, 5 + random.nextInt(3)), new ItemStack(ConfigItems.itemBaubleBlanks, 1, 3 + random.nextInt(6))));
      }

   }

   public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int i) {
      return new StructureVillagePieces.PieceWeight(ComponentWizardTower.class, 15, MathHelper.getInt(random, i, 1 + i));
   }

   public Class<?> getComponentClass() {
      return ComponentWizardTower.class;
   }

   @Override
   public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List<StructureComponent> pieces, Random random, int p1, int p2, int p3, EnumFacing facing, int p5) {
      return (StructureVillagePieces.Village) ComponentWizardTower.buildComponent(startPiece, pieces, random, p1, p2, p3, facing.getIndex(), p5);
   }
}
