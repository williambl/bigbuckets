package com.williambl.bigbuckets;

import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid="bigbuckets")
public class BigBuckets {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "bigbuckets";

    @GameRegistry.ObjectHolder("bigbuckets:bigbucket")
    public static BigBucketItem BIG_BUCKET_ITEM;

    @Mod.EventBusSubscriber
    public static class RegistryEvents {

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            event.getRegistry().register(new BigBucketItem().setRegistryName("bigbucket"));
        }

        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
            event.getRegistry().registerAll(
                    new BigBucketRecipe().setRegistryName("bigbuckets:big_bucket"),
                    new BigBucketIncreaseCapacityRecipe().setRegistryName("bigbuckets:big_bucket_increase_capacity")
            );
        }
    }
}
