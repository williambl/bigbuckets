package com.williambl.bigbuckets.platform;

import com.williambl.bigbuckets.Constants;
import com.williambl.bigbuckets.platform.services.IBigBucketsRegistry;
import com.williambl.bigbuckets.platform.services.IFluidHelper;
import com.williambl.bigbuckets.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IBigBucketsRegistry REGISTRY = load(IBigBucketsRegistry.class);
    public static final IFluidHelper FLUIDS = load(IFluidHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
