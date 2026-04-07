package thaumcraft.common.config;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.potions.PotionFluxTaint;
import thaumcraft.api.potions.PotionVisExhaust;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.entities.EntityFallingTaint;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.EntityTravelingTrunk;
import thaumcraft.common.entities.monster.EntityCultistCleric;
import thaumcraft.common.entities.monster.EntityCultistKnight;
import thaumcraft.common.entities.monster.EntityEldritchCrab;
import thaumcraft.common.entities.monster.EntityEldritchGuardian;
import thaumcraft.common.entities.monster.EntityInhabitedZombie;
import thaumcraft.common.entities.monster.EntityPech;
import thaumcraft.common.entities.monster.EntityTaintSpore;
import thaumcraft.common.entities.monster.EntityWisp;
import thaumcraft.common.entities.monster.boss.EntityCultistLeader;
import thaumcraft.common.entities.monster.boss.EntityCultistPortal;
import thaumcraft.common.entities.monster.boss.EntityEldritchGolem;
import thaumcraft.common.entities.monster.boss.EntityEldritchWarden;
import thaumcraft.common.items.baubles.ItemAmuletVis;
import thaumcraft.common.items.equipment.ItemElementalAxe;
import thaumcraft.common.lib.enchantment.EnchantmentHaste;
import thaumcraft.common.lib.enchantment.EnchantmentRepair;
import thaumcraft.common.lib.potions.PotionBlurredVision;
import thaumcraft.common.lib.potions.PotionDeathGaze;
import thaumcraft.common.lib.potions.PotionInfectiousVisExhaust;
import thaumcraft.common.lib.potions.PotionSunScorned;
import thaumcraft.common.lib.potions.PotionThaumarhia;
import thaumcraft.common.lib.potions.PotionUnnaturalHunger;
import thaumcraft.common.lib.potions.PotionWarpWard;
import thaumcraft.common.lib.utils.CropUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;
import thaumcraft.common.lib.world.biomes.BiomeGenEerie;
import thaumcraft.common.lib.world.biomes.BiomeGenEldritch;
import thaumcraft.common.lib.world.biomes.BiomeGenMagicalForest;
import thaumcraft.common.lib.world.biomes.BiomeGenTaint;
import thaumcraft.common.lib.world.biomes.BiomeHandler;

public class Config {
    public static Configuration config;
    public static final String CATEGORY_ENCH = "Enchantments";
    public static final String CATEGORY_ENTITIES = "Entities";
    public static final String CATEGORY_BIOMES = "Biomes";
    public static final String CATEGORY_RESEARCH = "Research";
    public static final String CATEGORY_GEN = "World_Generation";
    public static final String CATEGORY_REGEN = "World_Regeneration";
    public static final String CATEGORY_SPAWN = "Monster_Spawning";
    public static final String CATEGORY_RUNIC = "Runic_Shielding";

    public static int biomeTaintID = 192;//oneday all of you numbers order by hand will go fuck off
    public static int biomeMagicalForestID = 193;
    public static int biomeEerieID = 194;
    public static int biomeEldritchID = 195;
    public static int biomeTaintWeight = 2;
    public static int biomeMagicalForestWeight = 5;
    public static int dimensionOuterId = -42;

    public static int taintSpreadRate = 200;
    public static boolean taintFromFlux = true;

    public static boolean hardNode = true;
    public static boolean wuss = false;
    public static boolean championMobs = true;

    public static int shieldRecharge = 2000;
    public static int shieldWait = 80;
    public static int shieldCost = 50;

    public static boolean colorBlind = false;
    public static boolean shaders = true;
    public static boolean crooked = true;
    public static boolean showTags = false;
    public static boolean blueBiome = false;
    public static boolean allowMirrors = true;
    public static boolean dialBottom = false;

    public static int nodeRefresh = 10;
    public static final float auraSize = 4.0F;

