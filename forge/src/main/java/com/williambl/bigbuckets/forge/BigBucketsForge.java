package com.williambl.bigbuckets.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import com.williambl.bigbuckets.BigBuckets;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BigBuckets.MOD_ID)
public class BigBucketsForge {
    public BigBucketsForge() {
        EventBuses.registerModEventBus(BigBuckets.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        BigBuckets.init();
    }
}
