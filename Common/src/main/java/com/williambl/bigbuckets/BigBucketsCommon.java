package com.williambl.bigbuckets;

import net.minecraft.resources.ResourceLocation;

public class BigBucketsCommon {

    public static void init() {
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(Constants.MOD_ID, path);
    }
}