    public static boolean genAura = true;
    public static boolean genStructure = true;
    public static boolean genCinnibar = true;
    public static boolean genAmber = true;
    public static boolean genInfusedStone = true;
    public static boolean genTrees = true;
    public static boolean genTaint = true;
    public static boolean regenAura = false;
    public static boolean regenStructure = false;
    public static boolean regenCinnibar = false;
    public static boolean regenAmber = false;
    public static boolean regenInfusedStone = false;
    public static boolean regenTrees = false;
    public static boolean regenTaint = false;
    public static String regenKey = "DEFAULT";
    public static boolean wardedStone = true;
    public static boolean allowCheatSheet = true;
    public static boolean golemChestInteract = true;
    public static int nodeRarity = 36;
    public static int specialNodeRarity = 18;
    public static int notificationDelay = 5000;
    public static int notificationMax = 15;
    public static boolean glowyTaint = true;
    public static int researchDifficulty = 0;
    public static int aspectTotalCap = 100;
    public static int golemDelay = 5;
    public static int golemIgnoreDelay = 10000;
    public static int golemLinkQuality = 16;
    public static boolean CwardedStone = true;
    public static boolean CallowCheatSheet = true;
    public static boolean CallowMirrors = true;
    public static boolean ChardNode = true;
    public static boolean Cwuss = false;
    public static int CresearchDifficulty = 0;
    public static int CaspectTotalCap = 100;
    public static boolean spawnAngryZombie = true;
    public static boolean spawnFireBat = true;
    public static boolean spawnTaintacle = true;
    public static boolean spawnWisp = true;
    public static boolean spawnTaintSpore = true;
    public static boolean spawnPech = true;
    public static boolean spawnElder = true;
    public static int potionVisExhaustID = 18;
    public static int potionInfVisExhaustID = 18;
    public static int potionBlurredID = 18;
    public static int potionThaumarhiaID = 18;
    public static int potionTaintPoisonID = 19;
    public static int potionUnHungerID = 17;
    public static int potionSunScornedID = 17;
    public static int potionWarpWardID = 23;
    public static int potionDeathGazeID = 17;
    public static final Enchantment enchHaste = new EnchantmentHaste(0, 0);
    public static final Enchantment enchRepair = new EnchantmentRepair(0, 0);
    public static ArrayList<Aspect> aspectOrder = new ArrayList<>();
    public static boolean foundCopperIngot = false;
    public static boolean foundTinIngot = false;
    public static boolean foundSilverIngot = false;
    public static boolean foundLeadIngot = false;
    public static boolean foundCopperOre = false;
    public static boolean foundTinOre = false;
    public static boolean foundSilverOre = false;
    public static boolean foundLeadOre = false;
    public static final Material airyMaterial;
    public static final Material fluxGoomaterial;
    public static final Material taintMaterial;

    public static void initialize(File file) {
        config = new Configuration(file);
        config.addCustomCategoryComment(CATEGORY_ENCH, "Custom enchantments");
        config.addCustomCategoryComment(CATEGORY_SPAWN, "Will these mobs spawn");
        config.addCustomCategoryComment(CATEGORY_RESEARCH, "Various research related things.");
        config.addCustomCategoryComment(CATEGORY_GEN, "Settings to turn certain world-gen on or off.");
        config.addCustomCategoryComment(CATEGORY_REGEN, "If a chunk is encountered that skipped TC worldgen, then the game will attempt to regenerate certain world features if they are set to true. CAUTION: Best used for worlds created before you added this mod, and only if you know what you are doing. Backups are advised.");
        config.addCustomCategoryComment(CATEGORY_BIOMES, "Biomes and effects");
        config.addCustomCategoryComment(CATEGORY_RUNIC, "Runic Shielding");
        config.load();
        syncConfigurable();
        Property btcp = config.get(CATEGORY_BIOMES, "taint_biome_weight", 2);
        btcp.setComment("higher values increases number of taint biomes. If you are using biome addon mods you probably want to increase this weight quite a bit");
        biomeTaintWeight = btcp.getInt();
        Property biomeTaintProp = config.get(CATEGORY_BIOMES, "biome_taint", biomeTaintID);
        biomeTaintProp.setComment("Taint biome id");
        biomeTaintID = biomeTaintProp.getInt();
        if (Biome.getBiome(biomeTaintID) != null) {
            biomeTaintID = ThaumcraftWorldGenerator.getFirstFreeBiomeSlot(biomeTaintID);
            biomeTaintProp.set(biomeTaintID);
        }

        ThaumcraftWorldGenerator.biomeTaint = new BiomeGenTaint();

        Property mfcp = config.get(CATEGORY_BIOMES, "magical_forest_biome_weight", 5);
        mfcp.setComment("higher values increases number of magical forest biomes. If you are using biome addon mods you probably want to increase this weight quite a bit");
        biomeMagicalForestWeight = mfcp.getInt();
        Property biomeMFProp = config.get(CATEGORY_BIOMES, "biome_magical_forest", biomeMagicalForestID);
        biomeMFProp.setComment("Magical Forest biome id");
        biomeMagicalForestID = biomeMFProp.getInt();
        if (Biome.getBiome(biomeMagicalForestID) != null) {
            biomeMagicalForestID = ThaumcraftWorldGenerator.getFirstFreeBiomeSlot(biomeMagicalForestID);
            biomeMFProp.set(biomeMagicalForestID);
        }

        ThaumcraftWorldGenerator.biomeMagicalForest = new BiomeGenMagicalForest();

        Property biomeEerieProp = config.get(CATEGORY_BIOMES, "biome_eerie", biomeEerieID);
        biomeEerieProp.setComment("Eerie biome id");
        biomeEerieID = biomeEerieProp.getInt();
        if (Biome.getBiome(biomeEerieID) != null) {
            biomeEerieID = ThaumcraftWorldGenerator.getFirstFreeBiomeSlot(biomeEerieID);
            biomeEerieProp.set(biomeEerieID);
        }

        ThaumcraftWorldGenerator.biomeEerie = new BiomeGenEerie();

        Property biomeEldritchProp = config.get(CATEGORY_BIOMES, "biome_eldritch", biomeEldritchID);
        biomeEldritchProp.setComment("Eldritch Lands biome id");
        biomeEldritchID = biomeEldritchProp.getInt();
        if (Biome.getBiome(biomeEldritchID) != null) {
            biomeEldritchID = ThaumcraftWorldGenerator.getFirstFreeBiomeSlot(biomeEldritchID);
            biomeEldritchProp.set(biomeEldritchID);
        }

        ThaumcraftWorldGenerator.biomeEldritchLands = new BiomeGenEldritch();

        Property dimEldritch = config.get(CATEGORY_BIOMES, "outer_lands_dim", dimensionOuterId);
        dimensionOuterId = dimEldritch.getInt();
        config.save();
    }

