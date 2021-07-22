package com.williambl.bigbuckets.fabric;

import com.williambl.bigbuckets.BigBuckets;
import net.fabricmc.api.ModInitializer;

public class BigBucketsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        BigBuckets.init();
    }
}
