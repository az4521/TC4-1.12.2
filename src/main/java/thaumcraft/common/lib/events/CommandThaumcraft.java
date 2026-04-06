package thaumcraft.common.lib.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketSyncAspects;
import thaumcraft.common.lib.network.playerdata.PacketSyncResearch;
import thaumcraft.common.lib.network.playerdata.PacketSyncWarp;
import thaumcraft.common.lib.network.playerdata.PacketWarpMessage;
import thaumcraft.common.lib.research.ResearchManager;

public class CommandThaumcraft extends CommandBase {
   private List<String> aliases = new ArrayList<>();

   public CommandThaumcraft() {
      this.aliases.add("thaumcraft");
      this.aliases.add("thaum");
      this.aliases.add("tc");
   }

   @Override
   public String getName() {
      return "thaumcraft";
   }

   @Override
   public String getUsage(ICommandSender icommandsender) {
      return "/thaumcraft <action> [<player> [<params>]]";
   }

   @Override
   public List<String> getAliases() {
      return this.aliases;
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   @Override
   public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
      return new ArrayList<>();
   }

   @Override
   public boolean isUsernameIndex(String[] astring, int i) {
      return i == 1;
   }

   @Override
   public void execute(MinecraftServer server, ICommandSender icommandsender, String[] astring) throws CommandException {
      if (astring.length == 0) {
         icommandsender.sendMessage(new TextComponentTranslation("§cInvalid arguments"));
         icommandsender.sendMessage(new TextComponentTranslation("§cUse /thaumcraft help to get help"));
      } else {
         if (astring[0].equalsIgnoreCase("help")) {
            icommandsender.sendMessage(new TextComponentTranslation("§3You can also use /thaum or /tc instead of /thaumcraft."));
            icommandsender.sendMessage(new TextComponentTranslation("§3Use this to give research to a player."));
            icommandsender.sendMessage(new TextComponentTranslation("  /thaumcraft research <list|player> <all|reset|<research>>"));
            icommandsender.sendMessage(new TextComponentTranslation("§3Use this to give aspect research points to a player."));
            icommandsender.sendMessage(new TextComponentTranslation("  /thaumcraft aspect <player> <aspect|all> <amount>"));
            icommandsender.sendMessage(new TextComponentTranslation("§3Use this to give set a players warp level."));
            icommandsender.sendMessage(new TextComponentTranslation("  /thaumcraft warp <player> <add|set> <amount> <PERM|TEMP>"));
            icommandsender.sendMessage(new TextComponentTranslation("  not specifying perm or temp will just add normal warp"));
         } else if (astring.length >= 2) {
            if (astring[0].equalsIgnoreCase("research") && astring[1].equalsIgnoreCase("list")) {
               this.listResearch(icommandsender);
            } else {
               EntityPlayerMP entityplayermp = getPlayer(server, icommandsender, astring[1]);
               if (astring[0].equalsIgnoreCase("research")) {
                  if (astring.length == 3) {
                     if (astring[2].equalsIgnoreCase("all")) {
                        this.giveAllResearch(icommandsender, entityplayermp);
                     } else if (astring[2].equalsIgnoreCase("reset")) {
                        this.resetResearch(icommandsender, entityplayermp);
                     } else {
                        this.giveResearch(icommandsender, entityplayermp, astring[2]);
                     }
                  } else {
                     icommandsender.sendMessage(new TextComponentTranslation("§cInvalid arguments"));
                     icommandsender.sendMessage(new TextComponentTranslation("§cUse /thaumcraft research <list|player> <all|reset|<research>>"));
                  }
               } else if (astring[0].equalsIgnoreCase("aspect")) {
                  if (astring.length == 4) {
                     int i = parseInt(astring[3], 1);
                     this.giveAspect(icommandsender, entityplayermp, astring[2], i);
                  } else {
                     icommandsender.sendMessage(new TextComponentTranslation("§cInvalid arguments"));
                     icommandsender.sendMessage(new TextComponentTranslation("§cUse /thaumcraft aspect <player> <aspect|all> <amount>"));
                  }
               } else if (astring[0].equalsIgnoreCase("warp")) {
                  if (astring.length >= 4 && astring[2].equalsIgnoreCase("set")) {
                     int i = parseInt(astring[3], 0);
                     this.setWarp(icommandsender, entityplayermp, i, astring.length == 5 ? astring[4] : "");
                  } else if (astring.length >= 4 && astring[2].equalsIgnoreCase("add")) {
                     int i = parseInt(astring[3], -100, 100);
                     this.addWarp(icommandsender, entityplayermp, i, astring.length == 5 ? astring[4] : "");
                  } else {
                     icommandsender.sendMessage(new TextComponentTranslation("§cInvalid arguments"));
                     icommandsender.sendMessage(new TextComponentTranslation("§cUse /thaumcraft warp <player> <add|set> <amount> <PERM|TEMP>"));
                  }
               } else {
                  icommandsender.sendMessage(new TextComponentTranslation("§cInvalid arguments"));
                  icommandsender.sendMessage(new TextComponentTranslation("§cUse /thaumcraft help to get help"));
               }
            }
         } else {
            icommandsender.sendMessage(new TextComponentTranslation("§cInvalid arguments"));
            icommandsender.sendMessage(new TextComponentTranslation("§cUse /thaumcraft help to get help"));
         }

      }
   }