    public static void registerBiomes(net.minecraftforge.registries.IForgeRegistry<Biome> registry) {
        if (ThaumcraftWorldGenerator.biomeTaint != null) {
            registry.register(ThaumcraftWorldGenerator.biomeTaint.setRegistryName("thaumcraft:tainted_land"));
            BiomeDictionary.addTypes(ThaumcraftWorldGenerator.biomeTaint, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.WASTELAND);
        }
        if (ThaumcraftWorldGenerator.biomeMagicalForest != null) {
            registry.register(ThaumcraftWorldGenerator.biomeMagicalForest.setRegistryName("thaumcraft:magical_forest"));
            BiomeDictionary.addTypes(ThaumcraftWorldGenerator.biomeMagicalForest, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.FOREST);
        }
        if (ThaumcraftWorldGenerator.biomeEerie != null) {
            registry.register(ThaumcraftWorldGenerator.biomeEerie.setRegistryName("thaumcraft:eerie"));
            BiomeDictionary.addTypes(ThaumcraftWorldGenerator.biomeEerie, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.SPOOKY);
        }
        if (ThaumcraftWorldGenerator.biomeEldritchLands != null) {
            registry.register(ThaumcraftWorldGenerator.biomeEldritchLands.setRegistryName("thaumcraft:eldritch_lands"));
            BiomeDictionary.addTypes(ThaumcraftWorldGenerator.biomeEldritchLands, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.END);
        }
    }

    public static void save() {
        config.save();
    }

    public static void initPotions() {
        PotionFluxTaint.instance = new PotionFluxTaint(true, 6697847);
        PotionFluxTaint.init();
        PotionVisExhaust.instance = new PotionVisExhaust(true, 6702199);
        PotionVisExhaust.init();
        PotionInfectiousVisExhaust.instance = new PotionInfectiousVisExhaust(true, 6706551);
        PotionInfectiousVisExhaust.init();
        PotionUnnaturalHunger.instance = new PotionUnnaturalHunger(true, 4482611);
        PotionUnnaturalHunger.init();
        PotionWarpWard.instance = new PotionWarpWard(false, 14742263);
        PotionWarpWard.init();
        PotionDeathGaze.instance = new PotionDeathGaze(true, 6702131);
        PotionDeathGaze.init();
        PotionBlurredVision.instance = new PotionBlurredVision(true, 8421504);
        PotionBlurredVision.init();
        PotionSunScorned.instance = new PotionSunScorned(true, 16308330);
        PotionSunScorned.init();
        PotionThaumarhia.instance = new PotionThaumarhia(true, 6702199);
        PotionThaumarhia.init();
    }

    public static void syncConfigurable() {
        genAura = config.get(CATEGORY_GEN, "generate_aura_nodes", true).getBoolean(true);
        genStructure = config.get(CATEGORY_GEN, "generate_structures", true).getBoolean(true);
        genCinnibar = config.get(CATEGORY_GEN, "generate_cinnibar_ore", true).getBoolean(true);
        genAmber = config.get(CATEGORY_GEN, "generate_amber_ore", true).getBoolean(true);
        genInfusedStone = config.get(CATEGORY_GEN, "generate_infused_stone", true).getBoolean(true);
        genTrees = config.get(CATEGORY_GEN, "generate_trees", true).getBoolean(true);
        Property gt = config.get(CATEGORY_GEN, "generate_taint", genTaint);
        gt.setComment("Can taint biomes generate at worldgen");
        genTaint = gt.getBoolean(true);
        Property regKey = config.get(CATEGORY_REGEN, "regen_key", "DEFAULT");
        regKey.setComment("This key is used to keep track of which chunk have been generated/regenerated. Changing it will cause the regeneration code to run again, so only change it if you want it to happen. Useful to regen only one world feature at a time.");
        regenKey = regKey.getString();
        regenAura = config.get(CATEGORY_REGEN, "aura_nodes", false).getBoolean(false);
        regenStructure = config.get(CATEGORY_REGEN, "structures", false).getBoolean(false);
        regenCinnibar = config.get(CATEGORY_REGEN, "cinnibar_ore", false).getBoolean(false);
        regenAmber = config.get(CATEGORY_REGEN, "amber_ore", false).getBoolean(false);
        regenInfusedStone = config.get(CATEGORY_REGEN, "infused_stone", false).getBoolean(false);
        regenTrees = config.get(CATEGORY_REGEN, "trees", false).getBoolean(false);
        regenTaint = config.get(CATEGORY_REGEN, "taint", false).getBoolean(false);
        Property resDif = config.get(CATEGORY_RESEARCH, "research_difficulty", 0);
        resDif.setComment("0 = normal, -1 = easy (all research items are directly purchased with RP), 1 = Hard (all research items need to be solved via the research table)");
        CresearchDifficulty = researchDifficulty = resDif.getInt(8);
        Property aspTotCap = config.get(CATEGORY_RESEARCH, "aspect_total_cap", 100);
        aspTotCap.setComment("The total amount of RP you can have in your pool per aspect before the scanning soft cap kicks in.");
        CaspectTotalCap = aspectTotalCap = aspTotCap.getInt(100);
        spawnAngryZombie = config.get(CATEGORY_SPAWN, "spawn_angry_zombies", true).getBoolean(true);
        spawnFireBat = config.get(CATEGORY_SPAWN, "spawn_fire_bats", true).getBoolean(true);
        spawnWisp = config.get(CATEGORY_SPAWN, "spawn_wisps", true).getBoolean(true);
        spawnTaintacle = config.get(CATEGORY_SPAWN, "spawn_taintacles", true).getBoolean(true);
        spawnTaintSpore = config.get(CATEGORY_SPAWN, "spawn_taint_spores", true).getBoolean(true);
        spawnPech = config.get(CATEGORY_SPAWN, "spawn_pechs", true).getBoolean(true);
        spawnElder = config.get(CATEGORY_SPAWN, "spawn_eldercreatures", true).getBoolean(true);
        Property cm = config.get(CATEGORY_SPAWN, "champion_mobs", championMobs);
        cm.setComment("Setting this to false will disable spawning champion mobs. Even when false they will still have a greatly reduced chance of spawning in certain dangerous places.");
        championMobs = cm.getBoolean(true);
        Property am = config.get("general", "allow_mirrors", allowMirrors);
        am.setComment("Setting this to false will disable arcane mirror research and crafting recipes.");
        CallowMirrors = allowMirrors = am.getBoolean(true);
        Property cb = config.get("general", "color_blind", colorBlind);
        cb.setComment("Setting this to true will make certain colors higher contrast or darker to prevent them from being 'invisible' to color blind people.");
        colorBlind = cb.getBoolean(false);
        Property shad = config.get("general", "shaders", shaders);
        shad.setComment("This setting will disable certain thaumcraft shaders for those who experience FPS drops.");
        shaders = shad.getBoolean(false);
        Property ocd = config.get("general", "crooked", crooked);
        ocd.setComment("Hate crooked labels, kittens, puppies and all things awesome? If yes, set this to false.");
        crooked = ocd.getBoolean(true);
        Property hn = config.get("general", "hard_mode_nodes", hardNode);
        hn.setComment("Negative nodes like hungry, tainted or dark nodes will have additional, much nastier, effects.");
        ChardNode = hardNode = hn.getBoolean(true);
        Property wm = config.get("general", "wuss_mode", wuss);
        wm.setComment("Setting this to true disables Warp and similar mechanics. You wuss.");
        Cwuss = wuss = wm.getBoolean(false);
        Property dbp = config.get("general", "wand_dial_bottom", dialBottom);
        dbp.setComment("Set to true to have the wand dial display in the bottom left instead of the top left.");
        dialBottom = dbp.getBoolean(false);
        Property golDel = config.get("general", "golem_delay", golemDelay);
        golDel.setComment("How many ticks a golem waits between checking for tasks. Setting it higher will save server ticks, but will make the golems slower to react.");
        golemDelay = golDel.getInt();
        if (golemDelay < 1) {
            golemDelay = 1;
        }

        Property golIgDel = config.get("general", "golem_ignore_delay", golemIgnoreDelay);
        golIgDel.setComment("How many milliseconds a golem will ignore an item after it has failed to find a destination or use for it. Min value 1000");
        golemIgnoreDelay = golIgDel.getInt();
        if (golemIgnoreDelay < 1000) {
            golemIgnoreDelay = 1000;
        }

        Property golLinkQual = config.get("general", "golem_link_quality", golemLinkQuality);
        golLinkQual.setComment("The fx quality of the line connecting golems to marked blocks. Setting this below 4 deactives the effect entirely.");
        golemLinkQuality = golLinkQual.getInt();
        if (golemLinkQuality < 4) {
            golemLinkQuality = 0;
        }

        Property notDel = config.get("general", "notification_delay", notificationDelay);
        notDel.setComment("Determines how fast notifications scroll downwards.");
        notificationDelay = notDel.getInt();
        Property notMax = config.get("general", "notification_max", notificationMax);
        notMax.setComment("The maximum amount of notifications that are displayed onscreen.");
        notificationMax = notMax.getInt();
        Property nodRare = config.get("general", "node_rarity", nodeRarity);
        nodRare.setComment("How rare nodes are in the world. The number means there will be (on average) one node per N chunks.");
        nodeRarity = nodRare.getInt();
        Property nodSpec = config.get("general", "special_node_rarity", specialNodeRarity);
        nodSpec.setComment("The chance of a node being special (pure, dark, unstable, etc.). The number means roughly 1 in N nodes will be special, so setting the number to 5 will mean 1 in 5 nodes may be special.");
        specialNodeRarity = nodSpec.getInt();
        if (specialNodeRarity < 3) {
            specialNodeRarity = 3;
        }

        Property showtags = config.get("general", "display_aspects", false);
        showtags.setComment("Item aspects are hidden by default and pressing shift reveals them. Changing this setting to 'true' will reverse this behaviour and always display aspects unless shift is pressed.");
        showTags = showtags.getBoolean(false);
        Property cheatsheet = config.get("general", "allow_cheat_sheet", true);
        cheatsheet.setComment("Enables a version of the Thauminomicon in creative mode that grants you all the research when you first use it.");
        CallowCheatSheet = allowCheatSheet = cheatsheet.getBoolean(false);
        Property wardstone = config.get("general", "allow_warded_stone", true);
        wardstone.setComment("If set to false, warded stone, doors and glass will just be cosmetic in nature and not have its hardened properties (everyone will be able to break it with equal ease).");
        CwardedStone = wardedStone = wardstone.getBoolean(false);
        Property wiz_vil = config.get("general", "thaumcraft_villager_id", ConfigEntities.entWizardId);
        wiz_vil.setComment("Thaumcraft wizard villager id");
        ConfigEntities.entWizardId = wiz_vil.getInt();
        Property bank_vil = config.get("general", "thaumcraft_banker_id", ConfigEntities.entBankerId);
        bank_vil.setComment("Thaumcraft banker villager id");
        ConfigEntities.entBankerId = bank_vil.getInt();
        Property gci = config.get("general", "golem_chest_interact", true);
        gci.setComment("If set to true golems will attempt to play the chest opening animations and sounds whenever they interact with them.");
        golemChestInteract = gci.getBoolean(false);
        Property phblacklist = config.get("general", "portablehole_blacklist", "iron_door");
        phblacklist.setComment("This is a comma-delimited list of any block names the portable hole is not allowed to pass through.");
        String[] phbl = phblacklist.getString().split(",");
        for (String s : phbl) {
            try {
                Block b = Block.getBlockFromName(s);
                if (b != null && b != Blocks.AIR) {
                    ThaumcraftApi.portableHoleBlackList.add(b);
                }
            } catch (Exception ignored) {
            }
        }

        Property blueb = config.get("general", "blue_magical_forest", blueBiome);
        blueb.setComment("Set this to true to get the old blue magical forest back.");
        blueBiome = blueb.getBoolean(false);
        Property bff = config.get("general", "biome_taint_from_flux", taintFromFlux);
        bff.setComment("Can Taint be caused by flux effects.");
        taintFromFlux = bff.getBoolean(true);
        Property ts = config.get("general", "biome_taint_spread", taintSpreadRate);
        ts.setComment("The chance per block update (1 in n) of the Taint biome spreading. Setting it to 0 prevents the spread of Taint biomes.");
        taintSpreadRate = ts.getInt();
        Property glowT = config.get("general", "glowing_taint", glowyTaint);
        glowT.setComment("Setting this to false will remove the glowing purple nodules from taint fibres. This might prevent crashes some people experience and improve performance.");
        glowyTaint = glowT.getBoolean(true);
        Property rss = config.get(CATEGORY_RUNIC, "runic_recharge_speed", shieldRecharge);
        rss.setComment("How many milliseconds pass between runic shielding recharge ticks. Lower values equals faster recharge. Minimum of 500.");
        shieldRecharge = Math.max(500, rss.getInt());
        Property rsd = config.get(CATEGORY_RUNIC, "runic_recharge_delay", shieldWait);
        rsd.setComment("How many game ticks pass after a shield has been reduced to zero before it can start recharging again. Minimum of 0.");
        shieldWait = Math.max(0, rsd.getInt());
        Property rsc = config.get(CATEGORY_RUNIC, "runic_cost", shieldCost);
        rsc.setComment("How much aer and terra centi-vis (0.01 vis) it costs to reacharge a single unit of shielding. Minimum of 0.");
        shieldCost = Math.max(0, rsc.getInt());
    }