   private void giveAspect(ICommandSender icommandsender, EntityPlayerMP player, String string, int i) {
      if (string.equalsIgnoreCase("all")) {
         for(Aspect aspect : Aspect.aspects.values()) {
            Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), aspect, (short)i);
         }

         ResearchManager.scheduleSave(player);
         player.sendMessage(new TextComponentTranslation("§5" + icommandsender.getName() + " gave you " + i + " of all the aspects."));
         icommandsender.sendMessage(new TextComponentTranslation("§5Success!"));
         PacketHandler.INSTANCE.sendTo(new PacketSyncAspects(player), player);
      } else {
         Aspect aspect = Aspect.getAspect(string);
         if (aspect == null) {
            for(Aspect a : Aspect.aspects.values()) {
               if (string.equalsIgnoreCase(a.getName())) {
                  aspect = a;
                  break;
               }
            }
         }

         if (aspect != null) {
            Thaumcraft.proxy.playerKnowledge.addAspectPool(player.getName(), aspect, (short)i);
            ResearchManager.scheduleSave(player);
            PacketHandler.INSTANCE.sendTo(new PacketSyncAspects(player), player);
            player.sendMessage(new TextComponentTranslation("§5" + icommandsender.getName() + " gave you " + i + " " + aspect.getName()));
            icommandsender.sendMessage(new TextComponentTranslation("§5Success!"));
         } else {
            icommandsender.sendMessage(new TextComponentTranslation("§cAspect does not exist."));
         }
      }

   }

   private void setWarp(ICommandSender icommandsender, EntityPlayerMP player, int i, String type) {
      if (type.equalsIgnoreCase("PERM")) {
         Thaumcraft.proxy.playerKnowledge.setWarpPerm(player.getName(), i);
         ResearchManager.scheduleSave(player);
         PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)0), player);
      } else if (type.equalsIgnoreCase("TEMP")) {
         Thaumcraft.proxy.playerKnowledge.setWarpTemp(player.getName(), i);
         ResearchManager.scheduleSave(player);
         PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)2), player);
      } else {
         Thaumcraft.proxy.playerKnowledge.setWarpSticky(player.getName(), i);
         ResearchManager.scheduleSave(player);
         PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)1), player);
      }

      player.sendMessage(new TextComponentTranslation("§5" + icommandsender.getName() + " set your warp to " + i));
      icommandsender.sendMessage(new TextComponentTranslation("§5Success!"));
   }

   private void addWarp(ICommandSender icommandsender, EntityPlayerMP player, int i, String type) {
      if (type.equalsIgnoreCase("PERM")) {
         Thaumcraft.proxy.playerKnowledge.addWarpPerm(player.getName(), i);
         ResearchManager.scheduleSave(player);
         PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)0), player);
         PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)0, i), player);
      } else if (type.equalsIgnoreCase("TEMP")) {
         Thaumcraft.proxy.playerKnowledge.addWarpTemp(player.getName(), i);
         ResearchManager.scheduleSave(player);
         PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)2), player);
         PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)2, i), player);
      } else {
         Thaumcraft.proxy.playerKnowledge.addWarpSticky(player.getName(), i);
         ResearchManager.scheduleSave(player);
         PacketHandler.INSTANCE.sendTo(new PacketSyncWarp(player, (byte)1), player);
         PacketHandler.INSTANCE.sendTo(new PacketWarpMessage(player, (byte)1, i), player);
      }

      player.sendMessage(new TextComponentTranslation("§5" + icommandsender.getName() + " added " + i + " warp to your total."));
      icommandsender.sendMessage(new TextComponentTranslation("§5Success!"));
   }

   private void listResearch(ICommandSender icommandsender) {
      for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
         for(ResearchItem ri : cat.research.values()) {
            icommandsender.sendMessage(new TextComponentTranslation("§5" + ri.key));
         }
      }

   }

   void giveResearch(ICommandSender icommandsender, EntityPlayerMP player, String research) {
      if (ResearchCategories.getResearch(research) != null) {
         this.giveRecursiveResearch(player, research);
         PacketHandler.INSTANCE.sendTo(new PacketSyncResearch(player), player);
         player.sendMessage(new TextComponentTranslation("§5" + icommandsender.getName() + " gave you " + research + " research and its requisites."));
         icommandsender.sendMessage(new TextComponentTranslation("§5Success!"));
      } else {
         icommandsender.sendMessage(new TextComponentTranslation("§cResearch does not exist."));
      }

   }

   void giveRecursiveResearch(EntityPlayerMP player, String research) {
      if (!ResearchManager.isResearchComplete(player.getName(), research)) {
         Thaumcraft.proxy.getResearchManager().completeResearch(player, research);
         if (ResearchCategories.getResearch(research).parents != null) {
            for(String rsi : ResearchCategories.getResearch(research).parents) {
               this.giveRecursiveResearch(player, rsi);
            }
         }

         if (ResearchCategories.getResearch(research).parentsHidden != null) {
            for(String rsi : ResearchCategories.getResearch(research).parentsHidden) {
               this.giveRecursiveResearch(player, rsi);
            }
         }

         if (ResearchCategories.getResearch(research).siblings != null) {
            for(String rsi : ResearchCategories.getResearch(research).siblings) {
               this.giveRecursiveResearch(player, rsi);
            }
         }
      }

   }

   void giveAllResearch(ICommandSender icommandsender, EntityPlayerMP player) {
      for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
         for(ResearchItem ri : cat.research.values()) {
            if (!ResearchManager.isResearchComplete(player.getName(), ri.key)) {
               Thaumcraft.proxy.getResearchManager().completeResearch(player, ri.key);
            }
         }
      }

      player.sendMessage(new TextComponentTranslation("§5" + icommandsender.getName() + " has given you all research."));
      icommandsender.sendMessage(new TextComponentTranslation("§5Success!"));
      PacketHandler.INSTANCE.sendTo(new PacketSyncResearch(player), player);
   }

   void resetResearch(ICommandSender icommandsender, EntityPlayerMP player) {
      Thaumcraft.proxy.getPlayerKnowledge().researchCompleted.remove(player.getName());

      for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
         for(ResearchItem ri : cat.research.values()) {
            if (ri.isAutoUnlock()) {
               Thaumcraft.proxy.getResearchManager().completeResearch(player, ri.key);
            }
         }
      }

      player.sendMessage(new TextComponentTranslation("§5" + icommandsender.getName() + " has reset you research."));
      icommandsender.sendMessage(new TextComponentTranslation("§5Success!"));
      PacketHandler.INSTANCE.sendTo(new PacketSyncResearch(player), player);
   }
}