    public static void initLoot() {
        Random rand = new Random(System.currentTimeMillis());
        ItemStack amulet = new ItemStack(ConfigItems.itemAmuletVis, 1, 0);
        ItemAmuletVis ai = (ItemAmuletVis) amulet.getItem();

        for (Aspect a : Aspect.getPrimalAspects()) {
            ai.addVis(amulet, a, rand.nextInt(5), true);
        }

        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemResource, 1, 18), 2500, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.DIAMOND), 10, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EMERALD), 15, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemBaubleBlanks, 1, 0), 10, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemBaubleBlanks, 1, 1), 10, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemBaubleBlanks, 1, 2), 10, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemResource, 2, 18), 2250, 1);

        ThaumcraftApi.addLootBagItem(new ItemStack(Items.NETHER_STAR), 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemResource, 3, 18), 2000, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemEldritchObject, 1, 3), 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.DIAMOND), 50, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EMERALD), 75, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemResource, 1, 9), 25, 0, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLD_INGOT), 100, 0, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.ENDER_PEARL), 100, 0, 1, 2);

        for (int a = 3; a <= 8; ++a) {
            ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemBaubleBlanks, 1, a), 5, 1);
            ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemBaubleBlanks, 1, a), 7, 2);
        }

        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 5, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 10, 1);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1, 1), 1, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1, 0), 3, 0);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1, 0), 6, 1);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1, 1), 2, 1);

        ThaumcraftApi.addLootBagItem(new ItemStack(Items.EXPERIENCE_BOTTLE), 20, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1, 0), 9, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.GOLDEN_APPLE, 1, 1), 3, 2);
        ThaumcraftApi.addLootBagItem(amulet.copy(), 6, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(ConfigItems.itemRingRunic, 1, 0), 5, 1, 2);
        ThaumcraftApi.addLootBagItem(new ItemStack(Items.BOOK), 10, 0, 1, 2);
    }

    public static void initModCompatibility() {
        String[] ores = OreDictionary.getOreNames();

        for (String ore : ores) {
            if (ore != null) {
                if (ore.equals("oreCopper") && !OreDictionary.getOres(ore).isEmpty()) {
                    foundCopperOre = true;

                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        Utils.addSpecialMiningResult(is, new ItemStack(ConfigItems.itemNugget, 1, 17), 1.0F);
                    }
                }

                if (ore.equals("oreTin") && !OreDictionary.getOres(ore).isEmpty()) {
                    foundTinOre = true;

                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        Utils.addSpecialMiningResult(is, new ItemStack(ConfigItems.itemNugget, 1, 18), 1.0F);
                    }
                }

                if (ore.equals("oreSilver") && !OreDictionary.getOres(ore).isEmpty()) {
                    foundSilverOre = true;

                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        Utils.addSpecialMiningResult(is, new ItemStack(ConfigItems.itemNugget, 1, 19), 1.0F);
                    }
                }

                if (ore.equals("oreLead") && !OreDictionary.getOres(ore).isEmpty()) {
                    foundLeadOre = true;

                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        Utils.addSpecialMiningResult(is, new ItemStack(ConfigItems.itemNugget, 1, 20), 1.0F);
                    }
                }

                if (ore.equals("ingotCopper")) {
                    boolean first = true;

                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        if (is.getCount() > 1) {
                            is.setCount(1);
                        }

                        foundCopperIngot = true;
                        GameRegistry.addShapedRecipe(new net.minecraft.util.ResourceLocation("thaumcraft", "ore_copper_nugget"), null, new ItemStack(ConfigItems.itemNugget, 9, 1), "#", '#', is);
                        if (first) {
                            first = false;
                            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(ConfigItems.itemNugget, 1, 17), new ItemStack(is.getItem(), 2, is.getItemDamage()), 1.0F);
                            ConfigRecipes.oreDictRecipe(is, new Object[]{"###", "###", "###", '#', new ItemStack(ConfigItems.itemNugget, 1, 1)});
                        }
                    }
                } else if (ore.equals("ingotTin")) {
                    boolean first = true;

                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        if (is.getCount() > 1) {
                            is.setCount(1);
                        }

                        foundTinIngot = true;
                        GameRegistry.addShapedRecipe(new net.minecraft.util.ResourceLocation("thaumcraft", "ore_tin_nugget"), null, new ItemStack(ConfigItems.itemNugget, 9, 2), "#", '#', is);
                        if (first) {
                            first = false;
                            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(ConfigItems.itemNugget, 1, 18), new ItemStack(is.getItem(), 2, is.getItemDamage()), 1.0F);
                            ConfigRecipes.oreDictRecipe(is, new Object[]{"###", "###", "###", '#', new ItemStack(ConfigItems.itemNugget, 1, 2)});
                        }
                    }
                } else if (ore.equals("ingotSilver")) {
                    boolean first = true;

                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        if (is.getCount() > 1) {
                            is.setCount(1);
                        }

                        foundSilverIngot = true;
                        GameRegistry.addShapedRecipe(new net.minecraft.util.ResourceLocation("thaumcraft", "ore_silver_nugget"), null, new ItemStack(ConfigItems.itemNugget, 9, 3), "#", '#', is);
                        if (first) {
                            first = false;
                            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(ConfigItems.itemNugget, 1, 19), new ItemStack(is.getItem(), 2, is.getItemDamage()), 1.0F);
                            ConfigRecipes.oreDictRecipe(is, new Object[]{"###", "###", "###", '#', new ItemStack(ConfigItems.itemNugget, 1, 3)});
                        }
                    }
                } else if (!ore.equals("oreUranium") && !ore.equals("itemDropUranium") && !ore.equals("ingotUranium")) {
                    if (!ore.equals("ingotBrass") && !ore.equals("ingotBronze")) {
                        if (!ore.equals("dustBrass") && !ore.equals("dustBronze")) {
                            if (!ore.equals("gemRuby") && !ore.equals("gemGreenSapphire") && !ore.equals("gemSapphire")) {
                                switch (ore) {
                                    case "woodRubber":
                                        for (ItemStack is : OreDictionary.getOres(ore)) {
                                            ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.TREE, 3).add(Aspect.TOOL, 1));
                                        }
                                        break;
                                    case "itemRubber":
                                        for (ItemStack is : OreDictionary.getOres(ore)) {
                                            ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.MOTION, 2).add(Aspect.TOOL, 2));
                                        }
                                        break;
                                    case "ingotSteel":
                                        for (ItemStack is : OreDictionary.getOres(ore)) {
                                            ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.METAL, 3).add(Aspect.ORDER, 1));
                                        }
                                        break;
                                    case "crystalQuartz":
                                        for (ItemStack is : OreDictionary.getOres(ore)) {
                                            ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.CRYSTAL, 1).add(Aspect.ENERGY, 1));
                                        }
                                        break;
                                    case "woodLog":
                                        for (ItemStack is : OreDictionary.getOres(ore)) {
                                            ItemElementalAxe.oreDictLogs.add(Arrays.asList(Item.getIdFromItem(is.getItem()), is.getItemDamage()));
                                        }
                                        break;
                                    case "ingotLead":
                                        boolean first = true;

                                        for (ItemStack is : OreDictionary.getOres(ore)) {
                                            if (is.getCount() > 1) {
                                                is.setCount(1);
                                            }

                                            foundLeadIngot = true;
                                            GameRegistry.addShapedRecipe(new net.minecraft.util.ResourceLocation("thaumcraft", "ore_lead_nugget"), null, new ItemStack(ConfigItems.itemNugget, 9, 4), "#", '#', is);
                                            if (first) {
                                                first = false;
                                                FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(ConfigItems.itemNugget, 1, 20), new ItemStack(is.getItem(), 2, is.getItemDamage()), 1.0F);
                                                ConfigRecipes.oreDictRecipe(is,
                                                        new Object[]{"###", "###", "###", '#', new ItemStack(ConfigItems.itemNugget, 1, 4)});
                                            }
                                        }
                                        break;
                                }
                            } else {
                                for (ItemStack is : OreDictionary.getOres(ore)) {
                                    ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.CRYSTAL, 2).add(Aspect.GREED, 2));
                                }
                            }
                        } else {
                            for (ItemStack is : OreDictionary.getOres(ore)) {
                                ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.METAL, 2).add(Aspect.ENTROPY, 1).add(Aspect.TOOL, 1));
                            }
                        }
                    } else {
                        for (ItemStack is : OreDictionary.getOres(ore)) {
                            ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.METAL, 3).add(Aspect.TOOL, 1));
                        }
                    }
                } else {
                    for (ItemStack is : OreDictionary.getOres(ore)) {
                        ThaumcraftApi.registerObjectTag(is, (new AspectList()).add(Aspect.METAL, 2).add(Aspect.POISON, 2).add(Aspect.ENERGY, 2));
                    }
                }
            }
        }

        Thaumcraft.log.info("Adding entities to MFR safari net blacklist.");
        registerSafariNetBlacklist(EntityGolemBase.class);
        registerSafariNetBlacklist(EntityTravelingTrunk.class);
        registerSafariNetBlacklist(EntityAspectOrb.class);
        registerSafariNetBlacklist(EntityFallingTaint.class);
        registerSafariNetBlacklist(EntityWisp.class);
        registerSafariNetBlacklist(EntityPech.class);
        registerSafariNetBlacklist(EntityTaintSpore.class);
        registerSafariNetBlacklist(EntityEldritchGuardian.class);
        registerSafariNetBlacklist(EntityEldritchWarden.class);
        registerSafariNetBlacklist(EntityEldritchGolem.class);
        registerSafariNetBlacklist(EntityCultistCleric.class);
        registerSafariNetBlacklist(EntityCultistKnight.class);
        registerSafariNetBlacklist(EntityCultistLeader.class);
        registerSafariNetBlacklist(EntityCultistPortal.class);
        registerSafariNetBlacklist(EntityEldritchCrab.class);
        registerSafariNetBlacklist(EntityInhabitedZombie.class);
    }

    public static void registerSafariNetBlacklist(Class<?> blacklistedEntity) {
        try {
            Class<?> registry = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
            Method reg = registry.getMethod("registerSafariNetBlacklist", Class.class);
            reg.invoke(registry, blacklistedEntity);
        } catch (Exception ignored) {
        }

    }

    public static void registerBiomes() {
        // BiomeDictionary.addTypes for TC biomes is called from registerBiomes(IForgeRegistry)
        // after the biomes are registered, so they are not added here.
        BiomeHandler.registerBiomeInfo(Type.WATER, 100, Aspect.WATER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.OCEAN, 120, Aspect.WATER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.RIVER, 100, Aspect.WATER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.WET, 80, Aspect.WATER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.HOT, 100, Aspect.FIRE, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.SANDY, 100, Aspect.FIRE, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.NETHER, 120, Aspect.FIRE, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.MESA, 80, Aspect.FIRE, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.DENSE, 100, Aspect.ORDER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.SNOWY, 80, Aspect.ORDER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.COLD, 80, Aspect.ORDER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.SNOWY, 100, Aspect.ORDER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.MUSHROOM, 140, Aspect.ORDER, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.CONIFEROUS, 100, Aspect.EARTH, true, 0.2F);
        BiomeHandler.registerBiomeInfo(Type.FOREST, 120, Aspect.EARTH, true, 1.0F);
        BiomeHandler.registerBiomeInfo(Type.SANDY, 80, Aspect.EARTH, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.BEACH, 80, Aspect.EARTH, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.SAVANNA, 80, Aspect.AIR, true, 0.2F);
        BiomeHandler.registerBiomeInfo(Type.MOUNTAIN, 100, Aspect.AIR, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.HILLS, 120, Aspect.AIR, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.PLAINS, 80, Aspect.AIR, true, 0.2F);
        BiomeHandler.registerBiomeInfo(Type.DRY, 80, Aspect.ENTROPY, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.SPARSE, 80, Aspect.ENTROPY, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.SWAMP, 120, Aspect.ENTROPY, true, 0.2F);
        BiomeHandler.registerBiomeInfo(Type.WASTELAND, 80, Aspect.ENTROPY, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.JUNGLE, 100, Aspect.PLANT, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.LUSH, 100, Aspect.PLANT, true, 0.5F);
        BiomeHandler.registerBiomeInfo(Type.MAGICAL, 100, null, true, 1.0F);
        BiomeHandler.registerBiomeInfo(Type.END, 80, Aspect.VOID, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.SPOOKY, 80, Aspect.SOUL, false, 0.0F);
        BiomeHandler.registerBiomeInfo(Type.DEAD, 50, Aspect.DEATH, false, 0.0F);
    }

    public static void initMisc() {
        CropUtils.addStandardCrop(new ItemStack(Blocks.MELON_BLOCK), 32767);
        CropUtils.addStandardCrop(new ItemStack(Blocks.PUMPKIN), 32767);
        CropUtils.addStandardCrop(new ItemStack(ConfigBlocks.blockManaPod), 7);
        CropUtils.addStackedCrop(Blocks.REEDS, 32767);
        CropUtils.addStackedCrop(Blocks.CACTUS, 32767);
        Utils.addSpecialMiningResult(new ItemStack(Blocks.IRON_ORE), new ItemStack(ConfigItems.itemNugget, 1, 16), 1.0F);
        Utils.addSpecialMiningResult(new ItemStack(Blocks.GOLD_ORE), new ItemStack(ConfigItems.itemNugget, 1, 31), 0.9F);
        Utils.addSpecialMiningResult(new ItemStack(ConfigBlocks.blockCustomOre, 1, 0), new ItemStack(ConfigItems.itemNugget, 1, 21), 0.9F);

        aspectOrder.addAll(Aspect.aspects.values());

    }

    static {
        airyMaterial = new MaterialAiry(MapColor.AIR);
        fluxGoomaterial = (new MaterialTaint(MapColor.GRASS)).setNoPushMobility();
        taintMaterial = new MaterialTaint(MapColor.GRASS);
    }
}
